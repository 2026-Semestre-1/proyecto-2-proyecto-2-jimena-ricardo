package com.mycompany.pyso.Classes.Memory;

import com.mycompany.pyso.Classes.Core.Instruction;
import com.mycompany.pyso.Classes.Process.OSProcess;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Disk {

    private String[] storage;
    private final Map<String, DiskEntry> fileIndex = new LinkedHashMap<>();

    private int indexReserved;
    private int swapStart;   
    private int swapAreaSize; 
    private int fileStart;    
    private int nextFilePos;   

    private final List<SwapEntry> swapList = new ArrayList<>();

    private static final int DEFAULT_SIZE = 512;

    public static class SwapEntry {
        public final int    pid;
        public final String processName;
        public final int    instructionCount;
        public final int    diskAddress;   // address in fileArea where instructions live

        public SwapEntry(int pid, String name, int count, int diskAddr) {
            this.pid              = pid;
            this.processName      = name;
            this.instructionCount = count;
            this.diskAddress      = diskAddr;
        }

        @Override
        public String toString() {
            return "PID=" + pid + " | " + processName
                 + " | Instr=" + instructionCount
                 + " | @" + diskAddress;
        }
    }

    public Disk() { this(DEFAULT_SIZE); }

    public Disk(int size) {
        storage = new String[size];
        for (int i = 0; i < size; i++) storage[i] = "";
        computeLayout(size);
        writeHeaders();
    }

    private void computeLayout(int size) {
        indexReserved = Math.max(5,  (int) Math.ceil(size * 0.05));
        swapAreaSize  = Math.max(10, (int) Math.ceil(size * 0.20));
        swapStart     = indexReserved;
        fileStart     = swapStart + swapAreaSize;
        nextFilePos   = fileStart;
    }

    private void writeHeaders() {
        storage[0]         = "=== FILE INDEX ===";
        storage[swapStart] = "=== SWAP / VIRTUAL MEMORY ===";
    }

    public synchronized void swapOut(OSProcess process) {
        swapList.removeIf(e -> e.pid == process.getPID());
        swapList.add(new SwapEntry(
            process.getPID(),
            process.getName(),
            process.getInstructions() != null ? process.getInstructions().size() : 0,
            process.getDiskAddress()
        ));
        flushSwapToStorage();
    }

    public synchronized SwapEntry swapIn(int pid) {
        SwapEntry found = swapList.stream().filter(e -> e.pid == pid).findFirst().orElse(null);
        if (found != null) { swapList.remove(found); flushSwapToStorage(); }
        return found;
    }

    public synchronized SwapEntry swapInNext() {
        if (swapList.isEmpty()) return null;
        SwapEntry e = swapList.remove(0);
        flushSwapToStorage();
        return e;
    }

    public synchronized boolean isInSwap(int pid) {
        return swapList.stream().anyMatch(e -> e.pid == pid);
    }

    public synchronized List<SwapEntry> getSwapList() { return new ArrayList<>(swapList); }
    public synchronized boolean isSwapEmpty()         { return swapList.isEmpty(); }
    public synchronized int getSwapCount()            { return swapList.size(); }

    /** Writes swap list into disk storage area so the UI can display it. */
    private void flushSwapToStorage() {
        // clear swap area (keep header at swapStart)
        for (int i = swapStart + 1; i < fileStart && i < storage.length; i++) storage[i] = "";
        int pos = swapStart + 1;
        for (SwapEntry e : swapList) {
            if (pos >= fileStart || pos >= storage.length) break;
            storage[pos++] = e.toString();
        }
    }

    public synchronized int save(String fileName, List<Instruction> instructions) {
        int needed = instructions.size();
        if (nextFilePos + needed > storage.length) return -1;

        int start = nextFilePos;
        for (Instruction inst : instructions)
            storage[nextFilePos++] = fileName + " - " + inst.getInstruction();

        fileIndex.put(fileName, new DiskEntry(fileName, start, needed));
        flushIndexToStorage();
        return start;
    }

    public synchronized List<String> read(int startAddress, int size) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int pos = startAddress + i;
            if (pos < storage.length && storage[pos] != null && !storage[pos].isEmpty())
                result.add(storage[pos]);
        }
        return result;
    }

    public synchronized boolean delete(String fileName) {
        DiskEntry entry = fileIndex.remove(fileName);
        if (entry == null) return false;
        for (int i = entry.address; i < entry.address + entry.size && i < storage.length; i++)
            storage[i] = "";
        flushIndexToStorage();
        return true;
    }

    public synchronized boolean exists(String fileName) { return fileIndex.containsKey(fileName); }
    public synchronized DiskEntry getEntry(String fileName) { return fileIndex.get(fileName); }

    public synchronized boolean hasSpace(int size) {
        return nextFilePos + size <= storage.length;
    }

    private void flushIndexToStorage() {
        for (int i = 1; i < indexReserved && i < storage.length; i++) storage[i] = "";
        int pos = 1;
        for (DiskEntry e : fileIndex.values()) {
            if (pos >= indexReserved) break;
            storage[pos++] = e.name + " | @" + e.address + " | size=" + e.size;
        }
    }

    public synchronized void resize(int newSize) {
        String[] ns = new String[newSize];
        for (int i = 0; i < newSize; i++) ns[i] = i < storage.length ? storage[i] : "";
        storage = ns;
        computeLayout(newSize);
        writeHeaders();
        flushIndexToStorage();
        flushSwapToStorage();
    }

    public String[] getStorage()                 { return storage; }
    public int getIndexReserved()                { return indexReserved; }
    public int getSwapStartPosition()            { return swapStart; }
    public int getSwapSize()                     { return swapAreaSize; }
    public int getFileStart()                    { return fileStart; }
    public Map<String, DiskEntry> getFileIndex() { return fileIndex; }
    public int getNextFreePosition()             { return nextFilePos; }
}