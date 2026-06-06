/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.Memory;

/**
 *
 * @author jimen
 */


public class VirtualMemory {
    
    public static class SwapEntry {
        public final int pid;
        public final String processName;
        public final int instructionCount;
        public final int diskAddress;

        public SwapEntry(int pid, String name, int count, int diskAddr) {
            this.pid = pid;
            this.processName = name;
            this.instructionCount = count;
            this.diskAddress = diskAddr;
        }

        @Override
        public String toString() {
            return "PID=" + pid + " | " + processName
                 + " | Size=" + instructionCount
                 + " | Disk@" + diskAddress;
        }
    }
}