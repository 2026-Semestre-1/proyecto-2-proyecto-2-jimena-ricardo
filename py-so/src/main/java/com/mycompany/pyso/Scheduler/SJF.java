/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Scheduler;

import com.mycompany.pyso.Classes.Process.OSProcess;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author jimen
 */
public class SJF implements SchedulerStrategy {

    @Override
    public OSProcess selectNext(List<OSProcess> readyQueue) {
        if (readyQueue.isEmpty()) return null;

        OSProcess chosen = readyQueue.stream()
            .min(Comparator
                .comparingInt(OSProcess::getBurstTime)
                .thenComparingLong(p -> p.getBcp().getArrivalMillis()))
            .orElse(null);

        if (chosen != null) readyQueue.remove(chosen);
        return chosen;
    }

    @Override
    public void onTick(OSProcess running, List<OSProcess> readyQueue) {
        // Non-preemptive — nothing to track per tick
    }

    @Override
    public boolean shouldPreempt(OSProcess running, List<OSProcess> readyQueue) {
        return false; // never preempts
    }

    @Override
    public String getName() { return "SJF"; }
}