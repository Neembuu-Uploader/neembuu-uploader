/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package neembuu.uploader.accountgui;

import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;
import javax.swing.*;
import javax.swing.table.*;

public class PanelInTable {

    private JFrame frame;
    private JTable CompTable = null;
    private PanelTableModel CompModel = null;
    private JButton addButton = null;

    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception fail) {
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new PanelInTable().makeUI();
            }
        });
    }

    public void makeUI() {
        CompTable = CreateCompTable();
        JScrollPane CompTableScrollpane = new JScrollPane(CompTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JPanel bottomPanel = CreateBottomPanel();
        frame = new JFrame("Comp Table Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(CompTableScrollpane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setPreferredSize(new Dimension(800, 400));
        frame.setLocation(150, 150);
        frame.pack();
        frame.setVisible(true);
    }

    public JTable CreateCompTable() {
        CompModel = new PanelTableModel();
        CompModel.addRow();
        JTable table = new JTable(CompModel);
        table.setRowHeight(new CompCellPanel().getPreferredSize().height);
        table.setTableHeader(null);
        PanelCellEditorRenderer PanelCellEditorRenderer = new PanelCellEditorRenderer();
        table.setDefaultRenderer(Object.class, PanelCellEditorRenderer);
        table.setDefaultEditor(Object.class, PanelCellEditorRenderer);
        return table;
    }

    public JPanel CreateBottomPanel() {
        addButton = new JButton("Add Comp");
        addButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                Object source = ae.getSource();
                if (source == addButton) {
                    CompModel.addRow();
                }
            }
        });
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(addButton);
        return panel;
    }
}

class PanelCellEditorRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {

    private static final long serialVersionUID = 1L;
    private CompCellPanel renderer = new CompCellPanel();
    private CompCellPanel editor = new CompCellPanel();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        renderer.setComp((Comp) value);
        return renderer;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        editor.setComp((Comp) value);
        return editor;
    }

    @Override
    public Object getCellEditorValue() {
        return editor.getComp();
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return false;
    }
}

class PanelTableModel extends DefaultTableModel {

    private static final long serialVersionUID = 1L;

    @Override
    public int getColumnCount() {
        return 1;
    }

    public void addRow() {
        super.addRow(new Object[]{new Comp(0, 0, "", "")});
    }
}

class Comp {

    public int type;
    public int relation;
    public String lower;
    public String upper;

    public Comp(int type, int relation, String lower, String upper) {
        this.type = type;
        this.relation = relation;
        this.lower = lower;
        this.upper = upper;
    }
}

class CompCellPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JLabel labelWith = new JLabel("With ");
    private JComboBox typeCombo = new JComboBox(new Object[]{"height", "length", "volume"});
    private JComboBox relationCombo = new JComboBox(new Object[]{"above", "below", "between"});
    private JTextField lowerField = new JTextField();
    private JLabel labelAnd = new JLabel(" and ");
    private JTextField upperField = new JTextField();
    private JButton removeButton = new JButton("remove");

    CompCellPanel() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        relationCombo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                enableUpper(relationCombo.getSelectedIndex() == 2);
            }
        });
        enableUpper(false);
        removeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JTable table = (JTable) SwingUtilities.getAncestorOfClass(JTable.class, (Component) e.getSource());
                int row = table.getEditingRow();
                table.getCellEditor().stopCellEditing();
                ((DefaultTableModel) table.getModel()).removeRow(row);
            }
        });
        add(labelWith);
        add(typeCombo);
        add(relationCombo);
        add(lowerField);
        add(labelAnd);
        add(upperField);
        add(Box.createHorizontalStrut(100));
        add(removeButton);
    }

    private void enableUpper(boolean enable) {
        labelAnd.setEnabled(enable);
        upperField.setEnabled(enable);
    }

    public void setComp(Comp Comp) {
        typeCombo.setSelectedIndex(Comp.type);
        relationCombo.setSelectedIndex(Comp.relation);
        lowerField.setText(Comp.lower);
        upperField.setText(Comp.upper);
        enableUpper(Comp.relation == 2);
    }

    public Comp getComp() {
        return new Comp(typeCombo.getSelectedIndex(), relationCombo.getSelectedIndex(), lowerField.getText(), upperField.getText());
    }
}
