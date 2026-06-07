package com.mycompany.pyso.Scheduler;

import com.mycompany.pyso.Classes.Process.OSProcess;
import java.util.List;

public class HRRN implements SchedulerStrategy {

    private int currentTick = 0;

    @Override
    public OSProcess selectNext(List<OSProcess> readyQueue) {
        if (readyQueue.isEmpty()) return null;

        int bestIndex = 0;
        double bestRatio = responseRatio(readyQueue.get(0));

        for (int i = 1; i < readyQueue.size(); i++) {
            double ratio = responseRatio(readyQueue.get(i));
            if (ratio > bestRatio) {
                bestRatio = ratio;
                bestIndex = i;
            }
        }

        return readyQueue.remove(bestIndex);
    }

    @Override
    public void onTick(OSProcess running, List<OSProcess> readyQueue) {
        currentTick++;
    }

    @Override
    public boolean shouldPreempt(OSProcess running, List<OSProcess> readyQueue) {
        return false;
    }

    @Override
    public String getName() {
        return "HRRN";
    }

    private double responseRatio(OSProcess p) {
        int remaining = p.getBurstTime() - p.getBcp().getCpuCyclesUsed();
        if (remaining <= 0) return 0;
        int waiting = currentTick - p.getBcp().getCpuCyclesUsed();
        return (double) (waiting + remaining) / remaining;
    }
}
