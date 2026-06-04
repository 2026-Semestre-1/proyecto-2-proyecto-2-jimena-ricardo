/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.pyso.Classes.Process;
import java.util.List;
/**
 *
 * @author jimen
 */

public interface IProcessQueue {

    void enqueue(Process process);

    Process dequeue();

    boolean hasNext();

    int count();

    List<Process> getAll();

    boolean isEmpty();
}