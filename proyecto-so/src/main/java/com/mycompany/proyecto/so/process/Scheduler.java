package com.mycompany.proyecto.so.process;

import com.mycompany.proyecto.so.core.memory.IMemoryAllocationStrategy;
import com.mycompany.proyecto.so.core.memory.RAM;
import java.util.*;

/**
 * Job Scheduler.
 * Manages the job queue, ready queue, and waiting queue.
 * Admits processes to RAM using the configured IMemoryAllocationStrategy.
 *
 * SRP: queue/admission logic only.  Execution is in Dispatcher.
 * OCP: swap memory strategy without changing this class.
 */
public class Scheduler {

    private final RAM                        ram;
    private final IMemoryAllocationStrategy  memStrategy;
    private final List<Process>              jobQueue   = new ArrayList<>();
    private final List<Process>              readyQueue = new ArrayList<>();
    private final List<Process>              waitingQueue = new ArrayList<>();
    private int pidCounter = 0;

    public Scheduler(RAM ram, IMemoryAllocationStrategy memStrategy) {
        this.ram         = ram;
        this.memStrategy = memStrategy;
    }


    public void addProcess(Process process) {
        jobQueue.add(process);
        tryAdmit(process);
    }

    /**
     * Try to load any NEW process that fits in RAM.
     * Called each simulator tick.
     */
    public void tryAdmitPending() {
        for (Process p : jobQueue) {
            if (p.getBcp().getState() == ProcessState.NEW) {
                tryAdmit(p);
            }
        }
    }

    private boolean tryAdmit(Process p) {
        if (p.getBcp().getState() != ProcessState.NEW) return false;
        boolean ok = memStrategy.allocate(p, ram);
        if (ok) {
            p.getBcp().setState(ProcessState.READY);
            readyQueue.add(p);
        }
        return ok;
    }


    public void terminate(Process p, int currentTime) {
        p.getBcp().markTerminated(currentTime);
        memStrategy.free(p, ram);
        readyQueue.remove(p);
        waitingQueue.remove(p);
        tryAdmitPending();
    }


    public void rebuildBcpChain() {
        for (int i = 0; i < jobQueue.size() - 1; i++) {
            jobQueue.get(i).getBcp().setNextBcpPid(jobQueue.get(i + 1).getPid());
        }
        if (!jobQueue.isEmpty()) {
            jobQueue.get(jobQueue.size() - 1).getBcp().setNextBcpPid(-1);
        }
    }


    public void moveToWaiting(Process p) {
        readyQueue.remove(p);
        p.getBcp().setState(ProcessState.WAITING);
        waitingQueue.add(p);
    }

    public void releaseFromWaiting(int pid) {
        waitingQueue.stream()
            .filter(p -> p.getPid() == pid)
            .findFirst()
            .ifPresent(p -> {
                waitingQueue.remove(p);
                p.getBcp().setState(ProcessState.READY);
                readyQueue.add(p);
            });
    }


    public boolean allTerminated() {
        return jobQueue.stream().allMatch(p -> p.getBcp().getState() == ProcessState.TERMINATED);
    }

    public boolean hasReady()      { return !readyQueue.isEmpty(); }
    public int     nextPid()       { return pidCounter++; }


    public List<Process> getJobQueue()     { return Collections.unmodifiableList(jobQueue); }
    public List<Process> getReadyQueue()   { return readyQueue; }
    public List<Process> getWaitingQueue() { return Collections.unmodifiableList(waitingQueue); }
    public RAM           getRam()          { return ram; }
    public IMemoryAllocationStrategy getMemStrategy() { return memStrategy; }
}
