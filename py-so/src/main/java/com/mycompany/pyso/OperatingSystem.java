/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso;

import com.mycompany.pyso.Classes.Core.CPU;
import com.mycompany.pyso.Classes.Core.Scheduler;
import com.mycompany.pyso.Classes.Memory.RAM;
import com.mycompany.pyso.Classes.Memory.Disk;
import com.mycompany.pyso.Classes.Process.Dispatcher;
import com.mycompany.pyso.Classes.Process.Process;
import com.mycompany.pyso.Classes.Interrupts.InterruptHandler;
import com.mycompany.pyso.Interface.UI;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class OperatingSystem {
    private CPU  cpu;
    private RAM  memory;
    private Disk disk;
    private Scheduler scheduler;
    private Dispatcher dispatcher;
    private InterruptHandler interruptHandler;
    private final UI gui;
    private Thread thread;
    private boolean isRunning = false;
    private long simulatorStartMillis;

    public OperatingSystem(UI gui, int memorySize) {
        this.gui = gui;
        initialize(memorySize,512);
    }

    public void reset(int memorySize, int diskSize) {
        stop();
        initialize(memorySize, diskSize);
    }
    private void initialize(int memorySize, int diskSize) {
        this.simulatorStartMillis = System.currentTimeMillis();

        this.cpu = new CPU();
        this.memory = new RAM(memorySize);
        this.disk = new Disk(diskSize);

        this.scheduler = new Scheduler(memory, disk, simulatorStartMillis);

        this.interruptHandler = new InterruptHandler(cpu, disk,
            msg -> {
                if (gui != null) gui.printConsole(msg);
            });

        this.dispatcher = new Dispatcher(cpu, scheduler, interruptHandler, simulatorStartMillis);
    }

    public Process loadProcess(String path) {
        Process process = scheduler.loadProcess(path);
        if (process == null) {
            JOptionPane.showMessageDialog(gui,
                "No se pudo cargar: " + path + "\nVerifique que el archivo sea .asm válido.",
                "Error de carga", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (process.getBcp().getState() == com.mycompany.pyso.Classes.Process.ProcessState.NEW) {
            JOptionPane.showMessageDialog(gui,
                "Proceso '" + process.getName() + "' cargado en cola de trabajo.\n" +
                "No hay espacio en RAM ahora - esperará hasta que se libere.",
                "En espera de RAM", JOptionPane.INFORMATION_MESSAGE);
        }
        gui.printConsole(">> Proceso "+ process.getName()+" cargado");
        return process;
    }

    public void run(boolean stepMode) {
        if (!scheduler.hasProcessReady() && !scheduler.hasNewProcesses()) {
            JOptionPane.showMessageDialog(gui,
                "No hay procesos en estado READY.\nCargue al menos un archivo .asm primero.",
                "Sin procesos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        this.isRunning = true;

        thread = new Thread(() -> {
            while (isRunning) {
                
                scheduler.tryLoadNewProcesses();
                Integer executedPC = dispatcher.CPUcycle();

                SwingUtilities.invokeLater(() -> {
                    gui.refreshAll();

                    if (executedPC != null) {
                        gui.highlightRow(executedPC);
                    } else {
                        memory.updateKernelFromBCP(null);
                    }
                });

                if (scheduler.allTerminated()) {
                    isRunning = false;

                    SwingUtilities.invokeLater(() -> {
                        memory.updateKernelFromBCP(null);
                        gui.refreshAll();
                        JOptionPane.showMessageDialog(gui,
                            "Todos los procesos terminaron.",
                            "Ejecución finalizada", JOptionPane.INFORMATION_MESSAGE);
                    });
                    break;
                }

                try {
                    if (stepMode) break;
                    else Thread.sleep(300);
                } catch (InterruptedException e) {
                    thread.interrupt();
                }
            }
        });

        thread.setDaemon(true);
        thread.start();
    }
    
    public void cpuCycle() {
        if (!scheduler.hasProcessReady()) {
            JOptionPane.showMessageDialog(gui, "No hay procesos en READY");
            return;
        }

        Integer executedPC = dispatcher.CPUcycle();

        SwingUtilities.invokeLater(() -> {
            gui.refreshAll();

            if (executedPC != null) {
                gui.highlightRow(executedPC);
            }
        });

        if (scheduler.allTerminated()) {
            isRunning = false;
        }
    }
    
    

    public void stop() {
        isRunning = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

    public void reset(int memorySize) {
        stop();
        initialize(memorySize,512);
    }

    public CPU getCpu() {
        return cpu;
    }

    public RAM getMemory() {
        return memory;
    }

    public Disk getDisk() {
        return disk;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public InterruptHandler getInterruptHandler() {
        return interruptHandler;
    }

    public UI getGui() {
        return gui;
    }

    public Thread getThread() {
        return thread;
    }

    public boolean isIsRunning() {
        return isRunning;
    }

    public long getSimulatorStartMillis() {
        return simulatorStartMillis;
    }

    
}