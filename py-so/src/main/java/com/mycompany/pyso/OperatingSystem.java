package com.mycompany.pyso;

import com.mycompany.pyso.Classes.Core.CPU;
import com.mycompany.pyso.Classes.Core.Scheduler;
import com.mycompany.pyso.Classes.Memory.RAM;
import com.mycompany.pyso.Classes.Memory.Disk;
import com.mycompany.pyso.Classes.Memory.VirtualMemory;
import com.mycompany.pyso.Classes.Process.Dispatcher;
import com.mycompany.pyso.Classes.Process.OSProcess;
import com.mycompany.pyso.Classes.Process.ProcessState;
import com.mycompany.pyso.Classes.Interrupts.InterruptHandler;
import com.mycompany.pyso.Interface.UI;
import com.mycompany.pyso.Scheduler.FCFS;
import com.mycompany.pyso.Scheduler.SchedulerStrategy;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.Timer;

public class OperatingSystem {

    private final List<CPU>        cpus        = new ArrayList<>();
    private final List<Dispatcher> dispatchers = new ArrayList<>();
    private RAM  memory;
    private Disk disk;

    private Scheduler         scheduler;
    private VirtualMemory     virtualMemory;
    private SchedulerStrategy strategy;

    private final List<InterruptHandler> interruptHandlers = new ArrayList<>();

    private Timer   swingTimer;
    private boolean isRunning   = false;
    private int     globalClock = 0;
    private long    simulatorStartMillis;

    private final UI gui;
    
    public OperatingSystem(UI gui, int memorySize) {
        this.gui = gui;
        initialize(memorySize, 512, 2, new FCFS());
    }

    public void reset(int memorySize, int diskSize,
                      int numCpus, SchedulerStrategy strategy) {
        stop();
        initialize(memorySize, diskSize, numCpus, strategy);
    }

    public void reset(int memorySize, int diskSize) {
        reset(memorySize, diskSize, 2, new FCFS());
    }

    public void reset(int memorySize) {
        reset(memorySize, 512, 2, new FCFS());
    }

    private void initialize(int memorySize, int diskSize,
                            int numCpus, SchedulerStrategy strat) {
        this.simulatorStartMillis = System.currentTimeMillis();
        this.strategy      = strat;
        this.globalClock   = 0;
        this.virtualMemory = new VirtualMemory();
        this.memory        = new RAM(memorySize);
        this.disk          = new Disk(diskSize);
        this.scheduler     = new Scheduler(memory, disk, simulatorStartMillis);

        cpus.clear();
        dispatchers.clear();
        interruptHandlers.clear();

        int clampedCpus = Math.max(2, Math.min(4, numCpus));

        for (int i = 0; i < clampedCpus; i++) {
            CPU cpu = new CPU();
            cpu.setId(i + 1);

            InterruptHandler ih = new InterruptHandler(cpu, disk,
                msg -> { if (gui != null) gui.printConsole(msg); });

            Dispatcher d = new Dispatcher(cpu, scheduler, ih, simulatorStartMillis);

            cpus.add(cpu);
            interruptHandlers.add(ih);
            dispatchers.add(d);
        }
    }

