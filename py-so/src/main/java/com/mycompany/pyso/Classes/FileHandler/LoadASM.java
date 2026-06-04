/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.FileHandler;
import com.mycompany.pyso.Classes.Core.Instruction;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author jimen
 */
public class LoadASM {

    private List<Instruction> instructions;
    private boolean formatError = false;
    private boolean extensionError = false;

    public LoadASM() {
        this.instructions = new ArrayList<>();
    }

    public void readFile(String path) {
        if (!path.toLowerCase().endsWith(".asm")) {
            setExtensionError(true);
            JOptionPane.showMessageDialog(null, "Error in the file format", "Only allows .asm extension", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            int lineNumber = 1;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    createInstruction(line, lineNumber);
                    lineNumber++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createInstruction(String line, int lineNumber) {
        line = line.trim().toUpperCase().replaceAll("\\s+", " ");

        if (!line.matches("[A-Z0-9,\\s+\\-H]+")) {
            error("Caracteres inválidos en la instrucción", lineNumber);
            return;
        }

        String[] parts = line.split(" ");
        String operation = parts[0];

        int op = switch (operation) {
            case "LOAD"  -> Instruction.TYPE_LOAD;
            case "STORE" -> Instruction.TYPE_STORE;
            case "MOV"   -> Instruction.TYPE_MOV;
            case "SUB"   -> Instruction.TYPE_SUB;
            case "ADD"   -> Instruction.TYPE_ADD;
            case "INC"   -> Instruction.TYPE_INC;
            case "DEC"   -> Instruction.TYPE_DEC;
            case "SWAP"  -> Instruction.TYPE_SWAP;
            case "INT"   -> Instruction.TYPE_INT;
            case "JMP"   -> Instruction.TYPE_JMP;
            case "CMP"   -> Instruction.TYPE_CMP;
            case "JE"    -> Instruction.TYPE_JE;
            case "JNE"   -> Instruction.TYPE_JNE;
            case "PUSH"  -> Instruction.TYPE_PUSH;
            case "POP"   -> Instruction.TYPE_POP;
            case "PARAM" -> Instruction.TYPE_PARAM;
            default      -> -1;
        };

        if (op == -1) {
            error("Operación inválida: " + operation, lineNumber);
            return;
        }


        try {
            switch (op) {
                case Instruction.TYPE_MOV -> {
                    String paramsText = line.substring(4).trim();
                    String[] params = paramsText.split(",");
                    if (params.length != 2) {
                        error("MOV requiere formato: MOV REG, VALOR o MOV REG, REG", lineNumber);
                        return;
                    }
                    int destReg = parseRegister(params[0].trim(), lineNumber);
                    if (destReg == -1) return;

                    String second = params[1].trim();
                    int srcReg = resolveRegister(second);

                    if (srcReg != -1) {
                        // MOV REG, REG
                        instructions.add(new Instruction(op, destReg, srcReg, line));
                    } else {
                        // MOV REG, value
                        int value = parseValue(second, -128, 127, lineNumber);
                        if (value == Integer.MIN_VALUE) return;
                        instructions.add(new Instruction(op, destReg, line, value));
                    }
                }
                case Instruction.TYPE_SWAP, Instruction.TYPE_CMP -> {
                    if (parts.length < 2) { error("Faltan operandos", lineNumber); return; }
                    String operands = line.substring(operation.length()).trim();
                    String[] regs = operands.split(",");
                    if (regs.length != 2) { error(operation + " requiere dos registros", lineNumber); return; }
                    int reg1 = parseRegister(regs[0].trim(), lineNumber);
                    int reg2 = parseRegister(regs[1].trim(), lineNumber);
                    if (reg1 == -1 || reg2 == -1) return;
                    instructions.add(new Instruction(op, reg1, reg2, line));
                }
                case Instruction.TYPE_INC, Instruction.TYPE_DEC -> {
                    if (parts.length == 1) {
                        instructions.add(new Instruction(op, line));
                    } else {
                        int reg = parseRegister(parts[1].trim(), lineNumber);
                        if (reg == -1) return;
                        instructions.add(new Instruction(op, reg, line));
                    }
                }
                case Instruction.TYPE_INT -> {
                    if (parts.length < 2) { error("INT requiere código: 20H, 10H, 09H, 21H", lineNumber); return; }
                    String code = parts[1].trim();
                    if (!code.matches("(20H|10H|09H|21H)")) {
                        error("Código de interrupción inválido: " + code, lineNumber);
                        return;
                    }
                    instructions.add(new Instruction(op, code, line, true));
                }
                case Instruction.TYPE_JMP, Instruction.TYPE_JE, Instruction.TYPE_JNE -> {
                    if (parts.length < 2) { error(operation + " requiere un desplazamiento", lineNumber); return; }
                    int offset = parseValue(parts[1].trim(), -128, 127, lineNumber);
                    if (offset == Integer.MIN_VALUE) return;
                    instructions.add(new Instruction(op, offset, line, true));
                }
                case Instruction.TYPE_PUSH, Instruction.TYPE_POP -> {
                    if (parts.length < 2) { error(operation + " requiere un registro", lineNumber); return; }
                    int reg = parseRegister(parts[1].trim(), lineNumber);
                    if (reg == -1) return;
                    instructions.add(new Instruction(op, reg, line));
                }
                case Instruction.TYPE_PARAM -> {
                    String paramText = line.substring(5).trim();
                    String[] paramParts = paramText.split(",");
                    if (paramParts.length < 1 || paramParts.length > 3) {
                        error("PARAM acepta entre 1 y 3 valores numéricos", lineNumber);
                        return;
                    }
                    int[] values = new int[paramParts.length];
                    for (int i = 0; i < paramParts.length; i++) {
                        int v = parseValue(paramParts[i].trim(), -128, 127, lineNumber);
                        if (v == Integer.MIN_VALUE) return;
                        values[i] = v;
                    }
                    instructions.add(new Instruction(op, values, line));
                }
                default -> {
                    if (parts.length < 2) { error("Falta registro", lineNumber); return; }
                    int reg = parseRegister(parts[1].trim(), lineNumber);
                    if (reg == -1) return;
                    instructions.add(new Instruction(op, reg, line));
                }
            }

        } catch (Exception e) {
            error("Error inesperado en parámetros", lineNumber);
        }
    }

    private int parseRegister(String reg, int lineNumber) {
        int result = resolveRegister(reg);
        if (result == -1) {
            error("Registro inválido: " + reg, lineNumber);
        }
        return result;
    }

    private int resolveRegister(String reg) {
        return switch (reg) {
            case "AX" -> Instruction.REG_AX;
            case "BX" -> Instruction.REG_BX;
            case "CX" -> Instruction.REG_CX;
            case "DX" -> Instruction.REG_DX;
            default   -> -1;
        };
    }

    private int parseValue(String text, int min, int max, int lineNumber) {
        try {
            int value = Integer.parseInt(text);
            if (value < min || value > max) {
                error("Valor fuera de rango (" + min + " a " + max + ")", lineNumber);
                return Integer.MIN_VALUE;
            }
            return value;
        } catch (NumberFormatException e) {
            error("Valor numérico inválido: " + text, lineNumber);
            return Integer.MIN_VALUE;
        }
    }

    private void error(String message, int lineNumber) {
        setFormatError(true);
        JOptionPane.showMessageDialog(
            null,
            message + " en la línea " + lineNumber,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public boolean isFormatError() {
        return formatError;
    }

    public void setFormatError(boolean formatError) {
        this.formatError = formatError;
    }

    public boolean isExtensionError() {
        return extensionError;
    }

    public void setExtensionError(boolean extensionError) {
        this.extensionError = extensionError;
    }
    
    
}
