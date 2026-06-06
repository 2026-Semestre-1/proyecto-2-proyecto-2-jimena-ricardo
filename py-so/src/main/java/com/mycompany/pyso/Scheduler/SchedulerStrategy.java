/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.pyso.Scheduler;

import com.mycompany.pyso.Classes.Process.OSProcess;
import java.util.List;

public interface SchedulerStrategy {

    OSProcess selectNext(List<OSProcess> readyQueue);
    void onTick(OSProcess running, List<OSProcess> readyQueue);
    boolean shouldPreempt(OSProcess running, List<OSProcess> readyQueue);
    String getName();
}