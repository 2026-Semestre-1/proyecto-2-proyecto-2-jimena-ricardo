/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.Memory;
import com.mycompany.pyso.Classes.Process.BCP;
/**
 *
 * @author jimen
 */

public class RAM {
    private String[] memory;
    private int positionUser;

    public RAM(int size) {
        memory = new String[size];
        positionUser = 20;
        for (int i = 0; i < size; i++) {
            memory[i] = "";
        }
    }

    public void saveInstruction(String instructionBin) {
        memory[positionUser] = instructionBin;
        positionUser++;
    }

    public int getPositionUser() {
        return positionUser;
    }

    public void setPositionUser(int positionUser) {
        this.positionUser = positionUser;
    }

    public static final int KERNEL_SIZE = 150;

    public void updateKernelFromBCP(BCP bcp) {
        memory[0] = "=== KERNEL SPACE (0-149) ===";

        if (bcp == null) {
            for (int i = 1; i < KERNEL_SIZE; i++) memory[i] = "";
            return;
        }

        memory[1]  = "PID        : " + bcp.getPID();
        memory[2]  = "Nombre     : " + bcp.getProcessName();
        memory[3]  = "Estado     : " + bcp.getState().name();
        memory[4]  = "PC         : " + bcp.getPC();
        memory[5]  = "IR         : " + bcp.getIR();
        memory[6]  = "AC         : " + bcp.getAC();
        memory[7]  = "AX         : " + bcp.getAX();
        memory[8]  = "BX         : " + bcp.getBX();
        memory[9]  = "CX         : " + bcp.getCX();
        memory[10] = "DX         : " + bcp.getDX();
        memory[11] = "Base       : " + bcp.getBaseAddress();
        memory[12] = "Limite     : " + bcp.getLimitAddress();
        memory[13] = "Sig. BCP   : " + (bcp.getNextBCP() != null
                ? "Addr[" + bcp.getNextBCP().getBaseAddress() + "]"
                : "(ninguno)");
        memory[14] = "Prioridad  : " + bcp.getPriority();
        memory[15] = "T.Llegada  : " + bcp.formatElapsed(bcp.getArrivalMillis());
        memory[16] = "T.Inicio   : " + bcp.formatElapsed(bcp.getStartMillis());
        memory[17] = "Ciclos CPU : " + bcp.getCpuCyclesUsed();
        memory[18] = "Pila       : " + buildStackString(bcp);
        memory[19] = "Archivos   : " + buildFilesString(bcp);

        for (int i = 20; i < KERNEL_SIZE; i++) memory[i] = "";
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

    public String getValue(int pos) {
        return memory[pos];
    }

    public String[] getMemory() {
        return memory;
    }

    public void setMemory(String[] memory) {
        this.memory = memory;
    }
}