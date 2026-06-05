package com.mycompany.pyso.Interface;

import com.mycompany.pyso.OperatingSystem;
import com.mycompany.pyso.Classes.Core.CPU;
import com.mycompany.pyso.Classes.Process.OSProcess;
import com.mycompany.pyso.Classes.Process.BCP;
import com.mycompany.pyso.Classes.Process.ProcessState;
import com.mycompany.pyso.Scheduler.FCFS;
import com.mycompany.pyso.Scheduler.RoundRobin;
import com.mycompany.pyso.Scheduler.SchedulerStrategy;
import javax.swing.text.StyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.border.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.swing.Timer;

public class UI extends JFrame {

    private OperatingSystem os;
    private int highlightedRow = -1;
    private static final int KERNEL_SIZE = 150;
    
    // Panel principal con pestañas
    private JTabbedPane tabbedPane;
    
    // ==================== PESTAÑA: SISTEMA ====================
    private JSplitPane systemSplitPane;
    private JTable ramTable;
    private DefaultTableModel ramTableModel;
    private JPanel bcpPanel;
    private CardLayout bcpCardLayout;
    private Map<Integer, JPanel> bcpCards;
    private List<Integer> bcpPidOrder;
    private int currentBcpIndex;
    private JLabel bcpNavLabel;
    
    // ==================== PESTAÑA: PROCESOS ====================
    private JTable processTable;
    private DefaultTableModel processTableModel;
    
    // ==================== PESTAÑA: CPUs ====================
    private JPanel cpusContainer;
    private List<CPUPanel> cpuPanels;
    
    // ==================== PESTAÑA: CONSOLA ====================
    private JTextPane consoleTextPane;
    private JTextField consoleInputField;
    private JButton consoleSendButton;
    private CompletableFuture<Integer> pendingKeyboardInput;
    
    // ==================== PESTAÑA: CONFIGURACION ====================
    private JComboBox<String> schedulerCombo;
    private JSpinner quantumSpinner;
    private JSpinner numCpusSpinner;
    private JSpinner ramSizeSpinner;
    private JSpinner diskSizeSpinner;
    
    // ==================== BARRA DE HERRAMIENTAS ====================
    private JToolBar toolBar;
    private JButton loadButton;
    private JButton executeButton;
    private JButton stepButton;
    private JButton stopButton;
    private JButton clearButton;
    private JButton statsButton;
    private JLabel statusLabel;
    
    // Colores para estados de procesos
    private final Color COLOR_RUNNING = new Color(129, 199, 132);
    private final Color COLOR_READY = new Color(255, 213, 79);
    private final Color COLOR_WAITING = new Color(100, 181, 246);
    private final Color COLOR_TERMINATED = new Color(189, 189, 189);
    private final Color COLOR_KERNEL = new Color(224, 224, 224);
    private final Color COLOR_HIGHLIGHT = new Color(76, 175, 80);
    
    public UI() {
        os = new OperatingSystem(this, 512);
        bcpCards = new LinkedHashMap<>();
        bcpPidOrder = new ArrayList<>();
        cpuPanels = new ArrayList<>();
        pendingKeyboardInput = null;
        
        initUI();
        setupWindow();
        refreshAll();
    }
    
