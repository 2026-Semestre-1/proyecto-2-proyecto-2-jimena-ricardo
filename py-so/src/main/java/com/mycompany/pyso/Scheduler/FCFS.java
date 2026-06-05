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
public class FCFS implements SchedulerStrategy {

    @Override
    public OSProcess selectNext(List<OSProcess> readyQueue) {
        if (readyQueue.isEmpty()) return null;
        return readyQueue.remove(0);
    }

    @Override
    public void onTick(OSProcess running, List<OSProcess> readyQueue) {
        // FCFS does nothing
    }

    @Override
    public boolean shouldPreempt(OSProcess running, List<OSProcess> readyQueue) {
        return false;
    }

    @Override
    public String getName() { 
        return "FCFS"; 
    }
}