/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.Memory;
import com.mycompany.pyso.Classes.Process.BCP;
import java.util.List;
/**
 *
 * @author jimen
 */
public class RAM {
    private String[] memory;
    private int positionUser;

    public static final int KERNEL_SIZE    = 150;
    public static final int KERNEL_HEADER  = 20;  
    private static final int BCP_SLOT_SIZE = 20;

    public RAM(int size) {
        memory = new String[size];
        positionUser = KERNEL_SIZE;
        for (int i = 0; i < size; i++) memory[i] = "";
    }

    public void updateKernelFromBCP(BCP bcp) {
        memory[0] = "=== KERNEL SPACE (0-149) ===";
        if (bcp == null) {
            for (int i = 1; i < KERNEL_HEADER; i++) memory[i] = "";
            return;
        }
        writeBCPSlot(bcp, 1);
    }

    public void updateAllBCPsInKernel(List<BCP> allBcps) {

        for (int i = KERNEL_HEADER; i < KERNEL_SIZE; i++) memory[i] = "";

        int slot = 0;
        for (BCP bcp : allBcps) {
            int base = KERNEL_HEADER + slot * BCP_SLOT_SIZE;
            if (base + BCP_SLOT_SIZE > KERNEL_SIZE) break; 
            memory[base] = "--- BCP PID:" + bcp.getPID() + " [" + bcp.getState().name() + "] ---";
            writeBCPSlot(bcp, base + 1);
            slot++;
        }
    }

    private void writeBCPSlot(BCP bcp, int startPos) {
        int p = startPos;
        if (p >= memory.length) return;
        safeWrite(p++, "PID        : " + bcp.getPID());
        safeWrite(p++, "Nombre     : " + bcp.getProcessName());
        safeWrite(p++, "Estado     : " + bcp.getState().name());
        safeWrite(p++, "PC         : " + bcp.getPC());
        safeWrite(p++, "IR         : " + bcp.getIR());
        safeWrite(p++, "AC         : " + bcp.getAC());
        safeWrite(p++, "AX         : " + bcp.getAX());
        safeWrite(p++, "BX         : " + bcp.getBX());
        safeWrite(p++, "CX         : " + bcp.getCX());
        safeWrite(p++, "DX         : " + bcp.getDX());
        safeWrite(p++, "Base       : " + bcp.getBaseAddress());
        safeWrite(p++, "Limite     : " + bcp.getLimitAddress());
        safeWrite(p++, "Sig. BCP   : " + (bcp.getNextBCP() != null
                ? "Addr[" + bcp.getNextBCP().getBaseAddress() + "]" : "(ninguno)"));
        safeWrite(p++, "Prioridad  : " + bcp.getPriority());
        safeWrite(p++, "T.Llegada  : " + bcp.formatElapsed(bcp.getArrivalMillis()));
        safeWrite(p++, "T.Inicio   : " + bcp.formatElapsed(bcp.getStartMillis()));
        safeWrite(p++, "Ciclos CPU : " + bcp.getCpuCyclesUsed());
        safeWrite(p++, "Pila       : " + buildStackString(bcp));
        safeWrite(p,   "Archivos   : " + buildFilesString(bcp));
    }

    private void safeWrite(int pos, String val) {
        if (pos >= 0 && pos < memory.length) memory[pos] = val;
    }

    private String buildStackString(BCP bcp) {
        if (bcp.getStack() == null || bcp.getStack().isEmpty()) return "[]";
        int[] vals = bcp.getStack().getValues();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vals.length; i++) {
            sb.append(vals[i]);
            if (i < vals.length - 1) sb.append(", ");
        }
        return sb.append("]").toString();
    }

    private String buildFilesString(BCP bcp) {
        if (bcp.getOpenFiles() == null || bcp.getOpenFiles().isEmpty()) return "(ninguno)";
        return String.join(", ", bcp.getOpenFiles());
    }

    public void saveInstruction(String instructionBin) {
        if (positionUser < memory.length) memory[positionUser++] = instructionBin;
    }

    public String getValue(int pos)          { return memory[pos]; }
    public String[] getMemory()              { return memory; }
    public void setMemory(String[] memory)   { this.memory = memory; }
    public int getPositionUser()             { return positionUser; }
    public void setPositionUser(int v)       { this.positionUser = v; }
}