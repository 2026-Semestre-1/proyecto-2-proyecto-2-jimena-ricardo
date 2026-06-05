/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.pyso.Classes.Memory;

import com.mycompany.pyso.Classes.Process.OSProcess;

/**
 *
 * @author jimen
 */

public interface MemoryManager {
    int allocate(OSProcess process, String[] ram);
    void free(OSProcess process, String[] ram);
    String getStrategyName();
}