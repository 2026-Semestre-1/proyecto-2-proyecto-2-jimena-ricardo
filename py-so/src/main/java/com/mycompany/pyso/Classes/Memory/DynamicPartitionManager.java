/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.Memory;

import com.mycompany.pyso.Classes.Process.OSProcess;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jimen
 */

public class DynamicPartitionManager implements MemoryManager {
    
    private List<MemoryBlock> freeBlocks;
    private List<MemoryBlock> allocatedBlocks;
    private final int totalSize;
    private boolean autoCompact;
    private int fragmentationThreshold;
    private int externalFragmentation = 0;
    
    public DynamicPartitionManager(int ramSize, boolean autoCompact, int fragmentationThreshold) {
        this.totalSize = ramSize;
        this.freeBlocks = new ArrayList<>();
        this.allocatedBlocks = new ArrayList<>();
        this.autoCompact = autoCompact;
        this.fragmentationThreshold = fragmentationThreshold;
        
        freeBlocks.add(new MemoryBlock(RAM.KERNEL_SIZE, ramSize - RAM.KERNEL_SIZE));
    }
    
    @Override
    public int allocate(OSProcess process, String[] ram) {
        int required = process.getInstructions().size();
        
        MemoryBlock bestBlock = null;
        int minWaste = Integer.MAX_VALUE;
        
        for (MemoryBlock block : freeBlocks) {
            if (block.isFree() && block.getSize() >= required) {
                int waste = block.getSize() - required;
                if (waste < minWaste) {
                    minWaste = waste;
                    bestBlock = block;
                }
            }
        }
        
        if (bestBlock == null) {
            if (autoCompact) {
                compact(ram);
                for (MemoryBlock block : freeBlocks) {
                    if (block.isFree() && block.getSize() >= required) {
                        bestBlock = block;
                        break;
                    }
                }
                if (bestBlock == null) return -1;
            } else {
                return -1;
            }
        }
        
        int allocatedAddress = bestBlock.getStartAddress();
        
        MemoryBlock allocated = new MemoryBlock(allocatedAddress, required);
        allocated.setFree(false);
        allocated.setProcessId(process.getPID());
        allocatedBlocks.add(allocated);
        
        bestBlock.setStartAddress(allocatedAddress + required);
        bestBlock.setSize(bestBlock.getSize() - required);
        
        if (bestBlock.getSize() == 0) {
            freeBlocks.remove(bestBlock);
        }
        
        for (int i = 0; i < required; i++) {
            ram[allocatedAddress + i] = process.getName() + " - " + 
                process.getInstructions().get(i).getInstruction();
        }
        
        updateFragmentation();
        return allocatedAddress;
    }
    
    @Override
    public void free(OSProcess process, String[] ram) {
        int pid = process.getPID();
        MemoryBlock toFree = null;
        
        for (MemoryBlock block : allocatedBlocks) {
            if (block.getProcessId() != null && block.getProcessId() == pid) {
                toFree = block;
                break;
            }
        }
        
        if (toFree == null) return;
        
        allocatedBlocks.remove(toFree);
        
        for (int i = toFree.getStartAddress(); i < toFree.getEndAddress(); i++) {
            ram[i] = "";
        }
        
        toFree.setFree(true);
        toFree.setProcessId(null);
        freeBlocks.add(toFree);
        
        coalesce();
        
        updateFragmentation();
    }
    
    private void coalesce() {
        freeBlocks.sort((a, b) -> Integer.compare(a.getStartAddress(), b.getStartAddress()));
        
        List<MemoryBlock> merged = new ArrayList<>();
        MemoryBlock current = null;
        
        for (MemoryBlock block : freeBlocks) {
            if (current == null) {
                current = block;
            } else if (current.getEndAddress() == block.getStartAddress()) {
                current.setSize(current.getSize() + block.getSize());
            } else {
                merged.add(current);
                current = block;
            }
        }
        if (current != null) merged.add(current);
        
        freeBlocks = merged;
    }
    
    @Override
    public void compact(String[] ram) {
        allocatedBlocks.sort((a, b) -> Integer.compare(a.getStartAddress(), b.getStartAddress()));
        
        int nextAddr = RAM.KERNEL_SIZE;
        
        for (MemoryBlock block : allocatedBlocks) {
            if (block.getStartAddress() != nextAddr) {
                int size = block.getSize();
                for (int i = 0; i < size; i++) {
                    ram[nextAddr + i] = ram[block.getStartAddress() + i];
                    ram[block.getStartAddress() + i] = "";
                }
                block.setStartAddress(nextAddr);
            }
            nextAddr += block.getSize();
        }
        
        freeBlocks.clear();
        if (nextAddr < totalSize) {
            freeBlocks.add(new MemoryBlock(nextAddr, totalSize - nextAddr));
        }
        
        System.out.println("[MEMORY] Compactación completada");
    }
    
    private void updateFragmentation() {
        externalFragmentation = 0;
        for (MemoryBlock block : freeBlocks) {
            externalFragmentation += block.getSize();
        }
        
        if (autoCompact && externalFragmentation > 0) {
            int totalFree = externalFragmentation;
            int largestFree = freeBlocks.stream().mapToInt(MemoryBlock::getSize).max().orElse(0);
            int fragmentationPercent = (totalFree - largestFree) * 100 / (totalFree + 1);
            
            if (fragmentationPercent > fragmentationThreshold) {
                System.out.println("[MEMORY] Umbral de fragmentación excedido: " + fragmentationPercent + "%");
            }
        }
    }
    
    @Override
    public String getStrategyName() {
        return "Particiones Dinámicas (Best Fit)" + (autoCompact ? " + Compactación" : "");
    }
    
    @Override
    public String getFragmentationInfo() {
        return "Fragmentación externa: " + externalFragmentation + " bytes | Bloques libres: " + freeBlocks.size();
    }
    
    public List<MemoryBlock> getFreeBlocks() { return freeBlocks; }
    public List<MemoryBlock> getAllocatedBlocks() { return allocatedBlocks; }
    public boolean needsCompaction() {
        if (!autoCompact) return false;
        int totalFree = freeBlocks.stream().mapToInt(MemoryBlock::getSize).sum();
        int largestFree = freeBlocks.stream().mapToInt(MemoryBlock::getSize).max().orElse(0);
        return (totalFree - largestFree) * 100 / (totalFree + 1) > fragmentationThreshold;
    }
}