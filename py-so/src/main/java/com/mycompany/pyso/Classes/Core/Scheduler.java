/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.Core;
import com.mycompany.pyso.Classes.Process.JobQueue;
import com.mycompany.pyso.Classes.Process.ReadyQueue;
import com.mycompany.pyso.Classes.Process.WaitingQueue;
import com.mycompany.pyso.Classes.Memory.Disk;
import com.mycompany.pyso.Classes.Memory.RAM;
import com.mycompany.pyso.Classes.Process.Process;
import com.mycompany.pyso.Classes.Process.ProcessState;
import com.mycompany.pyso.Classes.FileHandler.LoadASM;
import java.util.List;
import com.mycompany.pyso.Classes.Process.BCP;

/**
 *
 * @author jimen
 */

public class Scheduler {

    private final JobQueue     jobQueue;
    private final ReadyQueue   readyQueue;
    private final WaitingQueue waitingQueue;
    private final RAM  ram;
    private final Disk disk;
    private final long simulatorStartMillis;
    private int pidCounter = 0;
    private int nextFreeAddress;

    private static final int KERNEL_SIZE = 20;

    public Scheduler(RAM ram, Disk disk, long simulatorStartMillis) {
        this.ram  = ram;
        this.disk = disk;
        this.simulatorStartMillis = simulatorStartMillis;
        this.jobQueue    = new JobQueue();
        this.readyQueue  = new ReadyQueue();
        this.waitingQueue = new WaitingQueue();
        this.nextFreeAddress = KERNEL_SIZE;
    }

    public Process loadProcess(String path) {
        LoadASM loader = new LoadASM();
        loader.readFile(path);
        if (loader.isFormatError() || loader.isExtensionError()) return null;

        List<Instruction> instructions = loader.getInstructions();
        if (instructions.isEmpty()) return null;

        int diskAddress = disk.save(extractName(path), instructions);
        int pid  = pidCounter++;
        String name = extractName(path);

        BCP bcp = new BCP(pid, name, -1, -1, 0);
        bcp.markArrival(simulatorStartMillis);

        Process process = new Process();
        process.setPID(pid);
        process.setName(name);
        process.setBcp(bcp);
        process.setInstructions(instructions);
        process.setDiskAddress(diskAddress);
        process.setDiskSize(instructions.size());
        process.getBcp().setState(ProcessState.NEW);

        jobQueue.add(process);

        linkBCP(process);

        admitToRAM(process);
        return process;
    }

    private void linkBCP(Process newProcess) {
        List<Process> all = jobQueue.getAll();
        for (int i = all.size() - 2; i >= 0; i--) {
            Process prev = all.get(i);
            if (prev.getBcp().getNextBCP() == null) {
                prev.getBcp().setNextBCP(newProcess.getBcp());
                break;
            }
        }
    }

    public boolean admitToRAM(Process process) {
        if (process.getBcp().getState() != ProcessState.NEW) return false;

        int instrCount = process.getInstructions().size();
        int base = findFreeBlock(instrCount);

        if (base == -1) return false;

        int limit = base + instrCount;
        nextFreeAddress = limit;

        process.setBaseAddress(base);
        process.setLimitAddress(limit);
        process.getBcp().setBaseAddress(base);
        process.getBcp().setLimitAddress(limit);
        process.getBcp().setPC(base);

        String name = process.getName();
        for (int i = 0; i < instrCount; i++) {
            String instr = process.getInstructions().get(i).getInstruction();
            ram.getMemory()[base + i] = name + " - " + instr;
        }

        process.getBcp().setState(ProcessState.READY);
        readyQueue.enqueue(process);
        return true;
    }

    public Process nextToRun() {
        return readyQueue.dequeue();
    }

    public boolean hasNewProcesses() {
        return jobQueue.getAll().stream()
            .anyMatch(p -> p.getBcp().getState() == ProcessState.NEW);
    }

    public boolean hasProcessReady() {
        return readyQueue.hasNext();
    }

    public void moveToWaiting(Process process) {
        waitingQueue.enqueue(process);
    }

    public void releaseFromWaiting(int PID) {
        Process process = waitingQueue.release(PID);
        if (process != null) {
            readyQueue.enqueue(process);
        }
    }

    public void terminateProcess(Process process) {
        process.getBcp().markTerminated(simulatorStartMillis);

        int base  = process.getBaseAddress();
        int limit = process.getLimitAddress();
        for (int i = base; i < limit; i++) {
            ram.getMemory()[i] = "";
        }

        if (limit == nextFreeAddress) {
            nextFreeAddress = base;
        }

        tryLoadNewProcesses();
    }

    public void tryLoadNewProcesses() {
        for (Process p : jobQueue.getAll()) {
            if (p.getBcp().getState() == ProcessState.NEW) {
                boolean loaded = admitToRAM(p);
                if (loaded) {
                    p.getBcp().setState(ProcessState.READY);
                }
            }
        }
    }

    public boolean allTerminated() {
        return jobQueue.getAll().stream()
            .allMatch(p -> p.getBcp().getState() == ProcessState.TERMINATED);
    }

    private int findFreeBlock(int size) {
        String[] memory = ram.getMemory();
        int count = 0;
        int start = -1;

        for (int i = KERNEL_SIZE; i < memory.length; i++) {
            if (memory[i] == null || memory[i].equals("")) {
                if (count == 0) start = i;
                count++;
                if (count == size) return start;
            } else {
                count = 0;
                start = -1;
            }
        }
        return -1;
    }

    private String extractName(String path) {
        return path.substring(path.lastIndexOf(java.io.File.separator) + 1)
                   .replace(".asm", "");
    }


    public int getPidCounter()                  { return pidCounter; }
    public void setPidCounter(int v)            { this.pidCounter = v; }
    public int getNextFreeAddress()             { return nextFreeAddress; }
    public void setNextFreeAddress(int v)       { this.nextFreeAddress = v; }
    public JobQueue getJobQueue()               { return jobQueue; }
    public ReadyQueue getReadyQueue()           { return readyQueue; }
    public WaitingQueue getWaitingQueue()       { return waitingQueue; }
    public RAM getRam()                         { return ram; }
    public Disk getDisk()                       { return disk; }
    public long getSimulatorStartMillis()       { return simulatorStartMillis; }
    public static int getKERNEL_SIZE()          { return KERNEL_SIZE; }
}