/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Scheduler;

import com.mycompany.pyso.Classes.Process.OSProcess;
import java.util.List;

/**
 *
 * @author jimen
 */

public class RoundRobin implements SchedulerStrategy {

    private final int quantum;
    private int ticksUsed = 0;
    private OSProcess currentProcess = null;

    public RoundRobin(int quantum) {
        this.quantum = quantum;
    }

    @Override
    public OSProcess selectNext(List<OSProcess> readyQueue) {
        if (readyQueue.isEmpty()) return null;
        ticksUsed = 0; 
        return readyQueue.get(0);
    }

    @Override
    public void onTick(OSProcess running, List<OSProcess> readyQueue) {
        if (running != null) {
            ticksUsed++;
            currentProcess = running;
        }
    }

    @Override
    public boolean shouldPreempt(OSProcess running, List<OSProcess> readyQueue) {
        if (running == null) return false;

        if (readyQueue.isEmpty()) return false;

        return ticksUsed >= quantum;
    }

    @Override
    public String getName() { 
        return "RR (q=" + quantum + ")"; 
    }

    public int getQuantum() { return quantum; }
    public int getTicksUsed() { return ticksUsed; }
    public void resetTicks() { ticksUsed = 0; }
}