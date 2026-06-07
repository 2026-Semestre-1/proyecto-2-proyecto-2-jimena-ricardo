package com.mycompany.pyso.Scheduler;

import com.mycompany.pyso.Classes.Process.OSProcess;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Lottery implements SchedulerStrategy {

    private static final int TICKETS_PER_PROCESS = 10;

    private final int quantum;
    private int ticksUsed = 0;

    public Lottery(int quantum) {
        this.quantum = quantum;
    }

    @Override
    public OSProcess selectNext(List<OSProcess> readyQueue) {
        if (readyQueue.isEmpty()) return null;

        int totalTickets = readyQueue.size() * TICKETS_PER_PROCESS;
        int winner = ThreadLocalRandom.current().nextInt(totalTickets);
        System.out.println(winner);

        int cumulative = 0;
        for (int i = 0; i < readyQueue.size(); i++) {
            cumulative += TICKETS_PER_PROCESS;
            if (cumulative > winner) {
                ticksUsed = 0;
                System.out.println("Selected i: "+ i);

                return readyQueue.remove(i);
            }
        }

        // We will never reach here, but just in case.
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
        return "Lottery (q=" + quantum + ")";
    }
}
