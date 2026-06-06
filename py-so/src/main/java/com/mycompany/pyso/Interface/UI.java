
package com.mycompany.pyso.Interface;

import com.mycompany.pyso.OperatingSystem;
import com.mycompany.pyso.Classes.Process.OSProcess;
import com.mycompany.pyso.Classes.Process.BCP;
import com.mycompany.pyso.Classes.Process.ProcessState;
import com.mycompany.pyso.Classes.Core.CPU;
import com.mycompany.pyso.Classes.Memory.RAM;
import com.mycompany.pyso.Classes.Memory.Disk;
import com.mycompany.pyso.Scheduler.FCFS;
import com.mycompany.pyso.Scheduler.RoundRobin;
import com.mycompany.pyso.Scheduler.SchedulerStrategy;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.text.*;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UI extends JFrame {

    // ── Colors ───────────────────────────────────────────────────────────
    private static final Color C_TEAL      = new Color(82, 176, 176);
    private static final Color C_TEAL_DARK = new Color(55, 140, 140);
    private static final Color C_BG        = new Color(245, 247, 250);
    private static final Color C_WHITE     = Color.WHITE;
    private static final Color C_RUNNING   = new Color(123, 245, 184);
    private static final Color C_READY     = new Color(255, 255, 180);
    private static final Color C_WAITING   = new Color(255, 200, 130);
    private static final Color C_TERM      = new Color(210, 210, 210);
    private static final Color C_KERNEL    = new Color(220, 220, 240);
    private static final Color C_INDEX     = new Color(230, 230, 230);
    private static final Color C_SWAP      = new Color(255, 235, 180);
    private static final Color C_EXEC_HL   = new Color(80, 220, 140);

    // One highlight color per CPU (active instruction row)
    private static final Color[] CPU_COLORS = {
        new Color(80,  220, 140),
        new Color(80,  160, 240),
        new Color(240, 160,  80),
        new Color(220,  80, 220),
    };
    // Lighter tint for the full address range of a running process
    private static final Color[] CPU_COLORS_LIGHT = {
        new Color(210, 255, 230),
        new Color(210, 230, 255),
        new Color(255, 235, 210),
        new Color(245, 210, 245),
    };

    // ── State ────────────────────────────────────────────────────────────
    private OperatingSystem os;
    private int  highlightedRow  = -1;
    private boolean stepModeActive = false;
    private Timer autoExecutionTimer;

    // BCP navigation
    private final Map<Integer, DefaultTableModel> bcpModels  = new LinkedHashMap<>();
    private final Map<Integer, JPanel>            bcpCards   = new LinkedHashMap<>();
    private final List<Integer>                   pidOrder   = new ArrayList<>();
    private int currentBCPIndex = 0;
    private CardLayout bcpCardLayout;
    private JPanel     bcpContentPanel;

    // ── Widgets — top toolbar ────────────────────────────────────────────
    private JButton btnLoad, btnRun, btnStep, btnStepOnce, btnClear, btnStats;

    // ── Tab: Sistema ─────────────────────────────────────────────────────
    private JTable  tblProcess, tblRAM, tblVirtualMem, tblDisk;
    private DefaultTableModel virtualMemoryModel;
    private JPanel  bcpPanel;
    private JButton btnBCPPrev, btnBCPNext;

    // ── Tab: CPUs ────────────────────────────────────────────────────────
    private JPanel cpuTabPanel;
    private final List<JPanel> cpuCards = new ArrayList<>();
    private final List<DefaultTableModel> cpuModels = new ArrayList<>();

    // ── Tab: Algoritmos ──────────────────────────────────────────────────
    private JLabel  lblAlgoName;
    private JTable  tblAlgoHeader;
    private JPanel  ganttPanel;
    private JTable  tblStats;

    // ── Tab: Consola ─────────────────────────────────────────────────────
    private JTextPane consoleOutput;
    private JTextField consoleInput;
    private JButton    btnConsoleSend;

    // ── Tab: Configuracion ───────────────────────────────────────────────
    private JComboBox<String> cbAlgo;
    private JSpinner  spQuantum, spCPUs, spRAM, spDisk;
    private JButton   btnApplyConfig;

    // ─────────────────────────────────────────────────────────────────────
    public UI() {
        os = new OperatingSystem(this, 512);
        buildFrame();
        refreshAll();
    }

    // ══════════════════════════════════════════════════════════════════════
    // FRAME CONSTRUCTION
    // ══════════════════════════════════════════════════════════════════════
    private void buildFrame() {
        setTitle("Gestor de Procesos y Memoria — Proyecto 2");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1500, 850));
        getContentPane().setBackground(C_BG);
        setLayout(new BorderLayout(0, 0));

        add(buildToolBar(),  BorderLayout.NORTH);
        add(buildTabbedPane(), BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildToolBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bar.setBackground(new Color(50, 50, 55));

        btnLoad     = toolBtn("Cargar archivos");
        btnRun      = toolBtn("Ejecutar");
        btnStep     = toolBtn("Paso a paso");
        btnStepOnce = toolBtn("▶");
        btnClear    = toolBtn("Limpiar");
        btnStats    = toolBtn("Estadísticas");

        btnStepOnce.setEnabled(false);

        btnLoad    .addActionListener(e -> doLoad());
        btnRun     .addActionListener(e -> doRun());
        btnStep    .addActionListener(e -> doEnterStepMode());
        btnStepOnce.addActionListener(e -> doStepOnce());
        btnClear   .addActionListener(e -> doClear());
        btnStats   .addActionListener(e -> showStatistics());

        for (JButton b : List.of(btnLoad, btnRun, btnStep, btnStepOnce, btnClear, btnStats))
            bar.add(b);
        return bar;
    }

    private JButton toolBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(C_TEAL);
        b.setForeground(C_WHITE);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        b.setFocusPainted(false);
        b.setBorder(new RoundedBorder(18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JTabbedPane buildTabbedPane() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabs.setBackground(C_BG);

        tabs.addTab("Sistema",        buildSistemaTab());
        tabs.addTab("CPUs",           buildCPUsTab());
        tabs.addTab("Algoritmos",     buildAlgoritmosTab());
        tabs.addTab("Consola",        buildConsolaTab());
        tabs.addTab("Configuración",  buildConfigTab());

        return tabs;
    }

    // ══════════════════════════════════════════════════════════════════════
    // TAB: SISTEMA (5 columnas: Procesos | BCP | RAM | Swap | Disco)
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildSistemaTab() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(C_BG);
        root.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel row = new JPanel(new GridLayout(1, 5, 10, 0));
        row.setOpaque(false);

        row.add(buildProcessPanel());
        row.add(buildBCPPanel());
        row.add(buildRAMPanel());
        row.add(buildVirtualMemoryPanel());
        row.add(buildDiskPanel());
        root.add(row, BorderLayout.CENTER);

        root.add(buildLegend(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildProcessPanel() {
        tblProcess = makeTable(new String[]{"Número", "Estado"});
        colorRows(tblProcess, this::processColor);
        JPanel p = titled("Procesos", scroll(tblProcess));
        p.setPreferredSize(new Dimension(180, 0));
        return p;
    }

    private JPanel buildBCPPanel() {
        bcpPanel = new JPanel(new BorderLayout());
        bcpPanel.setBackground(C_WHITE);
        bcpPanel.setBorder(titledBorder("BCP"));

        bcpCardLayout  = new CardLayout();
        bcpContentPanel = new JPanel(bcpCardLayout);
        bcpPanel.add(bcpContentPanel, BorderLayout.CENTER);

        JPanel nav = new JPanel(new GridLayout(1, 2, 4, 0));
        nav.setBackground(new Color(60, 60, 60));
        btnBCPPrev = navBtn("Anterior");
        btnBCPNext = navBtn("Siguiente");
        btnBCPPrev.addActionListener(e -> moveBCP(-1));
        btnBCPNext.addActionListener(e -> moveBCP(+1));
        nav.add(btnBCPPrev);
        nav.add(btnBCPNext);
        bcpPanel.add(nav, BorderLayout.SOUTH);
        return bcpPanel;
    }

    private JPanel buildRAMPanel() {
        tblRAM = makeTable(new String[]{"Posición", "Valor en memoria"});
        tblRAM.setDefaultRenderer(Object.class, new RAMRenderer());
        return titled("RAM", scroll(tblRAM));
    }

    private JPanel buildVirtualMemoryPanel() {
        virtualMemoryModel = new DefaultTableModel(new String[]{"Posición", "Valor en memoria"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblVirtualMem = new JTable(virtualMemoryModel);
        tblVirtualMem.setFont(new Font("Consolas", Font.PLAIN, 11));
        tblVirtualMem.setRowHeight(18);
        tblVirtualMem.setDefaultRenderer(Object.class, new VirtualMemoryRenderer());
        return titled("Memoria Virtual (Swap)", scroll(tblVirtualMem));
    }

    private JPanel buildDiskPanel() {
        tblDisk = makeTable(new String[]{"Posición", "Valor en memoria"});
        tblDisk.setDefaultRenderer(Object.class, new DiskRenderer());
        return titled("Disco", scroll(tblDisk));
    }

    private JPanel buildLegend() {
        JPanel leg = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        leg.setOpaque(false);
        leg.add(legendDot(C_RUNNING,   "Proceso ejecutando"));
        leg.add(legendDot(C_KERNEL,    "Espacio kernel"));
        leg.add(legendDot(C_WAITING,   "Proceso en espera"));
        leg.add(legendDot(C_SWAP,      "Memoria Virtual (Swap)"));
        return leg;
    }

    private JPanel legendDot(Color c, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JPanel dot = new JPanel();
        dot.setBackground(c);
        dot.setPreferredSize(new Dimension(18, 18));
        dot.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        p.add(dot);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(l);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════
    // TAB: CPUs
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildCPUsTab() {
        cpuTabPanel = new JPanel(new GridLayout(2, 2, 14, 14));
        cpuTabPanel.setBackground(C_BG);
        cpuTabPanel.setBorder(new EmptyBorder(14, 14, 14, 14));
        rebuildCPUCards(2);
        return cpuTabPanel;
    }

    public void rebuildCPUCards(int n) {
        cpuTabPanel.removeAll();
        cpuCards.clear();
        cpuModels.clear();
        for (int i = 0; i < n; i++) {
            JPanel card = buildOneCPUCard(i + 1);
            cpuCards.add(card);
            cpuTabPanel.add(card);
        }
        cpuTabPanel.revalidate();
        cpuTabPanel.repaint();
    }

    private JPanel buildOneCPUCard(int cpuId) {
        DefaultTableModel model = new DefaultTableModel(new String[]{"", ""}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        String[] fields = {"Proceso", "Estado", "PC", "IR", "AC", "AX", "BX", "CX", "DX", "Progreso"};
        for (String f : fields) model.addRow(new Object[]{f, "—"});
        cpuModels.add(model);

        JTable tbl = new JTable(model);
        tbl.setRowHeight(22);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl.getColumnModel().getColumn(0).setPreferredWidth(70);
        tbl.getColumnModel().getColumn(0).setMaxWidth(90);
        tbl.setShowGrid(true);
        tbl.setGridColor(new Color(220, 220, 220));
        tbl.getTableHeader().setVisible(false);

        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(C_WHITE);
        card.setBorder(titledBorder("CPU " + cpuId));
        card.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════
    // TAB: ALGORITMOS
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildAlgoritmosTab() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(C_BG);
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        lblAlgoName = new JLabel("FCFS", SwingConstants.CENTER);
        lblAlgoName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        root.add(lblAlgoName, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);

        tblAlgoHeader = makeTable(new String[]{"", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"});
        tblAlgoHeader.setRowHeight(28);
        DefaultTableModel hModel = (DefaultTableModel) tblAlgoHeader.getModel();
        hModel.addRow(new Object[]{"Proceso", "", "", "", "", "", "", "", "", "", ""});
        hModel.addRow(new Object[]{"Arrivo", "", "", "", "", "", "", "", "", "", ""});
        hModel.addRow(new Object[]{"Ráfaga", "", "", "", "", "", "", "", "", "", ""});
        center.add(new JScrollPane(tblAlgoHeader), BorderLayout.NORTH);

        ganttPanel = new JPanel();
        ganttPanel.setBackground(new Color(200, 210, 230));
        ganttPanel.setPreferredSize(new Dimension(0, 200));
        ganttPanel.setBorder(titledBorder("Ejecución"));
        center.add(ganttPanel, BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);

        tblStats = makeTable(new String[]{"Tf", "Tr", "Tr/Ts"});
        tblStats.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblStats.setRowHeight(24);
        JPanel statsWrap = titled("Estadísticas", scroll(tblStats));
        statsWrap.setPreferredSize(new Dimension(220, 0));
        root.add(statsWrap, BorderLayout.EAST);

        return root;
    }

    // ══════════════════════════════════════════════════════════════════════
    // TAB: CONSOLA
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildConsolaTab() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(C_BG);
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        consoleOutput = new JTextPane();
        consoleOutput.setEditable(false);
        consoleOutput.setBackground(new Color(22, 22, 26));
        consoleOutput.setForeground(C_WHITE);
        consoleOutput.setFont(new Font("Consolas", Font.PLAIN, 14));
        JScrollPane sp = new JScrollPane(consoleOutput);
        sp.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 70), 2));
        root.add(sp, BorderLayout.CENTER);

        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setOpaque(false);
        JLabel lbl = new JLabel("Entrada");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        consoleInput = new JTextField();
        consoleInput.setFont(new Font("Consolas", Font.PLAIN, 13));
        consoleInput.setBorder(new RoundedBorder(14));
        btnConsoleSend = toolBtn("Enviar");
        btnConsoleSend.addActionListener(e -> doConsoleSend());
        consoleInput.addActionListener(e -> doConsoleSend());

        inputRow.add(lbl, BorderLayout.WEST);
        inputRow.add(consoleInput, BorderLayout.CENTER);
        inputRow.add(btnConsoleSend, BorderLayout.EAST);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(inputRow, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);

        return root;
    }

    // ══════════════════════════════════════════════════════════════════════
    // TAB: CONFIGURACIÓN
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildConfigTab() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(C_BG);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 20, 10, 20);
        gc.anchor = GridBagConstraints.EAST;

        cbAlgo    = new JComboBox<>(new String[]{"FCFS", "RR"});
        spQuantum = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        spCPUs    = new JSpinner(new SpinnerNumberModel(2, 1, 4, 1));
        spRAM     = new JSpinner(new SpinnerNumberModel(512, 256, 2048, 64));
        spDisk    = new JSpinner(new SpinnerNumberModel(512, 256, 2048, 64));
        btnApplyConfig = toolBtn("Aplicar configuración");
        btnApplyConfig.addActionListener(e -> doApplyConfig());

        String[] labels = {"Algoritmo de CPU", "Quantum si es RR", "Numero de CPUs", "Tamaño RAM", "Tamaño Disco"};
        Component[] inputs = {cbAlgo, spQuantum, spCPUs, spRAM, spDisk};

        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.fill = GridBagConstraints.NONE;
            JLabel l = new JLabel(labels[i]);
            l.setFont(new Font("Segoe UI", Font.BOLD, 15));
            root.add(l, gc);
            gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
            Component inp = inputs[i];
            if (inp instanceof JComponent jc) {
                jc.setPreferredSize(new Dimension(260, 36));
                jc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            }
            root.add(inp, gc);
        }

        gc.gridx = 0; gc.gridy = labels.length; gc.gridwidth = 2;
        gc.fill = GridBagConstraints.NONE; gc.anchor = GridBagConstraints.CENTER;
        root.add(btnApplyConfig, gc);

        return root;
    }

    // ══════════════════════════════════════════════════════════════════════
    // ACTION HANDLERS
    // ══════════════════════════════════════════════════════════════════════
    private void doLoad() {
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("ASM files", "asm"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        for (File f : fc.getSelectedFiles()) {
            OSProcess p = os.loadProcess(f.getAbsolutePath());
            if (p != null) registerBCPCard(p);
        }
        refreshAll();
    }

    private void doRun() {
        // Verificar si hay procesos para ejecutar
        boolean hasProcesses = false;
        for (OSProcess p : os.getScheduler().getJobQueue().getAll()) {
            if (p.getState() == ProcessState.READY || 
                p.getState() == ProcessState.RUNNING ||
                p.getState() == ProcessState.NEW ||
                p.getState() == ProcessState.WAITING) {
                hasProcesses = true;
                break;
            }
        }
        
        if (!hasProcesses) {
            JOptionPane.showMessageDialog(this, 
                "No hay procesos para ejecutar.\nCargue archivos ASM primero.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Forzar carga de procesos desde swap antes de ejecutar
        os.getScheduler().loadFromSwap();
        os.getScheduler().tryLoadNewProcesses();
        
        if (os.getScheduler().getReadyQueue().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No hay procesos listos en RAM.\n" +
                "Los procesos estan en swap. Aumente el tamaño de la memoria RAM en Configuración.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        stepModeActive = false;
        btnStepOnce.setEnabled(false);
        
        if (autoExecutionTimer != null) autoExecutionTimer.stop();
        autoExecutionTimer = new Timer(1000, e -> {
            if (os.getScheduler().allTerminated()) {
                autoExecutionTimer.stop();
                btnRun.setEnabled(true);
                btnStep.setEnabled(true);
                btnClear.setEnabled(true);
                btnLoad.setEnabled(true);
                printConsole("[SISTEMA] Ejecución finalizada", new Color(241, 196, 15));
                showStatistics();
                return;
            }
            os.cpuCycle();
            refreshAll();
        });
        autoExecutionTimer.start();
        
        btnRun.setEnabled(false);
        btnStep.setEnabled(false);
        btnClear.setEnabled(false);
        btnLoad.setEnabled(false);
        printConsole("[SISTEMA] Iniciando ejecución automática", new Color(46, 204, 113));
    }

    private void doEnterStepMode() {
        if (os.getScheduler().getReadyQueue().isEmpty() && !os.getScheduler().hasNewProcesses()) {
            JOptionPane.showMessageDialog(this, 
                "No hay procesos listos para ejecutar.\nCargue archivos ASM primero.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        stepModeActive = true;
        btnStepOnce.setEnabled(true);
        printConsole("[SISTEMA] Modo paso a paso activado", new Color(241, 196, 15));
    }

    private void doStepOnce() {
        if (!stepModeActive) return;
        if (os.getScheduler().allTerminated()) {
            stepModeActive = false;
            btnStepOnce.setEnabled(false);
            printConsole("[SISTEMA] Todos los procesos terminaron", new Color(241, 196, 15));
            showStatistics();
            return;
        }
        os.cpuCycle();
        refreshAll();
        printConsole("[SISTEMA] Paso ejecutado - Ciclo: " + os.getGlobalClock(), new Color(52, 152, 219));
    }

    private void doClear() {
        if (autoExecutionTimer != null) autoExecutionTimer.stop();
        os.stop();
        os.reset(512);
        clearBCPCards();
        stepModeActive = false;
        highlightedRow = -1;
        btnStepOnce.setEnabled(false);
        btnRun.setEnabled(true);
        btnStep.setEnabled(true);
        btnClear.setEnabled(true);
        btnLoad.setEnabled(true);
        refreshAll();
        printConsole("[SISTEMA] Sistema reiniciado", new Color(46, 204, 113));
    }

    private void doConsoleSend() {
        String txt = consoleInput.getText().trim();
        if (!txt.isEmpty()) {
            printConsole(">> " + txt);
            consoleInput.setText("");
        }
    }

    private void doApplyConfig() {
        String algo   = (String) cbAlgo.getSelectedItem();
        int quantum   = (int) spQuantum.getValue();
        int numCpus   = (int) spCPUs.getValue();
        int ramSize   = (int) spRAM.getValue();
        int diskSize  = (int) spDisk.getValue();

        SchedulerStrategy strat = switch (algo) {
            case "RR"  -> new RoundRobin(quantum);
            default    -> new FCFS();
        };

        if (autoExecutionTimer != null) autoExecutionTimer.stop();
        os.stop();
        os.reset(ramSize, diskSize, numCpus, strat);
        clearBCPCards();
        rebuildCPUCards(numCpus);
        lblAlgoName.setText(algo + (algo.equals("RR") ? " (q=" + quantum + ")" : ""));
        highlightedRow = -1;
        refreshAll();

        printConsole("[SISTEMA] Configuración aplicada: " + numCpus + " CPUs, " + strat.getName(), new Color(46, 204, 113));
        JOptionPane.showMessageDialog(this,
            "Configuración aplicada:\nAlgoritmo: " + algo +
            "\nCPUs: " + numCpus + "\nRAM: " + ramSize + "\nDisco: " + diskSize,
            "Config OK", JOptionPane.INFORMATION_MESSAGE);
    }

    // ══════════════════════════════════════════════════════════════════════
    // BCP CARD MANAGEMENT
    // ══════════════════════════════════════════════════════════════════════
    public void registerBCPCard(OSProcess process) {
        int pid = process.getBcp().getPID();
        if (bcpModels.containsKey(pid)) return;

        DefaultTableModel model = new DefaultTableModel(new String[]{"Campo", "Valor"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        fillBCPModel(model, process.getBcp());

        JTable tbl = new JTable(model);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tbl.setRowHeight(18);
        tbl.getColumnModel().getColumn(0).setPreferredWidth(90);
        tbl.getColumnModel().getColumn(0).setMaxWidth(110);
        tbl.setShowGrid(true);
        tbl.setGridColor(new Color(220, 220, 220));
        tbl.setDefaultRenderer(Object.class, new BCPRenderer());

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        header.setBackground(C_TEAL);
        JLabel title = new JLabel("PID " + pid + " — " + process.getName());
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(C_WHITE);
        header.add(title);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(C_WHITE);
        card.add(header, BorderLayout.NORTH);
        card.add(new JScrollPane(tbl), BorderLayout.CENTER);

        bcpModels.put(pid, model);
        bcpCards.put(pid, card);
        pidOrder.add(pid);
        bcpContentPanel.add(card, String.valueOf(pid));
        currentBCPIndex = pidOrder.size() - 1;
        showBCPCard();
    }

    private void fillBCPModel(DefaultTableModel m, BCP bcp) {
        m.setRowCount(0);
        m.addRow(new Object[]{"PID",        bcp.getPID()});
        m.addRow(new Object[]{"Estado",     bcp.getState() != null ? bcp.getState().name() : "NEW"});
        m.addRow(new Object[]{"PC",         bcp.getPC()});
        m.addRow(new Object[]{"Limit",      bcp.getLimitAddress()});
        m.addRow(new Object[]{"IR",         bcp.getIR() != null ? bcp.getIR() : ""});
        m.addRow(new Object[]{"AX",         bcp.getAX()});
        m.addRow(new Object[]{"BX",         bcp.getBX()});
        m.addRow(new Object[]{"CX",         bcp.getCX()});
        m.addRow(new Object[]{"DX",         bcp.getDX()});
        m.addRow(new Object[]{"Stack",      stackStr(bcp)});
        m.addRow(new Object[]{"Next BCP",   bcp.getNextBCP() != null
                ? "Addr[" + bcp.getNextBCP().getBaseAddress() + "]" : "(ninguno)"});
    }

    private String stackStr(BCP bcp) {
        if (bcp.getStack() == null || bcp.getStack().isEmpty()) return "[]";
        int[] v = bcp.getStack().getValues();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < v.length; i++) { 
            sb.append(v[i]); 
            if (i < v.length-1) sb.append(","); 
        }
        return sb.append("]").toString();
    }

    private void moveBCP(int delta) {
        if (pidOrder.isEmpty()) return;
        currentBCPIndex = (currentBCPIndex + delta + pidOrder.size()) % pidOrder.size();
        showBCPCard();
    }

    private void showBCPCard() {
        if (pidOrder.isEmpty()) return;
        bcpCardLayout.show(bcpContentPanel, String.valueOf(pidOrder.get(currentBCPIndex)));
    }

    private void clearBCPCards() {
        bcpModels.clear(); 
        bcpCards.clear(); 
        pidOrder.clear(); 
        currentBCPIndex = 0;
        bcpContentPanel.removeAll();
        bcpContentPanel.revalidate(); 
        bcpContentPanel.repaint();
    }

    // ══════════════════════════════════════════════════════════════════════
    // REFRESH
    // ══════════════════════════════════════════════════════════════════════
    public void refreshAll() {
        refreshProcessTable();
        refreshRAMTable();
        refreshVirtualMemoryTable();
        refreshDiskTable();
        refreshBCPCards();
        refreshCPUCards();
    }

    private void refreshProcessTable() {
        DefaultTableModel m = (DefaultTableModel) tblProcess.getModel();
        m.setRowCount(0);
        for (OSProcess p : os.getScheduler().getJobQueue().getAll()) {
            String stateName = "NEW";
            if (p.getState() != null) {
                stateName = p.getState().name();
            } else if (p.getBcp() != null && p.getBcp().getState() != null) {
                stateName = p.getBcp().getState().name();
            }
            m.addRow(new Object[]{p.getPID() + " - " + p.getName(), stateName});
        }
    }

    private void refreshRAMTable() {
        DefaultTableModel m = (DefaultTableModel) tblRAM.getModel();
        m.setRowCount(0);
        String[] mem = os.getMemory().getMemory();
        for (int i = 0; i < mem.length; i++) {
            String content = mem[i] != null ? mem[i] : "";
            m.addRow(new Object[]{i, content});
        }
        tblRAM.repaint();
    }

    private void refreshVirtualMemoryTable() {
        if (virtualMemoryModel == null || os.getDisk() == null) return;
        virtualMemoryModel.setRowCount(0);
        // Show the logical swap entries (what processes are in swap)
        List<Disk.SwapEntry> swapEntries = os.getDisk().getSwapList();
        if (swapEntries.isEmpty()) {
            virtualMemoryModel.addRow(new Object[]{"—", "(vacío)"});
        } else {
            for (int i = 0; i < swapEntries.size(); i++) {
                Disk.SwapEntry e = swapEntries.get(i);
                virtualMemoryModel.addRow(new Object[]{i, e.toString()});
            }
        }
    }

    private void refreshDiskTable() {
        DefaultTableModel m = (DefaultTableModel) tblDisk.getModel();
        m.setRowCount(0);
        String[] storage = os.getDisk().getStorage();
        for (int i = 0; i < storage.length; i++) {
            String content = storage[i] != null ? storage[i] : "";
            m.addRow(new Object[]{i, content});
        }
    }

    private void refreshBCPCards() {
        for (OSProcess p : os.getScheduler().getJobQueue().getAll()) {
            DefaultTableModel m = bcpModels.get(p.getBcp().getPID());
            if (m != null) fillBCPModel(m, p.getBcp());
        }
    }

    private void refreshCPUCards() {
        List<CPU> cpuList = os.getAllCpus();
        for (int i = 0; i < cpuModels.size(); i++) {
            DefaultTableModel m = cpuModels.get(i);
            boolean active = i < cpuList.size();
            CPU cpu = active ? cpuList.get(i) : null;
            OSProcess p = (cpu != null) ? cpu.getCurrentProcess() : null;

            setCell(m, 0, p != null ? p.getName() + " [" + p.getPID() + "]" : "—");
            
            String stateName = "IDLE";
            if (p != null) {
                if (p.getState() != null) {
                    stateName = p.getState().name();
                } else if (p.getBcp() != null && p.getBcp().getState() != null) {
                    stateName = p.getBcp().getState().name();
                }
            }
            setCell(m, 1, stateName);
            setCell(m, 2, cpu != null ? String.valueOf(cpu.getPC()) : "—");
            setCell(m, 3, cpu != null ? cpu.getIR() : "—");
            setCell(m, 4, cpu != null ? String.valueOf(cpu.getAC()) : "—");
            setCell(m, 5, cpu != null ? String.valueOf(cpu.getAX()) : "—");
            setCell(m, 6, cpu != null ? String.valueOf(cpu.getBX()) : "—");
            setCell(m, 7, cpu != null ? String.valueOf(cpu.getCX()) : "—");
            setCell(m, 8, cpu != null ? String.valueOf(cpu.getDX()) : "—");
            int burst = (p != null) ? p.getBurstTime() : 0;
            int cycles = (p != null && p.getBcp() != null) ? p.getBcp().getCpuCyclesUsed() : 0;
            setCell(m, 9, burst > 0 ? cycles + "/" + burst : "—");
        }
    }

    private void setCell(DefaultTableModel m, int row, Object val) {
        if (row < m.getRowCount()) m.setValueAt(val, row, 1);
    }

    public void highlightRow(int row) {
        highlightedRow = row;
        SwingUtilities.invokeLater(() -> {
            if (tblRAM != null) {
                tblRAM.repaint();
                if (row >= 0) {
                    tblRAM.scrollRectToVisible(tblRAM.getCellRect(row, 0, true));
                }
            }
        });
    }

    public void printConsole(String msg) { 
        printConsole(msg, C_WHITE); 
    }
    
    public void printConsole(String msg, Color color) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = consoleOutput.getStyledDocument();
                Style style = consoleOutput.addStyle("s", null);
                StyleConstants.setForeground(style, color);
                doc.insertString(doc.getLength(), msg + "\n", style);
                consoleOutput.setCaretPosition(doc.getLength());
            } catch (BadLocationException ignored) {}
        });
    }

    private void showStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("        ESTADISTICAS DE EJECUCION       \n");
        sb.append("========================================\n\n");
        
        sb.append(String.format("%-6s %-15s %-12s %-12s %-12s %-10s\n", 
            "PID", "Nombre", "Llegada", "Inicio", "Fin", "Dur(s)"));
        sb.append("------------------------------------------------------------\n");
        
        for (OSProcess p : os.getScheduler().getJobQueue().getAll()) {
            BCP b = p.getBcp();
            long duration = b.getDurationSeconds();
            sb.append(String.format("%-6d %-15s %-12s %-12s %-12s %-10d\n",
                b.getPID(), b.getProcessName(),
                b.formatElapsed(b.getArrivalMillis()),
                b.formatElapsed(b.getStartMillis()),
                b.formatElapsed(b.getEndMillis()),
                duration));
        }
        
        JTextArea ta = new JTextArea(sb.toString());
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setEditable(false);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(640, 320));
        JOptionPane.showMessageDialog(this, sp, "Estadísticas", JOptionPane.INFORMATION_MESSAGE);
    }

    // ══════════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════════
    private JTable makeTable(String[] cols) {
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(m);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setRowHeight(20);
        t.setShowGrid(true);
        t.setGridColor(new Color(220, 220, 220));
        return t;
    }

    private JScrollPane scroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createEmptyBorder());
        return sp;
    }

    private JPanel titled(String title, JComponent content) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_WHITE);
        p.setBorder(titledBorder(title));
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    private TitledBorder titledBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 210), 1),
            title, TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13), new Color(60, 60, 70));
    }

    private JButton navBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(C_TEAL_DARK);
        b.setForeground(C_WHITE);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        return b;
    }

    private Color processColor(int row) {
        List<OSProcess> list = os.getScheduler().getJobQueue().getAll();
        if (row >= list.size()) return C_WHITE;
        ProcessState state = list.get(row).getState();
        if (state == null && list.get(row).getBcp() != null) {
            state = list.get(row).getBcp().getState();
        }
        if (state == null) return C_WHITE;
        return switch (state) {
            case RUNNING    -> C_RUNNING;
            case READY      -> C_READY;
            case WAITING    -> C_WAITING;
            case TERMINATED -> C_TERM;
            default         -> C_WHITE;
        };
    }

    private void colorRows(JTable tbl, java.util.function.IntFunction<Color> colorFn) {
        tbl.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                c.setBackground(colorFn.apply(row));
                c.setForeground(Color.BLACK);
                return c;
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    // CUSTOM RENDERERS
    // ══════════════════════════════════════════════════════════════════════
    private class RAMRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            c.setForeground(Color.BLACK);
            if (c instanceof JLabel l) l.setFont(l.getFont().deriveFont(Font.PLAIN));

            // Kernel area — fixed purple tint
            if (row < RAM.KERNEL_SIZE) {
                c.setBackground(C_KERNEL);
                return c;
            }

            // Check every active CPU
            List<CPU> cpuList = os.getAllCpus();
            for (int i = 0; i < cpuList.size(); i++) {
                CPU cpu = cpuList.get(i);
                OSProcess p = cpu.getCurrentProcess();
                if (p == null) continue;

                // Currently-executing instruction: bright color + bold
                if (row == cpu.getPC()) {
                    c.setBackground(CPU_COLORS[i % CPU_COLORS.length]);
                    c.setForeground(Color.WHITE);
                    if (c instanceof JLabel l) l.setFont(l.getFont().deriveFont(Font.BOLD));
                    return c;
                }

                // Full address range of the running process: light tint
                if (row >= p.getBaseAddress() && row < p.getLimitAddress()) {
                    c.setBackground(CPU_COLORS_LIGHT[i % CPU_COLORS_LIGHT.length]);
                    return c;
                }
            }

            // Default: occupied vs empty
            String v = os.getMemory().getMemory()[row];
            c.setBackground((v != null && !v.isBlank()) ? C_WHITE : new Color(250, 250, 250));
            return c;
        }
    }

    private class VirtualMemoryRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            c.setForeground(Color.BLACK);
            c.setBackground(C_SWAP);
            if (c instanceof JLabel l) l.setFont(l.getFont().deriveFont(Font.ITALIC));
            return c;
        }
    }

    private class DiskRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            c.setForeground(Color.BLACK);
            if (row < os.getDisk().getIndexReserved()) {
                c.setBackground(C_INDEX);
                if (c instanceof JLabel l) l.setFont(l.getFont().deriveFont(Font.ITALIC));
            } else {
                c.setBackground(row % 2 == 0 ? C_WHITE : new Color(248, 248, 255));
            }
            return c;
        }
    }

    private static class BCPRenderer extends DefaultTableCellRenderer {
        private static final Color C_RUNNING = new Color(123, 245, 184);
        private static final Color C_READY   = new Color(255, 255, 180);
        private static final Color C_WAITING = new Color(255, 200, 130);
        private static final Color C_TERM    = new Color(210, 210, 210);
        
        @Override public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            c.setForeground(Color.BLACK);
            if (row == 1 && col == 1) {
                String txt = val == null ? "" : val.toString();
                c.setBackground(switch (txt) {
                    case "RUNNING"    -> C_RUNNING;
                    case "READY"      -> C_READY;
                    case "WAITING"    -> C_WAITING;
                    case "TERMINATED" -> C_TERM;
                    default           -> Color.WHITE;
                });
            } else {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 252));
            }
            return c;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // ROUNDED BORDER
    // ══════════════════════════════════════════════════════════════════════
    private static class RoundedBorder extends AbstractBorder {
        private final int radius;
        RoundedBorder(int r) { this.radius = r; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(180, 180, 185));
            g2.drawRoundRect(x, y, w-1, h-1, radius, radius);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(4, 10, 4, 10); }
    }

    // ══════════════════════════════════════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName()); 
                    break;
                }
            }
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new UI().setVisible(true));
    }
}