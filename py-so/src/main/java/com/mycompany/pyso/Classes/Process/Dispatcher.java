package com.mycompany.pyso.Classes.Process;

import com.mycompany.pyso.Classes.Core.CPU;
import com.mycompany.pyso.Classes.Core.Instruction;
import com.mycompany.pyso.Classes.Core.Scheduler;
import com.mycompany.pyso.Classes.Interrupts.InterruptHandler;

public class Dispatcher {

    private final CPU cpu;
    private final Scheduler scheduler;
    private final InterruptHandler interruptHandler;
    private final long simulatorStartMillis;

    private OSProcess currentProcess;

    public Dispatcher(CPU cpu, Scheduler scheduler,
                      InterruptHandler interruptHandler,
                      long simulatorStartMillis) {
        this.cpu = cpu;
        this.scheduler = scheduler;
        this.interruptHandler = interruptHandler;
        this.simulatorStartMillis = simulatorStartMillis;
        this.currentProcess = null;
    }

    public Integer CPUcycle() {
        if (currentProcess == null) return null;

        OSProcess p = currentProcess;
        scheduler.getRam().updateKernelFromBCP(p.getBcp());

        int pc = cpu.getPC();
        int index = pc - p.getBcp().getBaseAddress();

        // Verificar si el proceso ya terminó su ejecución
        if (index < 0 || index >= p.getInstructions().size()) {
            System.out.println("[DISPATCHER] Proceso " + p.getName() + " terminó (fin de instrucciones)");
            terminate(p);
            return pc;
        }

        Instruction inst = p.getInstructions().get(index);
        cpu.setIR(inst.getInstruction());

        switch (inst.getType()) {

            case Instruction.TYPE_INT -> {
                interruptHandler.handle(inst, p, this::terminate);
                if (currentProcess != null) {
                    p.getBcp().saveFromCPU(cpu, inst.getInstruction());
                    p.getBcp().setCpuCyclesUsed(p.getBcp().getCpuCyclesUsed() + 1);
                    scheduler.getRam().updateKernelFromBCP(p.getBcp());
                }
                return pc;
            }

            case Instruction.TYPE_PUSH -> { 
                interruptHandler.executePush(inst, p);  
                cpu.setPC(pc + 1); 
            }
            case Instruction.TYPE_POP -> { 
                interruptHandler.executePop(inst, p);   
                cpu.setPC(pc + 1); 
            }
            case Instruction.TYPE_PARAM -> { 
                interruptHandler.executeParam(inst, p); 
                cpu.setPC(pc + 1); 
            }

            default -> {
                cpu.execute(inst);
                if (inst.getType() != Instruction.TYPE_JMP &&
                    inst.getType() != Instruction.TYPE_JE  &&
                    inst.getType() != Instruction.TYPE_JNE) {
                    cpu.setPC(pc + 1);
                }
            }
        }

        p.getBcp().saveFromCPU(cpu, inst.getInstruction());
        p.getBcp().setCpuCyclesUsed(p.getBcp().getCpuCyclesUsed() + 1);
        scheduler.getRam().updateKernelFromBCP(p.getBcp());

        int newIndex = cpu.getPC() - p.getBcp().getBaseAddress();
        if (newIndex >= p.getInstructions().size()) {
            System.out.println("[DISPATCHER] Proceso " + p.getName() + " terminó después de ejecutar instrucción");
            terminate(p);
        }

        return pc;
    }
    
    public void terminate(OSProcess process) {
        System.out.println("[DISPATCHER] Terminando proceso " + process.getName());
        scheduler.terminateProcess(process);
        currentProcess = null;
    }

    public void moveToWaiting(OSProcess process) {
        process.getBcp().saveFromCPU(cpu, cpu.getIR());
        scheduler.moveToWaiting(process);
        currentProcess = null;
    }

    public void releaseFromWaiting(int pid) {
        scheduler.releaseFromWaiting(pid);
    }

    public OSProcess getCurrentProcess() { return currentProcess; }
    public void setCurrentProcess(OSProcess p) { this.currentProcess = p; }
    public CPU getCpu() { return cpu; }
}