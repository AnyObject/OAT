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
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.TableColumnModel;
import OAT.ui.util.UiUtil;

/**
 *
 * @author Antonio Yip
 */
public class ChartDataFrame extends AbstractTableFrame {

    public ChartDataFrame(Component component, BarChartFrame chartFrame) {
        super(component, chartFrame);

        setTitle(chartFrame.getTitle());
    }

    public BarChartFrame getChartFrame() {
        return (BarChartFrame) theObject;
    }

    @Override
    protected void initTable() {
        theTable.setAutoCreateRowSorter(false);
        theTable.getTableHeader().setReorderingAllowed(false);

        TableColumnModel columnModel = theTable.getColumnModel();

        columnModel.getColumn(0).setMinWidth(100);

        columnModel.getColumn(0).setCellRenderer(UiUtil.GENERAL_CELL_RENDERER);

        for (int i = 1; i < 4; i++) {
            columnModel.getColumn(i).setCellRenderer(UiUtil.PRICE_CELL_RENDERER);
        }

        columnModel.getColumn(5).setCellRenderer(UiUtil.SIZE_CELL_RENDERER);

        for (int i = 6; i < getColumnHeaders().length; i++) {
            columnModel.getColumn(i).setCellRenderer(UiUtil.PRICE_CELL_RENDERER);
        }

        for (int i = 0; i < getChartFrame().getDatasetCount(); i++) {
            getChartFrame().getDataset(i).addChangeListener(this);
        }
    }

    @Override
    protected boolean isUpdatingAll() {
        return false;
    }

    @Override
    protected Dimension getMaxSize() {
        if (getTableModel().getColumnCount() < 10) {
            return new Dimension(600, 600);
        } else {
            return new Dimension(830, 600);
        }
    }

    @Override
    protected Object[] getColumnHeaders() {
        return getChartFrame().getChart().getColumnHeaders();
    }

    @Override
    protected Object[][] getRows() {
        List<Object[]> rows = new ArrayList<Object[]>();

        for (int i = 0; i < getChartFrame().getChart().getItemCount(); i++) {
            rows.add(getChartFrame().getChart().getRow(i));
        }

        return rows.toArray(new Object[0][0]);
    }

    @Override
    protected void preExit() {
        for (int i = 0; i < getChartFrame().getDatasetCount(); i++) {
            getChartFrame().getDataset(i).removeChangeListener(this);
        }
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);

        if (!b) {
            getChartFrame().toFront();
        }
    }

    @Override
    protected void windowLostFocusHandler(WindowEvent evt) {
    }

    @Override
    protected void tableMouseClickHandler(MouseEvent evt) {
    }

    @Override
    protected void tableMouseEnteredHandler(MouseEvent evt) {
    }

    @Override
    protected void tableMouseExitedHandler(MouseEvent evt) {
    }
}
