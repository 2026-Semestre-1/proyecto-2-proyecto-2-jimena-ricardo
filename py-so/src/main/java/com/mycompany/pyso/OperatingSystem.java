package com.mycompany.pyso;

import com.mycompany.pyso.Classes.Core.CPU;
import com.mycompany.pyso.Classes.Core.Scheduler;
import com.mycompany.pyso.Classes.Memory.RAM;
import com.mycompany.pyso.Classes.Memory.Disk;
import com.mycompany.pyso.Classes.Process.BCP;
import com.mycompany.pyso.Classes.Process.Dispatcher;
import com.mycompany.pyso.Classes.Process.OSProcess;
import com.mycompany.pyso.Classes.Process.ProcessState;
import com.mycompany.pyso.Classes.Interrupts.InterruptHandler;
import com.mycompany.pyso.Interface.UI;
import com.mycompany.pyso.Scheduler.FCFS;
import com.mycompany.pyso.Scheduler.SchedulerStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;

public class OperatingSystem {

    private final List<CPU> cpus = new ArrayList<>();
    private final List<Dispatcher> dispatchers = new ArrayList<>();
    private RAM memory;
    private Disk disk;

    private Scheduler scheduler;
    private SchedulerStrategy strategy;

    private final List<InterruptHandler> interruptHandlers = new ArrayList<>();

    private volatile boolean isRunning = false;
    private volatile boolean stepMode = false;
    private int globalClock = 0;
    private long simulatorStartMillis;

    private final UI gui;
    private ExecutorService cpuExecutor;
    private final Object tickLock = new Object();
    private int completedTicks = 0;

    public OperatingSystem(UI gui, int memorySize) {
        this.gui = gui;
        initialize(memorySize, 512, 2, new FCFS());
    }

    public void reset(int memorySize, int diskSize, int numCpus, SchedulerStrategy strategy) {
        stop();
        initialize(memorySize, diskSize, numCpus, strategy);
    }

    public void reset(int memorySize, int diskSize) {
        reset(memorySize, diskSize, 2, new FCFS());
    }
    
    public void reset(int memorySize) {
        reset(memorySize, 512, 2, new FCFS());
    }

    private void initialize(int memorySize, int diskSize, int numCpus, SchedulerStrategy strat) {
        this.simulatorStartMillis = System.currentTimeMillis();
        this.strategy = strat;
        this.globalClock = 0;
        this.memory = new RAM(memorySize);
        this.disk = new Disk(diskSize);
        this.scheduler = new Scheduler(memory, disk, simulatorStartMillis);

        cpus.clear();
        dispatchers.clear();
        interruptHandlers.clear();

        int n = Math.max(2, Math.min(4, numCpus));
        for (int i = 0; i < n; i++) {
            CPU cpu = new CPU();
            cpu.setId(i + 1);
            InterruptHandler ih = new InterruptHandler(cpu, disk,
                msg -> { if (gui != null) gui.printConsole(msg); });
            Dispatcher d = new Dispatcher(cpu, scheduler, ih, simulatorStartMillis);
            dispatchers.add(d);
            cpus.add(cpu);
            interruptHandlers.add(ih);
        }
        
        cpuExecutor = Executors.newFixedThreadPool(n);
    }

