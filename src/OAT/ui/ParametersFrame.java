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
import OAT.trading.StrategyPlugin;
import OAT.trading.thread.TradingThread;
import OAT.ui.util.UiUtil;

/**
 *
 * @author Antonio Yip
 */
public class ParametersFrame extends AbstractTableFrame {

    public ParametersFrame(Component component, TradingThread tradingThread) {
        super(component, tradingThread);

        setTitle(tradingThread.getSymbol() + " - " + tradingThread.getClass().getSimpleName());
    }

    public TradingThread getTradingThread() {
        return (TradingThread) theObject;
    }

    @Override
    protected void initTable() {
        theTable.setAutoCreateRowSorter(true);
        theTable.getTableHeader().setReorderingAllowed(false);

        TableColumnModel columnModel = theTable.getColumnModel();
        columnModel.getColumn(1).setCellRenderer(UiUtil.GENERAL_CELL_RENDERER);
        columnModel.getColumn(0).setMinWidth(150);

        getTradingThread().addChangeListener(this);
    }

    @Override
    protected boolean isUpdatingAll() {
        return true;
    }

    @Override
    protected Dimension getMaxSize() {
        return new Dimension(400, 600);
    }

    @Override
    protected Object[][] getRows() {
        Object[][] parametersArray = getTradingThread().getParametersArray();

        for (Object[] param : parametersArray) {
            if (param[1] instanceof String) {
                String s = (String) param[1];

                if (s.contains(StrategyPlugin.class.getPackage().getName())) {
                    String[] ss = (s).split("\\.");

                    if (ss.length > 0) {
                        s = ss[ss.length - 1];
                    }

                    s = "*" + s;
                }

                param[1] = s;
            }
        }

        return parametersArray;
    }

    @Override
    protected Object[] getColumnHeaders() {
        return new Object[]{"Variable", "Current Value"};
    }

    @Override
    protected void preExit() {
        getTradingThread().removeChangeListener(this);
    }

    @Override
    protected void windowLostFocusHandler(WindowEvent evt) {
        setVisible(false);
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
