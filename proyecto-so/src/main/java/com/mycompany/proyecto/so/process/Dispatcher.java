package com.mycompany.proyecto.so.process;

import com.mycompany.proyecto.so.core.cpu.CPU;
import com.mycompany.proyecto.so.core.cpu.Instruction;
import com.mycompany.proyecto.so.core.io.InterruptHandler;
import com.mycompany.proyecto.so.core.memory.RAM;
import com.mycompany.proyecto.so.core.scheduling.ISchedulingAlgorithm;
import java.util.List;

/**
 * Dispatcher for one CPU core.
 * Executes one instruction per call to tick().
 *
 * OCP/DIP: depends on ISchedulingAlgorithm — swap algorithms without touching this class.
 * P2: each CPU core has its own Dispatcher instance.
 */
public class Dispatcher {

    private final CPU                  cpu;
    private final InterruptHandler     interruptHandler;
    private final RAM                  ram;
    private       ISchedulingAlgorithm algorithm;   // swappable at runtime
    private       Process              currentProcess;
    private       int                  simulatorTime = 0; // set externally each tick

    public Dispatcher(CPU cpu, InterruptHandler interruptHandler, RAM ram,
                      ISchedulingAlgorithm algorithm) {
        this.cpu              = cpu;
        this.interruptHandler = interruptHandler;
        this.ram              = ram;
        this.algorithm        = algorithm;
    }


    /**
     * Execute one CPU cycle.
     * @param readyQueue processes in READY state available to this core
     * @param currentTime current simulator second
     * @return the PC address that was executed, or -1 if idle
     */
    public int tick(List<Process> readyQueue, int currentTime) {
        this.simulatorTime = currentTime;

        if (currentProcess != null && !readyQueue.isEmpty()) {
            if (algorithm.shouldPreempt(currentProcess, readyQueue, currentTime)) {
                preempt(readyQueue);
            }
        }

        if (currentProcess == null) {
            if (readyQueue.isEmpty()) return -1;
            currentProcess = algorithm.selectNext(readyQueue, currentTime);
            if (currentProcess == null) return -1;

            readyQueue.remove(currentProcess);
            dispatch(currentProcess, currentTime);
        }

        Process p   = currentProcess;
        int pc      = cpu.getPC();
        int index   = pc - p.getBcp().getBaseAddress();

        if (index < 0 || index >= p.getInstructions().size()) {
            terminate(p);
            return pc;
        }

        Instruction inst = p.getInstructions().get(index);
        cpu.setIR(inst.getText());

        boolean terminated = executeInstruction(inst, p);

        if (!terminated) {
            p.getBcp().saveFromCPU(cpu);
            p.getBcp().incrementCycles();
            ram.writeKernelBCP(p.getBcp());

            // Auto-terminate if all instructions done
            int newIdx = cpu.getPC() - p.getBcp().getBaseAddress();
            if (newIdx >= p.getInstructions().size()) {
                terminate(p);
            }
        }

        return pc;
    }


    private boolean executeInstruction(Instruction inst, Process p) {
        int type = inst.getType();
        int pc   = cpu.getPC();

        switch (type) {
            case Instruction.TYPE_INT -> {
                boolean shouldTerminate = interruptHandler.handle(inst, cpu, p);
                if (shouldTerminate) { terminate(p); return true; }
                cpu.setPC(pc + 1);
            }
            case Instruction.TYPE_PUSH  -> { interruptHandler.executePush(inst, cpu, p); cpu.setPC(pc + 1); }
            case Instruction.TYPE_POP   -> { interruptHandler.executePop(inst, cpu, p);  cpu.setPC(pc + 1); }
            case Instruction.TYPE_PARAM -> { interruptHandler.executeParam(inst, p);      cpu.setPC(pc + 1); }
            default -> {
                cpu.execute(inst);
                // Only auto-advance PC for non-jump instructions
                if (type != Instruction.TYPE_JMP &&
                    type != Instruction.TYPE_JE  &&
                    type != Instruction.TYPE_JNE) {
                    cpu.setPC(pc + 1);
                }
            }
        }
        return false;
    }

    /** Load process into CPU (context restore). */
    private void dispatch(Process p, int currentTime) {
        p.getBcp().markStarted(currentTime);
        p.getBcp().setState(ProcessState.RUNNING);
        p.getBcp().setAssignedCore(cpu.getCoreId());
        p.getBcp().restoreIntoCPU(cpu);
        cpu.setPC(p.getBcp().getBaseAddress());
        ram.writeKernelBCP(p.getBcp());
    }

    /** Save current process back to READY and pick next. */
    private void preempt(List<Process> readyQueue) {
        currentProcess.getBcp().saveFromCPU(cpu);
        currentProcess.getBcp().setState(ProcessState.READY);
        readyQueue.add(currentProcess);
        currentProcess = null;
    }

    public void terminate(Process p) {
        p.getBcp().markTerminated(simulatorTime);
        ram.writeKernelBCP(null);
        currentProcess = null;
    }

    public void setAlgorithm(ISchedulingAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public ISchedulingAlgorithm getAlgorithm() { return algorithm; }
    public Process  getCurrentProcess()        { return currentProcess; }
    public CPU      getCpu()                   { return cpu; }
    public boolean  isIdle()                   { return currentProcess == null; }
}
