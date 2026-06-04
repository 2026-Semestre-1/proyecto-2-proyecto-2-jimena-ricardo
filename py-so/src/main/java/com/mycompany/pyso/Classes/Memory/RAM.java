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
    
    public void saveInstruction(String instructionBin){
        memory[positionUser] = instructionBin;
        positionUser++;
    }

    public int getPositionUser() {
        return positionUser;
    }

    public void setPositionUser(int positionUser) {
        this.positionUser = positionUser;
    }
    
    public void updateKernelFromBCP(BCP bcp) {
        memory[0]  = "KERNEL SPACE";
        if (bcp == null) {
            memory[1]  = "KERNEL SPACE";
            for (int i = 2; i < 20; i++) memory[i] = "";
            return;
        }
        memory[1]  = "PID : " + bcp.getPID();
        memory[2]  = "Nombre : " + bcp.getProcessName();
        memory[3]  = "Estado : " + bcp.getState().name();
        memory[4]  = "PC : " + bcp.getPC();
        memory[5]  = "IR : " + bcp.getIR();
        memory[6]  = "AC : " + bcp.getAC();
        memory[7]  = "AX : " + bcp.getAX();
        memory[8]  = "BX : " + bcp.getBX();
        memory[9]  = "CX : " + bcp.getCX();
        memory[10] = "DX : " + bcp.getDX();
        memory[11] = "Base : " + bcp.getBaseAddress();
        memory[12] = "Limite : " + bcp.getLimitAddress();
        memory[13] = "Sig. BCP : " + (bcp.getNextBCP() != null ? "PID " + bcp.getNextBCP().getPID() : "(ninguno)");
        
        for (int i = 14; i < 20; i++) memory[i] = "KERNEL SPACE";
    }
    
    public String getValue(int pos){
        return memory[pos];
    }
    
    public String[] getMemory() {
        return memory;
    }

    public void setMemory(String[] memory) {
        this.memory = memory;
    }
    
    
}