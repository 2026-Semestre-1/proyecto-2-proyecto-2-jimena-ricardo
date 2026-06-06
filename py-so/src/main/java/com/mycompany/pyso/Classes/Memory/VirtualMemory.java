/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.Memory;

/**
 *
 * @author jimen
 */

import com.mycompany.pyso.Classes.Process.OSProcess;
import java.util.ArrayList;
import java.util.List;

public class VirtualMemory {

    public static class SwapEntry {
        public final int    pid;
        public final String processName;
        public final int    instructionCount;
        public final int    diskAddress;

        public SwapEntry(int pid, String name, int count, int diskAddr) {
            this.pid              = pid;
            this.processName      = name;
            this.instructionCount = count;
            this.diskAddress      = diskAddr;
        }

        @Override
        public String toString() {
            return "PID=" + pid + " | " + processName
                 + " | Size=" + instructionCount
                 + " | Disk@" + diskAddress;
        }
    }

    private final List<SwapEntry> swapList = new ArrayList<>();

    public void swapOut(OSProcess process) {
        swapList.removeIf(e -> e.pid == process.getPID());
        swapList.add(new SwapEntry(
            process.getPID(),
            process.getName(),
            process.getInstructions() != null ? process.getInstructions().size() : 0,
            process.getDiskAddress()
        ));
    }

    public SwapEntry swapIn(int pid) {
        SwapEntry found = swapList.stream()
            .filter(e -> e.pid == pid)
            .findFirst().orElse(null);
        if (found != null) swapList.remove(found);
        return found;
    }

    public SwapEntry swapInNext() {
        if (swapList.isEmpty()) return null;
        return swapList.remove(0);
    }

    public boolean isInSwap(int pid) {
        return swapList.stream().anyMatch(e -> e.pid == pid);
    }

    public List<SwapEntry> getSwapList() { return swapList; }
    public boolean isEmpty()             { return swapList.isEmpty(); }
    public int size()                    { return swapList.size(); }
}