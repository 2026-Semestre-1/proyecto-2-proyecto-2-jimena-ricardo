/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.Memory;

/**
 *
 * @author jimen
 */
public class DiskEntry {
    public final String name;
    public final int address;
    public final int size;

    public DiskEntry(String name, int address, int size) {
        this.name= name;
        this.address= address;
        this.size= size;
    }

    @Override
    public String toString() {
        return name + " @" + address + " (" + size + " instructions)";
    }
}