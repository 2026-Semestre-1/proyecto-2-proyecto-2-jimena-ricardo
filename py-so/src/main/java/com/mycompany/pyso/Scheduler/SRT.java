package com.mycompany.pyso.Scheduler;

import com.mycompany.pyso.Classes.Process.OSProcess;
import java.util.List;


public class SRT implements SchedulerStrategy {

    @Override
    public OSProcess selectNext(List<OSProcess> readyQueue) {
        if (readyQueue.isEmpty()) return null;

        int bestIndex = 0;
        int bestRemaining = remaining(readyQueue.get(0));

        for (int i = 1; i < readyQueue.size(); i++) {
            int r = remaining(readyQueue.get(i));
            if (r < bestRemaining) {
                bestRemaining = r;
                bestIndex = i;
            }
        }

        return readyQueue.remove(bestIndex);
    }

    @Override
    public void onTick(OSProcess running, List<OSProcess> readyQueue) {
        // SRT does nothing on tick because the selection is done based in other things.
    }

    @Override
    public boolean shouldPreempt(OSProcess running, List<OSProcess> readyQueue) {
        if (readyQueue.isEmpty()) return false;

        int runningRemaining = remaining(running);

        for (OSProcess p : readyQueue) {
            if (remaining(p) < runningRemaining) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return "SRT";
    }

    private int remaining(OSProcess p) {
        return p.getBurstTime() - p.getBcp().getCpuCyclesUsed();
    }
}