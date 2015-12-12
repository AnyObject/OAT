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
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.table.TableColumnModel;
import OAT.trading.Main;
import OAT.trading.thread.HybridStrategy;
import OAT.ui.util.UiUtil;

/**
 *
 * @author Antonio Yip
 */
public class BacktestDayBreakdownFrame extends BacktestSummaryFrame {

    public int strategyId;
//    protected String toolTip;
    protected List<BarChartFrame> chartFrames = new ArrayList<BarChartFrame>();

    public BacktestDayBreakdownFrame(Object[][] object, int strategyId) {
        this(null, object, strategyId);
    }

    public BacktestDayBreakdownFrame(Component component, Object[][] object, int strategyId) {
        super(component, object);
        this.strategyId = strategyId;

        setTitle("Backtest Daily Breakdown [" + strategyId + "]");
    }

    @Override
    protected void initTable() {
        super.initTable();

        TableColumnModel columnModel = theTable.getColumnModel();
        columnModel.getColumn(1).setMinWidth(125);
        columnModel.getColumn(2).setMinWidth(125);
        columnModel.getColumn(1).setCellRenderer(UiUtil.DATE_TIME_CELL_RENDERER);
        columnModel.getColumn(2).setCellRenderer(UiUtil.DATE_TIME_CELL_RENDERER);
    }

    @Override
    protected void preExit() {
        for (int i = chartFrames.size() - 1; i >= 0; i--) {
            chartFrames.get(i).setVisible(false);
            chartFrames.remove(i);
        }
        
//        for (BarChartFrame chartFrame : chartFrames) {
//            chartFrame.setVisible(false);
//        }

        Main.backtestThread.breakdownFrames.remove(this);
    }

    @Override
    protected void tableMouseClickHandler(MouseEvent evt) {
        int dayId = Integer.parseInt(
                theTable.getValueAt(theTable.getSelectedRow(), 0).toString());


        HybridStrategy strategy = Main.backtestThread.getStrategy(strategyId, dayId);

        if (strategy.getPrimaryChart().isEmpty()) {
            Main.backtestThread.log(Level.INFO, "Incomplete data.");
            return;
        }

        BarChartFrame chartFrame = strategy.getPrimaryChartFrame();

        chartFrames.add(chartFrame);

        chartFrame.setVisible(true);

        if (!strategy.getTrades().isEmpty()) {
            new TradeDatasetFrame(chartFrame, strategy.getTrades()).setVisible(true);
        }
    }
//    @Override
//    protected void tableMouseEnteredHandler(MouseEvent evt) {
//        if (toolTip == null) {
//            toolTip = Main.backtestThread.getStrategyVariables(strategyId);
//        }
//    }
//    @Override
//    protected void tableMouseMovedHandler(MouseEvent evt) {
//        int rowId = theTable.rowAtPoint(evt.getPoint());
//        int columnId = theTable.columnAtPoint(evt.getPoint());
//
//        getCellRenderer(rowId, columnId).setToolTipText(toolTip);
//    }
}
