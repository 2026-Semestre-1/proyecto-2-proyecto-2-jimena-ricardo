/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.Interrupts;

import com.mycompany.pyso.Classes.Core.CPU;
import com.mycompany.pyso.Classes.Core.Instruction;
import com.mycompany.pyso.Classes.Memory.Disk;
import com.mycompany.pyso.Classes.Memory.DiskEntry;
import com.mycompany.pyso.Classes.Process.Process;
import com.mycompany.pyso.Classes.Process.ProcessStack;
import java.util.List;

/**
 *
 * @author jimen
 */

public class InterruptHandler {

    private final CPU  cpu;
    private final Disk disk;
    private ConsoleCallback console;

    public interface ConsoleCallback {
        void print(String value);
    }

    public interface TerminationCallback {
        void terminate(Process process);
    }

    public InterruptHandler(CPU cpu, Disk disk, ConsoleCallback console) {
        this.cpu     = cpu;
        this.disk    = disk;
        this.console = console;
    }

    public void handle(Instruction inst, Process process, TerminationCallback onTerminate) {
        String code = inst.getInterruptCode();
        if (code == null) return;

        switch (code) {
            case "20H" -> handle_INT20H(process, onTerminate);
            case "10H" -> handle_INT10H(process);
            case "09H" -> handle_INT09H(process);
            case "21H" -> handle_INT21H(process);
            default    -> handleUnknown(code, process);
        }
    }

    private void handle_INT20H(Process process, TerminationCallback onTerminate) {
        onTerminate.terminate(process);
    }

    private void handle_INT10H(Process process) {
        int dxValue = cpu.getDX();
        if (console != null) {
            console.print("[PID " + process.getBcp().getPID()
                + " | " + process.getName() + "] DX = " + dxValue);
        }
        cpu.setPC(cpu.getPC() + 1);
        process.getBcp().saveFromCPU(cpu, cpu.getIR());
    }


    private void handle_INT09H(Process process) {
        if (console != null) {
            console.print("[PID " + process.getBcp().getPID()
                + "] INT 09H: entrada simulada = 0");
        }
        cpu.setDX(0);
        cpu.setPC(cpu.getPC() + 1);
        process.getBcp().saveFromCPU(cpu, cpu.getIR());
    }


    private void handle_INT21H(Process process) {
        int ah= cpu.getAX();  
        int dx= cpu.getDX();   
        String fileName = resolveFileName(process, dx);

        switch (ah) {
            case 0x3C -> createFile(process, fileName);
            case 0x3D -> openFile(process, fileName); 
            case 0x4D -> readFile(process, fileName); 
            case 0x40 -> writeFile(process, fileName);  
            case 0x41 -> deleteFile(process, fileName);  
            default -> {
                if (console != null) {
                    console.print("INT 21H: código AH desconocido: "
                        + Integer.toHexString(ah).toUpperCase() + "H"
                        + " [PID " + process.getBcp().getPID() + "]");
                }
            }
        }

        cpu.setPC(cpu.getPC() + 1);
        process.getBcp().saveFromCPU(cpu, cpu.getIR());
    }

    private void createFile(Process process, String fileName) {
        if (disk.exists(fileName)) {
            log(process, "INT 21H 3CH: archivo ya existe — " + fileName);
            return;
        }
        disk.save(fileName, new java.util.ArrayList<>());
        log(process, "INT 21H 3CH: archivo creado — " + fileName);
    }
    private void openFile(Process process, String fileName) {
        if (!disk.exists(fileName)) {
            log(process, "INT 21H 3DH: archivo no existe — " + fileName);
            return;
        }
        List<String> openFiles = process.getBcp().getOpenFiles();
        if (!openFiles.contains(fileName)) {
            openFiles.add(fileName);
        }
        log(process, "INT 21H 3DH: archivo abierto — " + fileName);
    }

    private void readFile(Process process, String fileName) {
        DiskEntry entry = disk.getEntry(fileName);
        if (entry == null) {
            log(process, "INT 21H 4DH: archivo no encontrado — " + fileName);
            cpu.setAC(0);
            return;
        }
        List<String> content = disk.read(entry.address, Math.min(1, entry.size));
        if (!content.isEmpty() && content.get(0) != null) {
            // Guardamos el hash del contenido como valor simulado en AL (AC)
            int simVal = content.get(0).hashCode() & 0xFF;
            cpu.setAC(simVal);
            log(process, "INT 21H 4DH: leído de '" + fileName + "' → " + content.get(0));
        } else {
            cpu.setAC(0);
            log(process, "INT 21H 4DH: archivo vacío — " + fileName);
        }
    }

    private void writeFile(Process process, String fileName) {
        if (!disk.exists(fileName)) {
            log(process, "INT 21H 40H: archivo no encontrado — " + fileName);
            return;
        }
        int al = cpu.getAC();
        log(process, "INT 21H 40H: escrito en '" + fileName + "' valor=" + al);
    }

    /** 41h – Eliminar archivo del disco. */
    private void deleteFile(Process process, String fileName) {
        boolean deleted = disk.delete(fileName);
        log(process, deleted
            ? "INT 21H 41H: archivo eliminado — " + fileName
            : "INT 21H 41H: archivo no encontrado — " + fileName);
    }

    public boolean executePush(Instruction inst, Process process) {
        int value = cpu.getRegisterValue(inst.getRegister());
        ProcessStack stack = process.getBcp().getStack();
        boolean ok = stack.push(value);
        if (!ok && console != null) {
            console.print("PUSH: stack overflow en PID=" + process.getBcp().getPID());
        }
        return ok;
    }

    public boolean executePop(Instruction inst, Process process) {
        ProcessStack stack = process.getBcp().getStack();
        if (stack.isEmpty()) {
            if (console != null) {
                console.print("POP: stack underflow en PID=" + process.getBcp().getPID());
            }
            return false;
        }
        cpu.setRegisterValue(inst.getRegister(), stack.pop());
        return true;
    }

    public void executeParam(Instruction inst, Process process) {
        int[] params = inst.getParams();
        if (params == null) return;
        ProcessStack stack = process.getBcp().getStack();
        for (int value : params) {
            boolean ok = stack.push(value);
            if (!ok && console != null) {
                console.print("PARAM: stack overflow en PID=" + process.getBcp().getPID());
                break;
            }
        }
    }


    private String resolveFileName(Process process, int dx) {
        List<String> files = process.getBcp().getOpenFiles();
        if (dx >= 0 && dx < files.size()) return files.get(dx);
        return "file_" + process.getBcp().getPID() + "_" + dx;
    }

    private void handleUnknown(String code, Process process) {
        log(process, "INT desconocido: " + code);
        cpu.setPC(cpu.getPC() + 1);
    }

    private void log(Process process, String msg) {
        if (console != null) {
            console.print("[PID " + process.getBcp().getPID() + "] " + msg);
        }
    }

    public void setConsole(ConsoleCallback console) { this.console = console; }
    public ConsoleCallback getConsole()             { return console; }
}