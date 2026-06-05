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

    // ── Hardware ──────────────────────────────────────────────────────────
    private final List<CPU>         cpus        = new ArrayList<>();
    private final List<Dispatcher>  dispatchers = new ArrayList<>();
    private RAM    memory;
    private Disk   disk;

    // ── Software ──────────────────────────────────────────────────────────
    private Scheduler      scheduler;
    private VirtualMemory  virtualMemory;
    private SchedulerStrategy strategy;

    // ── Interrupciones ────────────────────────────────────────────────────
    private List<InterruptHandler> interruptHandlers = new ArrayList<>();

    // ── Simulación ────────────────────────────────────────────────────────
    private Timer   swingTimer;
    private boolean isRunning   = false;
    private int     globalClock = 0;
    private long    simulatorStartMillis;

    // ── UI ────────────────────────────────────────────────────────────────
    private final UI gui;

    // ─────────────────────────────────────────────────────────────────────
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

    // ─────────────────────────────────────────────────────────────────────
    private void initialize(int memorySize, int diskSize,
                            int numCpus, SchedulerStrategy strat) {
        this.simulatorStartMillis = System.currentTimeMillis();
        this.strategy = strat;
        this.virtualMemory = new VirtualMemory();

        this.memory = new RAM(memorySize);
        this.disk = new Disk(diskSize);
        this.scheduler = new Scheduler(memory, disk, simulatorStartMillis);

        cpus.clear();
        dispatchers.clear();
        interruptHandlers.clear();

        for (int i = 0; i < numCpus; i++) {
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

    // ─────────────────────────────────────────────────────────────────────
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

    // ─────────────────────────────────────────────────────────────────────
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
            if (!isRunning) { 
                swingTimer.stop(); 
                return; 
            }
            tick();
        });
        swingTimer.start();
    }

    public void cpuCycle() {
        run(true);
    }

    public void stop() {
        isRunning = false;
        if (swingTimer != null) {
            swingTimer.stop();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    private void tick() {
        globalClock++;
        scheduler.tryLoadNewProcesses();

        List<Integer> executedPCs = new ArrayList<>();

        for (int i = 0; i < dispatchers.size(); i++) {
            Dispatcher d = dispatchers.get(i);
            CPU cpu = cpus.get(i);

            // Asignar nuevo proceso si la CPU está libre y hay procesos listos
            if (d.getCurrentProcess() == null && scheduler.hasProcessReady()) {
                List<OSProcess> readyList = new ArrayList<>(scheduler.getReadyQueue().getAll());
                OSProcess next = strategy.selectNext(readyList);
                if (next != null) {
                    scheduler.getReadyQueue().remove(next);
                    d.setCurrentProcess(next);
                    next.setState(ProcessState.RUNNING);
                    next.markStarted(simulatorStartMillis);
                    next.restoreIntoCPU(cpu);
                    cpu.setPC(next.getBaseAddress());
                }
            }

            // Ejecutar 1 instrucción
            Integer pc = d.CPUcycle();
            if (pc != null) {
                executedPCs.add(pc);
            }

            // Notificar preemption al algoritmo
            if (d.getCurrentProcess() != null) {
                List<OSProcess> readyList = new ArrayList<>(scheduler.getReadyQueue().getAll());
                strategy.onTick(d.getCurrentProcess(), readyList);
                if (strategy.shouldPreempt(d.getCurrentProcess(), readyList)) {
                    preempt(d, cpu);
                }
            }
        }

        // Actualizar UI
        if (gui != null) {
            gui.refreshAll();
            if (!executedPCs.isEmpty()) {
                gui.highlightRow(executedPCs.get(0));
            }
        }

        // Actualizar kernel en memoria
        if (memory != null) {
            OSProcess currentProc = dispatchers.isEmpty() ? null : dispatchers.get(0).getCurrentProcess();
            memory.updateKernelFromBCP(currentProc != null ? currentProc.getBcp() : null);
        }

        // Verificar si todos los procesos terminaron
        if (scheduler.allTerminated()) {
            stop();
            if (memory != null) {
                memory.updateKernelFromBCP(null);
            }
            if (gui != null) {
                gui.refreshAll();
            }
            JOptionPane.showMessageDialog(gui,
                "Todos los procesos terminaron. Ciclos: " + globalClock,
                "Ejecución finalizada", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void preempt(Dispatcher d, CPU cpu) {
        OSProcess p = d.getCurrentProcess();
        if (p == null) return;
        
        p.saveFromCPU(cpu, cpu.getIR());
        p.setState(ProcessState.READY);
        scheduler.getReadyQueue().enqueue(p);
        d.setCurrentProcess(null);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Getters
    public CPU getCpu() {
        return cpus.isEmpty() ? null : cpus.get(0);
    }
    
    public CPU getCpu(int index) {
        return (index >= 0 && index < cpus.size()) ? cpus.get(index) : null;
    }
    
    public List<CPU> getAllCpus() {
        return cpus;
    }
    
    public List<Dispatcher> getDispatchers() {
        return dispatchers;
    }
    
    public RAM getMemory() {
        return memory;
    }
    
    public Disk getDisk() {
        return disk;
    }
    
    public Scheduler getScheduler() {
        return scheduler;
    }
    
    public VirtualMemory getVirtualMemory() {
        return virtualMemory;
    }
    
    public SchedulerStrategy getStrategy() {
        return strategy;
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public int getGlobalClock() {
        return globalClock;
    }
    
    public long getSimulatorStartMillis() {
        return simulatorStartMillis;
    }
    
    public InterruptHandler getInterruptHandler() {
        return interruptHandlers.isEmpty() ? null : interruptHandlers.get(0);
    }
    
    public InterruptHandler getInterruptHandler(int i) {
        return (i >= 0 && i < interruptHandlers.size()) ? interruptHandlers.get(i) : null;
    }
}