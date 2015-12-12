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
import java.util.HashMap;
import java.util.logging.Level;
import javax.swing.JTable;
import OAT.trading.Account;
import OAT.trading.Main;

/**
 *
 * @author Antonio Yip
 */
public class PreferencesFrame extends javax.swing.JFrame {

    //private static File productsFile = new File("/Users/antonyip/Library/Scripts/Trading/products.dat");
    //private Product[] products = null;
    private HashMap treeNodeMap = new HashMap();

    /** Creates new form NewJFrame */
    public PreferencesFrame(Component component) {
        setLocationRelativeTo(component);
        setJMenuBar(new MainMenuBar(this));
        initComponents();
        initAccountCombo();
        initLogLevelCombo();
    }

    @Override
    public void setVisible(boolean b) {
        if (Main.getMainThread() != null) {
            /*
            products = Main.settings.products;
            productTree.setModel(productTreeModel(products));
            UiUtil.clearTreeIcon(productTree);
             * 
             */
            if (b) {
                accountCombo.setSelectedItem(Main.getAccount());
                logLevelCombo.setSelectedItem(Main.p_Log_Level);
            }
            super.setVisible(b);
        }
    }

    /*
    private DefaultTreeModel productTreeModel(Product[] products) {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Region");
    DefaultTreeModel treeModel = new DefaultTreeModel(root);
    DefaultMutableTreeNode regionNode;
    DefaultMutableTreeNode locationNode;
    DefaultMutableTreeNode exchangeNode;
    DefaultMutableTreeNode productNode;
    treeNodeMap.put(root, Region.class.getName());

    for (Product product : products) {
    regionNode = UiUtil.addChildNode(root, product.exchange.location.region);
    locationNode = UiUtil.addChildNode(regionNode, product.exchange.location);
    exchangeNode = UiUtil.addChildNode(locationNode, product.exchange);
    productNode = UiUtil.addChildNode(exchangeNode, product);

    treeNodeMap.put(regionNode, product.exchange.location.region);
    treeNodeMap.put(locationNode, product.exchange.location);
    treeNodeMap.put(exchangeNode, product.exchange);
    treeNodeMap.put(productNode, product);
    }

    return treeModel;
    }
     * 
     */
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        generalPanel = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        accountCombo = new javax.swing.JComboBox();
        logLevelCombo = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        productsPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton4 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        productTree = new javax.swing.JTree();
        strategiesPanel = new javax.swing.JPanel();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        closeButton = new javax.swing.JButton();
        applyButton = new javax.swing.JButton();

        setResizable(false);

        generalPanel.setOpaque(false);

        jCheckBox1.setSelected(true);
        jCheckBox1.setText("Login IB at Startup");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jLabel1.setText("IB Host");

        jTextField1.setText("localhost");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jLabel3.setText("Account");

        jLabel4.setText("Log Level");

