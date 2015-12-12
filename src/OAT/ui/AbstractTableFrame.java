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
import java.awt.Dimension;
import java.awt.event.*;
import java.util.EventObject;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.layout.GroupLayout;
import OAT.event.GenericListener;
import OAT.event.Listenable;
import OAT.ui.util.SimpleTableModel;

/**
 *
 * @author Antonio Yip
 */
public abstract class AbstractTableFrame extends javax.swing.JFrame implements GenericListener {

    protected JPanel jPanel1;
    protected JScrollPane jScrollPane1;
    protected JTable theTable;
    protected Object theObject;
    protected boolean mouseIn;
    protected Component parentComponent;

    public AbstractTableFrame(Component component, Object object) {
        parentComponent = component;
        theObject = object;

        setLocationRelativeTo(parentComponent);
        setJMenuBar(new MainMenuBar(this));

        initComponents();
    }

    @Override
    public void eventHandler(EventObject event) {
        updateTable();
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            initTable();
            updateTable();
            resizeFrame();

            if (theObject instanceof Listenable) {
                ((Listenable) theObject).addChangeListener(this);
            }
        } else {
            if (theObject instanceof Listenable) {
                ((Listenable) theObject).removeChangeListener(this);
            }

            preExit();
        }

        super.setVisible(b);

        if (!b) {
            dispose();
        }
    }

    protected void clearTable() {
        getTableModel().setRowCount(0);
    }

    protected DefaultTableModel getTableModel() {
        return (DefaultTableModel) theTable.getModel();
    }

    protected DefaultTableCellRenderer getCellRenderer(int rowId, int columnId) {
        return (DefaultTableCellRenderer) theTable.getCellRenderer(rowId, columnId);
    }

    protected void resizeFrame() {
        int maxWidth = getMaxSize().width;
        int maxHeight = getMaxSize().height;

        setSize(Math.min((maxWidth != 0 ? maxWidth : Integer.MAX_VALUE),
                theTable.getColumnCount() * 150),
                Math.min(maxHeight != 0 ? maxHeight : Integer.MAX_VALUE,
                (theTable.getRowCount() + 1) * theTable.getRowHeight() + 27)); //incl column headers and frame
    }

    protected void scrollToEnd() {
        if (!mouseIn || !isFocused()) {
            ((JViewport) theTable.getParent()).scrollRectToVisible(
                    theTable.getCellRect(theTable.getRowCount() - 1, 0, true));
        }
    }

    protected void updateTable() {
        DefaultTableModel tableModel = getTableModel();
        int rowCount = tableModel.getRowCount();

        if (isUpdatingAll() || rowCount == 0 || getItemCount() < rowCount) {
            tableModel.setRowCount(getItemCount());

            Object[][] data = getRows();
            for (int i = 0; i < getItemCount(); i++) {
                for (int j = 0; j < data[i].length; j++) {
                    tableModel.setValueAt(data[i][j], i, j);
                }
            }

            resizeFrame();

        } else {
            //update the last row
            Object[] array = getRow(rowCount - 1);

            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                if (j < array.length) {
                    tableModel.setValueAt(array[j], rowCount - 1, j);
                }
            }

            //add any new rows
            if (getItemCount() > rowCount) {
                for (int i = rowCount; i < getItemCount(); i++) {
                    tableModel.addRow(getRow(i));
                }

                resizeFrame();
            }
        }

        scrollToEnd();
    }

    protected abstract void initTable();

    //
    protected abstract boolean isUpdatingAll();

    protected abstract Dimension getMaxSize();

    protected abstract Object[] getColumnHeaders();

    protected abstract Object[][] getRows();

    protected int getItemCount() {
        return getRows().length;
    }

    protected Object[] getRow(int index) {
        return getRows()[index];
    }

    //Events
    protected abstract void preExit();

    protected abstract void windowLostFocusHandler(WindowEvent evt);

    protected abstract void tableMouseClickHandler(MouseEvent evt);

    protected abstract void tableMouseEnteredHandler(MouseEvent evt);

    protected abstract void tableMouseExitedHandler(MouseEvent evt);

    protected void tableMouseMovedHandler(MouseEvent evt) {
        int rowId = theTable.rowAtPoint(evt.getPoint());
        int columnId = theTable.columnAtPoint(evt.getPoint());

        Object value = getTableModel().getValueAt(rowId, columnId);

        if (value != null) {
            int width = theTable.getColumnModel().getColumn(columnId).getWidth();

            if (value.toString().length() > width / 6) {
                getCellRenderer(rowId, columnId).setToolTipText(value.toString());
            } else {
                getCellRenderer(rowId, columnId).setToolTipText(null);
            }
        }
    }
    //

    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        theTable = new javax.swing.JTable();

        setResizable(false);
        addWindowFocusListener(new WindowFocusListener() {

            @Override
            public void windowGainedFocus(WindowEvent evt) {
            }

            @Override
            public void windowLostFocus(WindowEvent evt) {
                windowLostFocusHandler(evt);
            }
        });
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentHidden(ComponentEvent evt) {
                setVisible(false);
            }
        });

        theTable.setModel(new SimpleTableModel(getColumnHeaders(), 0));
        theTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent evt) {
                tableMouseClickHandler(evt);
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                mouseIn = false;
                tableMouseExitedHandler(evt);
            }

            @Override
            public void mouseEntered(MouseEvent evt) {
                mouseIn = true;
                tableMouseEnteredHandler(evt);
            }
        });
        theTable.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent evt) {
                tableMouseMovedHandler(evt);
            }
        });
        jScrollPane1.setViewportView(theTable);

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.LEADING).add(jScrollPane1, GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE));
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.LEADING).add(jScrollPane1, GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE));

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.LEADING).add(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.LEADING).add(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

        pack();
    }
}
