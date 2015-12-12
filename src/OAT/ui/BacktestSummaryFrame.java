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
import javax.swing.table.TableColumnModel;
import OAT.trading.Main;
import OAT.trading.thread.HybridStrategy;
import OAT.ui.util.UiUtil;
import OAT.util.GeneralUtil;

/**
 *
 * @author Antonio Yip
 */
public class BacktestSummaryFrame extends AbstractTableFrame {

    public BacktestSummaryFrame(Component component, Object[][] array) {
        super(component, array);

        setTitle("Backtest Summary");
    }

    @Override
    protected void initTable() {
        theTable.setAutoCreateRowSorter(true);
        theTable.getTableHeader().setReorderingAllowed(false);

        TableColumnModel columnModel = theTable.getColumnModel();
        columnModel.getColumn(0).setMaxWidth(30);
        columnModel.getColumn(1).setMinWidth(60);
        columnModel.getColumn(0).setCellRenderer(UiUtil.GENERAL_CELL_RENDERER);
        columnModel.getColumn(1).setCellRenderer(UiUtil.DOLLAR_CELL_RENDERER);
    }

    @Override
    protected boolean isUpdatingAll() {
        return true;
    }

    @Override
    protected Dimension getMaxSize() {
        return new Dimension(1000, 600);
    }

    @Override
    protected Object[] getColumnHeaders() {
        return ((Object[][]) theObject)[0];
    }

    @Override
    protected Object[][] getRows() {
        return GeneralUtil.subArray((Object[][]) theObject, 1, ((Object[][]) theObject).length);
    }

    @Override
    protected void preExit() {
        if (Main.backtestThread == null) {
            return;
        }

        for (int i = Main.backtestThread.breakdownFrames.size() - 1; i >= 0; i--) {
            Main.backtestThread.breakdownFrames.get(i).setVisible(false);
        }
        
//        while (Main.backtestThread.breakdownFrames.size() > 0) {
//            Main.backtestThread.breakdownFrames.get(0).setVisible(false);
//        }

        if (!Main.backtestFrame.isVisible() && Main.backtestThread.getEndTime() != 0) {
            Main.frame.removeLogTextPane(Main.backtestThread.getDefaultLoggerName());
            Main.frame.selectLogTextPane(Main.selectedStrategyId + 1);
        }
    }

    @Override
    protected void windowLostFocusHandler(WindowEvent evt) {
    }

    @Override
    protected void tableMouseClickHandler(MouseEvent evt) {
        int selectedRow = theTable.getSelectedRow();

        if (selectedRow < 0) {
            return;
        }

        int strategyId = Integer.parseInt(
                theTable.getValueAt(theTable.getSelectedRow(), 0).toString());
        int columnId = theTable.getSelectedColumn();
        HybridStrategy strategy = Main.backtestThread.getStrategy(strategyId, 0);

        if (evt.isAltDown()) {
            Main.backtestFrame.setVisible(true);
            Main.backtestFrame.setParameters(strategy);
            return;
        }

        if (evt.isControlDown()) {
            new ParametersFrame(this, strategy).setVisible(true);
            return;
        }

        if (columnId == 0) {
            Main.backtestThread.showPortfolioEquityChart(strategyId);

        } else if (columnId == 1) {
            Main.backtestThread.showEquityChart(strategyId);

        } else if (columnId == 4) {
            Main.backtestThread.showDayProfitsDistribution(strategyId);

        } else if (columnId == 11) {
            Main.backtestThread.showTradeProfitsDistribution(strategyId);

        } else {
            Main.backtestThread.showDayBreakdown(strategyId);
        }
    }

    @Override
    protected void tableMouseEnteredHandler(MouseEvent evt) {
    }

    @Override
    protected void tableMouseExitedHandler(MouseEvent evt) {
    }
//    @Override
//    protected void tableMouseMovedHandler(MouseEvent evt) {
////        int rowId = theTable.rowAtPoint(evt.getPoint());
////        int columnId = theTable.columnAtPoint(evt.getPoint());
////        int strategyId = Integer.parseInt(
////                theTable.getValueAt(rowId, 0).toString());
////
////        String paramString = Main.backtestThread.getStrategyVariables(strategyId).
////                replaceAll(", \\*", "\n");
////        
////        getCellRenderer(rowId, columnId).setToolTipText(paramString);
//    }
}
