/*
    Open Auto Trading : A fully automatic equities trading platform with machine learning capabilities
    Copyright (C) 2015 AnyObject Ltd.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package OAT.ui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import OAT.trading.Main;
import OAT.trading.thread.BacktestThread;
import OAT.trading.thread.BacktestThread.BackDate;
import OAT.trading.thread.HybridStrategy;
import OAT.ui.util.UiUtil;
import OAT.util.GeneralUtil;
import OAT.util.MathUtil;

/**
 *
 * @author Antonio Yip
 */
public class BacktestFrame extends javax.swing.JFrame {

    private HybridStrategy defaultStrategy;
    private BackDate backDate;
    private Level logLevel = Level.INFO;

    public void setParameters(HybridStrategy strategy) {
        HybridStrategy selected = (HybridStrategy) strategyCombo.getSelectedItem();

        if (!selected.toString().equals(strategy.toString())) {
            return;
        }

        Object[][] parametersArray = strategy.getParametersArray();
        variablesTable.clearSelection();

        for (int i = 0; i < parametersArray.length; i++) {
            Object[] parameter = parametersArray[i];
            Object paramName = parameter[0];


            if (paramName.equals(variablesTable.getValueAt(i, 0))) {
                Object oldValue = variablesTable.getValueAt(i, 1);
                Object newValue = parameter[1];

                if (oldValue == null && newValue == null) {
                    continue;
                }

                if (oldValue == null || !oldValue.equals(newValue)) {
                    variablesTable.setValueAt(parameter[1], i, 1);
                    variablesTable.addRowSelectionInterval(i, i);

//                    if (newValue instanceof Boolean) {
//                        variablesTable.setValueAt(false, i, 2);
//                    }
                }
            }
        }
    }

    @Override
    public void setVisible(boolean b) {
        if (b && variablesTable.getRowCount() == 0) {
            strategyCombo.setSelectedIndex(Main.selectedStrategyId);
        }

        super.setVisible(b);
    }

    private void initRadioButtons() {
        buttonGroup1.add(jRadioButton1);
        buttonGroup1.add(jRadioButton2);
    }

    private class backtestTableModel extends DefaultTableModel {

        backtestTableModel(HybridStrategy strategy) {
            defaultStrategy = strategy;

            addColumn("Variable");
            addColumn("Default");
            addColumn("");
            addColumn("Start");
            addColumn("End");
            addColumn("Step");

            Object[][] parameters = strategy.getParametersArray();

            for (int i = 0; i < parameters.length; i++) {
                Object key = parameters[i][0];
                Object value = parameters[i][1];
                Object check = false;
                Object start = null;
                Object end = null;
                Object step = null;

                if (value instanceof Boolean) {
                    start = 0;
                    end = 1;
                    step = 1;
                } else {
                    if (MathUtil.isSteppable(value)) {
                        start = value;
                        end = value;
                        step = MathUtil.getStep(value);
//                    } else {
//                        check = null;
                    }
                }

                Object[] row = new Object[]{
                    key,
                    value,
                    check,
                    start,
                    end,
                    step
                };

                addRow(row);
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            if (col == 0) {
                return false;

            } else if (col == 1) {
                return true;

            } else {
                if (getValueAt(row, 5) != null) {
                    if (getValueAt(row, 1) instanceof Boolean
                            || getValueAt(row, 1).toString().equals("true")
                            || getValueAt(row, 1).toString().equals("false")) {
                        return col == 2;
                    }

                    return true;
                } else {
                    return false;
                }
            }
        }

        @Override
        public Class getColumnClass(int c) {
            if (c == 2) {
                return Boolean.class;

            } else if (c >= 3) {
                return Double.class;

            } else {
                return super.getColumnClass(c);
            }
        }
    }

    /**
     * Creates new form BacktestFrame
     *
     * @param component
     */
    public BacktestFrame(Component component) {
        setJMenuBar(new MainMenuBar(this));

        initComponents();
        initStrategyCombo();
        initDateCombo();
        initCheckBoxes();
        initRadioButtons();

        logLevelCombo.setSelectedItem(logLevel);

        setLocationRelativeTo(component);
    }