    private void initUI() {
        setTitle("Sistema Operativo Simulado - Proyecto #2");
        setLayout(new BorderLayout());
        
        // Barra de herramientas
        add(createToolBar(), BorderLayout.NORTH);
        
        // Panel de pestañas
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        tabbedPane.addTab("Sistema", createSystemPanel());
        tabbedPane.addTab("Procesos", createProcessesPanel());
        tabbedPane.addTab("CPUs", createCPUsPanel());
        tabbedPane.addTab("Consola", createConsolePanel());
        tabbedPane.addTab("Configuracion", createConfigPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Barra de estado
        add(createStatusBar(), BorderLayout.SOUTH);
    }
    
    private JToolBar createToolBar() {
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(new Color(52, 73, 94));
        toolBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        loadButton = createToolButton("Cargar ASM", new Color(46, 204, 113));
        loadButton.addActionListener(e -> loadAsmFiles());
        
        executeButton = createToolButton("Ejecutar", new Color(52, 152, 219));
        executeButton.addActionListener(e -> startExecution());
        
        stepButton = createToolButton("Paso a paso", new Color(155, 89, 182));
        stepButton.addActionListener(e -> stepOnce());
        
        stopButton = createToolButton("Detener", new Color(231, 76, 60));
        stopButton.addActionListener(e -> stopExecution());
        
        clearButton = createToolButton("Limpiar", new Color(149, 165, 166));
        clearButton.addActionListener(e -> clearAll());
        
        statsButton = createToolButton("Estadisticas", new Color(241, 196, 15));
        statsButton.addActionListener(e -> showStatistics());
        
        toolBar.add(loadButton);
        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(executeButton);
        toolBar.add(stepButton);
        toolBar.add(stopButton);
        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(clearButton);
        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(statsButton);
        
        return toolBar;
    }
    
    private JButton createToolButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private JPanel createStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(236, 240, 241));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        statusLabel = new JLabel("Listo");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(127, 140, 141));
        
        panel.add(statusLabel, BorderLayout.WEST);
        
        return panel;
    }
    
    // ==================== PESTAÑA: SISTEMA (Memoria + BCP) ====================
    
    private JPanel createSystemPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Split pane: izquierda memoria, derecha BCP
        systemSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        systemSplitPane.setDividerLocation(600);
        systemSplitPane.setResizeWeight(0.6);
        
        // Panel izquierdo: Memoria RAM
        JPanel memoryPanel = new JPanel(new BorderLayout());
        memoryPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)), 
            "Memoria RAM", 
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12)
        ));
        
        ramTableModel = new DefaultTableModel(new String[]{"Direccion", "Contenido"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        ramTable = new JTable(ramTableModel);
        ramTable.setFont(new Font("Consolas", Font.PLAIN, 11));
        ramTable.setRowHeight(18);
        ramTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        ramTable.getColumnModel().getColumn(1).setPreferredWidth(400);
        
        // Renderer para colorear filas
        ramTable.setDefaultRenderer(Object.class, new RAMTableCellRenderer());
        
        JScrollPane ramScroll = new JScrollPane(ramTable);
        memoryPanel.add(ramScroll, BorderLayout.CENTER);
        
        // Panel derecho: BCP del proceso actual
        JPanel bcpWrapper = new JPanel(new BorderLayout());
        bcpWrapper.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            "Bloque de Control de Proceso (BCP)",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12)
        ));
        
        bcpCardLayout = new CardLayout();
        bcpPanel = new JPanel(bcpCardLayout);
        bcpPanel.setBackground(Color.WHITE);
        
        // Navegación de BCP
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        navPanel.setBackground(new Color(236, 240, 241));
        
        JButton prevButton = new JButton("< Anterior");
        JButton nextButton = new JButton("Siguiente >");
        bcpNavLabel = new JLabel("Proceso 0 / 0");
        bcpNavLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        prevButton.addActionListener(e -> navigateBCP(-1));
        nextButton.addActionListener(e -> navigateBCP(1));
        
        navPanel.add(prevButton);
        navPanel.add(bcpNavLabel);
        navPanel.add(nextButton);
        
        bcpWrapper.add(bcpPanel, BorderLayout.CENTER);
        bcpWrapper.add(navPanel, BorderLayout.SOUTH);
        
        systemSplitPane.setLeftComponent(new JScrollPane(memoryPanel));
        systemSplitPane.setRightComponent(bcpWrapper);
        
        panel.add(systemSplitPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private class RAMTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                if (row == highlightedRow) {
                    c.setBackground(COLOR_HIGHLIGHT);
                    c.setForeground(Color.WHITE);
                    setFont(getFont().deriveFont(Font.BOLD));
                } else if (row < KERNEL_SIZE) {
                    c.setBackground(COLOR_KERNEL);
                    c.setForeground(new Color(52, 73, 94));
                    setFont(getFont().deriveFont(Font.ITALIC));
                } else {
                    // Verificar si hay un proceso ejecutándose en esta dirección
                    OSProcess runningProcess = getRunningProcess();
                    if (runningProcess != null && 
                        row >= runningProcess.getBaseAddress() && 
                        row < runningProcess.getLimitAddress()) {
                        c.setBackground(COLOR_RUNNING);
                        c.setForeground(Color.BLACK);
                    } else {
                        c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 255));
                        c.setForeground(Color.BLACK);
                    }
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
            }
            return c;
        }
    }
    
    private OSProcess getRunningProcess() {
        for (CPU cpu : os.getAllCpus()) {
            if (cpu.getCurrentProcess() != null && 
                cpu.getCurrentProcess().getState() == ProcessState.RUNNING) {
                return cpu.getCurrentProcess();
            }
        }
        return null;
    }
    
    private void createBCPCard(OSProcess process) {
        int pid = process.getPID();
        if (bcpCards.containsKey(pid)) return;
        
        BCP bcp = process.getBcp();
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(4, 8, 4, 8);
        
        // Título del proceso
        JLabel titleLabel = new JLabel("PID: " + pid + " - " + process.getName());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(41, 128, 185));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(titleLabel, gbc);
        
        // Separador
        JSeparator separator = new JSeparator();
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(separator, gbc);
        gbc.fill = GridBagConstraints.NONE;
        
        // Datos del BCP en dos columnas
        String[][] fields = {
            {"Estado", bcp.getState().name()},
            {"PC", String.valueOf(bcp.getPC())},
            {"IR", bcp.getIR()},
            {"AC", String.valueOf(bcp.getAC())},
            {"AX", String.valueOf(bcp.getAX())},
            {"BX", String.valueOf(bcp.getBX())},
            {"CX", String.valueOf(bcp.getCX())},
            {"DX", String.valueOf(bcp.getDX())},
            {"Base", String.valueOf(bcp.getBaseAddress())},
            {"Limite", String.valueOf(bcp.getLimitAddress())},
            {"Llegada", bcp.formatElapsed(bcp.getArrivalMillis())},
            {"Inicio CPU", bcp.formatElapsed(bcp.getStartMillis())},
            {"Ciclos", String.valueOf(bcp.getCpuCyclesUsed())},
            {"Prioridad", String.valueOf(bcp.getPriority())}
        };
        
        int row = 2;
        for (int i = 0; i < fields.length; i++) {
            gbc.gridy = row;
            gbc.gridx = 0;
            JLabel label = new JLabel(fields[i][0] + ":");
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setForeground(new Color(100, 100, 100));
            card.add(label, gbc);
            
            gbc.gridx = 1;
            JLabel valueLabel = new JLabel(fields[i][1]);
            valueLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
            
            // Colorear según estado
            if (fields[i][0].equals("Estado")) {
                valueLabel.setForeground(getStateColor(bcp.getState()));
                valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            }
            
            card.add(valueLabel, gbc);
            row++;
            
            // Dos columnas
            if (i + 1 < fields.length) {
                i++;
                gbc.gridy = row;
                gbc.gridx = 0;
                label = new JLabel(fields[i][0] + ":");
                label.setFont(new Font("Segoe UI", Font.BOLD, 11));
                label.setForeground(new Color(100, 100, 100));
                card.add(label, gbc);
                
                gbc.gridx = 1;
                valueLabel = new JLabel(fields[i][1]);
                valueLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
                card.add(valueLabel, gbc);
                row++;
            }
        }
        
        bcpCards.put(pid, card);
        bcpPanel.add(card, String.valueOf(pid));
        bcpPidOrder.add(pid);
        
        if (bcpPidOrder.size() == 1) {
            currentBcpIndex = 0;
            updateBCPNavigation();
        }
    }
    
    private void updateBCPCard(OSProcess process) {
        int pid = process.getPID();
        JPanel card = bcpCards.get(pid);
        if (card == null) {
            createBCPCard(process);
            return;
        }
        
        BCP bcp = process.getBcp();
        Component[] components = card.getComponents();
        int valueIndex = 0;
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                String text = label.getText();
                if (text != null && text.contains(":")) {
                    // Es etiqueta de campo, continuar
                    continue;
                } else if (text != null && !text.isEmpty() && !text.contains(":")) {
                    // Es valor
                    String[] values = {
                        bcp.getState().name(), String.valueOf(bcp.getPC()), bcp.getIR(),
                        String.valueOf(bcp.getAC()), String.valueOf(bcp.getAX()), 
                        String.valueOf(bcp.getBX()), String.valueOf(bcp.getCX()), 
                        String.valueOf(bcp.getDX()), String.valueOf(bcp.getBaseAddress()),
                        String.valueOf(bcp.getLimitAddress()), bcp.formatElapsed(bcp.getArrivalMillis()),
                        bcp.formatElapsed(bcp.getStartMillis()), String.valueOf(bcp.getCpuCyclesUsed()),
                        String.valueOf(bcp.getPriority())
                    };
                    if (valueIndex < values.length) {
                        label.setText(values[valueIndex]);
                        if (valueIndex == 0) { // Estado
                            label.setForeground(getStateColor(bcp.getState()));
                        }
                    }
                    valueIndex++;
                }
            }
        }
    }
    
    private void navigateBCP(int direction) {
        if (bcpPidOrder.isEmpty()) return;
        currentBcpIndex = (currentBcpIndex + direction + bcpPidOrder.size()) % bcpPidOrder.size();
        bcpCardLayout.show(bcpPanel, String.valueOf(bcpPidOrder.get(currentBcpIndex)));
        updateBCPNavigation();
    }
    
    private void updateBCPNavigation() {
        if (bcpPidOrder.isEmpty()) {
            bcpNavLabel.setText("Sin procesos");
        } else {
            bcpNavLabel.setText("Proceso " + (currentBcpIndex + 1) + " de " + bcpPidOrder.size());
        }
    }
    
    private Color getStateColor(ProcessState state) {
        return switch (state) {
            case RUNNING -> new Color(46, 204, 113);
            case READY -> new Color(241, 196, 15);
            case WAITING -> new Color(52, 152, 219);
            case TERMINATED -> new Color(149, 165, 166);
            default -> Color.BLACK;
        };
    }
    
    // ==================== PESTAÑA: PROCESOS ====================
    
    private JPanel createProcessesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        processTableModel = new DefaultTableModel(new String[]{"PID", "Nombre", "Estado", "Base", "Limite", "Ciclos", "Prioridad"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        processTable = new JTable(processTableModel);
        processTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        processTable.setRowHeight(24);
        processTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        processTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        processTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        
        // Renderer para colorear estado
        processTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected && column == 2) {
                    String state = (String) value;
                    if (state != null) {
                        switch (state) {
                            case "RUNNING": c.setBackground(COLOR_RUNNING); break;
                            case "READY": c.setBackground(COLOR_READY); break;
                            case "WAITING": c.setBackground(COLOR_WAITING); break;
                            case "TERMINATED": c.setBackground(COLOR_TERMINATED); break;
                            default: c.setBackground(Color.WHITE);
                        }
                    }
                } else if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 252));
                }
                return c;
            }
        });
        
        JScrollPane scroll = new JScrollPane(processTable);
        scroll.setBorder(BorderFactory.createTitledBorder("Lista de Procesos"));
        
        panel.add(scroll, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ==================== PESTAÑA: CPUs ====================
    
    private JPanel createCPUsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        cpusContainer = new JPanel(new GridLayout(2, 2, 15, 15));
        cpusContainer.setBackground(new Color(236, 240, 241));
        
        JScrollPane scroll = new JScrollPane(cpusContainer);
        scroll.setBorder(null);
        
        panel.add(scroll, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void rebuildCPUPanels() {
        cpusContainer.removeAll();
        cpuPanels.clear();
        
        int numCpus = os.getAllCpus().size();
        int cols = Math.min(2, numCpus);
        int rows = (int) Math.ceil((double) numCpus / cols);
        cpusContainer.setLayout(new GridLayout(rows, cols, 15, 15));
        
        for (int i = 0; i < numCpus; i++) {
            CPUPanel cpuPanel = new CPUPanel(i);
            cpuPanels.add(cpuPanel);
            cpusContainer.add(cpuPanel);
        }
        
        cpusContainer.revalidate();
        cpusContainer.repaint();
    }
    
    private void updateCPUPanels() {
        for (int i = 0; i < cpuPanels.size() && i < os.getAllCpus().size(); i++) {
            cpuPanels.get(i).update(os.getCpu(i));
        }
    }
    
    private class CPUPanel extends JPanel {
        private int cpuId;
        private JLabel processNameLabel;
        private JLabel stateLabel;
        private JLabel pcLabel;
        private JLabel irLabel;
        private JLabel acLabel;
        private JLabel axLabel, bxLabel, cxLabel, dxLabel;
        private JProgressBar progressBar;
        
        CPUPanel(int id) {
            this.cpuId = id;
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(4, 8, 4, 8);
            
            // Título
            JLabel titleLabel = new JLabel("CPU " + (id + 1));
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            titleLabel.setForeground(new Color(41, 128, 185));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            add(titleLabel, gbc);
            
            // Separador
            JSeparator sep = new JSeparator();
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(sep, gbc);
            gbc.fill = GridBagConstraints.NONE;
            
            // Datos
            int row = 2;
            processNameLabel = addRow(gbc, row++, "Proceso:");
            stateLabel = addRow(gbc, row++, "Estado:");
            pcLabel = addRow(gbc, row++, "PC:");
            irLabel = addRow(gbc, row++, "IR:");
            acLabel = addRow(gbc, row++, "AC:");
            axLabel = addRow(gbc, row++, "AX:");
            bxLabel = addRow(gbc, row++, "BX:");
            cxLabel = addRow(gbc, row++, "CX:");
            dxLabel = addRow(gbc, row++, "DX:");
            
            // Progress bar
            gbc.gridy = row;
            gbc.gridx = 0;
            add(new JLabel("Progreso:"), gbc);
            progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
            progressBar.setPreferredSize(new Dimension(120, 18));
            gbc.gridx = 1;
            add(progressBar, gbc);
        }
        
        private JLabel addRow(GridBagConstraints gbc, int row, String labelText) {
            gbc.gridy = row;
            gbc.gridx = 0;
            JLabel label = new JLabel(labelText);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setForeground(new Color(100, 100, 100));
            add(label, gbc);
            
            gbc.gridx = 1;
            JLabel valueLabel = new JLabel("-");
            valueLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
            add(valueLabel, gbc);
            
            return valueLabel;
        }
        
        void update(CPU cpu) {
            OSProcess p = cpu.getCurrentProcess();
            if (p != null) {
                processNameLabel.setText(p.getName());
                stateLabel.setText(p.getState().name());
                stateLabel.setForeground(getStateColor(p.getState()));
                pcLabel.setText(String.valueOf(cpu.getPC()));
                irLabel.setText(cpu.getIR());
                acLabel.setText(String.valueOf(cpu.getAC()));
                axLabel.setText(String.valueOf(cpu.getAX()));
                bxLabel.setText(String.valueOf(cpu.getBX()));
                cxLabel.setText(String.valueOf(cpu.getCX()));
                dxLabel.setText(String.valueOf(cpu.getDX()));
                
                int progress = (cpu.getPC() - p.getBaseAddress()) * 100 / p.getInstructions().size();
                progressBar.setValue(Math.min(100, Math.max(0, progress)));
            } else {
                processNameLabel.setText("(idle)");
                stateLabel.setText("IDLE");
                stateLabel.setForeground(new Color(149, 165, 166));
                pcLabel.setText("-");
                irLabel.setText("-");
                acLabel.setText("-");
                axLabel.setText("-");
                bxLabel.setText("-");
                cxLabel.setText("-");
                dxLabel.setText("-");
                progressBar.setValue(0);
            }
        }
    }
    
    // ==================== PESTAÑA: CONSOLA ====================
    
    private JPanel createConsolePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Área de texto
        consoleTextPane = new JTextPane();
        consoleTextPane.setEditable(false);
        consoleTextPane.setBackground(new Color(30, 30, 35));
        consoleTextPane.setForeground(new Color(187, 187, 187));
        consoleTextPane.setFont(new Font("Consolas", Font.PLAIN, 13));
        
        JScrollPane scrollPane = new JScrollPane(consoleTextPane);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Salida del Sistema"));
        
        // Panel de entrada
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Entrada de Usuario (INT 09H)"));
        
        consoleInputField = new JTextField();
        consoleInputField.setFont(new Font("Consolas", Font.PLAIN, 13));
        consoleInputField.addActionListener(e -> sendConsoleInput());
        
        consoleSendButton = new JButton("Enviar");
        consoleSendButton.setBackground(new Color(52, 152, 219));
        consoleSendButton.setForeground(Color.WHITE);
        consoleSendButton.setFocusPainted(false);
        consoleSendButton.addActionListener(e -> sendConsoleInput());
        
        JLabel hintLabel = new JLabel("Ingrese un número entre 0 y 255");
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        hintLabel.setForeground(new Color(149, 165, 166));
        
        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        southPanel.add(inputPanel, BorderLayout.CENTER);
        southPanel.add(hintLabel, BorderLayout.SOUTH);
        
        inputPanel.add(consoleInputField, BorderLayout.CENTER);
        inputPanel.add(consoleSendButton, BorderLayout.EAST);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(southPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void sendConsoleInput() {
        String text = consoleInputField.getText().trim();
        if (text.isEmpty()) return;
        
        try {
            int value = Integer.parseInt(text);
            if (value >= 0 && value <= 255) {
                if (pendingKeyboardInput != null && !pendingKeyboardInput.isDone()) {
                    pendingKeyboardInput.complete(value);
                    pendingKeyboardInput = null;
                }
                consoleInputField.setText("");
                printConsole("[INPUT] Valor recibido: " + value, new Color(46, 204, 113));
            } else {
                printConsole("[ERROR] Valor fuera de rango (0-255): " + value, new Color(231, 76, 60));
            }
        } catch (NumberFormatException e) {
            printConsole("[ERROR] Entrada inválida: '" + text + "' - Ingrese un número", new Color(231, 76, 60));
        }
    }
    
    public void printConsole(String message) {
        printConsole(message, Color.WHITE);
    }
    
    public void printConsole(String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = consoleTextPane.getStyledDocument();
                Style style = consoleTextPane.addStyle("Style", null);
                StyleConstants.setForeground(style, color);
                doc.insertString(doc.getLength(), message + "\n", style);
                consoleTextPane.setCaretPosition(doc.getLength());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    public int waitForKeyInput() {
        printConsole("[INT 09H] Esperando entrada de teclado...", new Color(241, 196, 15));
        try {
            pendingKeyboardInput = new CompletableFuture<>();
            int result = pendingKeyboardInput.join();
            printConsole("[INT 09H] Entrada recibida: " + result, new Color(46, 204, 113));
            return result;
        } catch (Exception e) {
            printConsole("[INT 09H] Error en entrada", new Color(231, 76, 60));
            return 0;
        }
    }
    
    // ==================== PESTAÑA: CONFIGURACION ====================
    
    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(236, 240, 241));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Algoritmo de CPU
        gbc.gridy = 0;
        gbc.gridx = 0;
        panel.add(createLabel("Algoritmo de CPU:"), gbc);
        schedulerCombo = new JComboBox<>(new String[]{"FCFS", "Round Robin"});
        schedulerCombo.setPreferredSize(new Dimension(150, 25));
        gbc.gridx = 1;
        panel.add(schedulerCombo, gbc);
        
        // Quantum
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(createLabel("Quantum (RR):"), gbc);
        quantumSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        quantumSpinner.setPreferredSize(new Dimension(80, 25));
        gbc.gridx = 1;
        panel.add(quantumSpinner, gbc);
        
        // Número de CPUs
        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(createLabel("Numero de CPUs:"), gbc);
        numCpusSpinner = new JSpinner(new SpinnerNumberModel(2, 2, 4, 1));
        numCpusSpinner.setPreferredSize(new Dimension(80, 25));
        gbc.gridx = 1;
        panel.add(numCpusSpinner, gbc);
        
        // Tamaño RAM
        gbc.gridy = 3;
        gbc.gridx = 0;
        panel.add(createLabel("Tamaño RAM:"), gbc);
        ramSizeSpinner = new JSpinner(new SpinnerNumberModel(512, 512, 2048, 64));
        ramSizeSpinner.setPreferredSize(new Dimension(100, 25));
        gbc.gridx = 1;
        panel.add(ramSizeSpinner, gbc);
        
        // Tamaño Disco
        gbc.gridy = 4;
        gbc.gridx = 0;
        panel.add(createLabel("Tamaño Disco:"), gbc);
        diskSizeSpinner = new JSpinner(new SpinnerNumberModel(512, 256, 4096, 128));
        diskSizeSpinner.setPreferredSize(new Dimension(100, 25));
        gbc.gridx = 1;
        panel.add(diskSizeSpinner, gbc);
        
        // Botón aplicar
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton applyButton = new JButton("Aplicar Configuracion");
        applyButton.setBackground(new Color(46, 204, 113));
        applyButton.setForeground(Color.WHITE);
        applyButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        applyButton.setFocusPainted(false);
        applyButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        applyButton.addActionListener(e -> applyConfiguration());
        panel.add(applyButton, gbc);
        
        return panel;
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(52, 73, 94));
        return label;
    }
    
    private void applyConfiguration() {
        int ramSize = (int) ramSizeSpinner.getValue();
        int diskSize = (int) diskSizeSpinner.getValue();
        int numCpus = (int) numCpusSpinner.getValue();
        
        SchedulerStrategy strategy;
        if ("Round Robin".equals(schedulerCombo.getSelectedItem())) {
            strategy = new RoundRobin((int) quantumSpinner.getValue());
        } else {
            strategy = new FCFS();
        }
        
        os.stop();
        os.reset(ramSize, diskSize, numCpus, strategy);
        
        // Limpiar BCPs
        bcpCards.clear();
        bcpPanel.removeAll();
        bcpPidOrder.clear();
        currentBcpIndex = 0;
        
        // Reconstruir CPUs
        rebuildCPUPanels();
        
        refreshAll();
        
        printConsole("[SISTEMA] Configuracion aplicada: " + numCpus + " CPUs, " + strategy.getName(), new Color(46, 204, 113));
        JOptionPane.showMessageDialog(this,
            "Configuracion aplicada:\n" +
            "RAM: " + ramSize + "\n" +
            "Disco: " + diskSize + "\n" +
            "CPUs: " + numCpus + "\n" +
            "Algoritmo: " + strategy.getName(),
            "Configuracion", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // ==================== ACCIONES PRINCIPALES ====================
    
    private void loadAsmFiles() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Archivos ASM (*.asm)", "asm"));
        fc.setMultiSelectionEnabled(true);
        
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        
        int loaded = 0;
        for (File f : fc.getSelectedFiles()) {
            OSProcess p = os.loadProcess(f.getAbsolutePath());
            if (p != null) {
                createBCPCard(p);
                loaded++;
                printConsole("[CARGA] Proceso cargado: " + p.getName(), new Color(52, 152, 219));
            }
        }
        
        refreshAll();
        statusLabel.setText("Cargados " + loaded + " archivos");
        printConsole("[SISTEMA] " + loaded + " proceso(s) cargado(s)", new Color(46, 204, 113));
    }
    
    private void startExecution() {
        if (os.getScheduler().allTerminated()) {
            printConsole("[ERROR] No hay procesos activos para ejecutar", new Color(231, 76, 60));
            JOptionPane.showMessageDialog(this, "No hay procesos activos. Cargue archivos ASM primero.", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        executeButton.setEnabled(false);
        stepButton.setEnabled(false);
        loadButton.setEnabled(false);
        clearButton.setEnabled(false);
        
        statusLabel.setText("Ejecutando...");
        printConsole("[SISTEMA] Iniciando ejecucion automatica", new Color(46, 204, 113));
        
        Timer timer = new Timer(1000, e -> {
            // ✅ CORREGIDO: isRunning() en lugar de isIsRunning()
            if (!os.isRunning()) {
                ((Timer) e.getSource()).stop();
                return;
            }
            
            os.run(true);
            refreshAll();
            
            if (os.getScheduler().allTerminated()) {
                ((Timer) e.getSource()).stop();
                executeButton.setEnabled(true);
                stepButton.setEnabled(true);
                loadButton.setEnabled(true);
                clearButton.setEnabled(true);
                statusLabel.setText("Ejecucion finalizada");
                printConsole("[SISTEMA] Todos los procesos han terminado", new Color(241, 196, 15));
                showStatistics();
            }
        });
        timer.start();
    }
    
    private void stepOnce() {
        if (os.getScheduler().allTerminated()) {
            printConsole("[ERROR] No hay procesos activos", new Color(231, 76, 60));
            return;
        }
        
        os.run(true);
        refreshAll();
        statusLabel.setText("Paso ejecutado - Ciclo: " + os.getGlobalClock());
        
        if (os.getScheduler().allTerminated()) {
            statusLabel.setText("Ejecucion finalizada");
            printConsole("[SISTEMA] Todos los procesos han terminado", new Color(241, 196, 15));
            showStatistics();
        }
    }
    
    private void stopExecution() {
        os.stop();
        executeButton.setEnabled(true);
        stepButton.setEnabled(true);
        loadButton.setEnabled(true);
        clearButton.setEnabled(true);
        statusLabel.setText("Ejecucion detenida");
        printConsole("[SISTEMA] Ejecucion detenida por el usuario", new Color(241, 196, 15));
    }
    
    private void clearAll() {
        stopExecution();
        os.reset(512);
        
        bcpCards.clear();
        bcpPanel.removeAll();
        bcpPidOrder.clear();
        currentBcpIndex = 0;
        
        refreshAll();
        statusLabel.setText("Sistema limpiado");
        printConsole("[SISTEMA] Sistema reiniciado", new Color(46, 204, 113));
    }
    
    private void showStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("        ESTADISTICAS DE EJECUCION       \n");
        sb.append("========================================\n\n");
        
        sb.append(String.format("%-6s %-15s %-12s %-12s %-12s %-10s\n", 
            "PID", "Nombre", "Llegada", "Inicio", "Fin", "Duracion(s)"));
        sb.append("------------------------------------------------------------\n");
        
        for (OSProcess p : os.getScheduler().getJobQueue().getAll()) {
            BCP bcp = p.getBcp();
            long duration = bcp.getDurationSeconds();
            sb.append(String.format("%-6d %-15s %-12s %-12s %-12s %-10d\n",
                bcp.getPID(), bcp.getProcessName(),
                bcp.formatElapsed(bcp.getArrivalMillis()),
                bcp.formatElapsed(bcp.getStartMillis()),
                bcp.formatElapsed(bcp.getEndMillis()),
                duration));
        }
        
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(650, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Estadisticas", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // ==================== ACTUALIZACION DE INTERFAZ ====================
    
    public void refreshAll() {
        SwingUtilities.invokeLater(() -> {
            // Actualizar RAM
            if (ramTable != null && os.getMemory() != null) {
                String[] memory = os.getMemory().getMemory();
                ramTableModel.setRowCount(0);
                for (int i = 0; i < memory.length; i++) {
                    String content = memory[i];
                    if (content == null || content.isEmpty()) {
                        content = "";
                    }
                    ramTableModel.addRow(new Object[]{i, content});
                }
                ramTable.repaint();
            }
            
            // Actualizar tabla de procesos
            if (processTable != null && os.getScheduler() != null) {
                processTableModel.setRowCount(0);
                for (OSProcess p : os.getScheduler().getJobQueue().getAll()) {
                    BCP bcp = p.getBcp();
                    processTableModel.addRow(new Object[]{
                        bcp.getPID(),
                        p.getName(),
                        bcp.getState().name(),
                        bcp.getBaseAddress(),
                        bcp.getLimitAddress(),
                        bcp.getCpuCyclesUsed(),
                        bcp.getPriority()
                    });
                }
            }
            
            // Actualizar BCP cards
            for (OSProcess p : os.getScheduler().getJobQueue().getAll()) {
                updateBCPCard(p);
            }
            
            // Actualizar CPUs
            updateCPUPanels();
            
            // Actualizar navegación
            updateBCPNavigation();
        });
    }
    
    public void highlightRow(int row) {
        this.highlightedRow = row;
        SwingUtilities.invokeLater(() -> {
            if (ramTable != null) {
                ramTable.repaint();
                if (row >= 0) {
                    ramTable.scrollRectToVisible(ramTable.getCellRect(row, 0, true));
                }
            }
        });
    }
    
    private void setupWindow() {
        setSize(1300, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 600));
    }
    
    // ==================== MAIN ====================
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new UI().setVisible(true);
        });
    }
}