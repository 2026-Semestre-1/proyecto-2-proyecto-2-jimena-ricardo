/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.Interrupts;

import com.mycompany.pyso.Classes.Core.CPU;
import com.mycompany.pyso.Classes.Core.Instruction;
import com.mycompany.pyso.Classes.Memory.Disk;
import com.mycompany.pyso.Classes.Process.Dispatcher;
import com.mycompany.pyso.Classes.Process.Process;
import com.mycompany.pyso.Classes.Process.ProcessStack;

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

    public InterruptHandler(CPU cpu, Disk disk, ConsoleCallback console) {
        this.cpu= cpu;
        this.disk= disk;
        this.console= console;
    }

    public void handle(Instruction inst, Process process, Dispatcher dispatcher) {
        String code = inst.getInterruptCode();
        if (code == null) return;

        switch (code) {
            case "20H" -> handle_INT20H(process, dispatcher);
            case "10H" -> handle_INT10H(process);
            case "09H" -> handle_INT09H(process);
            case "21H" -> handle_INT21H(process);
            default    -> handleUnknown(code, process);
        }
    }

    private void handle_INT20H(Process process, Dispatcher dispatcher) {
        dispatcher.terminate(process);
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
        if (console == null) return;

        String input = "";//It wasn't implemented
        try {
            int value = Integer.parseInt(input.trim());
            if (value < 0 || value > 255) {
                console.print("Error INT 09H: valor fuera de rango (0-255), se usará 0");
                value = 0;
            }
            cpu.setDX(value);
        } catch (NumberFormatException e) {
            console.print("Error INT 09H: entrada no numérica, se usará 0");
            cpu.setDX(0);
        }

        cpu.setPC(cpu.getPC() + 1);
        process.getBcp().saveFromCPU(cpu, cpu.getIR());
    }


    private void handle_INT21H(Process process) {
        int ah= cpu.getAX(); // AH simulated via AX
        int dx= cpu.getDX(); // DX = file name index
        String fileName = resolveFileName(process, dx);

        switch (ah) {
            case 0x3C -> createFile(process, fileName);
            default   -> {
                if (console != null) {
                    console.print(">> INT 21H: código AH desconocido o no implementado aún: " + Integer.toHexString(ah).toUpperCase() + "H");
                }
            }
        }

        cpu.setPC(cpu.getPC() + 1);
        process.getBcp().saveFromCPU(cpu, cpu.getIR());
    }

    private void createFile(Process process, String fileName) {
        if (disk.exists(fileName)) {
            if (console != null) console.print("INT 21H 3CH: archivo ya existe — " + fileName);
            return;
        }
        disk.save(fileName, new java.util.ArrayList<>());
        if (console != null) console.print("INT 21H 3CH: archivo creado — " + fileName);
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
        java.util.List<String> files = process.getBcp().getOpenFiles();
        if (dx >= 0 && dx < files.size()) return files.get(dx);
        return "file_" + process.getBcp().getPID() + "_" + dx;
    }

    private void handleUnknown(String code, Process process) {
        if (console != null) {
            console.print("[PID " + process.getBcp().getPID()+ "] INT desconocido: " + code);
        }
        cpu.setPC(cpu.getPC() + 1);
    }

    public void setConsole(ConsoleCallback console) { this.console = console; }
    public ConsoleCallback getConsole()             { return console; }
}