    public OSProcess loadProcess(String path) {
        OSProcess process = scheduler.loadProcess(path);
        if (process == null) {
            JOptionPane.showMessageDialog(gui, "No se pudo cargar: " + path,
                "Error de carga", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        refreshKernel();
        if (process.getState() == ProcessState.WAITING)
            gui.printConsole(">> " + process.getName() + " en swap (RAM llena)");
        else
            gui.printConsole(">> " + process.getName() + " cargado en RAM");
        return process;
    }

    public void run(boolean stepMode) {
        if (!scheduler.hasProcessReady() && !scheduler.hasNewProcesses()) {
            JOptionPane.showMessageDialog(gui, "No hay procesos listos.",
                "Sin procesos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        this.stepMode = stepMode;
        
        if (stepMode) {
            tick();
            return;
        }
        
        isRunning = true;
        
        for (int i = 0; i < dispatchers.size(); i++) {
            final int cpuIndex = i;
            cpuExecutor.submit(() -> {
                while (isRunning) {
                    try {
                        Dispatcher d = dispatchers.get(cpuIndex);
                        CPU cpu = cpus.get(cpuIndex);
                        
                        if (d.getCurrentProcess() == null && scheduler.hasProcessReady()) {
                            synchronized (scheduler.getReadyQueue()) {
                                List<OSProcess> snap = new ArrayList<>(scheduler.getReadyQueue().getAll());
                                OSProcess next = strategy.selectNext(snap);
                                if (next != null) {
                                    scheduler.getReadyQueue().remove(next);
                                    next.setState(ProcessState.RUNNING);
                                    next.markStarted(simulatorStartMillis);
                                    next.restoreIntoCPU(cpu);
                                    cpu.setPC(next.getBaseAddress());
                                    d.setCurrentProcess(next);
                                }
                            }
                        }
                       
                        if (d.getCurrentProcess() != null) {
                            Integer pc = d.CPUcycle();
                            if (pc != null && gui != null) {
                                gui.highlightRow(pc);
                            }
                            
                            List<OSProcess> snap = new ArrayList<>(scheduler.getReadyQueue().getAll());
                            if (strategy.shouldPreempt(d.getCurrentProcess(), snap)) {
                                preempt(d, cpu);
                            }
                        }
                       
                        Thread.sleep(1000);
                        
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }
        
        new Thread(() -> {
            while (isRunning) {
                try {
                    Thread.sleep(100);
                    globalClock++;
                    refreshKernel();
                    
                    if (gui != null) {
                        gui.refreshAll();
                    }
                    
                    if (scheduler.allTerminated()) {
                        stop();
                        if (gui != null) {
                            gui.refreshAll();
                            JOptionPane.showMessageDialog(gui,
                                "Todos los procesos terminaron.\nCiclos: " + globalClock,
                                "Ejecución finalizada", JOptionPane.INFORMATION_MESSAGE);
                        }
                        break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    public void cpuCycle() {
        run(true);
    }

    public void stop() {
        isRunning = false;
        stepMode = false;
        if (cpuExecutor != null) {
            cpuExecutor.shutdownNow();
            try {
                cpuExecutor.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void tick() {
        globalClock++;
        scheduler.tryLoadNewProcesses();

        List<Integer> executedPCs = new ArrayList<>();

        for (int i = 0; i < dispatchers.size(); i++) {
            Dispatcher d = dispatchers.get(i);
            CPU cpu = cpus.get(i);

            if (d.getCurrentProcess() == null && scheduler.hasProcessReady()) {
                synchronized (scheduler.getReadyQueue()) {
                    List<OSProcess> snap = new ArrayList<>(scheduler.getReadyQueue().getAll());
                    OSProcess next = strategy.selectNext(snap);
                    if (next != null) {
                        scheduler.getReadyQueue().remove(next);
                        next.setState(ProcessState.RUNNING);
                        next.markStarted(simulatorStartMillis);
                        next.restoreIntoCPU(cpu);
                        cpu.setPC(next.getBaseAddress());
                        d.setCurrentProcess(next);
                    }
                }
            }

            Integer pc = d.CPUcycle();
            if (pc != null) executedPCs.add(pc);

            if (d.getCurrentProcess() != null) {
                List<OSProcess> snap = new ArrayList<>(scheduler.getReadyQueue().getAll());
                strategy.onTick(d.getCurrentProcess(), snap);
                if (strategy.shouldPreempt(d.getCurrentProcess(), snap)) {
                    preempt(d, cpu);
                }
            }
        }

        refreshKernel();

        if (gui != null) {
            gui.refreshAll();
            if (!executedPCs.isEmpty()) gui.highlightRow(executedPCs.get(0));
        }

        if (scheduler.allTerminated()) {
            stop();
            refreshKernel();
            if (gui != null) {
                gui.refreshAll();
                JOptionPane.showMessageDialog(gui,
                    "Todos los procesos terminaron.\nCiclos: " + globalClock,
                    "Ejecución finalizada", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void refreshKernel() {
        OSProcess running = dispatchers.isEmpty() ? null : dispatchers.get(0).getCurrentProcess();
        memory.updateKernelFromBCP(running != null ? running.getBcp() : null);

        List<BCP> allBcps = scheduler.getJobQueue().getAll()
            .stream().map(OSProcess::getBcp).collect(Collectors.toList());
        memory.updateAllBCPsInKernel(allBcps);
    }

    private void preempt(Dispatcher d, CPU cpu) {
        OSProcess p = d.getCurrentProcess();
        if (p == null) return;
        p.getBcp().saveFromCPU(cpu, cpu.getIR());
        p.setState(ProcessState.READY);
        scheduler.getReadyQueue().enqueue(p);
        d.setCurrentProcess(null);
    }

    public CPU getCpu() { return cpus.isEmpty() ? null : cpus.get(0); }
    public CPU getCpu(int i) { return (i >= 0 && i < cpus.size()) ? cpus.get(i) : null; }
    public List<CPU> getAllCpus() { return cpus; }
    public List<Dispatcher> getDispatchers() { return dispatchers; }
    public RAM getMemory() { return memory; }
    public Disk getDisk() { return disk; }
    public Scheduler getScheduler() { return scheduler; }
    public SchedulerStrategy getStrategy() { return strategy; }
    public boolean isRunning() { return isRunning; }
    public int getGlobalClock() { return globalClock; }
    public long getSimulatorStartMillis() { return simulatorStartMillis; }
    public InterruptHandler getInterruptHandler() { return interruptHandlers.isEmpty() ? null : interruptHandlers.get(0); }
    public InterruptHandler getInterruptHandler(int i) { return (i >= 0 && i < interruptHandlers.size()) ? interruptHandlers.get(i) : null; }
}