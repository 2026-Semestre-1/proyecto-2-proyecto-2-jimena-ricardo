/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.Core;

import com.mycompany.pyso.Classes.Process.OSProcess;

/**
 *
 * @author jimen
 */
public class CPU {
    private int id;
    private OSProcess currentProcess;
    private int AC;
    private int PC;
    private String IR;
    private int AX;
    private int BX;
    private int CX;
    private int DX;
    private boolean overflowFlag;
    private boolean equalFlag;
    private int lastExecutedPC = -1;
    
    private int processesExecuted = 0;
    public static final int MAX_PROCESSES = 1000;

    public CPU() {
        this.AC = 0;
        this.PC = 20;
        this.IR = "";
        this.AX = 0;
        this.BX = 0;
        this.CX = 0;
        this.DX = 0;
        this.overflowFlag = false;
        this.equalFlag = false;
        this.processesExecuted = 0;
    }

    public void execute(Instruction inst) {
        overflowFlag = false;
        int type = inst.getType();
        int reg  = inst.getRegister();

        switch (type) {
            case Instruction.TYPE_LOAD  -> execute_load(reg);
            case Instruction.TYPE_STORE -> execute_store(reg);
            case Instruction.TYPE_MOV   -> {
                if (inst.isIsRegToReg()) {
                    execute_mov_reg(reg, inst.getRegister2());
                } else {
                    execute_mov(reg, inst.getValue());
                }
            }
            case Instruction.TYPE_SUB   -> execute_sub(reg);
            case Instruction.TYPE_ADD   -> execute_add(reg);
            case Instruction.TYPE_INC   -> execute_inc(reg); 
            case Instruction.TYPE_DEC   -> execute_dec(reg);
            case Instruction.TYPE_SWAP  -> execute_swap(reg, inst.getRegister2());
            case Instruction.TYPE_JMP   -> execute_jmp(inst.getValue());
            case Instruction.TYPE_CMP   -> execute_cmp(reg, inst.getRegister2());
            case Instruction.TYPE_JE    -> execute_je(inst.getValue());
            case Instruction.TYPE_JNE   -> execute_jne(inst.getValue());
        }
    }

    private void execute_mov(int register, int value) {
        switch (register) {
            case Instruction.REG_AX -> setAX(value);
            case Instruction.REG_BX -> setBX(value);
            case Instruction.REG_CX -> setCX(value);
            case Instruction.REG_DX -> setDX(value);
        }
    }

    private void execute_load(int register) {
        switch (register) {
            case Instruction.REG_AX -> setAC(AX);
            case Instruction.REG_BX -> setAC(BX);
            case Instruction.REG_CX -> setAC(CX);
            case Instruction.REG_DX -> setAC(DX);
        }
    }

    private void execute_store(int register) {
        int ACvalue = getAC();
        switch (register) {
            case Instruction.REG_AX -> setAX(ACvalue);
            case Instruction.REG_BX -> setBX(ACvalue);
            case Instruction.REG_CX -> setCX(ACvalue);
            case Instruction.REG_DX -> setDX(ACvalue);
        }
    }

    private void execute_add(int register) {
        int ACvalue  = getAC();
        int regValue = getRegisterValue(register);
        int result   = ACvalue + regValue;
        overflowFlag = (ACvalue > 0 && regValue > 0 && result < 0) ||
                       (ACvalue < 0 && regValue < 0 && result > 0);
        setAC((byte) result);
    }

    private void execute_sub(int register) {
        int ACvalue  = getAC();
        int regValue = getRegisterValue(register);
        int result   = ACvalue - regValue;
        overflowFlag = (ACvalue > 0 && regValue < 0 && result < 0) ||
                       (ACvalue < 0 && regValue > 0 && result > 0);
        setAC((byte) result);
    }

    private void execute_mov_reg(int destReg, int srcReg) {
        execute_mov(destReg, getRegisterValue(srcReg));
    }

    private void execute_inc(int register) {
        if (register == Instruction.REG_NONE) {
            setAC(AC + 1);
        } else {
            setRegisterValue(register, getRegisterValue(register) + 1);
        }
    }

    private void execute_dec(int register) {
        if (register == Instruction.REG_NONE) {
            setAC(AC - 1);
        } else {
            setRegisterValue(register, getRegisterValue(register) - 1);
        }
    }

    private void execute_swap(int reg1, int reg2) {
        int temp = getRegisterValue(reg1);
        setRegisterValue(reg1, getRegisterValue(reg2));
        setRegisterValue(reg2, temp);
    }

    private void execute_jmp(int offset) {
        setPC(PC - 1 + offset);
    }

    private void execute_cmp(int reg1, int reg2) {
        equalFlag = (getRegisterValue(reg1) == getRegisterValue(reg2));
    }

    private void execute_je(int offset) {
        if (equalFlag) {
            setPC(PC - 1 + offset);
        }
    }

    private void execute_jne(int offset) {
        if (!equalFlag) {
            setPC(PC - 1 + offset);
        }
    }
    
    public int getRegisterValue(int register) {
        return switch (register) {
            case Instruction.REG_AX -> AX;
            case Instruction.REG_BX -> BX;
            case Instruction.REG_CX -> CX;
            case Instruction.REG_DX -> DX;
            default -> 0;
        };
    }
    
    public void setRegisterValue(int register, int value) {
        switch (register) {
            case Instruction.REG_AX -> setAX(value);
            case Instruction.REG_BX -> setBX(value);
            case Instruction.REG_CX -> setCX(value);
            case Instruction.REG_DX -> setDX(value);
        }
    }

    public int getAC() {
        return AC;
    }

    public void setAC(int AC) {
        this.AC = AC;
    }

    public int getPC() {
        return PC;
    }

    public void setPC(int PC) {
        this.PC = PC;
    }

    public String getIR() {
        return IR;
    }

    public void setIR(String IR) {
        this.IR = IR;
    }

    public int getAX() {
        return AX;
    }

    public void setAX(int AX) {
        this.AX = AX;
    }

    public int getBX() {
        return BX;
    }

    public void setBX(int BX) {
        this.BX = BX;
    }

    public int getCX() {
        return CX;
    }

    public void setCX(int CX) {
        this.CX = CX;
    }

    public int getDX() {
        return DX;
    }

    public void setDX(int DX) {
        this.DX = DX;
    }

    public boolean isOverflowFlag() {
        return overflowFlag;
    }

    public void setOverflowFlag(boolean overflowFlag) {
        this.overflowFlag = overflowFlag;
    }

    public boolean isEqualFlag() {
        return equalFlag;
    }

    public void setEqualFlag(boolean equalFlag) {
        this.equalFlag = equalFlag;
    }
    
    public int getId()                        { return id; }
    public void setId(int id)                 { this.id = id; }
    
    public OSProcess getCurrentProcess()      { return currentProcess; }
    public void setCurrentProcess(OSProcess p) { 
        this.currentProcess = p;
        this.lastExecutedPC = -1;
        if (p != null && p.getPID() >= 0) {
            processesExecuted++;
        }
    }

    public int getLastExecutedPC()             { return lastExecutedPC; }
    public void setLastExecutedPC(int pc)      { this.lastExecutedPC = pc; }
    
    public int getProcessesExecuted()         { return processesExecuted; }
    public void resetProcessesExecuted()      { this.processesExecuted = 0; }
}