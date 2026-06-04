/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pyso.Classes.FileHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import java.io.File;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author jimen
 */
public class LoadXML {

    private static final Logger logger = Logger.getLogger(LoadXML.class.getName());

    private int ramSize= 100;
    private int diskSize= 512; 
    private boolean loadError = false;

    public void readFile(String path) {
        loadError = false;

        if (path.toLowerCase().endsWith(".xml")) {
            readXML(path);
        } else if (path.toLowerCase().endsWith(".json")) {
            readJSON(path);
        } else {
            JOptionPane.showMessageDialog(null,
                "Formato no soportado. Use .xml o .json",
                "Error de configuración",
                JOptionPane.ERROR_MESSAGE);
            loadError = true;
        }
    }
    /**
     * Expected format:
     * <config>
     *     <ram>512</ram>
     *     <disk>512</disk>
     * </config>
     */
    private void readXML(String path) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(path));
            doc.getDocumentElement().normalize();

            NodeList ramNodes  = doc.getElementsByTagName("ram");
            NodeList diskNodes = doc.getElementsByTagName("disk");

            if (ramNodes.getLength() > 0) {
                int parsed = Integer.parseInt(ramNodes.item(0).getTextContent().trim());
                ramSize = validateSize(parsed, "RAM");
            }

            if (diskNodes.getLength() > 0) {
                int parsed = Integer.parseInt(diskNodes.item(0).getTextContent().trim());
                diskSize = validateSize(parsed, "Disk");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al leer XML", e);
            JOptionPane.showMessageDialog(null,
                "Error al leer el archivo XML: " + e.getMessage(),
                "Error de configuración",
                JOptionPane.ERROR_MESSAGE);
            loadError = true;
        }
    }

    /**
     * Expected format:
     * {
     *     "ram": 512,
     *     "disk": 512
     * }
     */
    private void readJSON(String path) {
        try (FileReader reader = new FileReader(path)) {
            StringBuilder sb = new StringBuilder();
            int ch;
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }

            String content = sb.toString();

            Integer parsedRam  = extractJsonInt(content, "ram");
            Integer parsedDisk = extractJsonInt(content, "disk");

            if (parsedRam != null)  ramSize  = validateSize(parsedRam, "RAM");
            if (parsedDisk != null) diskSize = validateSize(parsedDisk, "Disk");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al leer JSON", e);
            JOptionPane.showMessageDialog(null,
                "Error al leer el archivo JSON: " + e.getMessage(),
                "Error de configuración",
                JOptionPane.ERROR_MESSAGE);
            loadError = true;
        }
    }

    private Integer extractJsonInt(String json, String key) {
        String pattern = "\"" + key + "\"";
        int keyIndex = json.indexOf(pattern);
        if (keyIndex == -1) return null;

        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1) return null;

        int start = colonIndex + 1;
        int end   = start;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == ',' || c == '}' || c == '\n') break;
            end++;
        }

        String valueStr = json.substring(start, end).trim();
        try {
            return Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int validateSize(int value, String label) {
        if (value < 100) {
            JOptionPane.showMessageDialog(null,
                label + ": tamaño mínimo es 100. Se usará 100.",
                "Advertencia de configuración",
                JOptionPane.WARNING_MESSAGE);
            return 100;
        }
        if (value > 10000) {
            JOptionPane.showMessageDialog(null,
                label + ": tamaño máximo es 10000. Se usará 10000.",
                "Advertencia de configuración",
                JOptionPane.WARNING_MESSAGE);
            return 10000;
        }
        return value;
    }

    public int getRamSize() {
        return ramSize;
    }

    public void setRamSize(int ramSize) {
        this.ramSize = ramSize;
    }

    public int getDiskSize() {
        return diskSize;
    }

    public void setDiskSize(int diskSize) {
        this.diskSize = diskSize;
    }

    public boolean isLoadError() {
        return loadError;
    }

    public void setLoadError(boolean loadError) {
        this.loadError = loadError;
    }
    
    
}