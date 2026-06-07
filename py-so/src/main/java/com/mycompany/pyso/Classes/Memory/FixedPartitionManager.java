/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.Memory;

import com.mycompany.pyso.Classes.Process.OSProcess;
import java.util.Arrays;

/**
 *
 * @author jimen
 */

public class FixedPartitionManager implements MemoryManager {
    
    private final int[] partitionSizes;
    private final int[] partitionBase;
    private final boolean[] partitionFree;
    private final Integer[] partitionProcessId;
    private final boolean singleQueue; 
    
    private int internalFragmentation = 0;
    
    public FixedPartitionManager(int numPartitions, int size, boolean singleQueue) {
        this.partitionSizes = new int[numPartitions];
        this.partitionBase = new int[numPartitions];
        this.partitionFree = new boolean[numPartitions];
        this.partitionProcessId = new Integer[numPartitions];
        this.singleQueue = singleQueue;
        
        int currentAddr = RAM.KERNEL_SIZE;
        for (int i = 0; i < numPartitions; i++) {
            partitionSizes[i] = size;
            partitionBase[i] = currentAddr;
            partitionFree[i] = true;
            partitionProcessId[i] = null;
            currentAddr += size;
        }
    }
    
    public FixedPartitionManager(int[] sizes, boolean singleQueue) {
        this.partitionSizes = Arrays.copyOf(sizes, sizes.length);
        this.partitionBase = new int[sizes.length];
        this.partitionFree = new boolean[sizes.length];
        this.partitionProcessId = new Integer[sizes.length];
        this.singleQueue = singleQueue;
        
        int currentAddr = RAM.KERNEL_SIZE;
        for (int i = 0; i < sizes.length; i++) {
            partitionBase[i] = currentAddr;
            partitionFree[i] = true;
            partitionProcessId[i] = null;
            currentAddr += sizes[i];
        }
    }
    
    @Override
    public int allocate(OSProcess process, String[] ram) {
        int required = process.getInstructions().size();
        int bestIndex = -1;
        
        if (singleQueue) {
            int minWaste = Integer.MAX_VALUE;
            for (int i = 0; i < partitionSizes.length; i++) {
                if (partitionFree[i] && partitionSizes[i] >= required) {
                    int waste = partitionSizes[i] - required;
                    if (waste < minWaste) {
                        minWaste = waste;
                        bestIndex = i;
                    }
                }
            }
        } else {
            for (int i = 0; i < partitionSizes.length; i++) {
                if (partitionFree[i] && partitionSizes[i] >= required) {
                    bestIndex = i;
                    break;
                }
            }
        }
        
        if (bestIndex == -1) return -1;
        
        partitionFree[bestIndex] = false;
        partitionProcessId[bestIndex] = process.getPID();
        internalFragmentation += partitionSizes[bestIndex] - required;
        
        int base = partitionBase[bestIndex];
        for (int i = 0; i < partitionSizes[bestIndex]; i++) {
            if (i < required) {
                ram[base + i] = process.getName() + " - " + 
                    process.getInstructions().get(i).getInstruction();
            } else {
                ram[base + i] = "=== FRAGMENTACIÓN INTERNA ===";
            }
        }
        
        return base;
    }
    
    @Override
    public void free(OSProcess process, String[] ram) {
        int pid = process.getPID();
        for (int i = 0; i < partitionProcessId.length; i++) {
            if (partitionProcessId[i] != null && partitionProcessId[i] == pid) {
                partitionFree[i] = true;
                partitionProcessId[i] = null;
                
                int base = partitionBase[i];
                for (int j = 0; j < partitionSizes[i]; j++) {
                    ram[base + j] = "";
                }
                break;
            }
        }
    }
    
    @Override
    public String getStrategyName() {
        if (partitionSizes.length > 0 && isAllEqual(partitionSizes)) {
            return "Particiones Fijas (iguales) - " + partitionSizes.length + " x " + partitionSizes[0];
        } else {
            return "Particiones Fijas (diferentes) - " + partitionSizes.length + " particiones";
        }
    }
    
    private boolean isAllEqual(int[] arr) {
        int first = arr[0];
        for (int v : arr) if (v != first) return false;
        return true;
    }
    
    @Override
    public String getFragmentationInfo() {
        return "Fragmentación interna: " + internalFragmentation + " bytes";
    }
    
    public int[] getPartitionSizes() { return partitionSizes; }
    public int[] getPartitionBase() { return partitionBase; }
    public boolean[] getPartitionFree() { return partitionFree; }
}
