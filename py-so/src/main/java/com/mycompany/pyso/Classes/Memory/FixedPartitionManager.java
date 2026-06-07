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
    private final int[][] partitionProgress; 
    private final boolean singleQueue;
    
    private int internalFragmentation = 0;
    
    /**
     * Constructor para particiones del mismo tamaño
     */
    public FixedPartitionManager(int partitionSize, int ramSize, boolean singleQueue) {
        int availableRam = ramSize - RAM.KERNEL_SIZE;
        int numPartitions = availableRam / partitionSize;
        
        this.partitionSizes = new int[numPartitions];
        this.partitionBase = new int[numPartitions];
        this.partitionFree = new boolean[numPartitions];
        this.partitionProcessId = new Integer[numPartitions];
        this.partitionProgress = new int[numPartitions][2]; // [instrIndex, totalInstrucciones]
        this.singleQueue = singleQueue;
        
        int currentAddr = RAM.KERNEL_SIZE;
        for (int i = 0; i < numPartitions; i++) {
            partitionSizes[i] = partitionSize;
            partitionBase[i] = currentAddr;
            partitionFree[i] = true;
            partitionProcessId[i] = null;
            partitionProgress[i][0] = 0;
            partitionProgress[i][1] = 0;
            currentAddr += partitionSize;
        }
        
        System.out.println("[FPM] Creadas " + numPartitions + " particiones de " + partitionSize + " bytes");
    }
    
    /**
     * Constructor para particiones de diferentes tamaños
     */
    public FixedPartitionManager(int[] sizes, int ramSize, boolean singleQueue) {
        this.singleQueue = singleQueue;
        
        java.util.ArrayList<Integer> allSizes = new java.util.ArrayList<>();
        int availableRam = ramSize - RAM.KERNEL_SIZE;
        int currentPos = 0;
        int patternIndex = 0;
        
        while (currentPos < availableRam) {
            int size = sizes[patternIndex % sizes.length];
            if (currentPos + size <= availableRam) {
                allSizes.add(size);
                currentPos += size;
            } else {
                int remaining = availableRam - currentPos;
                if (remaining > 0) {
                    allSizes.add(remaining);
                }
                break;
            }
            patternIndex++;
        }
        
        this.partitionSizes = allSizes.stream().mapToInt(i -> i).toArray();
        this.partitionBase = new int[partitionSizes.length];
        this.partitionFree = new boolean[partitionSizes.length];
        this.partitionProcessId = new Integer[partitionSizes.length];
        this.partitionProgress = new int[partitionSizes.length][2];
        
        int currentAddr = RAM.KERNEL_SIZE;
        for (int i = 0; i < partitionSizes.length; i++) {
            partitionBase[i] = currentAddr;
            partitionFree[i] = true;
            partitionProcessId[i] = null;
            partitionProgress[i][0] = 0;
            partitionProgress[i][1] = 0;
            currentAddr += partitionSizes[i];
        }
        
        System.out.println("[FPM] Creadas " + partitionSizes.length + " particiones: " + 
                           java.util.Arrays.toString(partitionSizes));
    }
    
    @Override
    public int allocate(OSProcess process, String[] ram) {
        int required = process.getInstructions().size();
        
        if (singleQueue) {
            // Encontrar suficientes particiones CONSECUTIVAS libres
            int neededBlocks = calculateNeededBlocks(required);
            int startIdx = findConsecutiveFreeBlocks(neededBlocks);
            
            if (startIdx == -1) return -1;
            
            // Asignar todas las particiones consecutivas al mismo proceso
            int totalSize = 0;
            int remainingInstructions = required;
            int instrOffset = 0;
            
            for (int i = startIdx; i < startIdx + neededBlocks && i < partitionSizes.length; i++) {
                partitionFree[i] = false;
                partitionProcessId[i] = process.getPID();
                partitionProgress[i][0] = instrOffset;      // índice de inicio en instrucciones
                partitionProgress[i][1] = Math.min(remainingInstructions, partitionSizes[i]); // cuántas instrucciones caben
                
                totalSize += partitionSizes[i];
                remainingInstructions -= partitionSizes[i];
                instrOffset += partitionSizes[i];
            }
            
            internalFragmentation += totalSize - required;
            int baseAddr = partitionBase[startIdx];
            
            // Cargar instrucciones distribuidas entre particiones
            int instrIdx = 0;
            for (int i = startIdx; i < startIdx + neededBlocks && instrIdx < required; i++) {
                int base = partitionBase[i];
                int blockSize = partitionSizes[i];
                int instructionsInThisBlock = Math.min(blockSize, required - instrIdx);
                
                for (int j = 0; j < instructionsInThisBlock; j++) {
                    ram[base + j] = process.getName() + " - " + 
                        process.getInstructions().get(instrIdx).getInstruction();
                    instrIdx++;
                }
                
                // Rellenar el resto del bloque con marcador de fragmentación
                for (int j = instructionsInThisBlock; j < blockSize; j++) {
                    if (j < blockSize && (ram[base + j] == null || ram[base + j].isEmpty())) {
                        ram[base + j] = "=== FRAG. INTERNA (parte " + (i-startIdx+1) + "/" + neededBlocks + ") ===";
                    }
                }
            }
            
            System.out.println("[FPM] Proceso " + process.getName() + " (" + required + " instr) asignado a " + 
                               neededBlocks + " particiones consecutivas desde índice " + startIdx);
            
            return baseAddr;
        } else {
            // Múltiples colas: buscar una partición INDIVIDUAL que quepa el proceso completo
            for (int i = 0; i < partitionSizes.length; i++) {
                if (partitionFree[i] && partitionSizes[i] >= required) {
                    partitionFree[i] = false;
                    partitionProcessId[i] = process.getPID();
                    partitionProgress[i][0] = 0;
                    partitionProgress[i][1] = required;
                    
                    internalFragmentation += partitionSizes[i] - required;
                    
                    int base = partitionBase[i];
                    for (int j = 0; j < required; j++) {
                        ram[base + j] = process.getName() + " - " + 
                            process.getInstructions().get(j).getInstruction();
                    }
                    for (int j = required; j < partitionSizes[i]; j++) {
                        ram[base + j] = "=== FRAGMENTACIÓN INTERNA ===";
                    }
                    
                    System.out.println("[FPM] Proceso " + process.getName() + " asignado a partición única " + i);
                    return base;
                }
            }
            return -1;
        }
    }
    
    /**
     * Calcula cuántas particiones necesita un proceso según su tamaño
     */
    private int calculateNeededBlocks(int requiredSize) {
        if (partitionSizes.length == 0) return 1;
        
        // En modo cola única, usamos el tamaño de bloque más común
        int blockSize = partitionSizes[0]; // Asumimos bloques iguales o usamos promedio
        return (int) Math.ceil((double) requiredSize / blockSize);
    }
    
    /**
     * Encuentra N bloques consecutivos libres
     */
    private int findConsecutiveFreeBlocks(int needed) {
        int count = 0;
        int start = -1;
        
        for (int i = 0; i < partitionFree.length; i++) {
            if (partitionFree[i]) {
                if (count == 0) start = i;
                count++;
                if (count == needed) return start;
            } else {
                count = 0;
                start = -1;
            }
        }
        return -1;
    }
    
    @Override
    public void free(OSProcess process, String[] ram) {
        int pid = process.getPID();
        for (int i = 0; i < partitionProcessId.length; i++) {
            if (partitionProcessId[i] != null && partitionProcessId[i] == pid) {
                partitionFree[i] = true;
                partitionProcessId[i] = null;
                partitionProgress[i][0] = 0;
                partitionProgress[i][1] = 0;
                
                // Limpiar RAM
                int base = partitionBase[i];
                for (int j = 0; j < partitionSizes[i] && base + j < ram.length; j++) {
                    ram[base + j] = "";
                }
            }
        }
        System.out.println("[FPM] Memoria liberada para proceso PID=" + pid);
    }
    
    @Override
    public String getStrategyName() {
        if (partitionSizes.length > 0 && isAllEqual(partitionSizes)) {
            return "Particiones Fijas - Bloques de " + partitionSizes[0] + " bytes (" + 
                   partitionSizes.length + " bloques) - Modo multibloque";
        } else {
            return "Particiones Fijas - Bloques variables (" + partitionSizes.length + " bloques)";
        }
    }
    
    private boolean isAllEqual(int[] arr) {
        if (arr.length == 0) return true;
        int first = arr[0];
        for (int v : arr) if (v != first) return false;
        return true;
    }
    
    @Override
    public String getFragmentationInfo() {
        int freeBlocks = 0;
        for (boolean free : partitionFree) if (free) freeBlocks++;
        return "Fragmentación interna: " + internalFragmentation + " bytes | Bloques libres: " + freeBlocks;
    }
    
    public int[] getPartitionSizes() { return partitionSizes; }
    public int[] getPartitionBase() { return partitionBase; }
    public boolean[] getPartitionFree() { return partitionFree; }
    public Integer[] getPartitionProcessId() { return partitionProcessId; }
    public int[][] getPartitionProgress() { return partitionProgress; }
    public int getNumPartitions() { return partitionSizes.length; }
}