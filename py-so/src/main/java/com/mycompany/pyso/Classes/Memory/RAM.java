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

    /**
     * Escribe TODOS los atributos del BCP en el espacio del kernel (posiciones 0-19).
     * Si bcp es null, limpia el kernel.
     *
     * CORRECCIÓN: nextBCP ahora muestra la dirección BASE en memoria del siguiente
     * proceso, no su PID. Esto cumple con el enunciado que pide un enlace a la
     * dirección de memoria donde está almacenado el siguiente BCP.
     *
     * CORRECCIÓN: Se agregan los atributos faltantes (prioridad, tiempos, ciclos CPU,
     * pila y archivos abiertos) en las posiciones 14-19.
     */
    public void updateKernelFromBCP(BCP bcp) {
        memory[0] = "[ KERNEL SPACE ]";

        if (bcp == null) {
            memory[1] = "KERNEL SPACE";
            for (int i = 2; i < 20; i++) memory[i] = "";
            return;
        }

        // --- Identificación ---
        memory[1]  = "PID        : " + bcp.getPID();
        memory[2]  = "Nombre     : " + bcp.getProcessName();
        memory[3]  = "Estado     : " + bcp.getState().name();

        // --- Registros de CPU ---
        memory[4]  = "PC         : " + bcp.getPC();
        memory[5]  = "IR         : " + bcp.getIR();
        memory[6]  = "AC         : " + bcp.getAC();
        memory[7]  = "AX         : " + bcp.getAX();
        memory[8]  = "BX         : " + bcp.getBX();
        memory[9]  = "CX         : " + bcp.getCX();
        memory[10] = "DX         : " + bcp.getDX();

        // --- Información de memoria ---
        memory[11] = "Base       : " + bcp.getBaseAddress();
        memory[12] = "Limite     : " + bcp.getLimitAddress();

        // CORRECCIÓN: apunta a la DIRECCIÓN DE MEMORIA del siguiente BCP, no al PID
        memory[13] = "Sig. BCP   : " + (bcp.getNextBCP() != null
                ? "Addr[" + bcp.getNextBCP().getBaseAddress() + "]"
                : "(ninguno)");

        // --- Atributos adicionales del BCP (antes faltaban) ---
        memory[14] = "Prioridad  : " + bcp.getPriority();
        memory[15] = "T.Llegada  : " + bcp.formatElapsed(bcp.getArrivalMillis());
        memory[16] = "T.Inicio   : " + bcp.formatElapsed(bcp.getStartMillis());
        memory[17] = "Ciclos CPU : " + bcp.getCpuCyclesUsed();
        memory[18] = "Pila       : " + buildStackString(bcp);
        memory[19] = "Archivos   : " + buildFilesString(bcp);
    }

    /** Convierte el contenido visible de la pila a un string legible. */
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

    /** Convierte la lista de archivos abiertos a string. */
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