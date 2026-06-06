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

    public RoundRobin(int quantum) {
        this.quantum = quantum;
    }

    @Override
    public OSProcess selectNext(List<OSProcess> readyQueue) {
        if (readyQueue.isEmpty()) return null;
        ticksUsed = 0;
        return readyQueue.remove(0);
    }

    @Override
    public void onTick(OSProcess running, List<OSProcess> readyQueue) {
        ticksUsed++;
    }

    @Override
    public boolean shouldPreempt(OSProcess running, List<OSProcess> readyQueue) {
        return ticksUsed >= quantum && !readyQueue.isEmpty();
    }

    @Override
    public String getName() { 
        return "RR (q=" + quantum + ")"; 
    }
    
    public void resetTicks() { 
        ticksUsed = 0; 
    }
    
    public int getTicksUsed() { 
        return ticksUsed; 
    }
}