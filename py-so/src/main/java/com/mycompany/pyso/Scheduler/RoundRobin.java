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

    private final java.util.concurrent.ConcurrentHashMap<Integer, Integer> ticksMap =
        new java.util.concurrent.ConcurrentHashMap<>();

    public RoundRobin(int quantum) {
        this.quantum = quantum;
    }

    @Override
    public OSProcess selectNext(List<OSProcess> readyQueue) {
        if (readyQueue.isEmpty()) return null;
        OSProcess next = readyQueue.remove(0);
        if (next != null) ticksMap.put(next.getPID(), 0);
        return next;
    }

    @Override
    public void onTick(OSProcess running, List<OSProcess> readyQueue) {
        if (running == null) return;
        ticksMap.merge(running.getPID(), 1, Integer::sum);
    }

    @Override
    public boolean shouldPreempt(OSProcess running, List<OSProcess> readyQueue) {
        if (running == null || readyQueue.isEmpty()) return false;
        int used = ticksMap.getOrDefault(running.getPID(), 0);
        return used >= quantum;
    }

    public void resetTicks() {
        ticksMap.clear();
    }

    public void resetTicks(int pid) {
        ticksMap.put(pid, 0);
    }

    @Override
    public String getName() { return "RR (q=" + quantum + ")"; }

    public int getQuantum()          { return quantum; }
    public int getTicksUsed(int pid) { return ticksMap.getOrDefault(pid, 0); }
}