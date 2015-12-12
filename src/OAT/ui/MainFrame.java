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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.text.*;
import OAT.data.SnapShot;
import OAT.trading.AccountPortfolio;
import OAT.trading.Main;
import OAT.trading.thread.HybridStrategy;
import OAT.trading.thread.TradingThread;
import OAT.ui.util.LogTextPane;
import OAT.ui.util.SimpleTableModel;
import OAT.ui.util.UiUtil;

/**
 *
 * @author Antonio Yip
 */
public class MainFrame extends javax.swing.JFrame {

    private static final String[] PORTFOLIO_COLUMN_NAMES = {"Symbol", "Exchange", "Description",
        "Position", "Currency", "U/R Profit", "Day Profit"};
    private boolean isMouseInLogText;

    /**
     * Creates new form MainFrame
     *
     * @param account
     * @param time
     * @param version
     */
    public MainFrame(String account, String time, String version) {
        setJMenuBar(new MainMenuBar(this));

        initComponents();

        accountLabel.setText(account);
        sinceLabel.setText(time);
        versionLabel.setText(version);

        logTabbedPane.requestFocusInWindow();

        strategyScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        contractScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        portfolioScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

//        jPanel3.getInsets().left=0;
//        jPanel3.getInsets().right=0;
//
//        portfolioScrollPane.setPreferredSize(new Dimension(647, 134));

        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {

                setVisible(true);
            }
        });
    }

    public void setupTables() {
        setupStrategyTable();
        setupContractTable();
        setupPortfolioTable();
    }

    public JTable getContractTable() {
        return contractTable;
    }

    public JTable getPortfolioTable() {
        return portfolioTable;
    }

    public JTable getStrategyTable() {
        return strategyTable;
    }

    public JLabel getAccountName() {
        return accountLabel;
    }

    public JLabel getLoginSince() {
        return sinceLabel;
    }

    public AccountPortfolio getSelectedAccount() {
        return (AccountPortfolio) accountCombo.getSelectedItem();
    }

    public void changeToPrimaryAccount() {
        accountCombo.setSelectedIndex(0);
    }

    public JTextPane addLogTextPane(String tabName) {
        JTextPane logTextPane = new LogTextPane();
        logTextPane.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
        javax.swing.JScrollPane jScrollPane = new JScrollPane();
        jScrollPane.setOpaque(false);
        jScrollPane.setViewportView(logTextPane);
        jScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        jScrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));
        logTabbedPane.addTab(tabName, jScrollPane);
        logTextPane.setEditable(false);

        logTextPane.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                isMouseInLogText = false;
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                isMouseInLogText = true;
            }
        });

        return logTextPane;
    }

    public JTextPane getLogTextPane(String tabName) {
        JTextPane logTextPane;

        int tabIndex = logTabbedPane.indexOfTab(tabName);

        if (tabIndex == -1) {
            logTextPane = addLogTextPane(tabName);
        } else {
            logTextPane = (JTextPane) ((JScrollPane) logTabbedPane.getComponentAt(tabIndex)).getViewport().getComponent(0);
        }

        return logTextPane;
    }

    public void selectLogTextPane(int index) {
        if (index < logTabbedPane.getTabCount()) {
            logTabbedPane.setSelectedIndex(index);
        }
    }

    public void selectLogTextPane(String tabName) {
        selectLogTextPane(logTabbedPane.indexOfTab(tabName));
    }

    public void appendLogText(JTextPane logTextPane, String msg, Color color) throws BadLocationException {
        Document doc = logTextPane.getDocument();

        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY,
                StyleConstants.Foreground, color);

        doc.insertString(doc.getLength(), msg, aset);

        if ((!isMouseInLogText || !isFocused())) {
            logTextPane.setCaretPosition(doc.getLength());
        }
    }

    public void removeLogTextPane(String tabName) {
        int tabIndex = logTabbedPane.indexOfTab(tabName);
        if (tabIndex != -1) {
            logTabbedPane.remove(tabIndex);
        }
    }

    private void setupContractTable() {
        JTable table = contractTable;
        table.setModel(new SimpleTableModel(
                SnapShot.COLUMN_HEADERS,
                Main.contracts.size()));

        TableColumnModel columnModel = table.getColumnModel();

        //set width
        columnModel.getColumn(0).setMinWidth(65);
        for (int i : new Integer[]{1, 4, 6}) {
            columnModel.getColumn(i).setMaxWidth(50);
        }

        //set renderer
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 7) {
                columnModel.getColumn(i).setCellRenderer(UiUtil.PRICE_CHANGE_CELL_RENDERER);
            } else if (i == 1 || i == 4 || i == 8) {
                columnModel.getColumn(i).setCellRenderer(UiUtil.SIZE_CELL_RENDERER);
            } else if (i != 0) {
                columnModel.getColumn(i).setCellRenderer(UiUtil.QUOTE_PRICE_CELL_RENDERER);
            }
        }

        table.getTableHeader().setReorderingAllowed(false);
        table.repaint();
    }

    private void setupPortfolioTable() {
        JTable table = portfolioTable;
        table.setModel(new SimpleTableModel(
                PORTFOLIO_COLUMN_NAMES, Main.contracts.size()));

        TableColumnModel columnModel = table.getColumnModel();

        //set width
        columnModel.getColumn(0).setMinWidth(65);
        columnModel.getColumn(0).setMaxWidth(80);
        columnModel.getColumn(1).setMinWidth(65);
        columnModel.getColumn(1).setMaxWidth(80);
        columnModel.getColumn(2).setMinWidth(150);
        columnModel.getColumn(3).setMaxWidth(60);
        columnModel.getColumn(4).setMaxWidth(60);

        //set renderer
        columnModel.getColumn(3).setCellRenderer(UiUtil.POSITION_CELL_RENDERER);
        for (int i = 4; i <= 6; i++) {
            columnModel.getColumn(i).setCellRenderer(UiUtil.CURRENCY_CELL_RENDERER);
        }

        table.getTableHeader().setReorderingAllowed(false);
        table.repaint();
    }

    private void setupStrategyTable() {
        JTable table = strategyTable;
        table.setModel(new SimpleTableModel(
                TradingThread.COLUMN_HEADERS,
                Main.contracts.size()));

        TableColumnModel columnModel = table.getColumnModel();

        //set width
        //table.getColumnModel().getColumn(2).setMinWidth(90);
        columnModel.getColumn(3).setMaxWidth(60);

        //set renderer
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i > 6) {
                columnModel.getColumn(i).setCellRenderer(UiUtil.PRICE_CHANGE_CELL_RENDERER);
            } else if (i == 3) {
                columnModel.getColumn(i).setCellRenderer(UiUtil.POSITION_CELL_RENDERER);
            } else if (i > 3) {
                columnModel.getColumn(i).setCellRenderer(UiUtil.PRICE_CELL_RENDERER);
            }
        }

        table.getTableHeader().setReorderingAllowed(false);
        table.repaint();
    }

    public void addAccountCombo(AccountPortfolio acct) {
        ((DefaultComboBoxModel) accountCombo.getModel()).addElement(acct);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        upperPanel = new javax.swing.JPanel();
        accountLabel = new javax.swing.JLabel();
        sinceLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        versionLabel = new javax.swing.JLabel();
        accountCombo = new javax.swing.JComboBox();
        outerPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        strategyScrollPane = new javax.swing.JScrollPane();
        strategyTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        contractScrollPane = new javax.swing.JScrollPane();
        contractTable = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        portfolioScrollPane = new javax.swing.JScrollPane();
        portfolioTable = new javax.swing.JTable();
        logTabbedPane = new javax.swing.JTabbedPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setBounds(new java.awt.Rectangle(0, 0, 0, 0));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        upperPanel.setPreferredSize(new java.awt.Dimension(628, 30));

        accountLabel.setText("          ");

        sinceLabel.setText("          ");

        jLabel2.setText("Login:");

        jLabel3.setText("Version:");

        versionLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        versionLabel.setText("          ");

        accountCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                accountComboActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout upperPanelLayout = new org.jdesktop.layout.GroupLayout(upperPanel);
        upperPanel.setLayout(upperPanelLayout);
        upperPanelLayout.setHorizontalGroup(
            upperPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(upperPanelLayout.createSequentialGroup()
                .add(8, 8, 8)
                .add(accountLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 53, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(accountCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 135, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sinceLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(versionLabel)
                .add(12, 12, 12))
        );
        upperPanelLayout.setVerticalGroup(
            upperPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(upperPanelLayout.createSequentialGroup()
                .add(upperPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(versionLabel)
                    .add(jLabel2)
                    .add(accountLabel)
                    .add(accountCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(sinceLabel))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        outerPanel.setAlignmentY(3.0F);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "Strategies"));
        jPanel1.setPreferredSize(new java.awt.Dimension(580, 160));

        strategyScrollPane.setHorizontalScrollBar(null);
        strategyScrollPane.setOpaque(false);
        strategyScrollPane.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                strategyScrollPaneMouseWheelMoved(evt);
            }
        });

        strategyTable.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        strategyTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        strategyTable.setOpaque(false);
        strategyTable.setSelectionBackground(new java.awt.Color(255, 255, 255));
        strategyTable.setSelectionForeground(new java.awt.Color(0, 0, 0));
        strategyTable.setShowHorizontalLines(false);
        strategyTable.setShowVerticalLines(false);
        strategyTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                strategyTableMouseClicked(evt);
            }
        });
        strategyScrollPane.setViewportView(strategyTable);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(strategyScrollPane)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(strategyScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "Contracts"));
        jPanel2.setPreferredSize(new java.awt.Dimension(580, 160));

        contractScrollPane.setHorizontalScrollBar(null);
        contractScrollPane.setOpaque(false);
        contractScrollPane.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                contractScrollPaneMouseWheelMoved(evt);
            }
        });

        contractTable.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        contractTable.setOpaque(false);
        contractTable.setRowSelectionAllowed(false);
        contractTable.setSelectionBackground(new java.awt.Color(255, 255, 255));
        contractTable.setSelectionForeground(new java.awt.Color(0, 0, 0));
        contractTable.setShowGrid(false);
        contractTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                contractTableMouseClicked(evt);
            }
        });
        contractScrollPane.setViewportView(contractTable);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(contractScrollPane)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, contractScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "Portfolio"));
        jPanel3.setPreferredSize(new java.awt.Dimension(580, 160));

        portfolioScrollPane.setOpaque(false);
        portfolioScrollPane.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                portfolioScrollPaneMouseWheelMoved(evt);
            }
        });

        portfolioTable.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        portfolioTable.setOpaque(false);
        portfolioTable.setSelectionBackground(new java.awt.Color(255, 255, 255));
        portfolioTable.setSelectionForeground(new java.awt.Color(0, 0, 0));
        portfolioTable.setShowGrid(false);
        portfolioTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                portfolioTableMouseClicked(evt);
            }
        });
        portfolioScrollPane.setViewportView(portfolioTable);

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(portfolioScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 637, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(portfolioScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
        );

        logTabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        logTabbedPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                logTabbedPaneMouseClicked(evt);
            }
        });

        org.jdesktop.layout.GroupLayout outerPanelLayout = new org.jdesktop.layout.GroupLayout(outerPanel);
        outerPanel.setLayout(outerPanelLayout);
        outerPanelLayout.setHorizontalGroup(
            outerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 647, Short.MAX_VALUE)
            .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 647, Short.MAX_VALUE)
            .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 647, Short.MAX_VALUE)
            .add(logTabbedPane)
        );
        outerPanelLayout.setVerticalGroup(
            outerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(outerPanelLayout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(logTabbedPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 194, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(upperPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 647, Short.MAX_VALUE)
            .add(outerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(upperPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(outerPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        Main.getMainThread().exit();
    }//GEN-LAST:event_formWindowClosing

    private void contractTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_contractTableMouseClicked
        int rowIndex = contractTable.getSelectedRow();

        if (rowIndex >= Main.contracts.size()) {
            return;
        }

        HybridStrategy strategy = Main.getStrategy(Main.contracts.get(rowIndex));

        if (strategy == null || !strategy.isInitialized()) {
            return;
        }

//        if (evt.isAltDown() && evt.isShiftDown()) {
//            strategy.displayChart(ChartType.TICK);
//        } else if (evt.isShiftDown() && evt.isControlDown()) {
//            strategy.displayChart(ChartType.CONTRACT);
        if (evt.isAltDown()) { //display historical charts for all contracts
            new Thread(new Runnable() {

                @Override
                public void run() {
                    for (HybridStrategy stg : Main.currentStrategies) {
                        if (stg.isActive() || !stg.isSleeping()) {
                            stg.displayHistoricalChart();
                        }
                    }
                }
            }).start();
        } else {
            strategy.displayHistoricalChart();
        }

        logTabbedPane.setSelectedIndex(rowIndex + 1);
    }//GEN-LAST:event_contractTableMouseClicked

    private void strategyTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_strategyTableMouseClicked
        int rowIndex = strategyTable.getSelectedRow();
        int columnIndex = strategyTable.getSelectedColumn();
        HybridStrategy strategy = Main.getStrategy(rowIndex);

        if (strategy == null) {
            return;
        }

        if (columnIndex < 2) {
            strategy.displayParametersFrame();

        } else if (columnIndex == 2) {
            if (evt.isAltDown()) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        for (HybridStrategy stg : Main.currentStrategies) {
                            if (stg.isActive() || !stg.isSleeping()) {
                                stg.displayPrimaryChart();
                            }
                        }
                    }
                }).start();
            } else {
                if (strategy.isInitialized()) {
                    strategy.displayPrimaryChart();
                }
            }

        } else {
            new TradeDatasetFrame(this, strategy.getTrades()).setVisible(true);
        }

        logTabbedPane.setSelectedIndex(rowIndex + 1);
    }//GEN-LAST:event_strategyTableMouseClicked

    private void logTabbedPaneMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logTabbedPaneMouseClicked
        evt.getComponent().requestFocusInWindow();

        HybridStrategy strategy = Main.getStrategy(logTabbedPane.getSelectedIndex() - 1);
    }//GEN-LAST:event_logTabbedPaneMouseClicked

    private void portfolioTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_portfolioTableMouseClicked
        int rowIndex = portfolioTable.getSelectedRow();

        if (rowIndex >= Main.contracts.size()) {
            return;
        }

        HybridStrategy strategy = Main.getStrategy(Main.contracts.get(rowIndex));

        if (strategy == null || !strategy.isInitialized()) {
            return;
        }

        if (portfolioTable.getSelectedColumn() < 3) {
            new ContractDetailsFrame(
                    portfolioTable,
                    strategy.getContractDetails()).setVisible(true);
        } else {
            new AccountPortfolioFrame(
                    portfolioTable,
                    getSelectedAccount()).setVisible(true);
        }

        logTabbedPane.setSelectedIndex(rowIndex + 1);
    }//GEN-LAST:event_portfolioTableMouseClicked

    private void accountComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_accountComboActionPerformed
        if (!Main.getMainThread().isInitialized()) {
            return;
        }

        Main.getMainThread().reqAccountUpdates();
    }//GEN-LAST:event_accountComboActionPerformed

    private void contractScrollPaneMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_contractScrollPaneMouseWheelMoved
        int value = contractScrollPane.getVerticalScrollBar().getValue();

        strategyScrollPane.getVerticalScrollBar().setValue(value);
        portfolioScrollPane.getVerticalScrollBar().setValue(value);
    }//GEN-LAST:event_contractScrollPaneMouseWheelMoved

    private void portfolioScrollPaneMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_portfolioScrollPaneMouseWheelMoved
        int value = portfolioScrollPane.getVerticalScrollBar().getValue();

        strategyScrollPane.getVerticalScrollBar().setValue(value);
        contractScrollPane.getVerticalScrollBar().setValue(value);
    }//GEN-LAST:event_portfolioScrollPaneMouseWheelMoved

    private void strategyScrollPaneMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_strategyScrollPaneMouseWheelMoved
        int value = strategyScrollPane.getVerticalScrollBar().getValue();

        portfolioScrollPane.getVerticalScrollBar().setValue(value);
        contractScrollPane.getVerticalScrollBar().setValue(value);
    }//GEN-LAST:event_strategyScrollPaneMouseWheelMoved
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox accountCombo;
    private javax.swing.JLabel accountLabel;
    private javax.swing.JScrollPane contractScrollPane;
    private javax.swing.JTable contractTable;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JTabbedPane logTabbedPane;
    private javax.swing.JPanel outerPanel;
    private javax.swing.JScrollPane portfolioScrollPane;
    private javax.swing.JTable portfolioTable;
    private javax.swing.JLabel sinceLabel;
    private javax.swing.JScrollPane strategyScrollPane;
    private javax.swing.JTable strategyTable;
    private javax.swing.JPanel upperPanel;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables
}
