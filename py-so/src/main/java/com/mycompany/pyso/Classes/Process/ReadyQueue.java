/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.Process;
import java.util.LinkedList;
import java.util.Queue;
/**
 *
 * @author jimen
 */

public class ReadyQueue {
    private final Queue<Process> queue;

    public ReadyQueue() {
        this.queue = new LinkedList<>();
    }

    public void enqueue(Process process) {
        process.getBcp().setState(ProcessState.READY);
        queue.add(process);
    }
    
    public Process dequeue() {
        return queue.poll();
    }

    public boolean hasNext(){
        return !queue.isEmpty(); 
    }
    public int count(){
        return queue.size();
    }
    public Queue<Process> getQueue(){
        return queue; 
    }
}