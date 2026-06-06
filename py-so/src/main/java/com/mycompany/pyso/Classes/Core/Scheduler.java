package com.mycompany.pyso.Classes.Core;

import com.mycompany.pyso.Classes.Memory.Disk;
import com.mycompany.pyso.Classes.Memory.RAM;
import com.mycompany.pyso.Classes.Process.BCP;
import com.mycompany.pyso.Classes.Process.JobQueue;
import com.mycompany.pyso.Classes.Process.OSProcess;
import com.mycompany.pyso.Classes.Process.ProcessState;
import com.mycompany.pyso.Classes.Process.ReadyQueue;
import com.mycompany.pyso.Classes.Process.WaitingQueue;
import com.mycompany.pyso.Classes.FileHandler.LoadASM;
import java.util.List;

public class Scheduler {

    private final JobQueue     jobQueue;
    private final ReadyQueue   readyQueue;
    private final WaitingQueue waitingQueue;
    private final RAM  ram;
    private final Disk disk;      
    private final long simulatorStartMillis;

    private int pidCounter     = 0;
    private int nextFreeAddr;

    private static final int KERNEL_SIZE = 150;

    private final Object ramLock = new Object();

    public Scheduler(RAM ram, Disk disk, long simulatorStartMillis) {
        this.ram                  = ram;
        this.disk                 = disk;
        this.simulatorStartMillis = simulatorStartMillis;
        this.jobQueue             = new JobQueue();
        this.readyQueue           = new ReadyQueue();  
        this.waitingQueue         = new WaitingQueue();
        this.nextFreeAddr         = KERNEL_SIZE;
    }

    public OSProcess loadProcess(String path) {
        LoadASM loader = new LoadASM();
        loader.readFile(path);
        if (loader.isFormatError() || loader.isExtensionError()) return null;

        List<Instruction> instructions = loader.getInstructions();
        if (instructions.isEmpty()) return null;

        String name       = extractName(path);
        int    diskAddr   = disk.save(name, instructions);
        int    pid        = pidCounter++;

        BCP bcp = new BCP(pid, name, -1, -1, 0);
        bcp.markArrival(simulatorStartMillis);

        OSProcess p = new OSProcess();
        p.setPID(pid);
        p.setName(name);
        p.setBcp(bcp);
        p.setState(ProcessState.NEW);
        p.setInstructions(instructions);
        p.setDiskAddress(diskAddr);
        p.setDiskSize(instructions.size());

        jobQueue.add(p);
        linkBCP(p);
        admitToRAM(p);
        return p;
    }

    private void linkBCP(OSProcess newProc) {
        List<OSProcess> all = jobQueue.getAll();
        for (int i = all.size() - 2; i >= 0; i--) {
            OSProcess prev = all.get(i);
            if (prev.getBcp().getNextBCP() == null) {
                prev.getBcp().setNextBCP(newProc.getBcp());
                break;
            }
        }
    }

    public boolean admitToRAM(OSProcess p) {
        synchronized (ramLock) {
            if (p.getState() != ProcessState.NEW && p.getState() != ProcessState.WAITING)
                return false;

            int size = p.getInstructions().size();
            int base = findFreeBlock(size);

            if (base == -1) {
                if (!disk.isInSwap(p.getPID())) disk.swapOut(p);
                p.setState(ProcessState.WAITING);
                return false;
            }

            int limit = base + size;
            if (limit > nextFreeAddr) nextFreeAddr = limit;

            p.setBaseAddress(base);
            p.setLimitAddress(limit);
            p.getBcp().setBaseAddress(base);
            p.getBcp().setLimitAddress(limit);
            p.getBcp().setPC(base);

            String[] mem = ram.getMemory();
            for (int i = 0; i < size; i++)
                mem[base + i] = p.getName() + " - " + p.getInstructions().get(i).getInstruction();

            p.setState(ProcessState.READY);
            readyQueue.enqueue(p);
            return true;
        }
    }

    public void terminateProcess(OSProcess p) {
        p.getBcp().markTerminated(simulatorStartMillis);
        p.setState(ProcessState.TERMINATED);

        synchronized (ramLock) {
            int base  = p.getBaseAddress();
            int limit = p.getLimitAddress();
            String[] mem = ram.getMemory();
            for (int i = base; i < limit && i < mem.length; i++) mem[i] = "";

            // Reclaim nextFreeAddr if this was at the top
            if (limit >= nextFreeAddr) {
                nextFreeAddr = base;
                for (int i = base - 1; i >= KERNEL_SIZE; i--) {
                    if (mem[i] == null || mem[i].isEmpty()) nextFreeAddr = i;
                    else break;
                }
            }
        }

        // Try loading waiting processes now that RAM has space
        loadFromSwap();
        tryLoadNewProcesses();
    }

    /** Loads as many swap processes into RAM as space allows. */
    public void loadFromSwap() {
        while (!disk.isSwapEmpty()) {
            Disk.SwapEntry entry = disk.swapInNext();
            if (entry == null) break;

            OSProcess p = jobQueue.getAll().stream()
                .filter(proc -> proc.getPID() == entry.pid)
                .findFirst().orElse(null);

            if (p == null) continue;

            p.setState(ProcessState.NEW);
            boolean loaded = admitToRAM(p);
            if (!loaded) {
                // Still no space — put back at front of swap
                disk.swapOut(p);
                break;
            }
        }
    }

    public void tryLoadNewProcesses() {
        for (OSProcess p : jobQueue.getAll()) {
            if (p.getState() == ProcessState.NEW) admitToRAM(p);
        }
    }

    public boolean hasProcessReady()  { return readyQueue.hasNext(); }
    public boolean hasNewProcesses()  {
        return jobQueue.getAll().stream().anyMatch(p -> p.getState() == ProcessState.NEW);
    }

    public void moveToWaiting(OSProcess p) { waitingQueue.enqueue(p); }

    public void releaseFromWaiting(int pid) {
        OSProcess p = waitingQueue.release(pid);
        if (p != null) { p.setState(ProcessState.READY); readyQueue.enqueue(p); }
    }

    public boolean allTerminated() {
        return !jobQueue.isEmpty() &&
               jobQueue.getAll().stream().allMatch(p -> p.getState() == ProcessState.TERMINATED);
    }

    private int findFreeBlock(int size) {
        String[] mem = ram.getMemory();
        int count = 0, start = -1;
        for (int i = KERNEL_SIZE; i < mem.length; i++) {
            if (mem[i] == null || mem[i].isEmpty()) {
                if (count == 0) start = i;
                if (++count == size) return start;
            } else { count = 0; start = -1; }
        }
        return -1;
    }

    private String extractName(String path) {
        String n = path.substring(path.lastIndexOf(java.io.File.separator) + 1);
        int dot = n.lastIndexOf('.');
        return dot > 0 ? n.substring(0, dot) : n;
    }

    public JobQueue     getJobQueue()               { return jobQueue; }
    public ReadyQueue   getReadyQueue()             { return readyQueue; }
    public WaitingQueue getWaitingQueue()           { return waitingQueue; }
    public RAM          getRam()                    { return ram; }
    public Disk         getDisk()                   { return disk; }
    public long         getSimulatorStartMillis()   { return simulatorStartMillis; }
    public int          getPidCounter()             { return pidCounter; }
    public static int   getKERNEL_SIZE()            { return KERNEL_SIZE; }
}