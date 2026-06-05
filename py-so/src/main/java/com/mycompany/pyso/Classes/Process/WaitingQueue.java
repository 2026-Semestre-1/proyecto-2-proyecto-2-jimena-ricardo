/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.Process;
import java.util.LinkedList;
import java.util.List;
/**
 *
 * @author jimen
 */

public class WaitingQueue {
    private final List<OSProcess> queue;

    public WaitingQueue() {
        this.queue = new LinkedList<>();
    }
    public void enqueue(OSProcess process) {
        process.getBcp().setState(ProcessState.WAITING);
        queue.add(process);
    }
    public OSProcess release(int PID) {
        OSProcess found = queue.stream()
                .filter(p -> p.getBcp().getPID() == PID)
                .findFirst()
                .orElse(null);
        if (found != null) {
            queue.remove(found);
        }
        return found;
    }
    public boolean isEmpty(){
        return queue.isEmpty(); 
    }
    public int count(){
        return queue.size(); 
    }
    public List<OSProcess> getAll(){
        return queue; 
    }
}