/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 *
 * @author jimen
 */

public class SchedulerSimulation {
    
    private List<Integer> arrivalTimes;
    private List<Integer> burstTimes;
    private List<Integer> processIds;
    private String algorithm;
    private int quantum;
    
    private int[][] executionMatrix;  
    private int[][] circularMatrix; 
    private int[] completionTimes; 
    private int[] turnaroundTimes;  
    private double[] responseRatios;   
    private int totalTime;
    private int maxCircularRows;         
    
    public SchedulerSimulation() {
        this.arrivalTimes = new ArrayList<>();
        this.burstTimes = new ArrayList<>();
        this.processIds = new ArrayList<>();
        this.quantum = 1;
    }
    
    public SchedulerSimulation(List<Integer> arrivals, List<Integer> bursts, String algorithm) {
        this(arrivals, bursts, algorithm, 1);
    }
    
    public SchedulerSimulation(List<Integer> arrivals, List<Integer> bursts, String algorithm, int quantum) {
        this.arrivalTimes = new ArrayList<>(arrivals);
        this.burstTimes = new ArrayList<>(bursts);
        this.algorithm = algorithm.toUpperCase();
        this.quantum = quantum;
        this.processIds = new ArrayList<>();
        
        for (int i = 0; i < arrivals.size(); i++) {
            processIds.add(i + 1);
        }
    }
        
    public void simulate() {
        int totalBurst = burstTimes.stream().mapToInt(Integer::intValue).sum();
        int maxArrival = arrivalTimes.stream().mapToInt(Integer::intValue).max().orElse(0);
        totalTime = totalBurst + maxArrival + 10;
        
        int n = arrivalTimes.size();
        executionMatrix = new int[n][totalTime];
        completionTimes = new int[n];
        turnaroundTimes = new int[n];
        responseRatios = new double[n];
        
        Arrays.fill(completionTimes, -1);
        
        switch (algorithm) {
            case "FCFS":
                simulateFCFS();
                break;
            case "SJF":
                simulateSJF();
                break;
            case "SRT":
                simulateSRT();
                break;
            case "RR":
                simulateRR();
                break;
            default:
                simulateFCFS();
        }
        
        calculateStatistics();
        adjustTotalTime();
    }
    
    private void simulateFCFS() {
        int n = arrivalTimes.size();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < n; i++) indices.add(i);
        indices.sort(Comparator.comparingInt(i -> arrivalTimes.get(i)));
        
        int currentTime = 0;
        
