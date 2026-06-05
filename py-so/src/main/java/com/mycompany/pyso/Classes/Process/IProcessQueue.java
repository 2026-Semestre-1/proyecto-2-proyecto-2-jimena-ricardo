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

    void enqueue(OSProcess process);

    OSProcess dequeue();

    boolean hasNext();

    int count();

    List<OSProcess> getAll();

    boolean isEmpty();
}