    private void setupVariablesTable(HybridStrategy strategy) {
        final JTable table = variablesTable;

        backtestTableModel tableModel = new backtestTableModel(strategy);

        table.setModel(tableModel);

        //set width
        table.getColumnModel().getColumn(0).setMinWidth(180);
        table.getColumnModel().getColumn(1).setMinWidth(80);
        table.getColumnModel().getColumn(2).setMaxWidth(15);
        //for (int i : new Integer[]{1, 4, 6}) {
        //    table.getColumnModel().getColumn(i).setMaxWidth(50);
        //}

        //set renderer
        for (int i = 3; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(UiUtil.RIGHT_ALIGNED_GENERAL_CELL_RENDERER);
        }

        table.getTableHeader().setReorderingAllowed(false);


        tableModel.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
                int column = e.getColumn();
                int row = e.getFirstRow();

                if (column > 2) {
                    table.setValueAt(true, row, 2);
                } else if (column == 1) {

                    table.setValueAt(false, row, 2);
                }
            }
        });
    }

    private void initStrategyCombo() {
        strategyCombo.setModel(new DefaultComboBoxModel(Main.savedStrategies.toArray()));
    }

    private void initDateCombo() {
        dateCombo.setModel(new DefaultComboBoxModel(BackDate.values()));
        dateCombo.setSelectedIndex(0);
    }

    private void initCheckBoxes() {
        //jCheckBox1.setSelected(Main.backtestThread.suppressClassifiers);
        //jCheckBox2.setSelected(Main.backtestThread.suppressStoppers);
    }

    private void dumpBacktest() {
        if (Main.backtestThread != null) {
            Main.backtestThread.exit();

//            new Waiting(Main.p_Exit_Time_Out, Main.p_Wait_Interval, null) {
//
//                @Override
//                public boolean waitWhile() {
//                    return !Main.backtestThread.isExited();
//                }
//
//                @Override
//                public void retry() {
//                }
//
//                @Override
//                public String message() {
//                    return null;
//                }
//
//                @Override
//                public void timeout() {
//                }
//            };

            Main.backtestThread = null;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        strategyCombo = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        dateCombo = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        logLevelCombo = new javax.swing.JComboBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        variablesTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jCheckBox3 = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jCheckBox4 = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jButton5 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setTitle("Backtest");
        setResizable(false);

        jLabel3.setText("Backtest based on");

        strategyCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                strategyComboActionPerformed(evt);
            }
        });

        jLabel5.setText("Back Date");

        dateCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dateComboActionPerformed(evt);
            }
        });

        jLabel6.setText("Log Level");

        logLevelCombo.setModel(new DefaultComboBoxModel(
            new Level[]{
                Level.INFO,
                Level.FINE,
                Level.FINER}));
    logLevelCombo.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            logLevelComboActionPerformed(evt);
        }
    });

    org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
        jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(jPanel1Layout.createSequentialGroup()
            .addContainerGap()
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                .add(jPanel1Layout.createSequentialGroup()
                    .add(jLabel6)
                    .add(168, 168, 168)
                    .add(logLevelCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .add(jPanel1Layout.createSequentialGroup()
                    .add(jLabel5)
                    .add(165, 165, 165)
                    .add(dateCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .add(org.jdesktop.layout.GroupLayout.CENTER, jPanel1Layout.createSequentialGroup()
                    .add(jLabel3)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(strategyCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 167, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap())
    );
    jPanel1Layout.setVerticalGroup(
        jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(jPanel1Layout.createSequentialGroup()
            .add(7, 7, 7)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(strategyCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jLabel3))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(dateCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jLabel5))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jLabel6)
                .add(logLevelCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(12, 12, 12))
    );

    jPanel1Layout.linkSize(new java.awt.Component[] {dateCombo, jLabel3, jLabel5, jLabel6, logLevelCombo, strategyCombo}, org.jdesktop.layout.GroupLayout.VERTICAL);

    variablesTable.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {

        },
        new String [] {

        }
    ));
    jScrollPane2.setViewportView(variablesTable);

    jCheckBox3.setText("Train Predictor");
    jCheckBox3.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jCheckBox3ActionPerformed(evt);
        }
    });

    jLabel1.setText("Size:");

    jTextField1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    jTextField1.setText("10");
    jTextField1.setEnabled(false);
    jTextField1.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jTextField1ActionPerformed(evt);
        }
    });

    jRadioButton1.setSelected(true);
    jRadioButton1.setText("Classification");
    jRadioButton1.setEnabled(false);
    jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jRadioButton1ActionPerformed(evt);
        }
    });

    jRadioButton2.setText("Prediction");
    jRadioButton2.setEnabled(false);
    jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jRadioButton2ActionPerformed(evt);
        }
    });

    jCheckBox4.setText("Append results");

    org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
    jPanel4.setLayout(jPanel4Layout);
    jPanel4Layout.setHorizontalGroup(
        jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(jPanel4Layout.createSequentialGroup()
            .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jPanel4Layout.createSequentialGroup()
                    .add(jCheckBox3)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jLabel1)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                    .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(jPanel4Layout.createSequentialGroup()
                    .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jPanel4Layout.createSequentialGroup()
                            .add(jRadioButton1)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(jRadioButton2))
                        .add(jCheckBox4))
                    .add(0, 0, Short.MAX_VALUE)))
            .addContainerGap())
    );
    jPanel4Layout.setVerticalGroup(
        jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(jPanel4Layout.createSequentialGroup()
            .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jCheckBox3)
                .add(jLabel1)
                .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jRadioButton1)
                .add(jRadioButton2))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
            .add(jCheckBox4))
    );

    jLabel2.setText("Suppress:");

    jCheckBox1.setText("Stopper");
    jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jCheckBox1ActionPerformed(evt);
        }
    });

    jCheckBox2.setText("Predictor");
    jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jCheckBox2ActionPerformed(evt);
        }
    });

    org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
    jPanel5.setLayout(jPanel5Layout);
    jPanel5Layout.setHorizontalGroup(
        jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(jPanel5Layout.createSequentialGroup()
            .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jLabel2)
                .add(jCheckBox1)
                .add(jCheckBox2))
            .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    jPanel5Layout.setVerticalGroup(
        jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(jPanel5Layout.createSequentialGroup()
            .add(jLabel2)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jCheckBox1)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jCheckBox2)
            .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
    jPanel2.setLayout(jPanel2Layout);
    jPanel2Layout.setHorizontalGroup(
        jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(jPanel2Layout.createSequentialGroup()
            .addContainerGap()
            .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(32, 32, 32)
            .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addContainerGap())
    );
    jPanel2Layout.setVerticalGroup(
        jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(jPanel2Layout.createSequentialGroup()
            .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 71, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );

    jButton5.setText("Save");
    jButton5.setMaximumSize(new java.awt.Dimension(90, 29));
    jButton5.setMinimumSize(new java.awt.Dimension(90, 29));
    jButton5.setPreferredSize(new java.awt.Dimension(90, 29));
    jButton5.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton5ActionPerformed(evt);
        }
    });

    jButton1.setText("Reset");
    jButton1.setMaximumSize(new java.awt.Dimension(90, 29));
    jButton1.setMinimumSize(new java.awt.Dimension(90, 29));
    jButton1.setPreferredSize(new java.awt.Dimension(90, 29));
    jButton1.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton1ActionPerformed(evt);
        }
    });

    jButton7.setText("Dump");
    jButton7.setMaximumSize(new java.awt.Dimension(90, 29));
    jButton7.setMinimumSize(new java.awt.Dimension(90, 29));
    jButton7.setPreferredSize(new java.awt.Dimension(90, 29));
    jButton7.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton7ActionPerformed(evt);
        }
    });

    jButton4.setText("Show Last");
    jButton4.setMaximumSize(new java.awt.Dimension(90, 29));
    jButton4.setMinimumSize(new java.awt.Dimension(90, 29));
    jButton4.setPreferredSize(new java.awt.Dimension(90, 29));
    jButton4.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton4ActionPerformed(evt);
        }
    });

    jButton3.setText("Start");
    jButton3.setMaximumSize(new java.awt.Dimension(90, 29));
    jButton3.setMinimumSize(new java.awt.Dimension(90, 29));
    jButton3.setPreferredSize(new java.awt.Dimension(90, 29));
    jButton3.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton3ActionPerformed(evt);
        }
    });

    jButton2.setText("Close");
    jButton2.setMaximumSize(new java.awt.Dimension(90, 29));
    jButton2.setMinimumSize(new java.awt.Dimension(90, 29));
    jButton2.setPreferredSize(new java.awt.Dimension(90, 29));
    jButton2.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton2ActionPerformed(evt);
        }
    });

    org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(
        jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(jPanel3Layout.createSequentialGroup()
            .addContainerGap()
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jPanel3Layout.createSequentialGroup()
                    .add(0, 0, Short.MAX_VALUE)
                    .add(jButton5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 91, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(103, 103, 103))
                .add(jPanel3Layout.createSequentialGroup()
                    .add(jButton1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 91, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jButton4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 91, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jPanel3Layout.createSequentialGroup()
                    .add(1, 1, 1)
                    .add(jButton7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
            .add(18, 18, 18)
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, jButton3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, jButton2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addContainerGap())
    );

    jPanel3Layout.linkSize(new java.awt.Component[] {jButton1, jButton2, jButton4, jButton5}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

    jPanel3Layout.setVerticalGroup(
        jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(jPanel3Layout.createSequentialGroup()
            .addContainerGap()
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jButton5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jButton3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jButton4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jButton1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jButton2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jButton7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap())
    );

    org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(jPanel1, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .add(jScrollPane2, 0, 0, Short.MAX_VALUE)
        .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(layout.createSequentialGroup()
            .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 109, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 365, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        setupVariablesTable(defaultStrategy);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void dateComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dateComboActionPerformed
        BackDate selected = (BackDate) ((JComboBox) evt.getSource()).getSelectedItem();

        backDate = selected;
    }//GEN-LAST:event_dateComboActionPerformed

    private void strategyComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_strategyComboActionPerformed
        HybridStrategy selected = (HybridStrategy) ((JComboBox) evt.getSource()).getSelectedItem();

        defaultStrategy = selected;
        setupVariablesTable(selected);
    }//GEN-LAST:event_strategyComboActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        new Thread(new Runnable() {

            @Override
            public void run() {

                if (variablesTable.getCellEditor() != null) {
                    variablesTable.getCellEditor().stopCellEditing();
                }

                String[] keys = new String[variablesTable.getRowCount()];
                List[] steps = new ArrayList[variablesTable.getRowCount()];
                String symbol = null;
                int expiryMonths = 0;
                int lastDay = 0;

                for (int i = 0; i < variablesTable.getRowCount(); i++) {
                    String name = (String) variablesTable.getValueAt(i, 0);
                    Object value = variablesTable.getValueAt(i, 1);

                    keys[i] = name;

                    if ("Symbol".equalsIgnoreCase(name)) {
                        symbol = value.toString();
                    } else if ("Expiry Every n Months".equalsIgnoreCase(name)) {
                        expiryMonths = (int) Double.parseDouble(value.toString());
                    } else if ("Last Day Before Expiry".equalsIgnoreCase(name)) {
                        lastDay = (int) Double.parseDouble(value.toString());
                    }

                    if ((Boolean) variablesTable.getValueAt(i, 2)) {
                        double start = Double.valueOf(variablesTable.getValueAt(i, 3).toString());
                        double end = Double.valueOf(variablesTable.getValueAt(i, 4).toString());
                        double step = Double.valueOf(variablesTable.getValueAt(i, 5).toString());

                        steps[i] = MathUtil.getSteps(start, end, step);

                    } else {
                        steps[i] = new ArrayList(Arrays.asList(value));
                    }
                }

//                if (Main.backtestThread != null) {
//                    Main.backtestThread.exit();
//                }

                dumpBacktest();

                Main.backtestThread = new BacktestThread(
                        symbol,
                        expiryMonths,
                        lastDay,
                        keys,
                        steps,
                        backDate,
                        logLevel);

                setVisible(false);

                Main.backtestThread.init();
                Main.backtestThread.suppressStoppers = jCheckBox1.isSelected();
                Main.backtestThread.suppressClassifiers = jCheckBox2.isSelected();
                Main.backtestThread.training = jCheckBox3.isSelected();
                Main.backtestThread.trainingSize = Integer.valueOf(jTextField1.getText());
                Main.backtestThread.predictionMode = jRadioButton2.isSelected();
                Main.backtestThread.appendResults = jCheckBox4.isSelected();

                Main.frame.selectLogTextPane(Main.backtestThread.getDefaultLoggerName());

                new Thread(Main.backtestThread).start();
            }
        }).start();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void logLevelComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logLevelComboActionPerformed
        logLevel = (Level) logLevelCombo.getSelectedItem();
}//GEN-LAST:event_logLevelComboActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (Main.backtestThread != null) {
                    Main.backtestThread.showSummary();
                }
            }
        }).start();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        if (variablesTable.getCellEditor() != null) {
            variablesTable.getCellEditor().stopCellEditing();
        }

        int rowCount = variablesTable.getRowCount();
        String[] keys = new String[rowCount];
        Object[] values = new Object[rowCount];

        for (int i = 0; i < rowCount; i++) {
            keys[i] = (String) variablesTable.getModel().getValueAt(i, 0);
            values[i] = variablesTable.getModel().getValueAt(i, 1);
        }

        HybridStrategy strategy = (HybridStrategy) strategyCombo.getSelectedItem();

        strategy.setParameters(GeneralUtil.getParameters(keys, values));
        strategy.savePreferences();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        //Main.backtestThread.suppressClassifiers = jCheckBox1.isSelected();
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        //Main.backtestThread.suppressStoppers = jCheckBox2.isSelected();
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        new Thread(new Runnable() {

            @Override
            public void run() {
                dumpBacktest();
            }
        }).start();
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jCheckBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox3ActionPerformed
        jCheckBox1.setEnabled(!jCheckBox3.isSelected());
        jCheckBox2.setEnabled(!jCheckBox3.isSelected());
        jTextField1.setEnabled(jCheckBox3.isSelected());
        jRadioButton1.setEnabled(jCheckBox3.isSelected());
        jRadioButton2.setEnabled(jCheckBox3.isSelected());
    }//GEN-LAST:event_jCheckBox3ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton2ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox dateCombo;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton7;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JComboBox logLevelCombo;
    private javax.swing.JComboBox strategyCombo;
    private javax.swing.JTable variablesTable;
    // End of variables declaration//GEN-END:variables
}
