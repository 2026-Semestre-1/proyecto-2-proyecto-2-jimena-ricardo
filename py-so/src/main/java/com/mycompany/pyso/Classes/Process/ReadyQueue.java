/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.Process;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ReadyQueue {
    
    private Queue<OSProcess> queue = new LinkedList<>();
    
    public void enqueue(OSProcess process) {
        if (process != null) {
            queue.add(process);
        }
    }
    
    public OSProcess dequeue() {
        return queue.poll();
    }
    
    public OSProcess peek() {
        return queue.peek();
    }
    
    public boolean remove(OSProcess process) {
        return queue.remove(process);
    }
    
    public List<OSProcess> getAll() {
        return new ArrayList<>(queue);
    }
    
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public boolean hasNext() {
        return !queue.isEmpty();
    }
    
    public int size() {
        return queue.size();
    }
    
    public void clear() {
        queue.clear();
    }
    
    public boolean contains(OSProcess process) {
        return queue.contains(process);
    }
}