package com.mycompany.pyso.Interface;

import com.mycompany.pyso.OperatingSystem;
import com.mycompany.pyso.Classes.Process.OSProcess;
import com.mycompany.pyso.Classes.Process.BCP;
import com.mycompany.pyso.Classes.Process.ProcessState;
import com.mycompany.pyso.Classes.Core.CPU;
import com.mycompany.pyso.Classes.Memory.RAM;
import com.mycompany.pyso.Classes.Memory.Disk;
import com.mycompany.pyso.Classes.Memory.MemoryManager;
import com.mycompany.pyso.Classes.Memory.FixedPartitionManager;
import com.mycompany.pyso.Classes.Memory.DynamicPartitionManager;
import com.mycompany.pyso.Classes.Memory.PagingManager;
import com.mycompany.pyso.Classes.Process.Dispatcher;
import com.mycompany.pyso.Scheduler.FCFS;
import com.mycompany.pyso.Scheduler.RoundRobin;
import com.mycompany.pyso.Scheduler.SRR;
import com.mycompany.pyso.Scheduler.SRT;
import com.mycompany.pyso.Scheduler.HRRN;
import com.mycompany.pyso.Scheduler.Lottery;
import com.mycompany.pyso.Scheduler.SJF;
import com.mycompany.pyso.Scheduler.SchedulerSimulation;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    // ── Colores base ──────────────────────────────────────────────────────
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

    // ── Colores brillantes por CPU ────────────────────────────────────────
    private static final Color[] CPU_BRIGHT = {
        new Color(46, 204, 113), new Color(52, 152, 219),
        new Color(241, 196, 15), new Color(155, 89, 182),
    };

    // ── Colores suaves por CPU ────────────────────────────────────────────
    private static final Color[] CPU_SOFT = {
        new Color(210, 255, 230), new Color(210, 232, 255),
        new Color(255, 249, 196), new Color(240, 220, 255),
    };

    // ── Colores para matrices ─────────────────────────────────────────────
    private static final Color[] PROC_COLORS = {
        new Color(46, 204, 113), new Color(52, 152, 219),
        new Color(241, 196, 15), new Color(230, 126, 34),
        new Color(231, 76, 60),  new Color(155, 89, 182),
    };
    private static final Color C_COL_HEADER = new Color(230, 230, 235);
    private static final Color C_PARTITION_SEL_BG   = new Color(255, 200, 200);
    private static final Color C_PARTITION_BORDER   = new Color(180, 180, 180);
    private static final Color C_PARTITION_SEL_LINE = new Color(200, 40, 40);

    // ── Estado ────────────────────────────────────────────────────────────
    private OperatingSystem os;
    private int  highlightedRow     = -1;
    private int  selectedPartitionIndex = -1;
    private boolean stepModeActive  = false;
    private Timer autoExecutionTimer;

    // Real-time execution log
    private final Map<Integer, List<Integer>> executionLog = new LinkedHashMap<>();
    private final Map<Integer, List<Integer>> circularLog = new LinkedHashMap<>();
    private int algoMaxTick = 0;

    // BCP navigation
    private final Map<Integer, DefaultTableModel> bcpModels  = new LinkedHashMap<>();
    private final Map<Integer, JPanel>            bcpCards   = new LinkedHashMap<>();
    private final List<Integer>                   pidOrder   = new ArrayList<>();
    private int currentBCPIndex = 0;
    private CardLayout bcpCardLayout;
    private JPanel     bcpContentPanel;

    // ── Widgets barra ─────────────────────────────────────────────────────
    private JButton btnLoad, btnRun, btnStep, btnStepOnce, btnClear, btnStats;

    // ── Tab Sistema ───────────────────────────────────────────────────────
    private JTable  tblProcess, tblRAM, tblVirtualMem, tblDisk;
    private DefaultTableModel virtualMemoryModel;
    private JPanel  bcpPanel;
    private JButton btnBCPPrev, btnBCPNext;

    // ── Tab CPUs ──────────────────────────────────────────────────────────
    private JPanel cpuTabPanel;
    private final List<JPanel>               cpuCards  = new ArrayList<>();
    private final List<DefaultTableModel>    cpuModels = new ArrayList<>();

    // ── Tab Algoritmos ────────────────────────────────────────────────────
    private JLabel              lblAlgoName;
    private JTable              tblProcessData;
    private DefaultTableModel   processDataModel;
    private JTable              tblExecutionMatrix;
    private DefaultTableModel   executionMatrixModel;
    private JTable              tblCircularMatrix;
    private DefaultTableModel   circularMatrixModel;
    private JPanel              circularMatrixPanel;
    private JTable              tblStats;
    private DefaultTableModel   statsModel;

    // ── Tab Consola ───────────────────────────────────────────────────────
    private JTextPane  consoleOutput;
    private JTextField consoleInput;
    private JButton    btnConsoleSend;

    // ── Tab Configuración ─────────────────────────────────────────────────
    private JComboBox<String> cbAlgo;
    private JSpinner spQuantum, spCPUs, spRAM, spDisk;
    private JButton  btnApplyConfig;

    private JComboBox<String> memoryStrategyCombo;
    private JPanel memoryParamsPanel;
    private CardLayout memoryParamsLayout;

    private JSpinner  fixedEqualNumSpinner, fixedEqualSizeSpinner;
    private JCheckBox fixedEqualSingleQueueCheck;

    private DefaultListModel<String> partitionListModel;
    private JList<String>            partitionList;
    private JSpinner  partitionSizeSpinner;
    private JCheckBox fixedDifferentSingleQueueCheck;

    private JCheckBox autoCompactCheck;
    private JSpinner  fragmentationThresholdSpinner;

    private JComboBox<Integer> pageSizeCombo;
    
    private Timer algorithmRefreshTimer; 

    // ─────────────────────────────────────────────────────────────────────
    public UI() {
        os = new OperatingSystem(this, 512);
        buildFrame();
        refreshAll();
    }

    // ═════════════════════════════════════════════════════════════════════
    // FRAME
    // ═════════════════════════════════════════════════════════════════════
    private void buildFrame() {
        setTitle("Gestor de Procesos y Memoria — Proyecto 2");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1500, 850));
        getContentPane().setBackground(C_BG);
        setLayout(new BorderLayout(0, 0));
        add(buildToolBar(), BorderLayout.NORTH);
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
        btnStepOnce = toolBtn("Step");
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
        tabs.addTab("Sistema",       buildSistemaTab());
        tabs.addTab("CPUs",          buildCPUsTab());
        tabs.addTab("Algoritmos",    buildAlgoritmosTab());
        tabs.addTab("Consola",       buildConsolaTab());
        tabs.addTab("Configuración", buildConfigTab());
        return tabs;
    }

    // ═════════════════════════════════════════════════════════════════════
    // TAB: SISTEMA
    // ═════════════════════════════════════════════════════════════════════
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
        bcpCardLayout   = new CardLayout();
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

        tblRAM.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int clickedRow = tblRAM.rowAtPoint(e.getPoint());
                if (clickedRow < RAM.KERNEL_SIZE) {
                    selectedPartitionIndex = -1;
                    tblRAM.repaint();
                    return;
                }
                MemoryManager mm = os.getScheduler().getMemoryManager();
                if (mm instanceof FixedPartitionManager fpm) {
                    int[] bases = fpm.getPartitionBase();
                    int[] sizes = fpm.getPartitionSizes();
                    selectedPartitionIndex = -1;
                    for (int i = 0; i < bases.length; i++) {
                        if (clickedRow >= bases[i] && clickedRow < bases[i] + sizes[i]) {
                            selectedPartitionIndex = i;
                            break;
                        }
                    }
                } else {
                    selectedPartitionIndex = -1;
                }
                tblRAM.repaint();
            }
        });

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
        JPanel leg = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 4));
        leg.setOpaque(false);
        leg.add(legendDot(C_KERNEL, "Kernel"));
        leg.add(legendDot(C_SWAP,   "Swap / Mem. Virtual"));
        for (int i = 0; i < CPU_BRIGHT.length; i++) {
            leg.add(legendDot(CPU_BRIGHT[i], "CPU " + (i + 1) + " (instrucción activa)"));
            leg.add(legendDot(CPU_SOFT[i],   "CPU " + (i + 1) + " (rango del proceso)"));
        }
        leg.add(legendDot(C_PARTITION_SEL_BG, "Partición seleccionada"));
        return leg;
    }

    private JPanel legendDot(Color c, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JPanel dot = new JPanel();
        dot.setBackground(c);
        dot.setPreferredSize(new Dimension(16, 16));
        dot.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        p.add(dot);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        p.add(l);
        return p;
    }

    // ═════════════════════════════════════════════════════════════════════
    // TAB: CPUs
    // ═════════════════════════════════════════════════════════════════════
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
        String[] fields = {"Proceso", "Estado", "PC", "IR", "AC", "AX", "BX", "CX", "DX", "Progreso", "Proc. ejecutados"};
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

        Color cpuColor = CPU_BRIGHT[(cpuId - 1) % CPU_BRIGHT.length];

        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(C_WHITE);

        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(cpuColor, 2),
            "CPU " + cpuId, TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13), cpuColor);
        card.setBorder(border);
        card.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return card;
    }

    // ═════════════════════════════════════════════════════════════════════
    // TAB: ALGORITMOS
    // ═════════════════════════════════════════════════════════════════════
    private JPanel buildAlgoritmosTab() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(C_BG);
        root.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel north = new JPanel(new BorderLayout(10, 0));
        north.setOpaque(false);
        lblAlgoName = new JLabel("—", SwingConstants.CENTER);
        lblAlgoName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        north.add(lblAlgoName, BorderLayout.CENTER);
        JButton btnSim = toolBtn("▶  Simular");
        btnSim.addActionListener(e -> runAlgoSimulation());
        north.add(btnSim, BorderLayout.EAST);
        root.add(north, BorderLayout.NORTH);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setResizeWeight(0.65);
        mainSplit.setBorder(null);

        JPanel leftColumn = new JPanel(new BorderLayout(0, 10));
        leftColumn.setBackground(C_BG);

        executionMatrixModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return String.class; }
        };
        tblExecutionMatrix = new JTable(executionMatrixModel);
        tblExecutionMatrix.setRowHeight(24);
        tblExecutionMatrix.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tblExecutionMatrix.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tblExecutionMatrix.setDefaultRenderer(Object.class, new ExecutionMatrixRenderer());
        tblExecutionMatrix.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        JScrollPane execScroll = new JScrollPane(tblExecutionMatrix);
        execScroll.setBorder(BorderFactory.createTitledBorder("Ejecución (Gantt)"));
        execScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        leftColumn.add(execScroll, BorderLayout.CENTER);

        circularMatrixModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return String.class; }
        };
        tblCircularMatrix = new JTable(circularMatrixModel);
        tblCircularMatrix.setRowHeight(24);
        tblCircularMatrix.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tblCircularMatrix.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tblCircularMatrix.setDefaultRenderer(Object.class, new CircularMatrixRenderer());
        tblCircularMatrix.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        circularMatrixPanel = new JPanel(new BorderLayout());
        circularMatrixPanel.setBorder(BorderFactory.createTitledBorder("Cola Circular (solo RR)"));
        circularMatrixPanel.add(new JScrollPane(tblCircularMatrix), BorderLayout.CENTER);
        circularMatrixPanel.setVisible(false);
        leftColumn.add(circularMatrixPanel, BorderLayout.SOUTH);

        JPanel rightColumn = new JPanel(new BorderLayout(0, 10));
        rightColumn.setBackground(C_BG);

        processDataModel = new DefaultTableModel(new String[]{"Proceso", "Arrivo", "Ráfaga"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblProcessData = new JTable(processDataModel);
        tblProcessData.setRowHeight(24);
        tblProcessData.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane dataScroll = new JScrollPane(tblProcessData);
        dataScroll.setBorder(BorderFactory.createTitledBorder("Datos de Procesos"));
        dataScroll.setPreferredSize(new Dimension(0, 200));
        rightColumn.add(dataScroll, BorderLayout.CENTER);

        statsModel = new DefaultTableModel(new String[]{"Proceso", "Tf", "Tr", "Tr/Ts"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblStats = new JTable(statsModel);
        tblStats.setRowHeight(24);
        tblStats.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane statsScroll = new JScrollPane(tblStats);
        statsScroll.setBorder(BorderFactory.createTitledBorder("Estadísticas"));
        rightColumn.add(statsScroll, BorderLayout.SOUTH);

        mainSplit.setLeftComponent(leftColumn);
        mainSplit.setRightComponent(rightColumn);
        root.add(mainSplit, BorderLayout.CENTER);
        
        return root;
    }

    // ═════════════════════════════════════════════════════════════════════
    // ALGORITMOS - RENDERERS
    // ═════════════════════════════════════════════════════════════════════
    private class ExecutionMatrixRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            c.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            setHorizontalAlignment(CENTER);
            if (col == 0) {
                c.setBackground(C_COL_HEADER);
                c.setForeground(Color.BLACK);
                c.setFont(new Font("Segoe UI", Font.BOLD, 11));
            } else if ("x".equals(val)) {
                c.setBackground(PROC_COLORS[row % PROC_COLORS.length]);
                c.setForeground(Color.WHITE);
            } else {
                c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);
            }
            return c;
        }
    }

    private class CircularMatrixRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            c.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            setHorizontalAlignment(CENTER);
            if (col == 0) {
                c.setBackground(C_COL_HEADER);
                c.setForeground(Color.BLACK);
                c.setFont(new Font("Segoe UI", Font.BOLD, 11));
            } else if (val != null && !val.toString().isEmpty()) {
                try {
                    int pid = Integer.parseInt(val.toString());
                    int idx = pid - 1;
                    c.setBackground(PROC_COLORS[idx % PROC_COLORS.length]);
                    c.setForeground(Color.WHITE);
                    c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                } catch (NumberFormatException ignored) {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
            } else {
                c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);
            }
            return c;
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // ALGO TAB — Real-time execution log
    // ═════════════════════════════════════════════════════════════════════

    public void resetAlgoLog() {
        executionLog.clear();
        circularLog.clear();
        algoMaxTick = 0;
        for (OSProcess p : os.getScheduler().getJobQueue().getAll()) {
            executionLog.put(p.getPID(), new ArrayList<>());
        }
        if (processDataModel != null) updateProcessTableData();
        if (lblAlgoName != null) lblAlgoName.setText(os.getStrategy().getName());
    }

    public void recordExecutionTick() {
    int tick = os.getGlobalClock();
    if (tick <= 0) return;
    
    if (tick > algoMaxTick) algoMaxTick = tick;
    
    // Registrar qué procesos se ejecutaron en este tick
    boolean hasRunning = false;
    for (CPU cpu : os.getAllCpus()) {
        OSProcess p = cpu.getCurrentProcess();
        if (p != null && p.getPID() >= 0 && p.getState() == ProcessState.RUNNING) {
            hasRunning = true;
            List<Integer> ticks = executionLog.get(p.getPID());
            if (ticks == null) {
                ticks = new ArrayList<>();
                executionLog.put(p.getPID(), ticks);
            }
            if (!ticks.contains(tick)) {
                ticks.add(tick);
                System.out.println("[LOG] Tick " + tick + ": P" + p.getPID() + " ejecutando");
            }
        }
    }
    
    // Si ningún proceso ejecutó, registrar tick vacío (opcional)
    if (!hasRunning && tick > 1) {
        System.out.println("[LOG] Tick " + tick + ": IDLE");
    }
    
    // Cola circular para RR
    if (os.getStrategy().getName().startsWith("RR")) {
        List<Integer> queueSnapshot = new ArrayList<>();
        for (CPU cpu : os.getAllCpus()) {
            OSProcess p = cpu.getCurrentProcess();
            if (p != null && !queueSnapshot.contains(p.getPID())) {
                queueSnapshot.add(p.getPID());
            }
        }
        for (OSProcess p : os.getScheduler().getReadyQueue().getAll()) {
            if (!queueSnapshot.contains(p.getPID())) {
                queueSnapshot.add(p.getPID());
            }
        }
        circularLog.put(tick, queueSnapshot);
    }
}

    private void refreshAlgoritmosTab() {
        List<OSProcess> processes = os.getScheduler().getJobQueue().getAll();
        if (processes.isEmpty()) {
            // Mostrar mensaje en la tabla
            executionMatrixModel.setRowCount(0);
            if (processDataModel != null) processDataModel.setRowCount(0);
            lblAlgoName.setText(os.getStrategy().getName() + " (sin procesos)");
            return;
        }

        // Actualizar datos de procesos
        updateProcessTableData();
        lblAlgoName.setText(os.getStrategy().getName() + " - Tick: " + os.getGlobalClock());

        // Si no hay ejecución activa, mostrar solo los datos
        if (algoMaxTick == 0 && !os.isRunning() && !stepModeActive) {
            return;
        }

        // Construir matriz de ejecución actualizada
        int currentTick = os.getGlobalClock();
        if (currentTick > algoMaxTick) {
            algoMaxTick = currentTick;

            // Reconstruir matriz con el nuevo tamaño
            String[] cols = new String[algoMaxTick + 1];
            cols[0] = "P \\ t";
            for (int t = 1; t <= algoMaxTick; t++) cols[t] = String.valueOf(t);
            executionMatrixModel.setColumnIdentifiers(cols);
            executionMatrixModel.setRowCount(0);

            // Llenar matriz con datos actuales
            for (OSProcess p : processes) {
                List<Integer> ticks = executionLog.getOrDefault(p.getPID(), new ArrayList<>());
                Object[] row = new Object[algoMaxTick + 1];
                row[0] = "P" + p.getPID();
                for (int t = 1; t <= algoMaxTick; t++) {
                    if (ticks.contains(t)) {
                        row[t] = "x";
                    } else if (t == currentTick && p.getState() == ProcessState.RUNNING) {
                        row[t] = "►";  // Indicar proceso actual
                    } else {
                        row[t] = "";
                    }
                }
                executionMatrixModel.addRow(row);
            }

            // Ajustar ancho de columnas
            if (tblExecutionMatrix.getColumnCount() > 0) {
                tblExecutionMatrix.getColumnModel().getColumn(0).setPreferredWidth(50);
                for (int c = 1; c < tblExecutionMatrix.getColumnCount() && c <= 30; c++) {
                    tblExecutionMatrix.getColumnModel().getColumn(c).setPreferredWidth(35);
                }
            }

            // Actualizar estadísticas
            statsModel.setRowCount(0);
            for (OSProcess p : processes) {
                BCP bcp = p.getBcp();
                int end = bcp.getEndTick();
                int arrival = bcp.getArrivalTick();
                int turnaround = (end > 0 && arrival >= 0) ? (end - arrival) : 0;
                double ratio = p.getBurstTime() > 0 ? (double) turnaround / p.getBurstTime() : 0;
                statsModel.addRow(new Object[]{
                    "P" + p.getPID(),
                    end > 0 ? String.valueOf(end) : (p.getState() == ProcessState.RUNNING ? "..." : "—"),
                    turnaround > 0 ? String.valueOf(turnaround) : (p.getState() == ProcessState.RUNNING ? "..." : "—"),
                    String.format("%.2f", ratio)
                });
            }
        }
    }

    private void updateProcessTableData() {
        processDataModel.setRowCount(0);
        for (OSProcess p : os.getScheduler().getJobQueue().getAll()) {
            processDataModel.addRow(new Object[]{
                "P" + p.getPID(), 
                p.getArrivalTick(), 
                p.getBurstTime()
            });
        }
    }

    private void runAlgoSimulation() {
        List<OSProcess> processes = os.getScheduler().getJobQueue().getAll();
        if (processes.isEmpty()) return;

        if (os.isRunning() || algoMaxTick > 0) {
            refreshAlgoritmosTab();
            return;
        }

        List<Integer> arrivals = new ArrayList<>();
        List<Integer> bursts = new ArrayList<>();
        for (OSProcess p : processes) {
            arrivals.add(p.getArrivalTick());
            bursts.add(p.getBurstTime());
        }

        String stratName = os.getStrategy().getName();
        int quantum = 3;
        String algoKey;
        if (stratName.startsWith("FCFS")) algoKey = "FCFS";
        else if (stratName.startsWith("SJF")) algoKey = "SJF";
        else if (stratName.startsWith("SRT")) algoKey = "SRT";
        else if (stratName.startsWith("RR")) {
            algoKey = "RR";
            try {
                quantum = Integer.parseInt(
                    stratName.substring(stratName.indexOf("q=") + 2, stratName.indexOf(")")));
            } catch (Exception ignored) {}
        } else algoKey = "FCFS";

        lblAlgoName.setText(stratName);
        SchedulerSimulation sim = new SchedulerSimulation(arrivals, bursts, algoKey, quantum);
        sim.simulate();
        
        updateProcessTableData();
        updateExecutionMatrixFromSim(sim, processes);
        updateStatsFromSim(sim, processes);
        if (sim.isCircularAlgorithm()) {
            updateCircularMatrixFromSim(sim);
            circularMatrixPanel.setVisible(true);
        } else {
            circularMatrixPanel.setVisible(false);
        }
    }

    private void updateExecutionMatrixFromSim(SchedulerSimulation sim, List<OSProcess> processes) {
        int totalTime = sim.getTotalTime();
        int[][] matrix = sim.getExecutionMatrix();
        String[] cols = new String[totalTime + 1];
        cols[0] = "P \\ t";
        for (int t = 1; t <= totalTime; t++) cols[t] = String.valueOf(t);
        executionMatrixModel.setColumnIdentifiers(cols);
        executionMatrixModel.setRowCount(0);
        for (int i = 0; i < processes.size() && i < matrix.length; i++) {
            Object[] row = new Object[totalTime + 1];
            row[0] = "P" + processes.get(i).getPID();
            for (int t = 0; t < totalTime; t++) {
                row[t + 1] = matrix[i][t] == 1 ? "x" : "";
            }
            executionMatrixModel.addRow(row);
        }
        if (tblExecutionMatrix.getColumnCount() > 0) {
            tblExecutionMatrix.getColumnModel().getColumn(0).setPreferredWidth(50);
            for (int c = 1; c < tblExecutionMatrix.getColumnCount() && c <= 30; c++) {
                tblExecutionMatrix.getColumnModel().getColumn(c).setPreferredWidth(35);
            }
        }
    }

    private void updateCircularMatrixFromSim(SchedulerSimulation sim) {
        int totalTime = sim.getTotalTime();
        int maxRows = sim.getMaxCircularRows();
        int[][] matrix = sim.getCircularMatrix();
        if (matrix == null || maxRows == 0) return;
        String[] cols = new String[totalTime + 1];
        cols[0] = "Posición \\ t";
        for (int t = 1; t <= totalTime; t++) cols[t] = String.valueOf(t);
        circularMatrixModel.setColumnIdentifiers(cols);
        circularMatrixModel.setRowCount(0);
        for (int r = 0; r < maxRows; r++) {
            Object[] row = new Object[totalTime + 1];
            row[0] = "Fila " + (r + 1);
            for (int t = 0; t < totalTime; t++) {
                int proc = matrix[r][t];
                row[t + 1] = proc != -1 ? String.valueOf(proc + 1) : "";
            }
            circularMatrixModel.addRow(row);
        }
        if (tblCircularMatrix.getColumnCount() > 0) {
            tblCircularMatrix.getColumnModel().getColumn(0).setPreferredWidth(55);
            for (int c = 1; c < tblCircularMatrix.getColumnCount() && c <= 30; c++) {
                tblCircularMatrix.getColumnModel().getColumn(c).setPreferredWidth(35);
            }
        }
    }

    private void updateStatsFromSim(SchedulerSimulation sim, List<OSProcess> processes) {
        statsModel.setRowCount(0);
        int[] tf = sim.getCompletionTimes();
        int[] tr = sim.getTurnaroundTimes();
        double[] ratio = sim.getResponseRatios();
        for (int i = 0; i < processes.size() && i < tf.length; i++) {
            statsModel.addRow(new Object[]{
                "P" + processes.get(i).getPID(), tf[i], tr[i],
                String.format("%.2f", ratio[i])
            });
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // TAB: CONSOLA
    // ═════════════════════════════════════════════════════════════════════
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

    // ═════════════════════════════════════════════════════════════════════
    // TAB: CONFIGURACIÓN
    // ═════════════════════════════════════════════════════════════════════
    private JPanel buildConfigTab() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(C_BG);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 20, 10, 20);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridy = 0; gc.gridx = 0; root.add(new JLabel("Algoritmo de CPU:"), gc);
        cbAlgo = new JComboBox<>(new String[]{"FCFS", "RR", "SRR", "SJF", "SRT", "HRRN", "Lottery"});
        cbAlgo.setPreferredSize(new Dimension(150, 25));
        gc.gridx = 1; root.add(cbAlgo, gc);

        gc.gridy = 1; gc.gridx = 0; root.add(new JLabel("Quantum si es RR/SRR:"), gc);
        spQuantum = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        spQuantum.setPreferredSize(new Dimension(80, 25));
        gc.gridx = 1; root.add(spQuantum, gc);

        gc.gridy = 2; gc.gridx = 0; root.add(new JLabel("Numero de CPUs:"), gc);
        spCPUs = new JSpinner(new SpinnerNumberModel(2, 1, 4, 1));
        spCPUs.setPreferredSize(new Dimension(80, 25));
        gc.gridx = 1; root.add(spCPUs, gc);

        gc.gridy = 3; gc.gridx = 0; gc.gridwidth = 2;
        JLabel memTitle = new JLabel("CONFIGURACIÓN DE MEMORIA");
        memTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        memTitle.setForeground(C_TEAL_DARK);
        root.add(memTitle, gc);

        gc.gridy = 4; gc.gridwidth = 1; gc.gridx = 0;
        root.add(new JLabel("Estrategia de Memoria:"), gc);
        memoryStrategyCombo = new JComboBox<>(new String[]{
            "Particiones Fijas (iguales)",
            "Particiones Fijas (diferentes)",
            "Particiones Dinámicas (Best Fit)",
            "Paginación"
        });
        memoryStrategyCombo.setPreferredSize(new Dimension(220, 25));
        memoryStrategyCombo.addActionListener(e -> updateMemoryParamsPanel());
        gc.gridx = 1; root.add(memoryStrategyCombo, gc);

        gc.gridy = 5; gc.gridx = 0; gc.gridwidth = 2; gc.fill = GridBagConstraints.BOTH;
        memoryParamsLayout = new CardLayout();
        memoryParamsPanel = new JPanel(memoryParamsLayout);
        memoryParamsPanel.add(buildFixedEqualPanel(), "FIXED_EQUAL");
        memoryParamsPanel.add(buildFixedDifferentPanel(), "FIXED_DIFFERENT");
        memoryParamsPanel.add(buildDynamicPanel(), "DYNAMIC");
        memoryParamsPanel.add(buildPagingPanel(), "PAGING");
        root.add(memoryParamsPanel, gc);

        gc.gridy = 6; gc.gridwidth = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.gridx = 0;
        root.add(new JLabel("Tamaño RAM:"), gc);
        spRAM = new JSpinner(new SpinnerNumberModel(512, 256, 4096, 64));
        spRAM.setPreferredSize(new Dimension(100, 25));
        gc.gridx = 1; root.add(spRAM, gc);

        gc.gridy = 7; gc.gridx = 0;
        root.add(new JLabel("Tamaño Disco:"), gc);
        spDisk = new JSpinner(new SpinnerNumberModel(512, 256, 4096, 64));
        spDisk.setPreferredSize(new Dimension(100, 25));
        gc.gridx = 1; root.add(spDisk, gc);

        gc.gridy = 8; gc.gridx = 0; gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER; gc.fill = GridBagConstraints.NONE;
        btnApplyConfig = toolBtn("Aplicar configuración");
        btnApplyConfig.addActionListener(e -> doApplyConfig());
        root.add(btnApplyConfig, gc);

        return root;
    }

    private JPanel buildFixedEqualPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        p.setBackground(C_BG);
        p.add(new JLabel("Tamaño de cada partición (bytes):"));
        fixedEqualSizeSpinner = new JSpinner(new SpinnerNumberModel(64, 4, 1024, 4));
        fixedEqualSizeSpinner.setPreferredSize(new Dimension(100, 25));
        p.add(fixedEqualSizeSpinner);
        p.add(Box.createHorizontalStrut(20));
        p.add(new JLabel("(Los procesos grandes ocuparán varias particiones consecutivas)"));
        fixedEqualSingleQueueCheck = new JCheckBox("Cola única (multibloque)", true);
        p.add(fixedEqualSingleQueueCheck);
        return p;
    }

    private JPanel buildFixedDifferentPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 5));
        p.setBackground(C_BG);
        partitionListModel = new DefaultListModel<>();
        partitionList = new JList<>(partitionListModel);
        partitionList.setPreferredSize(new Dimension(200, 100));
        int[] defaults = {32, 64, 128, 256};
        for (int s : defaults) partitionListModel.addElement(s + " bytes");
        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ctrl.add(new JLabel("Tamaño de bloque:"));
        partitionSizeSpinner = new JSpinner(new SpinnerNumberModel(64, 4, 1024, 4));
        ctrl.add(partitionSizeSpinner);
        ctrl.add(new JLabel("bytes"));
        JButton add = new JButton("Agregar bloque");
        JButton rem = new JButton("Remover bloque");
        add.addActionListener(e -> {
            int size = (int) partitionSizeSpinner.getValue();
            partitionListModel.addElement(size + " bytes");
            sortPartitionList();
        });
        rem.addActionListener(e -> {
            int idx = partitionList.getSelectedIndex();
            if (idx != -1) partitionListModel.remove(idx);
        });
        ctrl.add(add);
        ctrl.add(rem);
        fixedDifferentSingleQueueCheck = new JCheckBox("Cola única (multibloque)", true);
        ctrl.add(fixedDifferentSingleQueueCheck);
        p.add(new JScrollPane(partitionList), BorderLayout.CENTER);
        p.add(ctrl, BorderLayout.SOUTH);
        return p;
    }

    private void sortPartitionList() {
        List<Integer> sizes = new ArrayList<>();
        for (int i = 0; i < partitionListModel.size(); i++) {
            String item = partitionListModel.get(i);
            sizes.add(Integer.parseInt(item.replace(" bytes", "")));
        }
        sizes.sort(Integer::compareTo);
        partitionListModel.clear();
        for (int s : sizes) {
            partitionListModel.addElement(s + " bytes");
        }
    }

    private JPanel buildDynamicPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        p.setBackground(C_BG);
        autoCompactCheck = new JCheckBox("Compactación automática", true);
        p.add(autoCompactCheck);
        p.add(new JLabel("Umbral de fragmentación:"));
        fragmentationThresholdSpinner = new JSpinner(new SpinnerNumberModel(30, 0, 100, 5));
        p.add(fragmentationThresholdSpinner);
        p.add(new JLabel("%"));
        return p;
    }

    private JPanel buildPagingPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        p.setBackground(C_BG);
        p.add(new JLabel("Tamaño de página/frame:"));
        Integer[] sizes = {8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096};
        pageSizeCombo = new JComboBox<>(sizes);
        pageSizeCombo.setSelectedItem(64);
        p.add(pageSizeCombo);
        p.add(new JLabel("bytes"));
        return p;
    }

    private void updateMemoryParamsPanel() {
        String sel = (String) memoryStrategyCombo.getSelectedItem();
        if (sel.startsWith("Particiones Fijas (iguales)")) memoryParamsLayout.show(memoryParamsPanel, "FIXED_EQUAL");
        else if (sel.startsWith("Particiones Fijas (diferentes)")) memoryParamsLayout.show(memoryParamsPanel, "FIXED_DIFFERENT");
        else if (sel.startsWith("Particiones Dinámicas")) memoryParamsLayout.show(memoryParamsPanel, "DYNAMIC");
        else if (sel.startsWith("Paginación")) memoryParamsLayout.show(memoryParamsPanel, "PAGING");
    }

    // ═════════════════════════════════════════════════════════════════════
    // ACCIONES
    // ═════════════════════════════════════════════════════════════════════
    
    private void showArrivalDialog(List<OSProcess> processes) {
        String[] cols = {"Proceso", "PID", "Ráfaga (instr.)", "Tiempo de llegada (tick)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 3; }
        };
        for (int i = 0; i < processes.size(); i++) {
            OSProcess p = processes.get(i);
            model.addRow(new Object[]{p.getName(), p.getPID(), p.getBurstTime(), 0});
        }

        JTable tbl = new JTable(model);
        tbl.setRowHeight(26);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        tbl.getColumnModel().getColumn(3).setCellEditor(new SpinnerCellEditor());

        JScrollPane sp = new JScrollPane(tbl);
        sp.setPreferredSize(new Dimension(560, Math.min(300, 40 + processes.size() * 28)));

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JLabel("  Asigne el tiempo de llegada (tick) para cada proceso:"), BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel,
            "Tiempos de Llegada", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            for (OSProcess p : processes) {
                p.setArrivalTick(0);
                p.getBcp().setArrivalTick(0);
            }
            printConsole("[SISTEMA] Tiempos de llegada establecidos a 0 por defecto", new Color(52, 152, 219));
            return;
        }

        if (tbl.isEditing()) tbl.getCellEditor().stopCellEditing();

        for (int i = 0; i < processes.size(); i++) {
            try {
                int tick = Integer.parseInt(model.getValueAt(i, 3).toString().trim());
                if (tick < 0) tick = 0;
                processes.get(i).setArrivalTick(tick);
                processes.get(i).getBcp().setArrivalTick(tick);
                System.out.println("[DEBUG] Proceso " + processes.get(i).getName() + 
                                   " arrivalTick = " + tick);
            } catch (NumberFormatException ignored) {
                processes.get(i).setArrivalTick(i);
                processes.get(i).getBcp().setArrivalTick(i);
            }
        }
        printConsole("[SISTEMA] Tiempos de llegada asignados", new Color(52, 152, 219));
    }

    private class SpinnerCellEditor extends AbstractCellEditor implements TableCellEditor {
        private JSpinner spinner;
        private int currentValue;
        
        public SpinnerCellEditor() {
            spinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
            spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }
        
        @Override
        public Object getCellEditorValue() {
            return spinner.getValue().toString();
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            try {
                currentValue = Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                currentValue = 0;
            }
            spinner.setValue(currentValue);
            return spinner;
        }
    }

    private void doLoad() {
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("ASM files", "asm"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        List<OSProcess> loaded = new ArrayList<>();
        for (File f : fc.getSelectedFiles()) {
            OSProcess p = os.loadProcess(f.getAbsolutePath());
            if (p != null) {
                registerBCPCard(p);
                loaded.add(p);
                System.out.println("[DEBUG] Proceso cargado: " + p.getName() + 
                                   " estado=" + p.getState());
            }
        }
        
        if (!loaded.isEmpty()) {
            showArrivalDialog(loaded);
        }
        
        refreshAll();
    }

    private void doRun() {
        boolean hasProcs = os.getScheduler().getJobQueue().getAll().stream()
            .anyMatch(p -> p.getState() == ProcessState.READY
                       || p.getState() == ProcessState.RUNNING
                       || p.getState() == ProcessState.NEW
                       || p.getState() == ProcessState.WAITING);
        if (!hasProcs) {
            JOptionPane.showMessageDialog(this, "No hay procesos. Cargue archivos ASM.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        resetAlgoLog();
        os.resetGlobalClock();

        // IMPORTANTE: Cargar SOLO procesos con arrivalTick <= 0
        os.getScheduler().tryLoadProcessesReadyAt(0);

        if (os.getScheduler().getReadyQueue().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No hay procesos listos. Verifique los tiempos de llegada o aumente la RAM.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        stepModeActive = false;
        btnStepOnce.setEnabled(false);

        // Detener timers anteriores si existen
        if (autoExecutionTimer != null) autoExecutionTimer.stop();
        if (algorithmRefreshTimer != null) algorithmRefreshTimer.stop();

        // Timer para la ejecución de CPUs
        autoExecutionTimer = new Timer(1000, e -> {
            if (os.getScheduler().allTerminated()) {
                autoExecutionTimer.stop();
                algorithmRefreshTimer.stop();
                btnRun.setEnabled(true); btnStep.setEnabled(true);
                btnClear.setEnabled(true); btnLoad.setEnabled(true);
                printConsole("[SISTEMA] Ejecución finalizada", new Color(241,196,15));
                showStatistics();
                return;
            }
            os.cpuCycle();
            refreshAll();
        });

        // NUEVO: Timer para actualizar la pestaña Algoritmos cada segundo
        algorithmRefreshTimer = new Timer(1000, e -> {
            refreshAlgoritmosTab();
        });

        autoExecutionTimer.start();
        algorithmRefreshTimer.start();

        btnRun.setEnabled(false); btnStep.setEnabled(false); btnLoad.setEnabled(false);
        printConsole("[SISTEMA] Ejecución automática iniciada (tick 0)", new Color(46,204,113));
    }

    private void doEnterStepMode() {
        resetAlgoLog();
        os.resetGlobalClock();
        stepModeActive = true;
        btnStepOnce.setEnabled(true);
        printConsole("[SISTEMA] Modo paso a paso activado (tick 0)", new Color(241,196,15));
    }

    private void doStepOnce() {
        if (!stepModeActive) return;
        if (os.getScheduler().allTerminated()) {
            stepModeActive = false; 
            btnStepOnce.setEnabled(false);
            if (algorithmRefreshTimer != null) algorithmRefreshTimer.stop();
            printConsole("[SISTEMA] Todos los procesos terminaron", new Color(241,196,15));
            showStatistics();
            return;
        }
        os.cpuCycle();
        refreshAll();
        refreshAlgoritmosTab();  // ← Actualizar la pestaña Algoritmos
        printConsole("[SISTEMA] Paso ejecutado — Tick: " + os.getGlobalClock(), new Color(52,152,219));
    }

    private void doClear() {
        if (autoExecutionTimer != null) autoExecutionTimer.stop();
        if (algorithmRefreshTimer != null) algorithmRefreshTimer.stop();
        os.stop();
        os.reset(512);
        clearBCPCards();
        executionLog.clear();
        circularLog.clear();
        algoMaxTick = 0;
        if (executionMatrixModel != null) executionMatrixModel.setRowCount(0);
        if (circularMatrixModel != null) circularMatrixModel.setRowCount(0);
        if (statsModel != null) statsModel.setRowCount(0);
        if (processDataModel != null) processDataModel.setRowCount(0);
        stepModeActive = false;
        highlightedRow = -1;
        selectedPartitionIndex = -1;
        btnStepOnce.setEnabled(false);
        btnRun.setEnabled(true); btnStep.setEnabled(true);
        btnClear.setEnabled(true); btnLoad.setEnabled(true);
        refreshAll();
        printConsole("[SISTEMA] Sistema reiniciado", new Color(46,204,113));
    }

    private void doConsoleSend() {
        String txt = consoleInput.getText().trim();
        if (!txt.isEmpty()) { printConsole(">> " + txt); consoleInput.setText(""); }
    }

    private void doApplyConfig() {
        String algo = (String) cbAlgo.getSelectedItem();
        int quantum = (int) spQuantum.getValue();
        int numCpus = (int) spCPUs.getValue();
        int ramSize = (int) spRAM.getValue();
        int diskSize = (int) spDisk.getValue();

        SchedulerStrategy strat = switch (algo) {
            case "RR" -> new RoundRobin(quantum);
            case "SRR" -> new SRR(quantum);
            case "SJF" -> new SJF();
            case "SRT" -> new SRT();
            case "HRRN" -> new HRRN();
            case "Lottery" -> new Lottery(quantum);
            default -> new FCFS();
        };
        MemoryManager mm = createMemoryManager(ramSize);

        if (autoExecutionTimer != null) autoExecutionTimer.stop();
        os.stop();
        os.reset(ramSize, diskSize, numCpus, strat);
        os.getScheduler().setMemoryManager(mm);
        clearBCPCards();
        rebuildCPUCards(numCpus);
        selectedPartitionIndex = -1;
        lblAlgoName.setText(algo + (("RR".equals(algo) || "SRR".equals(algo)) ? " (q=" + quantum + ")" : ""));
        highlightedRow = -1;
        refreshAll();
        printConsole("[SISTEMA] Config aplicada: " + numCpus + " CPUs, " + strat.getName()
            + ", Mem: " + mm.getStrategyName(), new Color(46,204,113));
        JOptionPane.showMessageDialog(this,
            "Configuración aplicada:\nAlgoritmo: " + algo
            + "\nCPUs: " + numCpus + "\nRAM: " + ramSize + "\nDisco: " + diskSize
            + "\nMemoria: " + mm.getStrategyName(),
            "Config OK", JOptionPane.INFORMATION_MESSAGE);
    }

    private MemoryManager createMemoryManager(int ramSize) {
        String s = (String) memoryStrategyCombo.getSelectedItem();
        return switch (s) {
            case "Particiones Fijas (iguales)" -> {
                int blockSize = (int) fixedEqualSizeSpinner.getValue();
                boolean singleQueue = fixedEqualSingleQueueCheck.isSelected();
                yield new FixedPartitionManager(blockSize, ramSize, singleQueue);
            }
            case "Particiones Fijas (diferentes)" -> {
                int[] sizes = new int[partitionListModel.size()];
                for (int i = 0; i < sizes.length; i++) {
                    String item = partitionListModel.get(i);
                    sizes[i] = Integer.parseInt(item.replace(" bytes", "").trim());
                }
                boolean singleQueue = fixedDifferentSingleQueueCheck.isSelected();
                yield new FixedPartitionManager(sizes, ramSize, singleQueue);
            }
            case "Particiones Dinámicas (Best Fit)" ->
                new DynamicPartitionManager(ramSize,
                    autoCompactCheck.isSelected(),
                    (int) fragmentationThresholdSpinner.getValue());
            case "Paginación" ->
                new PagingManager(ramSize, (int) pageSizeCombo.getSelectedItem());
            default ->
                new FixedPartitionManager(64, ramSize, true);
        };
    }

    // ═════════════════════════════════════════════════════════════════════
    // BCP CARDS
    // ═════════════════════════════════════════════════════════════════════
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
        m.addRow(new Object[]{"PID", bcp.getPID()});
        m.addRow(new Object[]{"Estado", bcp.getState() != null ? bcp.getState().name() : "NEW"});
        m.addRow(new Object[]{"PC", bcp.getPC()});
        m.addRow(new Object[]{"Limit", bcp.getLimitAddress()});
        m.addRow(new Object[]{"IR", bcp.getIR() != null ? bcp.getIR() : ""});
        m.addRow(new Object[]{"AX", bcp.getAX()});
        m.addRow(new Object[]{"BX", bcp.getBX()});
        m.addRow(new Object[]{"CX", bcp.getCX()});
        m.addRow(new Object[]{"DX", bcp.getDX()});
        m.addRow(new Object[]{"Stack", stackStr(bcp)});
        m.addRow(new Object[]{"Next BCP", bcp.getNextBCP() != null
                ? "Addr[" + bcp.getNextBCP().getBaseAddress() + "]" : "(ninguno)"});
        m.addRow(new Object[]{"Arrival Tick", bcp.getArrivalTick()});
        m.addRow(new Object[]{"Start Tick", bcp.getStartTick() != -1 ? bcp.getStartTick() : "--"});
        m.addRow(new Object[]{"End Tick", bcp.getEndTick() != -1 ? bcp.getEndTick() : "--"});
    }

    private String stackStr(BCP bcp) {
        if (bcp.getStack() == null || bcp.getStack().isEmpty()) return "[]";
        int[] v = bcp.getStack().getValues();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < v.length; i++) {
            sb.append(v[i]);
            if (i < v.length - 1) sb.append(",");
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

    // ═════════════════════════════════════════════════════════════════════
    // REFRESH
    // ═════════════════════════════════════════════════════════════════════
    public void refreshAll() {
        refreshProcessTable();
        refreshRAMTable();
        refreshVirtualMemoryTable();
        refreshDiskTable();
        refreshBCPCards();
        refreshCPUCards();
        refreshAlgoritmosTab();
    }

    private void refreshProcessTable() {
        DefaultTableModel m = (DefaultTableModel) tblProcess.getModel();
        m.setRowCount(0);
        for (OSProcess p : os.getScheduler().getJobQueue().getAll()) {
            String st = p.getState() != null ? p.getState().name()
                : (p.getBcp() != null && p.getBcp().getState() != null ? p.getBcp().getState().name() : "NEW");
            m.addRow(new Object[]{p.getPID() + " - " + p.getName(), st});
        }
    }

    private void refreshRAMTable() {
        DefaultTableModel m = (DefaultTableModel) tblRAM.getModel();
        m.setRowCount(0);
        String[] mem = os.getMemory().getMemory();
        for (int i = 0; i < mem.length; i++)
            m.addRow(new Object[]{i, mem[i] != null ? mem[i] : ""});
        tblRAM.repaint();
    }

    private void refreshVirtualMemoryTable() {
        if (virtualMemoryModel == null || os.getDisk() == null) return;
        virtualMemoryModel.setRowCount(0);
        List<Disk.SwapEntry> swapEntries = os.getDisk().getSwapList();
        if (swapEntries.isEmpty())
            virtualMemoryModel.addRow(new Object[]{"—", "(vacío)"});
        else
            for (int i = 0; i < swapEntries.size(); i++)
                virtualMemoryModel.addRow(new Object[]{i, swapEntries.get(i).toString()});
    }

    private void refreshDiskTable() {
        DefaultTableModel m = (DefaultTableModel) tblDisk.getModel();
        m.setRowCount(0);
        String[] storage = os.getDisk().getStorage();
        for (int i = 0; i < storage.length; i++)
            m.addRow(new Object[]{i, storage[i] != null ? storage[i] : ""});
    }

    private void refreshBCPCards() {
        for (OSProcess p : os.getScheduler().getJobQueue().getAll()) {
            DefaultTableModel m = bcpModels.get(p.getBcp().getPID());
            if (m != null) fillBCPModel(m, p.getBcp());
        }
    }

    private void refreshCPUCards() {
        List<CPU> cpuList = os.getAllCpus();
        List<Dispatcher> dispList = os.getDispatchers();
 
        for (int i = 0; i < cpuModels.size(); i++) {
            DefaultTableModel m = cpuModels.get(i);
            CPU cpu = i < cpuList.size() ? cpuList.get(i) : null;
 
            OSProcess p = null;
            if (i < dispList.size()) p = dispList.get(i).getCurrentProcess();
            if (p == null && cpu != null) p = cpu.getCurrentProcess();
 
            setCell(m, 0, (p != null && p.getPID() >= 0) ? p.getPID() : "—");
 
            String estado = "IDLE";
            if (p != null) {
                if (p.getState() != null) estado = p.getState().name();
                else if (p.getBcp() != null && p.getBcp().getState() != null) estado = p.getBcp().getState().name();
                else estado = "RUNNING";
            }
            setCell(m, 1, estado);
 
            setCell(m, 2, cpu != null ? cpu.getPC()  : "—");
            setCell(m, 3, cpu != null ? cpu.getIR()  : "—");
            setCell(m, 4, cpu != null ? cpu.getAC()  : "—");
            setCell(m, 5, cpu != null ? cpu.getAX()  : "—");
            setCell(m, 6, cpu != null ? cpu.getBX()  : "—");
            setCell(m, 7, cpu != null ? cpu.getCX()  : "—");
            setCell(m, 8, cpu != null ? cpu.getDX()  : "—");
 
            int burst  = (p != null) ? p.getBurstTime() : 0;
            int cycles = (p != null && p.getBcp() != null) ? p.getBcp().getCpuCyclesUsed() : 0;
            setCell(m, 9, burst > 0 ? cycles + "/" + burst : "—");
            if (cpu != null) {
                setCell(m, 10, cpu.getProcessesExecuted() + " / " + CPU.MAX_PROCESSES);
            } else {
                setCell(m, 10, "— / " + CPU.MAX_PROCESSES);
            }
        }
    }

    private void setCell(DefaultTableModel m, int row, Object val) {
        if (row < m.getRowCount()) m.setValueAt(val, row, 1);
    }

    public void highlightRow(int row) {
        highlightedRow = row;
        SwingUtilities.invokeLater(() -> {
            if (tblRAM != null) tblRAM.repaint();
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
        sb.append(String.format("%-6s %-15s %-10s %-10s%n",
            "PID", "Nombre", "Duración", "Estado"));
        sb.append("—".repeat(50)).append("\n");

        for (OSProcess p : os.getScheduler().getJobQueue().getAll()) {
            BCP b = p.getBcp();
            String estado = b.getState() != null ? b.getState().name() : "NEW";

            sb.append(String.format("%-6d %-15s %-10d %-10s%n",
                b.getPID(), 
                b.getProcessName().length() > 15 ? b.getProcessName().substring(0, 12) + "..." : b.getProcessName(),
                p.getBurstTime(),
                estado));
        }

        JTextArea ta = new JTextArea(sb.toString());
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setEditable(false);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(500, 320));
        JOptionPane.showMessageDialog(this, sp, "Estadísticas de Procesos", JOptionPane.INFORMATION_MESSAGE);
    }

    // ═════════════════════════════════════════════════════════════════════
    // HELPERS
    // ═════════════════════════════════════════════════════════════════════
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
        ProcessState st = list.get(row).getState();
        if (st == null && list.get(row).getBcp() != null) st = list.get(row).getBcp().getState();
        if (st == null) return C_WHITE;
        return switch (st) {
            case RUNNING -> C_RUNNING;
            case READY -> C_READY;
            case WAITING -> C_WAITING;
            case TERMINATED -> C_TERM;
            default -> C_WHITE;
        };
    }

    private void colorRows(JTable tbl, java.util.function.IntFunction<Color> fn) {
        tbl.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                c.setBackground(fn.apply(row));
                c.setForeground(Color.BLACK);
                return c;
            }
        });
    }

    // ═════════════════════════════════════════════════════════════════════
    // RAM RENDERER
    // ═════════════════════════════════════════════════════════════════════
    private class RAMRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean foc, int row, int col) {
 
            Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            c.setForeground(Color.BLACK);
            if (c instanceof JLabel lbl) {
                lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN));
                lbl.setBorder(new EmptyBorder(0, 3, 0, 3));
            }
 
            if (row < RAM.KERNEL_SIZE) {
                c.setBackground(C_KERNEL);
                return c;
            }
 
            List<CPU> cpuList = os.getAllCpus();
            List<Dispatcher> dispList = os.getDispatchers();
 
            OSProcess[] active = new OSProcess[Math.max(cpuList.size(), dispList.size())];
            for (int i = 0; i < active.length; i++) {
                if (i < dispList.size() && dispList.get(i).getCurrentProcess() != null) {
                    active[i] = dispList.get(i).getCurrentProcess();
                } else if (i < cpuList.size()) {
                    active[i] = cpuList.get(i).getCurrentProcess();
                }
            }
 
            for (int i = 0; i < cpuList.size(); i++) {
                CPU cpu = cpuList.get(i);
                OSProcess proc = active[i];
                if (proc == null) continue;
                int base = proc.getBaseAddress();
                int limit = proc.getLimitAddress();
                if (base < 0 || limit < 0) continue;
                int hl = cpu.getLastExecutedPC();
                if (hl >= 0 && row == hl && hl >= base && hl < limit) {
                    c.setBackground(CPU_BRIGHT[i % CPU_BRIGHT.length]);
                    c.setForeground(Color.WHITE);
                    if (c instanceof JLabel lbl) lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                    return c;
                }
            }
 
            for (int i = 0; i < active.length; i++) {
                OSProcess proc = active[i];
                if (proc == null) continue;
 
                int base = proc.getBaseAddress();
                int limit = proc.getLimitAddress();
                if (base < 0 || limit <= 0 || limit <= base) continue;
 
                boolean isRunning = (proc.getState() == ProcessState.RUNNING);
                if (!isRunning && proc.getBcp() != null) {
                    isRunning = (proc.getBcp().getState() == ProcessState.RUNNING);
                }
                if (!isRunning) {
                    if (i < dispList.size() && proc == dispList.get(i).getCurrentProcess()) {
                        isRunning = true;
                    }
                }
 
                if (isRunning && row >= base && row < limit) {
                    c.setBackground(CPU_SOFT[i % CPU_SOFT.length]);
                    return c;
                }
            }
 
            MemoryManager mm = os.getScheduler().getMemoryManager();
            if (mm instanceof FixedPartitionManager fpm && selectedPartitionIndex >= 0) {
                int[] bases = fpm.getPartitionBase();
                int[] sizes = fpm.getPartitionSizes();
                if (selectedPartitionIndex < bases.length) {
                    int start = bases[selectedPartitionIndex];
                    int end = start + sizes[selectedPartitionIndex];
                    if (row >= start && row < end) {
                        c.setBackground(C_PARTITION_SEL_BG);
                        if (c instanceof JLabel lbl)
                            lbl.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0,
                                C_PARTITION_SEL_LINE));
                        return c;
                    }
                }
            }
 
            if (mm instanceof FixedPartitionManager fpm) {
                int[] bases = fpm.getPartitionBase();
                for (int base : bases) {
                    if (row == base) {
                        if (c instanceof JLabel lbl)
                            lbl.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0,
                                C_PARTITION_BORDER));
                        break;
                    }
                }
            }
 
            String v = os.getMemory().getMemory()[row];
            c.setBackground((v != null && !v.isBlank()) ? C_WHITE : new Color(250, 250, 250));
            return c;
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // RENDERERS AUXILIARES
    // ═════════════════════════════════════════════════════════════════════
    private class VirtualMemoryRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            c.setForeground(Color.BLACK);
            c.setBackground(C_SWAP);
            if (c instanceof JLabel l) l.setFont(l.getFont().deriveFont(Font.ITALIC));
            return c;
        }
    }

    private class DiskRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            c.setForeground(Color.BLACK);
            int idxRes = os.getDisk().getIndexReserved();
            int swapS = os.getDisk().getSwapStartPosition();
            int swapE = swapS + os.getDisk().getSwapSize();
            if (row < idxRes) {
                c.setBackground(C_INDEX);
                if (c instanceof JLabel l) l.setFont(l.getFont().deriveFont(Font.ITALIC));
            } else if (row >= swapS && row < swapE) {
                c.setBackground(C_SWAP);
            } else {
                c.setBackground(row % 2 == 0 ? C_WHITE : new Color(248, 248, 255));
            }
            return c;
        }
    }

    private static class BCPRenderer extends DefaultTableCellRenderer {
        private static final Color R = new Color(123, 245, 184);
        private static final Color Y = new Color(255, 255, 180);
        private static final Color O = new Color(255, 200, 130);
        private static final Color G = new Color(210, 210, 210);

        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            c.setForeground(Color.BLACK);
            if (row == 1 && col == 1) {
                String txt = val == null ? "" : val.toString();
                c.setBackground(switch (txt) {
                    case "RUNNING" -> R;
                    case "READY" -> Y;
                    case "WAITING" -> O;
                    case "TERMINATED" -> G;
                    default -> Color.WHITE;
                });
            } else {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 252));
            }
            return c;
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // ROUNDED BORDER
    // ═════════════════════════════════════════════════════════════════════
    private static class RoundedBorder extends AbstractBorder {
        private final int radius;
        RoundedBorder(int r) { this.radius = r; }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(180, 180, 185));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 10, 4, 10);
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // MAIN
    // ═════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new UI().setVisible(true));
    }
}