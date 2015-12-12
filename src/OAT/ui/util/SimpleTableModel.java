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

package OAT.ui.util;

/**
 *
 * @author Antonio Yip
 */
public class SimpleTableModel extends javax.swing.table.DefaultTableModel {

    private boolean editable;

    public SimpleTableModel() {
    }

    public SimpleTableModel(boolean editable) {
        this.editable = editable;
    }

    public SimpleTableModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
        editable = false;
    }

    public SimpleTableModel(Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);
        editable = false;
    }

    public SimpleTableModel(Object[] columnNames) {
        this(columnNames, 0);
    }

    @Override
    public Class getColumnClass(int c) {
        Class columnClass = Object.class;
        for (int i = 0; i < getRowCount(); i++) {
            if (getValueAt(i, c) != null) {
                columnClass = getValueAt(i, c).getClass();
                break;
            }
        }
        return columnClass;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (!this.editable) {
            return false;
        }

        return super.isCellEditable(row, col);
    }
}
