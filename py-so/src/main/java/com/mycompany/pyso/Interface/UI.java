package com.mycompany.pyso.Interface;
 
import com.mycompany.pyso.OperatingSystem;
import com.mycompany.pyso.Classes.Process.Process;
import com.mycompany.pyso.Classes.Process.BCP;
import com.mycompany.pyso.Classes.Process.ProcessState;
import static com.mycompany.pyso.Classes.Process.ProcessState.READY;
import static com.mycompany.pyso.Classes.Process.ProcessState.RUNNING;
import static com.mycompany.pyso.Classes.Process.ProcessState.TERMINATED;
import static com.mycompany.pyso.Classes.Process.ProcessState.WAITING;
import com.mycompany.pyso.Classes.FileHandler.LoadXML;
import javax.swing.text.StyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
 
public class UI extends javax.swing.JFrame {
 
    private OperatingSystem os;
    private int highlightedRow = -1;
    private static final int KERNEL_SIZE = 20;
    private static final java.util.logging.Logger logger =
        java.util.logging.Logger.getLogger(UI.class.getName());
    private final Map<Integer, DefaultTableModel> bcpTableModels = new LinkedHashMap<>();
    private final Map<Integer, JPanel>            bcpTablePanels = new LinkedHashMap<>();
    private boolean stepModeActive = false;
 
    private CardLayout cardLayout;
    private JPanel bcpContentPanel;
    private List<Integer> pidOrder = new ArrayList<>();
    private int currentIndex = 0;
    private JButton prevButton = new JButton("<");
    private JButton nextButton = new JButton(">");
    private enum RowKind { KERNEL, INSTR, EMPTY }
 
    /** Returns the display kind for a RAM row index. */
    private RowKind rowKind(int row) {
        if (row < KERNEL_SIZE) return RowKind.KERNEL;
        String[] mem = os.getMemory().getMemory();
        if (row >= mem.length) return RowKind.EMPTY;
        String val = mem[row];
        if (val == null || val.isBlank()) return RowKind.EMPTY;
        return RowKind.INSTR;
    }
    
    public UI() {
        os = new OperatingSystem(this, 100);
        initComponents();
        setupRenderer();
        initBCPScrollPanel();
        stepButton.setEnabled(false);
        setLocationRelativeTo(null);
        initBCPNavigation();
        setResizable(false);
        this.setSize(this.getPreferredSize());
        this.setMinimumSize(this.getPreferredSize());
        this.setMaximumSize(this.getPreferredSize());
        bcpPanel.setPreferredSize(new Dimension(240, 350));
        bcpPanel.setMinimumSize(new Dimension(240, 350));
        bcpPanel.setMaximumSize(new Dimension(240, 350));
        consoleTextPane.setEditable(true);
        consoleTextPane.requestFocusInWindow();
    }
    
    private void initBCPScrollPanel() {
        bcpPanel.setLayout(new BorderLayout());
        cardLayout      = new CardLayout();
        bcpContentPanel = new JPanel(cardLayout);
        bcpPanel.add(bcpContentPanel, BorderLayout.CENTER);
        bcpPanel.revalidate();
        bcpPanel.repaint();
    }
 
    private void initBCPNavigation() {
        prevButton = new JButton("<");
        nextButton = new JButton(">");
        prevButton.setFocusable(false);
        nextButton.setFocusable(false);
        styleNavButton(prevButton);
        styleNavButton(nextButton);
 
        prevButton.addActionListener(e -> {
            if (pidOrder.isEmpty()) return;
            currentIndex = (currentIndex - 1 + pidOrder.size()) % pidOrder.size();
            showCurrentBCP();
        });
        nextButton.addActionListener(e -> {
            if (pidOrder.isEmpty()) return;
            currentIndex = (currentIndex + 1) % pidOrder.size();
            showCurrentBCP();
        });
 
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(new Color(60, 60, 60));
        navPanel.add(prevButton, BorderLayout.WEST);
        navPanel.add(nextButton, BorderLayout.EAST);
        bcpPanel.add(navPanel, BorderLayout.SOUTH);
        bcpPanel.revalidate();
        bcpPanel.repaint();
    }
 
    private void styleNavButton(JButton b) {
        b.setBackground(new Color(82, 176, 176));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBorderPainted(false);
        b.setFocusPainted(false);
    }
 
    private void showCurrentBCP() {
        if (pidOrder.isEmpty()) return;
        cardLayout.show(bcpContentPanel, String.valueOf(pidOrder.get(currentIndex)));
    }
 
