/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.Process;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author jimen
 */

public class JobQueue {
    private final List<OSProcess> jobs;

    public JobQueue() {
        this.jobs = new ArrayList<>();
    }
    public void add(OSProcess process) {
        jobs.add(process);
    }
    public void remove(OSProcess process) {
        jobs.remove(process);
    }

    public OSProcess getByPID(int PID) {
        return jobs.stream()
                .filter(p -> p.getBcp().getPID() == PID)
                .findFirst()
                .orElse(null);
    }

    public List<OSProcess> getAll(){
        return jobs; 
    }
    public int count(){
        return jobs.size(); 
    }
    public boolean isEmpty(){
        return jobs.isEmpty(); 
    }
}