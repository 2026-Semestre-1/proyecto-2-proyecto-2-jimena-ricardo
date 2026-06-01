package com.mycompany.proyecto.so.process;

/**
 * Fixed-size stack (depth = 5) per process.
 * SRP: only stack operations.
 */
public class ProcessStack {

    public static final int MAX_DEPTH = 5;

    private final int[] data = new int[MAX_DEPTH];
    private int top = -1;

    public boolean push(int value) {
        if (top >= MAX_DEPTH - 1) return false; // overflow
        data[++top] = value;
        return true;
    }

    public int pop() {
        if (top < 0) throw new IllegalStateException("Stack underflow");
        return data[top--];
    }

    public boolean isEmpty()  { return top < 0; }
    public boolean isFull()   { return top >= MAX_DEPTH - 1; }
    public int     getTop()   { return top; }

    /** Snapshot for display purposes. */
    public int[] snapshot() {
        int[] snap = new int[top + 1];
        System.arraycopy(data, 0, snap, 0, top + 1);
        return snap;
    }
}
