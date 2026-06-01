package com.mycompany.proyecto.so.process;

import com.mycompany.proyecto.so.core.cpu.CPU;
import com.mycompany.proyecto.so.core.cpu.CpuRegisters;
import java.util.ArrayList;
import java.util.List;

/**
 * Block Control Process (BCP / PCB).
 * Stores ALL process state — fixes P1 feedback:
 *   - nextBCP now stores the PID of the next BCP (not a memory address).
 *   - All register + timing fields are fully persisted.
 */
public class BCP {

    private final int    pid;
    private final String processName;
    private ProcessState state = ProcessState.NEW;
    private int    pc;
    private String ir  = "";
    private int    ac;
    private int    ax;
    private int    bx;
    private int    cx;
    private int    dx;
    private int baseAddress  = -1;
    private int limitAddress = -1;
    private final ProcessStack stack = new ProcessStack();
    private int  priority      = 0;
    private int  burstSize;          // Total instructions (= file line count)
    private int  remainingBurst;     // For SRT/SJF/RR preemption
    private int  arrivalTime   = 0;  // In simulator seconds (t=0 by default)
    private int  startTime     = -1; // When first got CPU
    private int  endTime       = -1; // When TERMINATED
    private int  cpuCyclesUsed = 0;
    private int  assignedCore  = -1; // Which CPU core is running thi
    private final List<String> openFiles = new ArrayList<>();
    private int nextBcpPid = -1;
    public BCP(int pid, String processName, int burstSize) {
        this.pid           = pid;
        this.processName   = processName;
        this.burstSize     = burstSize;
        this.remainingBurst = burstSize;
    }


    public void saveFromCPU(CPU cpu) {
        CpuRegisters r = cpu.getRegisters();
        pc = r.getPC();
        ir = r.getIR();
        ac = r.getAC();
        ax = r.getAX();
        bx = r.getBX();
        cx = r.getCX();
        dx = r.getDX();
    }

    public void restoreIntoCPU(CPU cpu) {
        CpuRegisters r = cpu.getRegisters();
        r.setPC(pc);
        r.setIR(ir);
        r.setAC(ac);
        r.setAX(ax);
        r.setBX(bx);
        r.setCX(cx);
        r.setDX(dx);
    }


    public void markStarted(int currentTime) {
        if (startTime < 0) startTime = currentTime;
    }

    public void markTerminated(int currentTime) {
        endTime = currentTime;
        state   = ProcessState.TERMINATED;
    }

    /** Turnaround = endTime - arrivalTime */
    public int getTurnaround() {
        if (endTime < 0) return -1;
        return endTime - arrivalTime;
    }

    /** Tr/Ts = turnaround / burstSize */
    public double getTrTs() {
        if (burstSize == 0) return 0;
        return (double) getTurnaround() / burstSize;
    }

    public void incrementCycles() {
        cpuCyclesUsed++;
        remainingBurst = Math.max(0, remainingBurst - 1);
    }


    public int    getPid()              { return pid; }
    public String getProcessName()      { return processName; }
    public ProcessState getState()      { return state; }
    public void   setState(ProcessState s) { this.state = s; }
    public int    getPC()               { return pc; }
    public void   setPC(int pc)         { this.pc = pc; }
    public String getIR()               { return ir; }
    public int    getAC()               { return ac; }
    public int    getAX()               { return ax; }
    public int    getBX()               { return bx; }
    public int    getCX()               { return cx; }
    public int    getDX()               { return dx; }
    public int    getBaseAddress()      { return baseAddress; }
    public void   setBaseAddress(int v) { this.baseAddress = v; }
    public int    getLimitAddress()     { return limitAddress; }
    public void   setLimitAddress(int v){ this.limitAddress = v; }
    public ProcessStack getStack()      { return stack; }
    public int    getPriority()         { return priority; }
    public void   setPriority(int v)    { this.priority = v; }
    public int    getBurstSize()        { return burstSize; }
    public void   setBurstSize(int v)   { this.burstSize = v; this.remainingBurst = v; }
    public int    getRemainingBurst()   { return remainingBurst; }
    public void   setRemainingBurst(int v) { this.remainingBurst = v; }
    public int    getArrivalTime()      { return arrivalTime; }
    public void   setArrivalTime(int v) { this.arrivalTime = v; }
    public int    getStartTime()        { return startTime; }
    public int    getEndTime()          { return endTime; }
    public int    getCpuCyclesUsed()    { return cpuCyclesUsed; }
    public int    getAssignedCore()     { return assignedCore; }
    public void   setAssignedCore(int v){ this.assignedCore = v; }
    public List<String> getOpenFiles()  { return openFiles; }
    public int    getNextBcpPid()       { return nextBcpPid; }
    public void   setNextBcpPid(int v)  { this.nextBcpPid = v; }
}
