package com.mycompany.pyso;

import com.mycompany.pyso.Classes.Core.CPU;
import com.mycompany.pyso.Classes.Core.Scheduler;
import com.mycompany.pyso.Classes.Memory.Disk;
import com.mycompany.pyso.Classes.Memory.RAM;
import com.mycompany.pyso.Classes.Process.BCP;
import com.mycompany.pyso.Classes.Process.Dispatcher;
import com.mycompany.pyso.Classes.Process.OSProcess;
import com.mycompany.pyso.Classes.Process.ProcessState;
import com.mycompany.pyso.Classes.Interrupts.InterruptHandler;
import com.mycompany.pyso.Interface.UI;
import com.mycompany.pyso.Scheduler.FCFS;
import com.mycompany.pyso.Scheduler.RoundRobin;
import com.mycompany.pyso.Scheduler.SchedulerStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class OperatingSystem {

    private final List<CPU>        cpus        = new ArrayList<>();
    private final List<Dispatcher> dispatchers = new ArrayList<>();
    private RAM  memory;
    private Disk disk;

    private Scheduler         scheduler;
    private SchedulerStrategy strategy;
    private final List<InterruptHandler> interruptHandlers = new ArrayList<>();

    private final List<ScheduledExecutorService> cpuExecutors = new ArrayList<>();
    private final List<ScheduledFuture<?>>       cpuFutures   = new ArrayList<>();

    private volatile boolean isRunning   = false;
    private volatile int     globalClock = 0;
    private long simulatorStartMillis;

    private final Object scheduleLock = new Object();

    private final UI gui;

    public OperatingSystem(UI gui, int memorySize) {
        this.gui = gui;
        initialize(memorySize, 512, 2, new FCFS());
    }

    public void reset(int memorySize, int diskSize, int numCpus, SchedulerStrategy strategy) {
        stop();
        initialize(memorySize, diskSize, numCpus, strategy);
    }

    public void reset(int memorySize, int diskSize) { reset(memorySize, diskSize, 2, new FCFS()); }
    public void reset(int memorySize)               { reset(memorySize, 512, 2, new FCFS()); }

    private void initialize(int memorySize, int diskSize, int numCpus, SchedulerStrategy strat) {
        this.simulatorStartMillis = System.currentTimeMillis();
        this.strategy    = strat;
        this.globalClock = 0;
        this.memory      = new RAM(memorySize);
        this.disk        = new Disk(diskSize);
        this.scheduler   = new Scheduler(memory, disk, simulatorStartMillis);

        cpus.clear(); dispatchers.clear(); interruptHandlers.clear();
        cpuExecutors.forEach(ScheduledExecutorService::shutdownNow);
        cpuExecutors.clear(); cpuFutures.clear();

        int n = Math.max(2, Math.min(4, numCpus));
        for (int i = 0; i < n; i++) {
            final int idx = i;
            CPU cpu = new CPU();
            cpu.setId(idx + 1);
            InterruptHandler ih = new InterruptHandler(cpu, disk,
                msg -> { if (gui != null) gui.printConsole(msg); });
            Dispatcher d = new Dispatcher(cpu, scheduler, ih, simulatorStartMillis);
            cpus.add(cpu); dispatchers.add(d); interruptHandlers.add(ih);
            cpuExecutors.add(Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "CPU-" + (idx + 1));
                t.setDaemon(true);
                return t;
            }));
        }
    }

    public void resetGlobalClock() {
        this.globalClock = 0;
        this.simulatorStartMillis = System.currentTimeMillis();
    }

    public void resetProcessTimes() {
        long now = System.currentTimeMillis();
        this.simulatorStartMillis = now;

        for (OSProcess p : scheduler.getJobQueue().getAll()) {
            BCP bcp = p.getBcp();
            if (bcp == null) continue;

            bcp.setArrivalMillis(-1);
            bcp.setStartMillis(-1);
            bcp.setEndMillis(-1);
            bcp.setCpuCyclesUsed(0);

            bcp.markArrival(simulatorStartMillis);

            if (p.getState() == ProcessState.TERMINATED) {
            } else if (p.getState() == ProcessState.RUNNING) {
                p.setState(ProcessState.READY);
            }
        }

        for (int i = 0; i < dispatchers.size(); i++) {
            dispatchers.get(i).setCurrentProcess(null);
            cpus.get(i).setIR("");
        }
    }


    public OSProcess loadProcess(String path) {
        OSProcess process = scheduler.loadProcess(path);
        if (process == null) {
            JOptionPane.showMessageDialog(gui, "No se pudo cargar: " + path,
                "Error de carga", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        refreshKernel();
        SwingUtilities.invokeLater(() -> { if (gui != null) gui.refreshAll(); });
        if (process.getState() == ProcessState.WAITING)
            gui.printConsole(">> " + process.getName() + " enviado a SWAP (RAM llena)");
        else
            gui.printConsole(">> " + process.getName() + " cargado en RAM");
        return process;
    }

    public void run(boolean stepMode) {
        if (stepMode) { tickAll(); return; }

        isRunning = true;
        for (int i = 0; i < cpuExecutors.size(); i++) {
            final int idx = i;
            ScheduledFuture<?> f = cpuExecutors.get(i)
                .scheduleAtFixedRate(() -> tickCPU(idx), 0, 1, TimeUnit.SECONDS);
            cpuFutures.add(f);
        }
    }

    public void cpuCycle() { run(true); }

    public void stop() {
        isRunning = false;
        cpuFutures.forEach(f -> f.cancel(false));
        cpuFutures.clear();
    }

    private void tickCPU(int idx) {
        if (!isRunning) return;
        try {
            globalClock++;
            Dispatcher d   = dispatchers.get(idx);
            CPU        cpu = cpus.get(idx);

            if (d.getCurrentProcess() == null) {
                scheduler.tryLoadNewProcesses();
                scheduler.loadFromSwap();
                assignNext(d, cpu);
            }

            Integer pc = null;
            if (d.getCurrentProcess() != null) {
                OSProcess running = d.getCurrentProcess();
                running.getBcp().markStarted(simulatorStartMillis);
                pc = d.CPUcycle();
            }

            if (d.getCurrentProcess() != null) {
                List<OSProcess> snap = scheduler.getReadyQueue().getAll();
                strategy.onTick(d.getCurrentProcess(), snap);
                if (strategy.shouldPreempt(d.getCurrentProcess(), snap)) {
                    preempt(d, cpu);
                }
            }

            final Integer finalPc = pc;
            SwingUtilities.invokeLater(() -> {
                refreshKernel();
                if (gui != null) {
                    gui.refreshAll();
                    if (finalPc != null) gui.highlightRow(finalPc);
                }
            });

            if (scheduler.allTerminated()) {
                isRunning = false;
                SwingUtilities.invokeLater(() -> {
                    refreshKernel();
                    if (gui != null) {
                        gui.refreshAll();
                        JOptionPane.showMessageDialog(gui,
                            "Todos los procesos terminaron. Ciclos: " + globalClock,
                            "Ejecución finalizada", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            }
        } catch (Exception ex) {
            System.err.println("[CPU-" + idx + "] Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void tickAll() {
        globalClock++;
        scheduler.tryLoadNewProcesses();
        scheduler.loadFromSwap();

        for (int i = 0; i < dispatchers.size(); i++) {
            Dispatcher d   = dispatchers.get(i);
            CPU        cpu = cpus.get(i);

            if (d.getCurrentProcess() == null) {
                assignNext(d, cpu);
            }

            if (d.getCurrentProcess() != null) {
                d.getCurrentProcess().getBcp().markStarted(simulatorStartMillis);
                Integer pc = d.CPUcycle();
                if (pc != null && gui != null) gui.highlightRow(pc);
            }

            if (d.getCurrentProcess() != null) {
                List<OSProcess> snap = scheduler.getReadyQueue().getAll();
                strategy.onTick(d.getCurrentProcess(), snap);
                if (strategy.shouldPreempt(d.getCurrentProcess(), snap)) {
                    preempt(d, cpu);
                }
            }
        }

        refreshKernel();
        if (gui != null) gui.refreshAll();

        if (scheduler.allTerminated()) {
            stop();
            refreshKernel();
            if (gui != null) {
                gui.refreshAll();
                JOptionPane.showMessageDialog(gui,
                    "Todos los procesos terminaron. Ciclos: " + globalClock,
                    "Ejecución finalizada", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void assignNext(Dispatcher d, CPU cpu) {
        OSProcess next = null;
        synchronized (scheduleLock) {
            if (scheduler.hasProcessReady()) {
                List<OSProcess> snap = new ArrayList<>(scheduler.getReadyQueue().getAll());
                next = strategy.selectNext(snap);
                if (next != null) {
                    scheduler.getReadyQueue().remove(next);
                }
            }
        }

        if (next != null) {
            next.setState(ProcessState.RUNNING);
            next.restoreIntoCPU(cpu);
            if (cpu.getPC() < next.getBaseAddress() || cpu.getPC() >= next.getLimitAddress()) {
                cpu.setPC(next.getBaseAddress());
                next.getBcp().setPC(next.getBaseAddress());
            }
            cpu.setCurrentProcess(next);
            d.setCurrentProcess(next);
        }
    }

    private void preempt(Dispatcher d, CPU cpu) {
        OSProcess p = d.getCurrentProcess();
        if (p == null) return;

        p.getBcp().saveFromCPU(cpu, cpu.getIR());
        p.setState(ProcessState.READY);
        scheduler.getReadyQueue().enqueue(p);
        cpu.setCurrentProcess(null);
        d.setCurrentProcess(null);

        if (strategy instanceof RoundRobin) {
            ((RoundRobin) strategy).resetTicks();
        }
    }

    private void refreshKernel() {
        OSProcess running = dispatchers.isEmpty() ? null : dispatchers.get(0).getCurrentProcess();
        memory.updateKernelFromBCP(running != null ? running.getBcp() : null);
        List<BCP> all = scheduler.getJobQueue().getAll()
            .stream().map(OSProcess::getBcp).collect(Collectors.toList());
        memory.updateAllBCPsInKernel(all);
    }

    // Getters
    public CPU getCpu()                            { return cpus.isEmpty() ? null : cpus.get(0); }
    public CPU getCpu(int i)                       { return (i >= 0 && i < cpus.size()) ? cpus.get(i) : null; }
    public List<CPU> getAllCpus()                  { return cpus; }
    public List<Dispatcher> getDispatchers()       { return dispatchers; }
    public RAM getMemory()                         { return memory; }
    public Disk getDisk()                          { return disk; }
    public Scheduler getScheduler()               { return scheduler; }
    public SchedulerStrategy getStrategy()         { return strategy; }
    public boolean isRunning()                     { return isRunning; }
    public int getGlobalClock()                    { return globalClock; }
    public long getSimulatorStartMillis()          { return simulatorStartMillis; }
    public InterruptHandler getInterruptHandler()  { return interruptHandlers.isEmpty() ? null : interruptHandlers.get(0); }
    public InterruptHandler getInterruptHandler(int i) {
        return (i >= 0 && i < interruptHandlers.size()) ? interruptHandlers.get(i) : null;
    }
}