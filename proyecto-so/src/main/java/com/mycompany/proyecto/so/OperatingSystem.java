package com.mycompany.proyecto.so;

import com.mycompany.proyecto.so.config.SystemConfig;
import com.mycompany.proyecto.so.core.cpu.CPU;
import com.mycompany.proyecto.so.core.io.InterruptHandler;
import com.mycompany.proyecto.so.core.memory.Disk;
import com.mycompany.proyecto.so.core.memory.RAM;
import com.mycompany.proyecto.so.filehandler.AsmLoader;
import com.mycompany.proyecto.so.process.*;
import com.mycompany.proyecto.so.statistics.ExecutionStatistics;
import java.util.*;

/**
 * Main OS facade.
 * Wires all subsystems together and exposes a clean API to the UI.
 *
 * P2 changes:
 *   - 2-4 CPU cores, each with its own Dispatcher.
 *   - Single shared Scheduler + RAM + Disk.
 *   - Scheduling algorithm swappable at any time via setAlgorithm().
 *   - Simulator advances second-by-second (tick()).
 */
public class OperatingSystem {

    // ── Subsystems ────────────────────────────────────────────────────────────
    private final RAM                  ram;
    private final Disk                 disk;
    private final Scheduler            scheduler;
    private final InterruptHandler     interruptHandler;
    private final List<CPU>            cpus        = new ArrayList<>();
    private final List<Dispatcher>     dispatchers = new ArrayList<>();
    private final ExecutionStatistics  statistics  = new ExecutionStatistics();
    private final AsmLoader            asmLoader   = new AsmLoader();
    private       SystemConfig         config;

    // ── Simulation state ──────────────────────────────────────────────────────
    private int     simulatorTime = 0;
    private boolean running       = false;
    private Thread  simulationThread;

    // ── UI callback ───────────────────────────────────────────────────────────
    @FunctionalInterface public interface UICallback {
        void onTick(int time, List<Process> allProcesses, List<CPU> cpus);
    }
    @FunctionalInterface public interface ConsoleCallback { void print(String msg); }

    private UICallback      onTick;
    private ConsoleCallback onConsole;

    // ── Constructor ───────────────────────────────────────────────────────────

    public OperatingSystem(SystemConfig config,
                           UICallback uiCallback,
                           ConsoleCallback consoleCallback) {
        this.config    = config;
        this.onTick    = uiCallback;
        this.onConsole = consoleCallback;

        ram  = new RAM(config.getRamSize());
        disk = new Disk(config.getDiskSize());

        interruptHandler = new InterruptHandler(disk, msg -> {
            if (onConsole != null) onConsole.print(msg);
        });

        scheduler = new Scheduler(ram, config.getMemoryStrategy());

        // Create one CPU + Dispatcher per core
        for (int i = 0; i < config.getCpuCount(); i++) {
            CPU        cpu  = new CPU(i);
            Dispatcher disp = new Dispatcher(cpu, interruptHandler, ram, config.getSchedulingAlgorithm());
            cpus.add(cpu);
            dispatchers.add(disp);
        }
    }

    // ── Process loading ───────────────────────────────────────────────────────

    /**
     * Load a .asm file. Returns error messages (empty = success).
     */
    public List<String> loadProcess(String path) {
        AsmLoader.Result result = asmLoader.load(path);
        if (!result.isOk()) return result.errors();

        int     pid  = scheduler.nextPid();
        String  name = extractName(path);
        Process p    = new Process(pid, name, result.instructions());

        disk.save(name, result.instructions());
        p.setDiskAddress(disk.getEntry(name).map(e -> e.address()).orElse(-1));

        scheduler.addProcess(p);
        scheduler.rebuildBcpChain();

        if (onConsole != null) onConsole.print(">> Proceso cargado: " + name + " (PID=" + pid + ")");
        return Collections.emptyList();
    }

    // ── Execution ─────────────────────────────────────────────────────────────

    /** Auto-run: tick every second until all processes finish. */
    public void runAuto() {
        if (running) return;
        running = true;
        simulationThread = new Thread(() -> {
            while (running && !scheduler.allTerminated()) {
                tick();
                try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            running = false;
            recordStats();
        });
        simulationThread.setDaemon(true);
        simulationThread.start();
    }

    /** Step mode: execute exactly one tick. */
    public void step() {
        if (!scheduler.allTerminated()) {
            tick();
        }
    }

    public void stop() {
        running = false;
        if (simulationThread != null) simulationThread.interrupt();
    }

    // ── Core tick ─────────────────────────────────────────────────────────────

    /**
     * Advance simulation by 1 second.
     * Each Dispatcher gets one instruction executed on its assigned process.
     */
    private void tick() {
        // Try to admit any waiting NEW processes
        scheduler.tryAdmitPending();

        // Distribute ready processes across idle dispatchers
        List<Process> readyQueue = scheduler.getReadyQueue();

        // Each dispatcher takes one tick
        for (Dispatcher d : dispatchers) {
            int executedPc = d.tick(readyQueue, simulatorTime);

            // If the dispatcher's process terminated, record it
            // (Dispatcher calls scheduler.terminate internally via callback — or we check here)
        }

        // Check for freshly-terminated processes and release memory
        for (Dispatcher d : dispatchers) {
            // If dispatcher has no current process, it was terminated or never had one.
            // Terminated processes are collected via d.getTerminatedProcess() if we add that.
        }

        simulatorTime++;

        if (onTick != null) {
            onTick.onTick(simulatorTime, scheduler.getJobQueue(), cpus);
        }
    }

    // ── Swap algorithm at runtime ─────────────────────────────────────────────

    public void setSchedulingAlgorithm(com.so.p2.core.scheduling.ISchedulingAlgorithm alg) {
        dispatchers.forEach(d -> d.setAlgorithm(alg));
    }

    // ── Statistics ────────────────────────────────────────────────────────────

    private void recordStats() {
        String algName = dispatchers.isEmpty() ? "N/A"
            : dispatchers.get(0).getAlgorithm().getName();
        for (Process p : scheduler.getJobQueue()) {
            statistics.record(p, algName);
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public RAM                 getRam()         { return ram; }
    public Disk                getDisk()        { return disk; }
    public Scheduler           getScheduler()   { return scheduler; }
    public List<CPU>           getCpus()        { return Collections.unmodifiableList(cpus); }
    public List<Dispatcher>    getDispatchers() { return Collections.unmodifiableList(dispatchers); }
    public ExecutionStatistics getStatistics()  { return statistics; }
    public int                 getTime()        { return simulatorTime; }
    public boolean             isRunning()      { return running; }
    public SystemConfig        getConfig()      { return config; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String extractName(String path) {
        return path.substring(path.lastIndexOf(java.io.File.separator) + 1)
                   .replace(".asm", "");
    }
}
