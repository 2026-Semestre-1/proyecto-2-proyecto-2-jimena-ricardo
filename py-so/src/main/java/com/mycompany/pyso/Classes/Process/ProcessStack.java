/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.Process;

/**
 *
 * @author jimen
 */

public class ProcessStack {

    private static final int STACK_SIZE = 5; // Max depth required by the project spec
    private int[] stack;
    private int stackPointer;
    private int ownerPID;

    public ProcessStack(int ownerPID) {
        this.ownerPID = ownerPID;
        this.stack = new int[STACK_SIZE];
        this.stackPointer = -1;
    }

    public boolean push(int value) {
        if (isOverflow()) {
            return false;
        }
        stack[++stackPointer] = value;
        return true;
    }

    public int pop() {
        return stack[stackPointer--];
    }

    public int peek() {
        if (isEmpty()) {
            throw new RuntimeException("Stack is empty in process PID=" + ownerPID);
        }
        return stack[stackPointer];
    }

    public int[] getValues() {
        if (stackPointer < 0) return new int[0];
        int[] result = new int[stackPointer + 1];
        System.arraycopy(stack, 0, result, 0, stackPointer + 1);
        return result;
    }

    public boolean isOverflow() {
        return stackPointer >= STACK_SIZE - 1;
    }

    public boolean isEmpty() {
        return stackPointer < 0;
    }

    public int size() {
        return stackPointer + 1;
    }

    public void clear() {
        stackPointer = -1;
    }

    public int getOwnerPID() {
        return ownerPID;
    }
}