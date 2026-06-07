/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.Memory;

/**
 *
 * @author jimen
 */

public class MemoryBlock {
    private int startAddress;
    private int size;
    private boolean isFree;
    private Integer processId;
    
    public MemoryBlock(int startAddress, int size) {
        this.startAddress = startAddress;
        this.size = size;
        this.isFree = true;
        this.processId = null;
    }
    
    public int getStartAddress() { return startAddress; }
    public void setStartAddress(int startAddress) { this.startAddress = startAddress; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public boolean isFree() { return isFree; }
    public void setFree(boolean free) { isFree = free; }
    public Integer getProcessId() { return processId; }
    public void setProcessId(Integer processId) { this.processId = processId; }
    
    public int getEndAddress() { return startAddress + size; }
    
    @Override
    public String toString() {
        return String.format("[%d-%d] %s", startAddress, getEndAddress(), 
               isFree ? "LIBRE" : "PID:" + processId);
    }
}
