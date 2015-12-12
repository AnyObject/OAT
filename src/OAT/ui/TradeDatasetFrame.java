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
import OAT.trading.Trade;
import OAT.trading.TradeDataset;
import OAT.ui.util.UiUtil;
import OAT.util.TextUtil;

/**
 *
 * @author Antonio Yip
 */
public class TradeDatasetFrame extends AbstractTableFrame {

    List<TradeFrame> tradeFrames = new ArrayList<TradeFrame>();

    public TradeDatasetFrame(Component component, TradeDataset tradeDataset) {
        super(component, tradeDataset);

        setTitle(tradeDataset.getTitle());
    }

    public TradeDataset getTrades() {
        return (TradeDataset) theObject;
    }

    @Override
    protected boolean isUpdatingAll() {
        return false;
    }

    @Override
    protected Dimension getMaxSize() {
        return new Dimension(830, 600);
    }

    @Override
    protected Object[][] getRows() {
        List<Object[]> rows = new ArrayList<Object[]>();

        for (Trade trade : getTrades()) {
            
//        }
//        for (ListIterator<Trade> listIterator = getTrades().iterator(); listIterator.hasNext();) {
//            Trade trade = listIterator.next();
            rows.add(new Object[]{
                        trade.getSide(),
                        trade.getEnterTime(),
                        trade.getEnterQty(),
                        trade.getEnterPrice(),
                        trade.getExitTime(),
                        trade.getExitQty(),
                        trade.getExitPrice(),
                        trade.getProfit(),
                        trade.getNetAmount(),
                        trade.getCreatedTime(),
                        TextUtil.toString(trade.getCalculations()),});
        }

        return rows.toArray(new Object[0][0]);
    }

    @Override
    protected Object[] getColumnHeaders() {
        return new Object[]{
                    "Side",
                    "Enter Time",
                    "Qty",
                    "Price",
                    "Exit Time",
                    "Qty",
                    "Price",
                    "Gross @",
                    "Net P/L @",
                    "Created",
                    "Calculators"
                };
    }

    @Override
    protected void initTable() {
        theTable.setAutoCreateRowSorter(false);
        theTable.getTableHeader().setReorderingAllowed(false);

        TableColumnModel columnModel = theTable.getColumnModel();

        columnModel.getColumn(0).setMinWidth(40);
        columnModel.getColumn(1).setMinWidth(125);
        columnModel.getColumn(2).setMaxWidth(40);
        columnModel.getColumn(3).setMinWidth(70);
        columnModel.getColumn(4).setMinWidth(125);
        columnModel.getColumn(5).setMaxWidth(40);
        columnModel.getColumn(6).setMinWidth(70);
        columnModel.getColumn(7).setMinWidth(70);
        columnModel.getColumn(8).setMinWidth(90);
        columnModel.getColumn(9).setMinWidth(125);

        columnModel.getColumn(1).setCellRenderer(UiUtil.CENTER_ALIGNED_GENERAL_CELL_RENDERER);
        columnModel.getColumn(2).setCellRenderer(UiUtil.SIZE_CELL_RENDERER);
        columnModel.getColumn(3).setCellRenderer(UiUtil.PRICE_CELL_RENDERER);
        columnModel.getColumn(4).setCellRenderer(UiUtil.CENTER_ALIGNED_GENERAL_CELL_RENDERER);
        columnModel.getColumn(5).setCellRenderer(UiUtil.SIZE_CELL_RENDERER);
        columnModel.getColumn(6).setCellRenderer(UiUtil.PRICE_CELL_RENDERER);
        columnModel.getColumn(7).setCellRenderer(UiUtil.PRICE_CHANGE_CELL_RENDERER);
        columnModel.getColumn(8).setCellRenderer(UiUtil.CURRENCY_CELL_RENDERER);
        columnModel.getColumn(9).setCellRenderer(UiUtil.CENTER_ALIGNED_GENERAL_CELL_RENDERER);
    }

    @Override
    protected void preExit() {
        for (int i = tradeFrames.size() - 1; i >= 0; i--) {
           tradeFrames.get(i).setVisible(false);
           tradeFrames.remove(i);
        }
        
//        for (Iterator<TradeFrame> it = tradeFrames.iterator(); it.hasNext();) {
//            it.next().setVisible(false);
//            it.remove();
//        }
    }

    @Override
    protected void windowLostFocusHandler(WindowEvent evt) {
        for (TradeFrame tradeFrame : tradeFrames) {
            if (tradeFrame.isVisible() || tradeFrame.isActive()) {
                return;
            }
        }

        setVisible(false);
    }

    @Override
    protected void tableMouseClickHandler(MouseEvent evt) {
        int index = theTable.getSelectedRow();

        new TradeFrame(this, getTrades().get(index)).setVisible(true);
    }

    @Override
    protected void tableMouseEnteredHandler(MouseEvent evt) {
    }

    @Override
    protected void tableMouseExitedHandler(MouseEvent evt) {
    }
}
