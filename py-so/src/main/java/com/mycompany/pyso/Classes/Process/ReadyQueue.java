package com.mycompany.pyso.Classes.Process;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ReadyQueue {

    // ConcurrentLinkedQueue: lock-free, thread-safe for multi-CPU access
    private final ConcurrentLinkedQueue<OSProcess> queue = new ConcurrentLinkedQueue<>();

    public void enqueue(OSProcess process) {
        if (process != null) queue.add(process);
    }

    public OSProcess dequeue() { return queue.poll(); }
    public OSProcess peek()    { return queue.peek(); }

    public boolean remove(OSProcess process) { return queue.remove(process); }

    public List<OSProcess> getAll() { return new ArrayList<>(queue); }

    public boolean isEmpty()  { return queue.isEmpty(); }
    public boolean hasNext()  { return !queue.isEmpty(); }
    public int size()         { return queue.size(); }
    public void clear()       { queue.clear(); }
    public boolean contains(OSProcess p) { return queue.contains(p); }
}