    public void registerBCPTable(Process process) {
        int pid = process.getBcp().getPID();
        if (bcpTableModels.containsKey(pid)) return;
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        header.setBackground(new Color(82, 176, 176));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        JLabel title = new JLabel("PID " + pid + "  —  " + process.getName());
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(Color.WHITE);
        header.add(title);

        DefaultTableModel model = new DefaultTableModel(new String[]{"Campo", "Valor"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        populateBCPModel(model, process.getBcp());
 
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table.setRowHeight(18);
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(0).setMaxWidth(110);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.setShowGrid(true);
        table.setGridColor(new Color(220, 220, 220));
 
        table.setDefaultRenderer(Object.class,
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                    Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                    c.setForeground(Color.BLACK);
                    if (row == 2 && col == 1) { // Estado row
                        String txt = val == null ? "" : val.toString();
                        c.setBackground(switch (txt) {
                            case "RUNNING"    -> new Color(123, 245, 184);
                            case "READY"      -> new Color(255, 255, 180);
                            case "WAITING"    -> new Color(255, 200, 130);
                            case "TERMINATED" -> new Color(210, 210, 210);
                            default           -> Color.WHITE;
                        });
                    } else {
                        c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 252));
                    }
                    return c;
                }
            });
 
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
 
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.add(header, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
 
        bcpTableModels.put(pid, model);
        bcpTablePanels.put(pid, card);
        pidOrder.add(pid);
 
        bcpContentPanel.add(card, String.valueOf(pid));
        // Show the newest card automatically
        currentIndex = pidOrder.size() - 1;
        showCurrentBCP();
 
        bcpPanel.revalidate();
        bcpPanel.repaint();
    }
 
    private void populateBCPModel(DefaultTableModel model, BCP bcp) {
        model.setRowCount(0);
        model.addRow(new Object[]{"PID",        bcp.getPID()});
        model.addRow(new Object[]{"Nombre",     bcp.getProcessName()});
        model.addRow(new Object[]{"Estado",     bcp.getState().name()});
        model.addRow(new Object[]{"PC",         bcp.getPC()});
        model.addRow(new Object[]{"IR",         bcp.getIR()});
        model.addRow(new Object[]{"AC",         bcp.getAC()});
        model.addRow(new Object[]{"AX",         bcp.getAX()});
        model.addRow(new Object[]{"BX",         bcp.getBX()});
        model.addRow(new Object[]{"CX",         bcp.getCX()});
        model.addRow(new Object[]{"DX",         bcp.getDX()});
        model.addRow(new Object[]{"Base",       bcp.getBaseAddress()});
        model.addRow(new Object[]{"Límite",     bcp.getLimitAddress()});
        model.addRow(new Object[]{"Llegada",    bcp.formatElapsed(bcp.getArrivalMillis())});
        model.addRow(new Object[]{"Inicio CPU", bcp.formatElapsed(bcp.getStartMillis())});
        model.addRow(new Object[]{"Fin",        bcp.formatElapsed(bcp.getEndMillis())});
        model.addRow(new Object[]{"Ciclos",     bcp.getCpuCyclesUsed()});
        model.addRow(new Object[]{"Prioridad",  bcp.getPriority()});
    }
 
    public void refreshAllBCPTables() {
        for (Process p : os.getScheduler().getJobQueue().getAll()) {
            DefaultTableModel model = bcpTableModels.get(p.getPID());
            if (model != null) populateBCPModel(model, p.getBcp());
        }
    }
 
    private void clearBCPTables() {
        bcpTableModels.clear();
        bcpTablePanels.clear();
        pidOrder.clear();
        currentIndex = 0;
        bcpPanel.removeAll();
        initBCPScrollPanel();
        initBCPNavigation();
        bcpPanel.revalidate();
        bcpPanel.repaint();
    }
    
    public void loadMemoryTable() {
        DefaultTableModel model = (DefaultTableModel) MemoryValueTable.getModel();
        String[] memory = os.getMemory().getMemory();
        model.setRowCount(0);
        for (int i = 0; i < memory.length; i++)
            model.addRow(new Object[]{i, memory[i] != null ? memory[i] : ""});
    }
 
    public void loadDiskTable() {
        DefaultTableModel model = (DefaultTableModel) DiskValueTable.getModel();
        String[] storage = os.getDisk().getStorage();
        model.setRowCount(0);
        for (int i = 0; i < storage.length; i++)
            model.addRow(new Object[]{i, storage[i] != null ? storage[i] : ""});
    }
 
    public void loadProcessTable() {
        DefaultTableModel model = (DefaultTableModel) ProcessTable.getModel();
        model.setRowCount(0);
        for (Process p : os.getScheduler().getJobQueue().getAll())
            model.addRow(new Object[]{
                p.getPID() + " - " + p.getName(),
                p.getBcp().getState().name()});
    }
 
    public void highlightRow(int row) {
        highlightedRow = row;

        SwingUtilities.invokeLater(() -> {
            MemoryValueTable.repaint();
            MemoryValueTable.scrollRectToVisible(
                MemoryValueTable.getCellRect(row, 0, true)
            );
        });
    }
 
    public void printConsole(String value) {
        printConsole(value, Color.WHITE);
    }
    public void printConsole(String value, Color color) {
        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = consoleTextPane.getStyledDocument();

            Style style = consoleTextPane.addStyle("Style", null);
            StyleConstants.setForeground(style, color);

            try {
                doc.insertString(doc.getLength(), value + "\n", style);
            } catch (Exception e) {
                e.printStackTrace();
            }

            consoleTextPane.setCaretPosition(doc.getLength());
        });
    }
 
    public void refreshAll() {
        loadMemoryTable();
        loadDiskTable();
        loadProcessTable();
        refreshAllBCPTables();
    }
    
    private void showStatistics() {
        List<Process> processes = os.getScheduler().getJobQueue().getAll();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-6s %-15s %-12s %-12s %-12s %-10s%n",
            "PID","Nombre","Llegada","Inicio","Fin","Duración(s)"));
        sb.append("-".repeat(70)).append("\n");
        for (Process p : processes) {
            BCP bcp = p.getBcp();
            sb.append(String.format("%-6d %-15s %-12s %-12s %-12s %-10d%n",
                bcp.getPID(), bcp.getProcessName(),
                bcp.formatElapsed(bcp.getArrivalMillis()),
                bcp.formatElapsed(bcp.getStartMillis()),
                bcp.formatElapsed(bcp.getEndMillis()),
                bcp.getDurationSeconds()));
        }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setEditable(false);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(620, 300));
        JOptionPane.showMessageDialog(this, sp, "Estadísticas de procesos",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void setupRenderer() {
        MemoryValueTable.setDefaultRenderer(Object.class,
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                    c.setForeground(Color.BLACK);
 
                    if (row == highlightedRow) {
                        c.setBackground(new Color(80, 220, 140));
                        c.setForeground(Color.WHITE);
                        if (c instanceof JLabel l) l.setFont(l.getFont().deriveFont(Font.BOLD));
                    } else {
                        switch (rowKind(row)) {
                            case KERNEL -> c.setBackground(new Color(220, 220, 240));
                            case INSTR  -> c.setBackground(Color.WHITE); 
                            default     -> c.setBackground(new Color(250, 250, 250));
                        }
                        if (c instanceof JLabel l) l.setFont(l.getFont().deriveFont(Font.PLAIN));
                    }
                    return c;
                }
            });
 
        DiskValueTable.setDefaultRenderer(Object.class,
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                    c.setForeground(Color.BLACK);
                    // Index rows (0-9): light grey; rest: alternating white
                    if (row < os.getDisk().getIndexReserved()) {
                        c.setBackground(new Color(230, 230, 230));
                        if (c instanceof JLabel l) l.setFont(l.getFont().deriveFont(Font.ITALIC));
                    } else {
                        c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 255));
                        if (c instanceof JLabel l) l.setFont(l.getFont().deriveFont(Font.PLAIN));
                    }
                    return c;
                }
            });
        
        ProcessTable.setDefaultRenderer(Object.class,
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                    c.setForeground(Color.BLACK);
                    List<Process> processes = os.getScheduler().getJobQueue().getAll();
                    if (row < processes.size()) {
                        ProcessState state = processes.get(row).getBcp().getState();
                        c.setBackground(switch (state) {
                            case RUNNING    -> new Color(123, 245, 184);
                            case READY      -> new Color(255, 255, 200);
                            case WAITING    -> new Color(255, 200, 150);
                            case TERMINATED -> new Color(200, 200, 200);
                            default         -> Color.WHITE;
                        });
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                    return c;
                }
            });
    }
    
    /**
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDialog1 = new javax.swing.JDialog();
        menuBar1 = new java.awt.MenuBar();
        menu1 = new java.awt.Menu();
        menu2 = new java.awt.Menu();
        menuBar2 = new java.awt.MenuBar();
        menu3 = new java.awt.Menu();
        menu4 = new java.awt.Menu();
        jPanel1 = new javax.swing.JPanel();
        ExecuteButton = new javax.swing.JButton();
        StepbyStepButton = new javax.swing.JButton();
        CleanButton = new javax.swing.JButton();
        MemorySizeButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        MemoryValueTable = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        ProcessTable = new javax.swing.JTable();
        fileButton = new javax.swing.JButton();
        nameFileLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        stepButton = new javax.swing.JButton();
        kernelPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        bcpPanel = new javax.swing.JPanel();
        ramLabel = new javax.swing.JLabel();
        diskLabel = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        DiskValueTable = new javax.swing.JTable();
        processLabel = new javax.swing.JLabel();
        cmdLabel = new javax.swing.JLabel();
        bcpLabel = new javax.swing.JLabel();
        MemorySizeButton1 = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        consoleTextPane = new javax.swing.JTextPane();

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        menu1.setLabel("File");
        menuBar1.add(menu1);

        menu2.setLabel("Edit");
        menuBar1.add(menu2);

        menu3.setLabel("File");
        menuBar2.add(menu3);

        menu4.setLabel("Edit");
        menuBar2.add(menu4);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1484, 842));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setForeground(new java.awt.Color(51, 51, 51));
        jPanel1.setMinimumSize(new java.awt.Dimension(1484, 842));

        ExecuteButton.setBackground(new java.awt.Color(82, 176, 176));
        ExecuteButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        ExecuteButton.setForeground(new java.awt.Color(255, 255, 255));
        ExecuteButton.setText("Ejecutar");
        ExecuteButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        ExecuteButton.setMaximumSize(new java.awt.Dimension(90, 30));
        ExecuteButton.setMinimumSize(new java.awt.Dimension(90, 30));
        ExecuteButton.setPreferredSize(new java.awt.Dimension(98, 72));
        ExecuteButton.addActionListener(this::ExecuteButtonActionPerformed);

        StepbyStepButton.setBackground(new java.awt.Color(82, 176, 176));
        StepbyStepButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        StepbyStepButton.setForeground(new java.awt.Color(255, 255, 255));
        StepbyStepButton.setText("Paso a paso");
        StepbyStepButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        StepbyStepButton.setMaximumSize(new java.awt.Dimension(90, 30));
        StepbyStepButton.setMinimumSize(new java.awt.Dimension(90, 30));
        StepbyStepButton.setPreferredSize(new java.awt.Dimension(98, 72));
        StepbyStepButton.addActionListener(this::StepbyStepButtonActionPerformed);

        CleanButton.setBackground(new java.awt.Color(82, 176, 176));
        CleanButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        CleanButton.setForeground(new java.awt.Color(255, 255, 255));
        CleanButton.setText("Limpiar");
        CleanButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        CleanButton.setMaximumSize(new java.awt.Dimension(90, 30));
        CleanButton.setMinimumSize(new java.awt.Dimension(90, 30));
        CleanButton.setPreferredSize(new java.awt.Dimension(98, 72));
        CleanButton.addActionListener(this::CleanButtonActionPerformed);

        MemorySizeButton.setBackground(new java.awt.Color(82, 176, 176));
        MemorySizeButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        MemorySizeButton.setForeground(new java.awt.Color(255, 255, 255));
        MemorySizeButton.setText("Ajustar tamaño de memoria");
        MemorySizeButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        MemorySizeButton.setMaximumSize(new java.awt.Dimension(90, 30));
        MemorySizeButton.setMinimumSize(new java.awt.Dimension(90, 30));
        MemorySizeButton.setPreferredSize(new java.awt.Dimension(98, 72));
        MemorySizeButton.addActionListener(this::MemorySizeButtonActionPerformed);

        MemoryValueTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Posición", "Valor en memoria"
            }
        ));
        jScrollPane1.setViewportView(MemoryValueTable);

        ProcessTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Procesos", "Estados"
            }
        ));
        jScrollPane2.setViewportView(ProcessTable);

        fileButton.setBackground(new java.awt.Color(153, 153, 153));
        fileButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        fileButton.setForeground(new java.awt.Color(255, 255, 255));
        fileButton.setText("Cargar archivo");
        fileButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        fileButton.addActionListener(this::fileButtonActionPerformed);

        nameFileLabel.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        nameFileLabel.setForeground(new java.awt.Color(255, 255, 255));
        nameFileLabel.setText("Nombre del archivo:");

        jPanel2.setBackground(new java.awt.Color(123, 245, 184));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText(":  Espacio del kernel");

        stepButton.setBackground(new java.awt.Color(82, 176, 176));
        stepButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        stepButton.setForeground(new java.awt.Color(255, 255, 255));
        stepButton.setText("▼");
        stepButton.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Step", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N
        stepButton.addActionListener(this::stepButtonActionPerformed);

        kernelPanel.setBackground(new java.awt.Color(220, 220, 220));

        javax.swing.GroupLayout kernelPanelLayout = new javax.swing.GroupLayout(kernelPanel);
        kernelPanel.setLayout(kernelPanelLayout);
        kernelPanelLayout.setHorizontalGroup(
            kernelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );
        kernelPanelLayout.setVerticalGroup(
            kernelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setText(":  Programa ejecutando");

        javax.swing.GroupLayout bcpPanelLayout = new javax.swing.GroupLayout(bcpPanel);
        bcpPanel.setLayout(bcpPanelLayout);
        bcpPanelLayout.setHorizontalGroup(
            bcpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 272, Short.MAX_VALUE)
        );
        bcpPanelLayout.setVerticalGroup(
            bcpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        ramLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        ramLabel.setText("RAM");

        diskLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        diskLabel.setText("Disk space");

        DiskValueTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Posición", "Valor en memoria"
            }
        ));
        jScrollPane3.setViewportView(DiskValueTable);

        processLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        processLabel.setText("Procesos");

        cmdLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        cmdLabel.setText("Consola:");

        bcpLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        bcpLabel.setText("BCP");

        MemorySizeButton1.setBackground(new java.awt.Color(82, 176, 176));
        MemorySizeButton1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        MemorySizeButton1.setForeground(new java.awt.Color(255, 255, 255));
        MemorySizeButton1.setText("Estadísticas");
        MemorySizeButton1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        MemorySizeButton1.setMaximumSize(new java.awt.Dimension(90, 30));
        MemorySizeButton1.setMinimumSize(new java.awt.Dimension(90, 30));
        MemorySizeButton1.setPreferredSize(new java.awt.Dimension(98, 72));
        MemorySizeButton1.addActionListener(this::MemorySizeButton1ActionPerformed);

        consoleTextPane.setBackground(new java.awt.Color(51, 51, 51));
        consoleTextPane.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        consoleTextPane.setForeground(new java.awt.Color(255, 255, 255));
        jScrollPane5.setViewportView(consoleTextPane);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(processLabel)
                                        .addGap(74, 74, 74)
                                        .addComponent(nameFileLabel)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(stepButton, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(ExecuteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(StepbyStepButton, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(CleanButton, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(kernelPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel1))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel2)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(MemorySizeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(MemorySizeButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jScrollPane5)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(ramLabel)
                                            .addComponent(cmdLabel)
                                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE))
                                        .addGap(12, 12, 12)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(diskLabel))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(bcpLabel)
                                            .addComponent(bcpPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(37, 37, 37))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(fileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(125, 125, 125)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(nameFileLabel)
                            .addComponent(processLabel)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(MemorySizeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(CleanButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(StepbyStepButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ExecuteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(MemorySizeButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(22, 22, 22)
                                        .addComponent(fileButton)
                                        .addGap(25, 25, 25)
                                        .addComponent(ramLabel))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(diskLabel))))
                            .addComponent(bcpLabel))))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(bcpPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(19, 19, 19)
                        .addComponent(cmdLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(116, 116, 116)
                        .addComponent(stepButton, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 572, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(kernelPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileButtonActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("ASM Files", "asm"));
        fc.setMultiSelectionEnabled(true);
 
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
 
        File[] files = fc.getSelectedFiles();
        for (File f : files) {
            Process p = os.loadProcess(f.getAbsolutePath());
            if (p != null) {
                registerBCPTable(p);
            }
        }
        refreshAll();
        nameFileLabel.setText(files.length == 1
            ? "Archivo: " + files[0].getName()
            : files.length + " archivos cargados");
    }//GEN-LAST:event_fileButtonActionPerformed

    private void MemorySizeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MemorySizeButtonActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Config Files (.xml, .json)", "xml", "json"));

        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();

        LoadXML loader = new LoadXML();
        loader.readFile(file.getAbsolutePath());

        if (loader.isLoadError()) return;

        int ramSize  = loader.getRamSize();
        int diskSize = loader.getDiskSize();

        os.reset(ramSize, diskSize);

        clearBCPTables();

        stepButton.setEnabled(false);
        StepbyStepButton.setEnabled(true);
        ExecuteButton.setEnabled(true);
        fileButton.setEnabled(true);

        highlightedRow = -1;
        refreshAll();

        JOptionPane.showMessageDialog(this,
            "Configuración cargada:\nRAM: " + ramSize + "\nDisk: " + diskSize,
            "Configuración aplicada",
            JOptionPane.INFORMATION_MESSAGE);

    }//GEN-LAST:event_MemorySizeButtonActionPerformed

    private void CleanButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CleanButtonActionPerformed
        os.stop();
        os.reset(100);
 
        clearBCPTables();
        stepModeActive = false;
        stepButton.setEnabled(false);
        StepbyStepButton.setEnabled(true);
        ExecuteButton.setEnabled(true);
        MemorySizeButton.setEnabled(true);
        fileButton.setEnabled(true);
 
        highlightedRow = -1;
        nameFileLabel.setText("Nombre del archivo:");
        refreshAll();
    }//GEN-LAST:event_CleanButtonActionPerformed

    private void StepbyStepButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StepbyStepButtonActionPerformed
         if (!os.getScheduler().hasProcessReady()) {
            JOptionPane.showMessageDialog(this,
                "No hay procesos cargados en memoria", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        stepModeActive = true;
        stepButton.setEnabled(true);
        ExecuteButton.setEnabled(false);
        StepbyStepButton.setEnabled(false);
        MemorySizeButton.setEnabled(false);
        fileButton.setEnabled(false);
    }//GEN-LAST:event_StepbyStepButtonActionPerformed

    private void ExecuteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExecuteButtonActionPerformed
        if (!os.getScheduler().hasProcessReady()) {
            JOptionPane.showMessageDialog(this,
                "No hay procesos cargados en memoria", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        stepModeActive = false;
        StepbyStepButton.setEnabled(false);
        ExecuteButton.setEnabled(false);
        MemorySizeButton.setEnabled(false);
        fileButton.setEnabled(false);
        stepButton.setEnabled(false);
        os.run(false);
        
    }//GEN-LAST:event_ExecuteButtonActionPerformed

    private void stepButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stepButtonActionPerformed
        if (!stepModeActive) return;
        if (os.getScheduler().allTerminated()) {
            stepButton.setEnabled(false);
            return;
        }

        Integer executedPC = os.getDispatcher().CPUcycle();

        refreshAll();

        if (executedPC != null) {
            highlightRow(executedPC);
        }


        if (os.getDispatcher().getCurrentProcess() == null) {
            os.getMemory().updateKernelFromBCP(null);
            loadMemoryTable();  
        }

        if (os.getScheduler().allTerminated()) {
            stepModeActive = false;
            stepButton.setEnabled(false);
            os.getMemory().updateKernelFromBCP(null);
            refreshAll();
            JOptionPane.showMessageDialog(this,
                "Todos los procesos terminaron",
                "Ejecución finalizada", JOptionPane.INFORMATION_MESSAGE);
            showStatistics();
        }
    }//GEN-LAST:event_stepButtonActionPerformed

    private void MemorySizeButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MemorySizeButton1ActionPerformed
        showStatistics();
    }//GEN-LAST:event_MemorySizeButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new UI().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton CleanButton;
    private javax.swing.JTable DiskValueTable;
    private javax.swing.JButton ExecuteButton;
    private javax.swing.JButton MemorySizeButton;
    private javax.swing.JButton MemorySizeButton1;
    private javax.swing.JTable MemoryValueTable;
    private javax.swing.JTable ProcessTable;
    private javax.swing.JButton StepbyStepButton;
    private javax.swing.JLabel bcpLabel;
    private javax.swing.JPanel bcpPanel;
    private javax.swing.JLabel cmdLabel;
    private javax.swing.JTextPane consoleTextPane;
    private javax.swing.JLabel diskLabel;
    private javax.swing.JButton fileButton;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JPanel kernelPanel;
    private java.awt.Menu menu1;
    private java.awt.Menu menu2;
    private java.awt.Menu menu3;
    private java.awt.Menu menu4;
    private java.awt.MenuBar menuBar1;
    private java.awt.MenuBar menuBar2;
    private javax.swing.JLabel nameFileLabel;
    private javax.swing.JLabel processLabel;
    private javax.swing.JLabel ramLabel;
    private javax.swing.JButton stepButton;
    // End of variables declaration//GEN-END:variables
}
