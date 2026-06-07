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
import com.mycompany.pyso.Scheduler.FCFS;
import com.mycompany.pyso.Scheduler.RoundRobin;
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

    // ── Color de partición seleccionada ───────────────────────────────────
    private static final Color C_PARTITION_SEL_BG   = new Color(255, 200, 200);
    private static final Color C_PARTITION_BORDER    = new Color(180, 180, 180);
    private static final Color C_PARTITION_SEL_LINE  = new Color(200, 40, 40);

    // ── Estado ────────────────────────────────────────────────────────────
    private OperatingSystem os;
    private int  highlightedRow     = -1;
    private int  selectedPartitionIndex = -1;
    private boolean stepModeActive  = false;
    private Timer autoExecutionTimer;

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
            leg.add(legendDot(CPU_BRIGHT[i], "CPU " + (i + 1) + " activa"));
            leg.add(legendDot(CPU_SOFT[i],   "CPU " + (i + 1) + " rango"));
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

        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(C_BG);

        processDataModel = new DefaultTableModel(new String[]{"Proceso", "Llegada", "Ráfaga"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblProcessData = algoTable(processDataModel, null);
        JPanel p1 = algoPanel("Datos de Procesos", tblProcessData, 100);
        col.add(p1);
        col.add(Box.createVerticalStrut(8));

        executionMatrixModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return String.class; }
        };
        tblExecutionMatrix = algoTable(executionMatrixModel, new ExecutionMatrixRenderer());
        JPanel p2 = algoPanel("Ejecución (Gantt)", tblExecutionMatrix, 250);
        col.add(p2);
        col.add(Box.createVerticalStrut(8));

        circularMatrixModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return String.class; }
        };
        tblCircularMatrix = algoTable(circularMatrixModel, new CircularMatrixRenderer());
        circularMatrixPanel = algoPanel("Cola Circular (solo RR)", tblCircularMatrix, 200);
        circularMatrixPanel.setVisible(false);
        col.add(circularMatrixPanel);

        JScrollPane outerScroll = new JScrollPane(col);
        outerScroll.getVerticalScrollBar().setUnitIncrement(16);
        outerScroll.setBorder(BorderFactory.createEmptyBorder());
        root.add(outerScroll, BorderLayout.CENTER);

        statsModel = new DefaultTableModel(new String[]{"Proceso", "Tf", "Tr", "Tr/Ts"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblStats = new JTable(statsModel);
        tblStats.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblStats.setRowHeight(22);
        JPanel statsWrap = titled("Estadísticas", scroll(tblStats));
        statsWrap.setPreferredSize(new Dimension(230, 0));
        root.add(statsWrap, BorderLayout.EAST);

        return root;
    }

    private JTable algoTable(DefaultTableModel model, TableCellRenderer renderer) {
        JTable t = new JTable(model);
        t.setRowHeight(24);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        t.setShowGrid(true);
        t.setGridColor(new Color(220, 220, 220));
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        if (renderer != null) t.setDefaultRenderer(Object.class, renderer);
        return t;
    }

    private JPanel algoPanel(String title, JTable table, int height) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(C_WHITE);
        panel.setBorder(titledBorder(title));
        panel.setPreferredSize(new Dimension(900, height));
        JScrollPane sp = new JScrollPane(table,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    // ── Process colors shared between Gantt and circular renderers ────────
    private static final Color[] PROC_COLORS = {
        new Color(46, 204, 113), new Color(52, 152, 219),
        new Color(241, 196, 15), new Color(230, 126, 34),
        new Color(231, 76, 60),  new Color(155, 89, 182),
    };
    private static final Color C_COL_HEADER = new Color(230, 230, 235);

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
            } else if ("1".equals(val)) {
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

    // ── Simulation runner ─────────────────────────────────────────────────
    private void runAlgoSimulation() {
        List<OSProcess> processes = os.getScheduler().getJobQueue().getAll();
        if (processes.isEmpty()) {
            return;
        }

        List<Integer> arrivals = new ArrayList<>();
        List<Integer> bursts = new ArrayList<>();
        for (OSProcess p : processes) {
            arrivals.add((int) Math.max(0, p.getBcp().getArrivalMillis() / 1000));
            bursts.add(p.getBurstTime());
        }

        String stratName = os.getStrategy().getName();
        int quantum = 1;
        String algoKey;
        if (stratName.startsWith("FCFS"))    algoKey = "FCFS";
        else if (stratName.startsWith("SJF")) algoKey = "SJF";
        else if (stratName.startsWith("SRT")) algoKey = "SRT";
        else if (stratName.startsWith("RR")) {
            algoKey = "RR";
            try {
                quantum = Integer.parseInt(
                    stratName.substring(stratName.indexOf("q=") + 2, stratName.indexOf(")")));
            } catch (Exception ignored) { quantum = 3; }
        } else algoKey = "FCFS";

        lblAlgoName.setText(stratName);

        SchedulerSimulation sim = new SchedulerSimulation(arrivals, bursts, algoKey, quantum);
        sim.simulate();

        updateProcessTable(processes, arrivals, bursts);
        updateExecutionMatrix(sim);
        updateStatsTable(sim, processes);

        if (sim.isCircularAlgorithm()) {
            updateCircularMatrix(sim);
            circularMatrixPanel.setVisible(true);
        } else {
            circularMatrixPanel.setVisible(false);
        }
        circularMatrixPanel.getParent().revalidate();
    }

    private void updateProcessTable(List<OSProcess> processes,
                                    List<Integer> arrivals, List<Integer> bursts) {
        processDataModel.setRowCount(0);
        for (int i = 0; i < processes.size(); i++) {
            processDataModel.addRow(new Object[]{
                "P" + processes.get(i).getPID(),
                arrivals.get(i),
                bursts.get(i)
            });
        }
    }

    private void updateExecutionMatrix(SchedulerSimulation sim) {
        int totalTime = sim.getTotalTime();
        int numProcesses = sim.getProcessIds().size();
        int[][] matrix = sim.getExecutionMatrix();

        String[] cols = new String[totalTime + 1];
        cols[0] = "P \\ t";
        for (int t = 1; t <= totalTime; t++) cols[t] = String.valueOf(t);

        executionMatrixModel.setColumnIdentifiers(cols);
        executionMatrixModel.setRowCount(0);

        for (int i = 0; i < numProcesses; i++) {
            Object[] row = new Object[totalTime + 1];
            row[0] = "P" + sim.getProcessIds().get(i);
            for (int t = 0; t < totalTime; t++) {
                row[t + 1] = (matrix[i][t] == 1) ? "1" : "";
            }
            executionMatrixModel.addRow(row);
        }

        if (tblExecutionMatrix.getColumnCount() > 0) {
            tblExecutionMatrix.getColumnModel().getColumn(0).setPreferredWidth(55);
            for (int c = 1; c < tblExecutionMatrix.getColumnCount() && c < 50; c++) {
                tblExecutionMatrix.getColumnModel().getColumn(c).setPreferredWidth(35);
            }
        }
    }

    private void updateCircularMatrix(SchedulerSimulation sim) {
        int totalTime = sim.getTotalTime();
        int maxRows = sim.getMaxCircularRows();
        int[][] matrix = sim.getCircularMatrix();
        if (matrix == null || maxRows == 0) return;

        String[] cols = new String[totalTime + 1];
        cols[0] = "Cola \\ t";
        for (int t = 1; t <= totalTime; t++) cols[t] = String.valueOf(t);

        circularMatrixModel.setColumnIdentifiers(cols);
        circularMatrixModel.setRowCount(0);

        for (int r = 0; r < maxRows; r++) {
            Object[] row = new Object[totalTime + 1];
            row[0] = "F" + (r + 1);
            for (int t = 0; t < totalTime; t++) {
                int proc = matrix[r][t];
                row[t + 1] = (proc != -1) ? String.valueOf(proc + 1) : "";
            }
            circularMatrixModel.addRow(row);
        }

        if (tblCircularMatrix.getColumnCount() > 0) {
            tblCircularMatrix.getColumnModel().getColumn(0).setPreferredWidth(60);
            for (int c = 1; c < tblCircularMatrix.getColumnCount() && c < 50; c++) {
                tblCircularMatrix.getColumnModel().getColumn(c).setPreferredWidth(35);
            }
        }
    }

    private void updateStatsTable(SchedulerSimulation sim, List<OSProcess> processes) {
        statsModel.setRowCount(0);
        int[] tf = sim.getCompletionTimes();
        int[] tr = sim.getTurnaroundTimes();
        double[] ratio = sim.getResponseRatios();
        for (int i = 0; i < processes.size(); i++) {
            statsModel.addRow(new Object[]{
                "P" + processes.get(i).getPID(),
                tf[i],
                tr[i],
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
        cbAlgo = new JComboBox<>(new String[]{"FCFS", "RR", "SJF", "SRT", "HRRN", "Lottery"});
        cbAlgo.setPreferredSize(new Dimension(150, 25));
        gc.gridx = 1; root.add(cbAlgo, gc);

        gc.gridy = 1; gc.gridx = 0; root.add(new JLabel("Quantum si es RR:"), gc);
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
        boolean hasProcs = os.getScheduler().getJobQueue().getAll().stream()
            .anyMatch(p -> p.getState() == ProcessState.READY
                       || p.getState() == ProcessState.RUNNING
                       || p.getState() == ProcessState.NEW
                       || p.getState() == ProcessState.WAITING);
        if (!hasProcs) {
            JOptionPane.showMessageDialog(this, "No hay procesos. Cargue archivos ASM.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        os.getScheduler().loadFromSwap();
        os.getScheduler().tryLoadNewProcesses();
        if (os.getScheduler().getReadyQueue().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No hay procesos listos. Aumente el tamaño de RAM en Configuración.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        stepModeActive = false;
        btnStepOnce.setEnabled(false);
        if (autoExecutionTimer != null) autoExecutionTimer.stop();
        autoExecutionTimer = new Timer(1000, e -> {
            if (os.getScheduler().allTerminated()) {
                autoExecutionTimer.stop();
                btnRun.setEnabled(true); btnStep.setEnabled(true);
                btnClear.setEnabled(true); btnLoad.setEnabled(true);
                printConsole("[SISTEMA] Ejecución finalizada", new Color(241,196,15));
                showStatistics();
                return;
            }
            os.cpuCycle();
            refreshAll();
        });
        autoExecutionTimer.start();
        btnRun.setEnabled(false); btnStep.setEnabled(false); btnLoad.setEnabled(false);
        printConsole("[SISTEMA] Ejecución automática iniciada", new Color(46,204,113));
    }

    private void doEnterStepMode() {
        stepModeActive = true;
        btnStepOnce.setEnabled(true);
        printConsole("[SISTEMA] Modo paso a paso activado", new Color(241,196,15));
    }

    private void doStepOnce() {
        if (!stepModeActive) return;
        if (os.getScheduler().allTerminated()) {
            stepModeActive = false; btnStepOnce.setEnabled(false);
            printConsole("[SISTEMA] Todos los procesos terminaron", new Color(241,196,15));
            showStatistics();
            return;
        }
        os.cpuCycle();
        refreshAll();
        printConsole("[SISTEMA] Paso ejecutado — Ciclo: " + os.getGlobalClock(), new Color(52,152,219));
    }

    private void doClear() {
        if (autoExecutionTimer != null) autoExecutionTimer.stop();
        os.stop();
        os.reset(512);
        clearBCPCards();
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
        lblAlgoName.setText(algo + ("RR".equals(algo) ? " (q=" + quantum + ")" : ""));
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
        // ACTUALIZAR LA SIMULACIÓN EN TIEMPO REAL
        runAlgoSimulation();
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
        for (int i = 0; i < cpuModels.size(); i++) {
            DefaultTableModel m = cpuModels.get(i);
            boolean active = i < cpuList.size();
            CPU cpu = active ? cpuList.get(i) : null;
            OSProcess p = cpu != null ? cpu.getCurrentProcess() : null;

            String st = "IDLE";
            if (p != null) st = p.getState() != null ? p.getState().name()
                : (p.getBcp() != null && p.getBcp().getState() != null ? p.getBcp().getState().name() : "RUNNING");

            setCell(m, 0, p != null ? p.getName() + " [" + p.getPID() + "]" : "—");
            setCell(m, 1, st);
            setCell(m, 2, cpu != null ? String.valueOf(cpu.getPC()) : "—");
            setCell(m, 3, cpu != null ? cpu.getIR() : "—");
            setCell(m, 4, cpu != null ? String.valueOf(cpu.getAC()) : "—");
            setCell(m, 5, cpu != null ? String.valueOf(cpu.getAX()) : "—");
            setCell(m, 6, cpu != null ? String.valueOf(cpu.getBX()) : "—");
            setCell(m, 7, cpu != null ? String.valueOf(cpu.getCX()) : "—");
            setCell(m, 8, cpu != null ? String.valueOf(cpu.getDX()) : "—");
            int burst = p != null ? p.getBurstTime() : 0;
            int cycles = p != null && p.getBcp() != null ? p.getBcp().getCpuCyclesUsed() : 0;
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
                if (row >= 0) tblRAM.scrollRectToVisible(tblRAM.getCellRect(row, 0, true));
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
            } catch (BadLocationException ignored) {
            }
        });
    }

    private void showStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-6s %-15s %-12s %-12s %-12s %-10s%n",
            "PID", "Nombre", "Llegada", "Inicio", "Fin", "Dur(s)"));
        sb.append("—".repeat(70)).append("\n");
        for (OSProcess p : os.getScheduler().getJobQueue().getAll()) {
            BCP b = p.getBcp();
            sb.append(String.format("%-6d %-15s %-12s %-12s %-12s %-10d%n",
                b.getPID(), b.getProcessName(),
                b.formatElapsed(b.getArrivalMillis()),
                b.formatElapsed(b.getStartMillis()),
                b.formatElapsed(b.getEndMillis()),
                b.getDurationSeconds()));
        }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setEditable(false);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(640, 320));
        JOptionPane.showMessageDialog(this, sp, "Estadísticas", JOptionPane.INFORMATION_MESSAGE);
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

            for (int i = 0; i < cpuList.size(); i++) {
                CPU cpu = cpuList.get(i);
                if (cpu.getCurrentProcess() == null) continue;
                if (row == cpu.getPC()) {
                    Color bright = CPU_BRIGHT[i % CPU_BRIGHT.length];
                    c.setBackground(bright);
                    c.setForeground(Color.WHITE);
                    if (c instanceof JLabel lbl)
                        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                    return c;
                }
            }

            MemoryManager mm = os.getScheduler().getMemoryManager();
            FixedPartitionManager fpm = (mm instanceof FixedPartitionManager f) ? f : null;
            int partitionOfRow = -1;
            boolean isPartStart = false;

            if (fpm != null) {
                int[] bases = fpm.getPartitionBase();
                int[] sizes = fpm.getPartitionSizes();
                for (int i = 0; i < bases.length; i++) {
                    if (row >= bases[i] && row < bases[i] + sizes[i]) {
                        partitionOfRow = i;
                        isPartStart = (row == bases[i]);
                        break;
                    }
                }
            }

            if (fpm != null && selectedPartitionIndex >= 0 && partitionOfRow == selectedPartitionIndex) {
                c.setBackground(C_PARTITION_SEL_BG);
                if (c instanceof JLabel lbl) {
                    lbl.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, C_PARTITION_SEL_LINE));
                }
                return c;
            }

            for (int i = 0; i < cpuList.size(); i++) {
                CPU cpu = cpuList.get(i);
                OSProcess p = cpu.getCurrentProcess();
                if (p == null) continue;
                if (row >= p.getBaseAddress() && row < p.getLimitAddress()) {
                    c.setBackground(CPU_SOFT[i % CPU_SOFT.length]);
                    if (fpm != null && isPartStart && c instanceof JLabel lbl) {
                        lbl.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, C_PARTITION_BORDER));
                    }
                    return c;
                }
            }

            if (fpm != null && isPartStart && c instanceof JLabel lbl) {
                lbl.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, C_PARTITION_BORDER));
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
        RoundedBorder(int r) {
            this.radius = r;
        }

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
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new UI().setVisible(true));
    }
}