        for (int idx : indices) {
            if (currentTime < arrivalTimes.get(idx)) {
                currentTime = arrivalTimes.get(idx);
            }
            
            int burst = burstTimes.get(idx);
            for (int t = 0; t < burst; t++) {
                int timePos = currentTime + t;
                if (timePos < totalTime) {
                    executionMatrix[idx][timePos] = 1;
                }
            }
            currentTime += burst;
            completionTimes[idx] = currentTime;
        }
    }
    
    private void simulateSJF() {
        int n = arrivalTimes.size();
        boolean[] completed = new boolean[n];
        int completedCount = 0;
        int currentTime = 0;
        
        while (completedCount < n) {
            List<Integer> available = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if (!completed[i] && arrivalTimes.get(i) <= currentTime) {
                    available.add(i);
                }
            }
            
            if (available.isEmpty()) {
                int nextArrival = Integer.MAX_VALUE;
                for (int i = 0; i < n; i++) {
                    if (!completed[i] && arrivalTimes.get(i) < nextArrival) {
                        nextArrival = arrivalTimes.get(i);
                    }
                }
                currentTime = nextArrival;
                continue;
            }
            
            int selected = available.stream()
                .min(Comparator.comparingInt(i -> burstTimes.get(i)))
                .orElse(-1);
            
            if (selected != -1) {
                int burst = burstTimes.get(selected);
                for (int t = 0; t < burst; t++) {
                    executionMatrix[selected][currentTime + t] = 1;
                }
                currentTime += burst;
                completionTimes[selected] = currentTime;
                completed[selected] = true;
                completedCount++;
            }
        }
    }
    
    private void simulateSRT() {
        int n = arrivalTimes.size();
        int[] remaining = new int[n];
        for (int i = 0; i < n; i++) remaining[i] = burstTimes.get(i);
        
        boolean[] completed = new boolean[n];
        int completedCount = 0;
        int currentTime = 0;
        
        while (completedCount < n) {
            int selected = -1;
            int minRemaining = Integer.MAX_VALUE;
            
            for (int i = 0; i < n; i++) {
                if (!completed[i] && arrivalTimes.get(i) <= currentTime) {
                    if (remaining[i] < minRemaining) {
                        minRemaining = remaining[i];
                        selected = i;
                    }
                }
            }
            
            if (selected == -1) {
                int nextArrival = Integer.MAX_VALUE;
                for (int i = 0; i < n; i++) {
                    if (!completed[i] && arrivalTimes.get(i) < nextArrival) {
                        nextArrival = arrivalTimes.get(i);
                    }
                }
                currentTime = nextArrival;
                continue;
            }
            
            executionMatrix[selected][currentTime] = 1;
            remaining[selected]--;
            currentTime++;
            
            if (remaining[selected] == 0) {
                completionTimes[selected] = currentTime;
                completed[selected] = true;
                completedCount++;
            }
        }
    }
    
    private void simulateRR() {
        int n = arrivalTimes.size();
        int[] remaining = new int[n];
        for (int i = 0; i < n; i++) remaining[i] = burstTimes.get(i);
        
        boolean[] completed = new boolean[n];
        int completedCount = 0;
        int currentTime = 0;
        
        Queue<Integer> readyQueue = new LinkedList<>();
        boolean[] inQueue = new boolean[n];
        
        List<List<Integer>> circularHistory = new ArrayList<>();
        
        while (completedCount < n) {
            for (int i = 0; i < n; i++) {
                if (!completed[i] && !inQueue[i] && arrivalTimes.get(i) <= currentTime) {
                    readyQueue.add(i);
                    inQueue[i] = true;
                    List<Integer> newRow = new ArrayList<>();
                    newRow.add(currentTime);
                    newRow.add(i);
                    circularHistory.add(newRow);
                }
            }
            
            if (readyQueue.isEmpty()) {
                int nextArrival = Integer.MAX_VALUE;
                for (int i = 0; i < n; i++) {
                    if (!completed[i] && arrivalTimes.get(i) < nextArrival) {
                        nextArrival = arrivalTimes.get(i);
                    }
                }
                currentTime = nextArrival;
                continue;
            }
            
            int currentProc = readyQueue.poll();
            inQueue[currentProc] = false;
            
            int executeTime = Math.min(quantum, remaining[currentProc]);
            
            for (int t = 0; t < executeTime; t++) {
                executionMatrix[currentProc][currentTime + t] = 1;
            }
            
            currentTime += executeTime;
            remaining[currentProc] -= executeTime;
            
            for (int i = 0; i < n; i++) {
                if (!completed[i] && !inQueue[i] && arrivalTimes.get(i) <= currentTime && i != currentProc) {
                    readyQueue.add(i);
                    inQueue[i] = true;
                    List<Integer> newRow = new ArrayList<>();
                    newRow.add(currentTime);
                    newRow.add(i);
                    circularHistory.add(newRow);
                }
            }
            
            if (remaining[currentProc] == 0) {
                completionTimes[currentProc] = currentTime;
                completed[currentProc] = true;
                completedCount++;
            } else {
                // Volver a poner en la cola circular
                readyQueue.add(currentProc);
                inQueue[currentProc] = true;
                List<Integer> newRow = new ArrayList<>();
                newRow.add(currentTime);
                newRow.add(currentProc);
                circularHistory.add(newRow);
            }
        }
        
        buildCircularMatrix(circularHistory);
    }
    
    private void buildCircularMatrix(List<List<Integer>> history) {
        if (history.isEmpty()) {
            circularMatrix = new int[0][totalTime];
            maxCircularRows = 0;
            return;
        }
        
        Map<Integer, Integer> timeToQueueSize = new HashMap<>();
        for (List<Integer> entry : history) {
            int time = entry.get(0);
            timeToQueueSize.put(time, timeToQueueSize.getOrDefault(time, 0) + 1);
        }
        maxCircularRows = timeToQueueSize.values().stream().max(Integer::compare).orElse(1);
        
        circularMatrix = new int[maxCircularRows][totalTime];
        for (int i = 0; i < maxCircularRows; i++) {
            Arrays.fill(circularMatrix[i], -1);
        }
        
        Map<Integer, List<Integer>> timeToProcesses = new HashMap<>();
        for (List<Integer> entry : history) {
            int time = entry.get(0);
            int proc = entry.get(1);
            timeToProcesses.computeIfAbsent(time, k -> new ArrayList<>()).add(proc);
        }
        
        for (Map.Entry<Integer, List<Integer>> entry : timeToProcesses.entrySet()) {
            int time = entry.getKey();
            List<Integer> processes = entry.getValue();
            for (int row = 0; row < processes.size() && row < maxCircularRows; row++) {
                circularMatrix[row][time] = processes.get(row);
            }
        }
    }
    
    private void calculateStatistics() {
        int n = arrivalTimes.size();
        for (int i = 0; i < n; i++) {
            if (completionTimes[i] != -1) {
                turnaroundTimes[i] = completionTimes[i] - arrivalTimes.get(i);
                responseRatios[i] = (double) turnaroundTimes[i] / burstTimes.get(i);
            }
        }
    }
    
    private void adjustTotalTime() {
        int lastTime = 0;
        for (int i = 0; i < executionMatrix.length; i++) {
            for (int t = 0; t < executionMatrix[i].length; t++) {
                if (executionMatrix[i][t] == 1 && t > lastTime) {
                    lastTime = t;
                }
            }
        }
        totalTime = lastTime + 1;
    }
    
    public int[][] getExecutionMatrix() {
        return executionMatrix;
    }
    
    public String[][] getFormattedExecutionMatrix() {
        if (executionMatrix == null) return null;
        int n = arrivalTimes.size();
        String[][] formatted = new String[n][totalTime];
        for (int i = 0; i < n; i++) {
            for (int t = 0; t < totalTime; t++) {
                formatted[i][t] = (executionMatrix[i][t] == 1) ? "x" : "";
            }
        }
        return formatted;
    }
    
    public int[][] getCircularMatrix() {
        return circularMatrix;
    }
    
    public String[][] getFormattedCircularMatrix() {
        if (circularMatrix == null) return null;
        String[][] formatted = new String[maxCircularRows][totalTime];
        for (int r = 0; r < maxCircularRows; r++) {
            for (int t = 0; t < totalTime; t++) {
                int proc = circularMatrix[r][t];
                formatted[r][t] = (proc != -1) ? String.valueOf(proc) : "";
            }
        }
        return formatted;
    }
    
    public int getMaxCircularRows() {
        return maxCircularRows;
    }
    
    public int[] getCompletionTimes() {
        return completionTimes;
    }
    
    public int[] getTurnaroundTimes() {
        return turnaroundTimes;
    }
    
    public double[] getResponseRatios() {
        return responseRatios;
    }
    
    public int getTotalTime() {
        return totalTime;
    }
    
    public List<Integer> getProcessIds() {
        return processIds;
    }
    
    public List<Integer> getArrivalTimes() {
        return arrivalTimes;
    }
    
    public List<Integer> getBurstTimes() {
        return burstTimes;
    }
    
    public String getAlgorithm() {
        return algorithm;
    }
    
    public boolean isCircularAlgorithm() {
        return algorithm.equals("RR");
    }
    
    
    public void printExecutionMatrix() {
        System.out.println("\n=== TABLA 1: MATRIZ DE EJECUCIÓN ===");
        System.out.print("      ");
        for (int t = 0; t < totalTime && t < 30; t++) {
            System.out.printf("%3d", t + 1);
        }
        System.out.println();
        
        for (int i = 0; i < arrivalTimes.size(); i++) {
            System.out.printf("P%-4d ", processIds.get(i));
            for (int t = 0; t < totalTime && t < 30; t++) {
                System.out.print(executionMatrix[i][t] == 1 ? "  x" : "   ");
            }
            System.out.println();
        }
    }
    
    public void printCircularMatrix() {
        if (circularMatrix == null || !isCircularAlgorithm()) {
            System.out.println("No aplica para este algoritmo");
            return;
        }
        
        System.out.println("\n=== TABLA 2: MATRIZ CIRCULAR (SOLO RR/SRR) ===");
        System.out.print("      ");
        for (int t = 0; t < totalTime && t < 30; t++) {
            System.out.printf("%3d", t + 1);
        }
        System.out.println();
        
        for (int r = 0; r < maxCircularRows; r++) {
            System.out.printf("Fil%-3d ", r + 1);
            for (int t = 0; t < totalTime && t < 30; t++) {
                int proc = circularMatrix[r][t];
                System.out.print(proc != -1 ? String.format("%3d", proc) : "   ");
            }
            System.out.println();
        }
    }
    
    public void printStatistics() {
        System.out.println("\n=== ESTADÍSTICAS ===");
        System.out.printf("%-8s %-10s %-10s %-10s %-10s %-8s%n", 
            "Proceso", "Llegada", "Ráfaga", "Tf", "Tr", "Tr/Ts");
        System.out.println("------------------------------------------------");
        for (int i = 0; i < arrivalTimes.size(); i++) {
            System.out.printf("P%-7d %-10d %-10d %-10d %-10d %.2f%n",
                processIds.get(i), arrivalTimes.get(i), burstTimes.get(i),
                completionTimes[i], turnaroundTimes[i], responseRatios[i]);
        }
    }
}