package com.mycompany.proyecto.so.process;

import com.mycompany.proyecto.so.core.cpu.Instruction;
import java.util.List;

/**
 * Represents a loaded program instance.
 * Holds identity + its instruction list + its BCP.
 */
public class Process {

    private final int             pid;
    private final String          name;
    private final List<Instruction> instructions;
    private final BCP             bcp;

    // Secondary storage location
    private int diskAddress = -1;

    public Process(int pid, String name, List<Instruction> instructions) {
        this.pid          = pid;
        this.name         = name;
        this.instructions = instructions;
        this.bcp          = new BCP(pid, name, instructions.size());
    }

    public int                 getPid()           { return pid; }
    public String              getName()          { return name; }
    public List<Instruction>   getInstructions()  { return instructions; }
    public BCP                 getBcp()           { return bcp; }
    public int                 getDiskAddress()   { return diskAddress; }
    public void                setDiskAddress(int v) { this.diskAddress = v; }

    /** Convenience: total instruction count = burst size. */
    public int getBurstSize() { return instructions.size(); }
}
