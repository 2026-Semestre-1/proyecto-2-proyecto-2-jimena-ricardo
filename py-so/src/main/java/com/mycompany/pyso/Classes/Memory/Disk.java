/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.Memory;
import com.mycompany.pyso.Classes.Core.Instruction;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jimen
 */
public class Disk {

    private String[] storage;
    private int nextFreePosition;
    private final Map<String, DiskEntry> fileIndex;
    private static final int DEFAULT_SIZE= 512;
    private int indexReserved;

    public Disk() {
        this(DEFAULT_SIZE);
    }

    public Disk(int size) {
        this.storage = new String[size];
        for (int i = 0; i < size; i++) {
            storage[i] = "";
        }

        this.indexReserved = Math.max(1, (int) Math.ceil(size * 0.05));

        this.nextFreePosition = indexReserved;
        this.fileIndex = new LinkedHashMap<>();
        writeIndexToStorage();
    }

    public int save(String fileName, List<Instruction> instructions) {
        if (!hasSpace(instructions.size())) return -1;
 
        int startAddress = nextFreePosition;
 
        for (Instruction inst : instructions) {
            storage[nextFreePosition++] = fileName + " - " + inst.getInstruction();
        }
 
        fileIndex.put(fileName, new DiskEntry(fileName, startAddress, instructions.size()));
        writeIndexToStorage();
        return startAddress;
    }


    public List<String> read(int startAddress, int size) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add(storage[startAddress + i]);
        }
        return result;
    }

    public boolean delete(String fileName) {
        DiskEntry entry = fileIndex.get(fileName);
        if (entry == null) return false;

        for (int i = entry.address; i < entry.address + entry.size; i++) {
            storage[i] = "";
        }

        fileIndex.remove(fileName);
        writeIndexToStorage();
        return true;
    }

    public boolean exists(String fileName) {
        return fileIndex.containsKey(fileName);
    }

    public DiskEntry getEntry(String fileName) {
        return fileIndex.get(fileName);
    }

    public boolean hasSpace(int size) {
        return nextFreePosition + size <= storage.length;
    }

    private void writeIndexToStorage() {
        for (int i = 0; i < indexReserved; i++) {
            storage[i] = "";
        }

        storage[0] = "FILE INDEX";
        int pos = 1;
        for (DiskEntry entry : fileIndex.values()) {
            if (pos >= indexReserved) break;
            storage[pos++] = "File Name: " + entry.name ;
        }
    }

    public void resize(int newSize) {
        String[] newStorage = new String[newSize];
        for (int i = 0; i < newSize; i++) {
            newStorage[i] = i < storage.length ? storage[i] : "";
        }
        this.storage = newStorage;
    }

    public String[] getStorage() {
        return storage;
    }

    public void setStorage(String[] storage) {
        this.storage = storage;
    }

    public int getNextFreePosition() {
        return nextFreePosition;
    }

    public void setNextFreePosition(int nextFreePosition) {
        this.nextFreePosition = nextFreePosition;
    }

    public int getIndexReserved() {
        return indexReserved;
    }
    
    
}
