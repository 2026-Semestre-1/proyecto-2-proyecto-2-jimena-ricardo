/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.Process;

import com.mycompany.pyso.Classes.Core.Instruction;
import java.util.List;

/**
 *
 * @author jimen
 */
public class Process {
    private int PID;  
    private String name;   //File name
    private ProcessState state;  
    private int baseAddress;  //RAM Adress
    private int limitAddress; // RAM Limit
    private int diskAddress; 
    private int diskSize;  
    private BCP bcp;   
    private List<Instruction> instructions;
    private long startTime; 
    private long endTime;   
    private int cpuCyclesUsed; 
    private List<String> openFiles; 

    public int getPID() {
        return PID;
    }

    public void setPID(int PID) {
        this.PID = PID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        this.state = state;
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

    public int getDiskAddress() {
        return diskAddress;
    }

    public void setDiskAddress(int diskAddress) {
        this.diskAddress = diskAddress;
    }

    public int getDiskSize() {
        return diskSize;
    }

    public void setDiskSize(int diskSize) {
        this.diskSize = diskSize;
    }

    public BCP getBcp() {
        return bcp;
    }

    public void setBcp(BCP bcp) {
        this.bcp = bcp;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
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
    
    
}