        org.jdesktop.layout.GroupLayout generalPanelLayout = new org.jdesktop.layout.GroupLayout(generalPanel);
        generalPanel.setLayout(generalPanelLayout);
        generalPanelLayout.setHorizontalGroup(
            generalPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(generalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(generalPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jCheckBox1)
                    .add(generalPanelLayout.createSequentialGroup()
                        .add(generalPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel3)
                            .add(jLabel1))
                        .add(25, 25, 25)
                        .add(generalPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 129, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(accountCombo, 0, 129, Short.MAX_VALUE)))
                    .add(generalPanelLayout.createSequentialGroup()
                        .add(jLabel4)
                        .add(18, 18, 18)
                        .add(logLevelCombo, 0, 129, Short.MAX_VALUE)))
                .add(441, 441, 441))
        );
        generalPanelLayout.setVerticalGroup(
            generalPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(generalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(generalPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(generalPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(accountCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(generalPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(logLevelCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 128, Short.MAX_VALUE)
                .add(jCheckBox1)
                .addContainerGap())
        );

        jTabbedPane1.addTab("General", generalPanel);

        productsPanel.setOpaque(false);

        jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        jScrollPane2.setViewportView(jTable1);

        jButton4.setText("Add");

        jButton3.setText("Delete");

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        productTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        productTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                productTreeValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(productTree);

        org.jdesktop.layout.GroupLayout productsPanelLayout = new org.jdesktop.layout.GroupLayout(productsPanel);
        productsPanel.setLayout(productsPanelLayout);
        productsPanelLayout.setHorizontalGroup(
            productsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(productsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 179, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(productsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(productsPanelLayout.createSequentialGroup()
                        .add(jButton4)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jButton3))
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 438, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        productsPanelLayout.setVerticalGroup(
            productsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, productsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(productsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(productsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton4)
                    .add(jButton3))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Products", productsPanel);

        strategiesPanel.setOpaque(false);

        jButton5.setText("Add");

        jButton6.setText("Delete");

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane3.setViewportView(jTable2);

        org.jdesktop.layout.GroupLayout strategiesPanelLayout = new org.jdesktop.layout.GroupLayout(strategiesPanel);
        strategiesPanel.setLayout(strategiesPanelLayout);
        strategiesPanelLayout.setHorizontalGroup(
            strategiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(strategiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(strategiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 627, Short.MAX_VALUE)
                    .add(strategiesPanelLayout.createSequentialGroup()
                        .add(jButton5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jButton6)))
                .addContainerGap())
        );
        strategiesPanelLayout.setVerticalGroup(
            strategiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, strategiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(strategiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton5)
                    .add(jButton6))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Strategies", strategiesPanel);

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        applyButton.setText("Apply");
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jTabbedPane1)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(applyButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(closeButton)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(closeButton)
                    .add(applyButton))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_jTextField1ActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_closeButtonActionPerformed

    private void productTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_productTreeValueChanged
        /*
        //under construction

        if (jTree1.getSelectionPath() != null) {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jTree1.getSelectionPath().getLastPathComponent();

        if (selectedNode.getChildCount() > 0) {
        //System.out.println(securitiesMap.get(selectedNode));
        //System.out.println(securitiesMap.get(selectedNode).getClass());
        jTree1.expandPath(jTree1.getSelectionPath());
        jTable1.setModel(Util.setupTreeTableModel(selectedNode, treeNodeMap));
        } else {
        int i = (selectedNode.getParent().getIndex(selectedNode));
        jTable1.setModel(Util.setupTreeTableModel(selectedNode.getParent(), treeNodeMap));
        jTable1.changeSelection(i, 0, false, false);
        }

        Util.setupColumns(jTable1);
        }
         *
         */
    }//GEN-LAST:event_productTreeValueChanged

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        /* under construction
        try {
        FileUtil.store(products, productsFile);
        } catch (IOException ex) {
        Logger.getLogger(PreferencesFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
         *
         */
    }//GEN-LAST:event_applyButtonActionPerformed

    private void initAccountCombo() {
        for (Account account : Account.values()) {
            accountCombo.addItem(account);
        }

        accountCombo.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Account selected = (Account) accountCombo.getSelectedItem();
                if (selected != Main.getAccount()) {
//                    Main.ibAccount = selected;
                }
            }
        });
    }

    private void initLogLevelCombo() {
        logLevelCombo.addItem(Level.SEVERE);
        logLevelCombo.addItem(Level.WARNING);
        logLevelCombo.addItem(Level.INFO);
        logLevelCombo.addItem(Level.CONFIG);
        logLevelCombo.addItem(Level.FINE);
        logLevelCombo.addItem(Level.FINER);
        logLevelCombo.addItem(Level.ALL);

        logLevelCombo.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Main.getMainThread().setLogLevel((Level) logLevelCombo.getSelectedItem());
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox accountCombo;
    private javax.swing.JButton applyButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel generalPanel;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JComboBox logLevelCombo;
    private javax.swing.JTree productTree;
    private javax.swing.JPanel productsPanel;
    private javax.swing.JPanel strategiesPanel;
    // End of variables declaration//GEN-END:variables
}