    public OSProcess loadProcess(String path) {
        OSProcess process = scheduler.loadProcess(path);
        if (process == null) {
            JOptionPane.showMessageDialog(gui,
                "No se pudo cargar: " + path,
                "Error de carga", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (process.getState() == ProcessState.WAITING) {
            gui.printConsole(">> " + process.getName() + " enviado a swap (RAM llena)");
        } else {
            gui.printConsole(">> Proceso " + process.getName() + " cargado en RAM");
        }
        return process;
    }

    public void run(boolean stepMode) {
        if (!scheduler.hasProcessReady() && !scheduler.hasNewProcesses()) {
            JOptionPane.showMessageDialog(gui,
                "No hay procesos listos. Cargue archivos .asm primero.",
                "Sin procesos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (stepMode) {
            tick();
            return;
        }

        isRunning = true;
        swingTimer = new Timer(1000, e -> {
            if (!isRunning) { swingTimer.stop(); return; }
            tick();
        });
        swingTimer.setInitialDelay(0);
        swingTimer.start();
    }

    public void cpuCycle() { run(true); }

    public void stop() {
        isRunning = false;
        if (swingTimer != null) swingTimer.stop();
    }

    private void tick() {
        globalClock++;
        scheduler.tryLoadNewProcesses();


        List<Integer> executedPCs = new ArrayList<>();

        for (int i = 0; i < dispatchers.size(); i++) {
            Dispatcher  d   = dispatchers.get(i);
            CPU         cpu = cpus.get(i);

            if (d.getCurrentProcess() == null && scheduler.hasProcessReady()) {
                List<OSProcess> readySnapshot = new ArrayList<>(scheduler.getReadyQueue().getAll());
                OSProcess next = strategy.selectNext(readySnapshot);

                if (next != null) {
                    scheduler.getReadyQueue().remove(next);

                    next.setState(ProcessState.RUNNING);
                    next.getBcp().setState(ProcessState.RUNNING);
                    next.markStarted(simulatorStartMillis);
                    next.restoreIntoCPU(cpu);
                    cpu.setPC(next.getBaseAddress());
                    d.setCurrentProcess(next);
                }
            }

            Integer pc = d.CPUcycle();
            if (pc != null) executedPCs.add(pc);

            if (d.getCurrentProcess() != null) {
                List<OSProcess> readySnapshot = new ArrayList<>(scheduler.getReadyQueue().getAll());
                strategy.onTick(d.getCurrentProcess(), readySnapshot);

                if (strategy.shouldPreempt(d.getCurrentProcess(), readySnapshot)) {
                    preempt(d, cpu);
                }
            }
        }

        if (gui != null) {
            gui.refreshAll();
            if (!executedPCs.isEmpty()) gui.highlightRow(executedPCs.get(0));
        }

        OSProcess cpu0proc = dispatchers.isEmpty() ? null : dispatchers.get(0).getCurrentProcess();
        if (memory != null) {
            memory.updateKernelFromBCP(cpu0proc != null ? cpu0proc.getBcp() : null);
        }

        if (scheduler.allTerminated()) {
            stop();
            if (memory != null) memory.updateKernelFromBCP(null);
            if (gui != null) {
                gui.refreshAll();
                JOptionPane.showMessageDialog(gui,
                    "Todos los procesos terminaron.\nCiclos de reloj: " + globalClock,
                    "Ejecución finalizada", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void preempt(Dispatcher d, CPU cpu) {
        OSProcess p = d.getCurrentProcess();
        if (p == null) return;

        p.getBcp().saveFromCPU(cpu, cpu.getIR());
        p.setState(ProcessState.READY);
        p.getBcp().setState(ProcessState.READY);
        scheduler.getReadyQueue().enqueue(p);
        d.setCurrentProcess(null);
    }

    public CPU getCpu()                       { return cpus.isEmpty() ? null : cpus.get(0); }
    public CPU getCpu(int i)                  { return (i >= 0 && i < cpus.size()) ? cpus.get(i) : null; }
    public List<CPU> getAllCpus()             { return cpus; }
    public List<Dispatcher> getDispatchers()  { return dispatchers; }
    public RAM getMemory()                    { return memory; }
    public Disk getDisk()                     { return disk; }
    public Scheduler getScheduler()           { return scheduler; }
    public VirtualMemory getVirtualMemory()   { return virtualMemory; }
    public SchedulerStrategy getStrategy()    { return strategy; }
    public boolean isRunning()                { return isRunning; }
    public int getGlobalClock()               { return globalClock; }
    public long getSimulatorStartMillis()     { return simulatorStartMillis; }
    public InterruptHandler getInterruptHandler()    { return interruptHandlers.isEmpty() ? null : interruptHandlers.get(0); }
    public InterruptHandler getInterruptHandler(int i) { return (i >= 0 && i < interruptHandlers.size()) ? interruptHandlers.get(i) : null; }
}