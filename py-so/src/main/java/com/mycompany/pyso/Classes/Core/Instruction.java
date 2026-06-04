/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.Core;

/**
 *
 * @author jimen
 */
public class Instruction {
    public static final int TYPE_LOAD= 1;
    public static final int TYPE_STORE= 2; 
    public static final int TYPE_MOV= 3;
    public static final int TYPE_SUB= 4;
    public static final int TYPE_ADD= 5;
    public static final int TYPE_INC= 6;
    public static final int TYPE_DEC= 7;
    public static final int TYPE_SWAP= 8;
    public static final int TYPE_INT= 9;
    public static final int TYPE_JMP= 10;
    public static final int TYPE_CMP= 11;
    public static final int TYPE_JE= 12;
    public static final int TYPE_JNE= 13;
    public static final int TYPE_PUSH= 14;
    public static final int TYPE_POP= 15;
    public static final int TYPE_PARAM= 16;
    public static final int REG_NONE= 0;//Don't have any param
    public static final int REG_AX= 1;
    public static final int REG_BX= 2;
    public static final int REG_CX= 3;
    public static final int REG_DX= 4;
    private static final String BIN_LOAD= "0001";
    private static final String BIN_STORE= "0010";
    private static final String BIN_MOV= "0011";
    private static final String BIN_SUB= "0100";
    private static final String BIN_ADD= "0101";
    private static final String BIN_INC= "0110";
    private static final String BIN_DEC= "0111";
    private static final String BIN_SWAP= "1000";
    private static final String BIN_INT= "1001";
    private static final String BIN_JMP= "1010";
    private static final String BIN_CMP= "1011";
    private static final String BIN_JE= "1100";
    private static final String BIN_JNE= "1101";
    private static final String BIN_PUSH= "1110";
    private static final String BIN_POP= "1111";
    private static final String BIN_PARAM= "0000";
    private static final String BIN_AX= "0001";
    private static final String BIN_BX= "0010";
    private static final String BIN_CX= "0011";
    private static final String BIN_DX= "0100";
    
    
    private int type; //It could be 1:LOAD, 2:STORE, 3:MOV, 4:SUB, 5:ADD, 6:INC, 7:DEC, 8:SWAP, 9:INT, 10:JMP, 11:CMP, 12:JE, 13:JNE, 14:PUSH, 15:POP, 16:PARAM
    private int register; // It could be 1:AX, 2:BX, 3:CX, 4:DX
    private int register2;// Secondary register operand is used by SWAP, CMP, MOV reg,reg
    private boolean isRegToReg;// True when MOV uses two registers instead of a value
    private int value; // It could be any integer number
    private int[] params;// PARAM values
    private String interruptCode;// INT operand as string: "20H", "10H", "09H", "21H"
    private String instruction;// The exact instruction text as read from the .asm file
    private String instruction_bin;
    final String AX= "0001";
    final String BX= "0010";
    final String CX= "0011";
    final String DX= "0100";
    final String MOV= "0011";
    final String STORE= "0010";
    final String LOAD= "0001";
    final String SUB= "0100";
    final String ADD= "0101";
    
    public Instruction(int type, int register, String instruction, int value){ // Only if its MOV
        this.type=type;
        this.register=register;
        this.value=value;
        this.instruction=instruction;
        generateBin();
    }
    
    public Instruction(int type, int register, String instruction){
        this.type=type;
        this.register=register;
        this.instruction=instruction;
        generateBin();
    }

    public Instruction(int type, int register, int register2, String instruction){//For mov, swap
        this.type = type;
        this.register = register;
        this.register2 = register2;
        this.isRegToReg = true;
        this.instruction = instruction;
        generateBin();
    }

    public Instruction(int type, String instruction){//For INC or DEC without a register
        this.type = type;
        this.register = REG_NONE;
        this.instruction = instruction;
        generateBin();
    }

    public Instruction(int type, int offset, String instruction, boolean isJump) {//For JMP, JE, JNE
        this.type = type;
        this.register = REG_NONE;
        this.value = offset;
        this.instruction = instruction;
        generateBin();
    }

    public Instruction(int type, String interruptCode, String instruction, boolean isInt) { //For INT
        this.type = type;
        this.register = REG_NONE;
        this.interruptCode = interruptCode;
        this.instruction = instruction;
        generateBin();
    }

    public Instruction(int type, int[] params, String instruction) { //For PARAM 3 numeric values on a stack
        this.type = type;
        this.register = REG_NONE;
        this.params = params;
        this.instruction = instruction;
        generateBin();
    }

    private void generateBin() {
        String opcode = switch (type) {
            case TYPE_LOAD  -> BIN_LOAD;
            case TYPE_STORE -> BIN_STORE;
            case TYPE_MOV   -> BIN_MOV;
            case TYPE_SUB   -> BIN_SUB;
            case TYPE_ADD   -> BIN_ADD;
            case TYPE_INC   -> BIN_INC;
            case TYPE_DEC   -> BIN_DEC;
            case TYPE_SWAP  -> BIN_SWAP;
            case TYPE_INT   -> BIN_INT;
            case TYPE_JMP   -> BIN_JMP;
            case TYPE_CMP   -> BIN_CMP;
            case TYPE_JE    -> BIN_JE;
            case TYPE_JNE   -> BIN_JNE;
            case TYPE_PUSH  -> BIN_PUSH;
            case TYPE_POP   -> BIN_POP;
            case TYPE_PARAM -> BIN_PARAM;
            default         -> "0000";
        };

        String reg1 = registerToBin(register);

        String operand;
        if (isRegToReg) {
            operand = registerToBin(register2);
        } else if (type == TYPE_INT) {
            if (interruptCode!=null){
                operand= intTo8Bits(interruptCode.hashCode() & 0xFF);
            }else{
                operand="00000000";
            }
        } else if (type == TYPE_PARAM && params != null) {
            operand = intTo8Bits(params.length > 0 ? params[0] : 0);
        } else {
            operand = intTo8Bits(value);
        }

        setInstruction_bin(opcode + " " + reg1 + " " + operand);
    }

    private String registerToBin(int reg) {
        return switch (reg) {
            case REG_AX -> BIN_AX;
            case REG_BX -> BIN_BX;
            case REG_CX -> BIN_CX;
            case REG_DX -> BIN_DX;
            default     -> "0000";
        };
    }

    public static String intTo8Bits(int num) {
        int sign = (num < 0) ? 1 : 0;
        int value = Math.abs(num);
        String binary = Integer.toBinaryString(value);
        // Ensure 7 bits
        while (binary.length() < 7) {
            binary = "0" + binary;
        }
        return sign + binary;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRegister() {
        return register;
    }

    public void setRegister(int register) {
        this.register = register;
    }

    public int getRegister2() {
        return register2;
    }

    public void setRegister2(int register2) {
        this.register2 = register2;
    }

    public boolean isIsRegToReg() {
        return isRegToReg;
    }

    public void setIsRegToReg(boolean isRegToReg) {
        this.isRegToReg = isRegToReg;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int[] getParams() {
        return params;
    }

    public void setParams(int[] params) {
        this.params = params;
    }

    public String getInterruptCode() {
        return interruptCode;
    }

    public void setInterruptCode(String interruptCode) {
        this.interruptCode = interruptCode;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getInstruction_bin() {
        return instruction_bin;
    }

    public void setInstruction_bin(String instruction_bin) {
        this.instruction_bin = instruction_bin;
    }
}
