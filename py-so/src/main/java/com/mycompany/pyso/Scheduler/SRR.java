package com.mycompany.pyso.Scheduler;

import com.mycompany.pyso.Classes.Process.OSProcess;
import com.mycompany.pyso.Classes.Process.ReadyQueue;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Selfish Round Robin (SRR)
 * Processes that have already been served get priority over new ones.
 * A "selfish" queue holds previously-served processes; new arrivals
 * go to the standard ready queue. Selection always picks from the
 * selfish queue first.
 */
public class SRR implements SchedulerStrategy {

    private final int quantum;
    private final LinkedList<OSProcess> selfishQueue = new LinkedList<>();
    private final ConcurrentHashMap<Integer, Integer> ticksMap = new ConcurrentHashMap<>();

    public SRR(int quantum) {
        this.quantum = quantum;
    }

    @Override
    public OSProcess selectNext(List<OSProcess> readyQueue) {
        // 1. Prefer the selfish queue (previously-served processes)
        if (!selfishQueue.isEmpty()) {
            OSProcess next = selfishQueue.removeFirst();
            ticksMap.put(next.getPID(), 0);
            return next;
        }
        // 2. Fall back to the standard ready queue
        if (!readyQueue.isEmpty()) {
            OSProcess next = readyQueue.remove(0);
            if (next != null) {
                ticksMap.put(next.getPID(), 0);
            }
            return next;
        }
        return null;
    }

    @Override
    public void onTick(OSProcess running, List<OSProcess> readyQueue) {
        if (running == null) return;
        ticksMap.merge(running.getPID(), 1, Integer::sum);
    }

    @Override
    public boolean shouldPreempt(OSProcess running, List<OSProcess> readyQueue) {
        if (running == null) return false;
        // Preempt if quantum exhausted AND someone is waiting (selfish or standard)
        if (selfishQueue.isEmpty() && readyQueue.isEmpty()) return false;
        int used = ticksMap.getOrDefault(running.getPID(), 0);
        return used >= quantum;
    }

    @Override
    public void requeue(OSProcess p, ReadyQueue readyQueue) {
        selfishQueue.add(p);
    }

    public void resetTicks() {
        ticksMap.clear();
    }

    @Override
    public String getName() { return "SRR (q=" + quantum + ")"; }

    public int getQuantum()          { return quantum; }
    public int getTicksUsed(int pid) { return ticksMap.getOrDefault(pid, 0); }
}
