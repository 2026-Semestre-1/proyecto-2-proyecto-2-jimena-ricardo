/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.Process;
import com.mycompany.pyso.Classes.Core.CPU;
import java.util.List;
/**
 *
 * @author jimen
 */
public class BCP {
    private int PID;     
    private String processName;    
    private ProcessState state;  
    private int PC;//Process counter
    private int memory_limit;//limit of the process in memory
    private String IR;
    private int AC;
    private int AX;
    private int BX;
    private int CX;
    private int DX;
    private int baseAddress;    
    private int limitAddress; 
    private ProcessStack stack; 
    private int stackPointer;  
    private long arrivalMillis;
    private long startMillis;
    private long endMillis;
    private int cpuCyclesUsed;  
    private java.util.List<String> openFiles;
    private int priority;  
    private BCP nextBCP;  
    
    private int arrivalTick;
    private int startTick;
    private int endTick;

    public BCP(int PID, String processName, int baseAddress, int limitAddress, int priority) {
        this.PID = PID;
        this.processName = processName;
        this.state = ProcessState.NEW;
        this.PC = baseAddress; 
        this.IR = "";
        this.AC = 0;
        this.AX = 0;
        this.BX = 0;
        this.CX = 0;
        this.DX = 0;
        this.baseAddress = baseAddress;
        this.limitAddress = limitAddress;
        this.stack = new ProcessStack(PID);
        this.stackPointer = -1; 
        this.arrivalMillis = -1;
        this.startMillis = -1; 
        this.endMillis = -1; 
        this.cpuCyclesUsed = 0;
        this.openFiles = new java.util.ArrayList<>();
        this.priority = priority;
        this.nextBCP = null;
        this.arrivalTick = 0;
        this.startTick = -1;
        this.endTick = -1;
    }
    
    public void saveFromCPU(CPU cpu, String currentIR) {
        this.PC = cpu.getPC();
        this.IR = currentIR;
        this.AC = cpu.getAC();
        this.AX = cpu.getAX();
        this.BX = cpu.getBX();
        this.CX = cpu.getCX();
        this.DX = cpu.getDX();
    }
    
    public void restoreIntoCPU(CPU cpu) {
        cpu.setPC(this.PC);
        cpu.setIR(this.IR);
        cpu.setAC(this.AC);
        cpu.setAX(this.AX);
        cpu.setBX(this.BX);
        cpu.setCX(this.CX);
        cpu.setDX(this.DX);
    }
    
    private long simulatorStartMillis;

    public void start() {
        this.simulatorStartMillis = System.currentTimeMillis();
    }

    public void markArrival(long simulatorStartMillis) {
        this.arrivalMillis = System.currentTimeMillis() - simulatorStartMillis;
    }

    public void markStarted(long simulatorStartMillis) {
        if (startMillis == -1) {
            startMillis = System.currentTimeMillis() - simulatorStartMillis;
        }
    }

    public void markTerminated(long simulatorStartMillis) {
        endMillis = System.currentTimeMillis() - simulatorStartMillis;
        state = ProcessState.TERMINATED;
    }

    public long getDurationSeconds() {
        if (startMillis == -1 || endMillis == -1) return 0;
        return (endMillis - startMillis) / 1000;
    }

    public String formatElapsed(long millis) {
        if (millis == -1) return "--:--:--";
        long totalSeconds = millis / 1000;
        long hours   = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public int getArrivalTick() {
        return arrivalTick;
    }

    public void setArrivalTick(int arrivalTick) {
        this.arrivalTick = arrivalTick;
    }

    public int getStartTick() {
        return startTick;
    }

    public void setStartTick(int startTick) {
        this.startTick = startTick;
    }

    public int getEndTick() {
        return endTick;
    }

    public void setEndTick(int endTick) {
        this.endTick = endTick;
    }

    public int getMemory_limit() {
        return memory_limit;
    }

    public void setMemory_limit(int memory_limit) {
        this.memory_limit = memory_limit;
    }

    public int getPID() {
        return PID;
    }
    
    public void setLimit(int limit) {
        this.limitAddress = limit;
    }
    public int getProgramCounter() {
        return this.PC;
    }
    public String getInstructionRegister() {
        return this.IR;
    }

    public void setPID(int PID) {
        this.PID = PID;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }
    
    public void setStartTime(long startTime) {
        this.startMillis = startTime;
    }

    public long getStartTime() {
        return startMillis;
    }
    
    public void setEndTime(long endTime) {
        this.endMillis = endTime;
    }

    public long getEndTime() {
        return endMillis;
    }

    public int getPC() {
        return PC;
    }

    public void setPC(int PC) {
        this.PC = PC;
    }

    public String getIR() {
        return IR;
    }

    public void setIR(String IR) {
        this.IR = IR;
    }

    public int getAC() {
        return AC;
    }

    public void setAC(int AC) {
        this.AC = AC;
    }

    public int getAX() {
        return AX;
    }

    public void setAX(int AX) {
        this.AX = AX;
    }

    public int getBX() {
        return BX;
    }

    public void setBX(int BX) {
        this.BX = BX;
    }

    public int getCX() {
        return CX;
    }

    public void setCX(int CX) {
        this.CX = CX;
    }

    public int getDX() {
        return DX;
    }

    public void setDX(int DX) {
        this.DX = DX;
    }

    public int getBaseAddress() {
        return baseAddress;
    }

    public void setBaseAddress(int baseAddress) {
        this.baseAddress = baseAddress;
    }

    public int getLimitAddress() {
        return limitAddress;
    }

    public void setLimitAddress(int limitAddress) {
        this.limitAddress = limitAddress;
    }

    public ProcessStack getStack() {
        return stack;
    }

    public void setStack(ProcessStack stack) {
        this.stack = stack;
    }

    public int getStackPointer() {
        return stackPointer;
    }

    public void setStackPointer(int stackPointer) {
        this.stackPointer = stackPointer;
    }

    public long getArrivalMillis() {
        return arrivalMillis;
    }

    public void setArrivalMillis(long arrivalMillis) {
        this.arrivalMillis = arrivalMillis;
    }

    public long getStartMillis() {
        return startMillis;
    }

    public void setStartMillis(long startMillis) {
        this.startMillis = startMillis;
    }

    public long getEndMillis() {
        return endMillis;
    }

    public void setEndMillis(long endMillis) {
        this.endMillis = endMillis;
    }

    public int getCpuCyclesUsed() {
        return cpuCyclesUsed;
    }

    public void setCpuCyclesUsed(int cpuCyclesUsed) {
        this.cpuCyclesUsed = cpuCyclesUsed;
    }

    public List<String> getOpenFiles() {
        return openFiles;
    }

    public void setOpenFiles(List<String> openFiles) {
        this.openFiles = openFiles;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public BCP getNextBCP() {
        return nextBCP;
    }

    public void setNextBCP(BCP nextBCP) {
        this.nextBCP = nextBCP;
    }

    public long getSimulatorStartMillis() {
        return simulatorStartMillis;
    }

    public void setSimulatorStartMillis(long simulatorStartMillis) {
        this.simulatorStartMillis = simulatorStartMillis;
    }
}