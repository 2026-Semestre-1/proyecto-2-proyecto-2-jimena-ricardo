/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.Process;

import com.mycompany.pyso.Classes.Core.CPU;
import com.mycompany.pyso.Classes.Core.Instruction;
import java.util.List;

/**
 *
 * @author jimen
 */
public class OSProcess {
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

    // ─────────────────────────────────────────────────────────
    // MÉTODOS NUEVOS QUE NECESITA OperatingSystem
    // ─────────────────────────────────────────────────────────
    
    /**
     * Marca el tiempo de inicio del proceso
     */
    public void markStarted(long simulatorStartMillis) {
        this.startTime = System.currentTimeMillis() - simulatorStartMillis;
        if (bcp != null) {
            bcp.setStartTime(this.startTime);
        }
    }
    
    /**
     * Restaura los registros del BCP a la CPU
     */
    public void restoreIntoCPU(CPU cpu) {
        if (bcp != null && cpu != null) {
            bcp.restoreIntoCPU(cpu);
        }
    }
    
    /**
     * Guarda el estado actual de la CPU en el BCP
     */
    public void saveFromCPU(CPU cpu, String ir) {
        if (bcp != null && cpu != null) {
            bcp.saveFromCPU(cpu, ir);
        }
    }
    
    /**
     * Obtiene el estado del proceso (delegado al BCP o directo)
     */
    public ProcessState getProcessState() {
        if (bcp != null) {
            return bcp.getState();
        }
        return state;
    }
    
    /**
     * Obtiene el PC (Program Counter) del BCP
     */
    public int getProgramCounter() {
        if (bcp != null) {
            return bcp.getProgramCounter();
        }
        return baseAddress;
    }
    
    /**
     * Obtiene el IR (Instruction Register) del BCP
     */
    public String getInstructionRegister() {
        if (bcp != null) {
            return bcp.getInstructionRegister();
        }
        return "";
    }
    
    /**
     * Obtiene el valor del registro AC
     */
    public int getAC() {
        if (bcp != null) {
            return bcp.getAC();
        }
        return 0;
    }
    
    /**
     * Obtiene el valor del registro AX
     */
    public int getAX() {
        if (bcp != null) {
            return bcp.getAX();
        }
        return 0;
    }
    
    /**
     * Obtiene el valor del registro BX
     */
    public int getBX() {
        if (bcp != null) {
            return bcp.getBX();
        }
        return 0;
    }
    
    /**
     * Obtiene el valor del registro CX
     */
    public int getCX() {
        if (bcp != null) {
            return bcp.getCX();
        }
        return 0;
    }
    
    /**
     * Obtiene el valor del registro DX
     */
    public int getDX() {
        if (bcp != null) {
            return bcp.getDX();
        }
        return 0;
    }
    
    /**
     * Verifica si el proceso ya terminó
     */
    public boolean isTerminated() {
        return state == ProcessState.TERMINATED;
    }
    
    /**
     * Marca el tiempo de finalización
     */
    public void markEnded() {
        this.endTime = System.currentTimeMillis();
        if (bcp != null) {
            bcp.setEndTime(this.endTime);
        }
        this.state = ProcessState.TERMINATED;
        if (bcp != null) {
            bcp.setState(ProcessState.TERMINATED);
        }
    }
    
    /**
     * Obtiene el turnaround time (tiempo de estancia)
     */
    public long getTurnaroundTime() {
        if (startTime > 0 && endTime > 0) {
            return endTime - startTime;
        }
        return 0;
    }
    
    /**
     * Obtiene la ráfaga total (cantidad de instrucciones)
     */
    public int getBurstTime() {
        return instructions != null ? instructions.size() : 0;
    }
    
    // ─────────────────────────────────────────────────────────
    // GETTERS Y SETTERS EXISTENTES
    // ─────────────────────────────────────────────────────────

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
        if (bcp != null) {
            bcp.setState(state);
        }
    }

    public int getBaseAddress() {
        return baseAddress;
    }

    public void setBaseAddress(int baseAddress) {
        this.baseAddress = baseAddress;
        if (bcp != null) {
            bcp.setBaseAddress(baseAddress);
        }
    }

    public int getLimitAddress() {
        return limitAddress;
    }

    public void setLimitAddress(int limitAddress) {
        this.limitAddress = limitAddress;
        if (bcp != null) {
            bcp.setLimit(limitAddress); 
        }
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
