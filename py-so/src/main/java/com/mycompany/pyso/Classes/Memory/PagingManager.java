/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.Memory;

import com.mycompany.pyso.Classes.Process.OSProcess;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jimen
 */

public class PagingManager implements MemoryManager {
    
    private final int frameSize;
    private final int numFrames;
    private final BitSet freeFrames;
    private final Map<Integer, int[]> pageTables;
    private final int ramSize;
    private int internalFragmentation = 0;
    
    public PagingManager(int ramSize, int frameSize) {
        this.ramSize = ramSize;
        this.frameSize = frameSize;
        this.numFrames = (ramSize - RAM.KERNEL_SIZE) / frameSize;
        this.freeFrames = new BitSet(numFrames);
        this.freeFrames.set(0, numFrames);
        this.pageTables = new HashMap<>();
    }
    
    @Override
    public int allocate(OSProcess process, String[] ram) {
        int requiredPages = (int) Math.ceil((double) process.getInstructions().size() / frameSize);
        
        int[] frames = new int[requiredPages];
        int found = 0;
        
        for (int i = 0; i < numFrames && found < requiredPages; i++) {
            if (freeFrames.get(i)) {
                frames[found] = i;
                found++;
            }
        }
        
        if (found < requiredPages) return -1;
        
        for (int f : frames) {
            freeFrames.clear(f);
        }
        
        pageTables.put(process.getPID(), frames);
        
        int lastPageSize = process.getInstructions().size() % frameSize;
        if (lastPageSize > 0) {
            internalFragmentation += frameSize - lastPageSize;
        }
        
        int baseAddress = RAM.KERNEL_SIZE + frames[0] * frameSize;
        int pageIndex = 0;
        int offset = 0;
        
        for (int i = 0; i < process.getInstructions().size(); i++) {
            int frame = frames[pageIndex];
            int address = RAM.KERNEL_SIZE + frame * frameSize + offset;
            ram[address] = process.getName() + " - " + 
                process.getInstructions().get(i).getInstruction();
            
            offset++;
            if (offset >= frameSize) {
                offset = 0;
                pageIndex++;
            }
        }
        
        if (offset > 0) {
            int lastFrame = frames[frames.length - 1];
            for (int i = offset; i < frameSize; i++) {
                ram[RAM.KERNEL_SIZE + lastFrame * frameSize + i] = "";
            }
        }
        
        return baseAddress;
    }
    
    @Override
    public void free(OSProcess process, String[] ram) {
        int[] frames = pageTables.remove(process.getPID());
        if (frames == null) return;
        
        for (int frame : frames) {
            freeFrames.set(frame);
            for (int i = 0; i < frameSize; i++) {
                int address = RAM.KERNEL_SIZE + frame * frameSize + i;
                if (address < ram.length) ram[address] = "";
            }
        }
    }
    
    @Override
    public String getStrategyName() {
        return "Paginación - Frame size: " + frameSize + " bytes, Frames: " + numFrames;
    }
    
    @Override
    public String getFragmentationInfo() {
        int free = freeFrames.cardinality();
        return "Frames libres: " + free + "/" + numFrames + 
               " | Fragmentación interna: " + internalFragmentation + " bytes";
    }
    
    public int getFrameSize() { return frameSize; }
    public int getNumFrames() { return numFrames; }
    public BitSet getFreeFrames() { return freeFrames; }
    public int[] getPageTable(int pid) { return pageTables.get(pid); }
}
