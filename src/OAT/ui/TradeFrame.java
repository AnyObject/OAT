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

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import OAT.event.OrderChangeEvent;
import OAT.event.PositionEnterEvent;
import OAT.event.PositionExitEvent;
import OAT.trading.OrderRecord;
import OAT.trading.Trade;
import OAT.ui.util.UiUtil;
import OAT.util.GeneralUtil;

/**
 *
 * @author Antonio Yip
 */
public class TradeFrame extends AbstractTableFrame {

    Trade trade;
    TradeDatasetFrame tradeDatasetFrame;

    public TradeFrame(TradeDatasetFrame component, Trade object) {
        super(component, object);
        this.trade = object;
        this.tradeDatasetFrame = component;

        setTitle("Trade");
    }

    @Override
    public void eventHandler(EventObject event) {
        if (event instanceof OrderChangeEvent
                || event instanceof PositionEnterEvent
                || event instanceof PositionExitEvent) {
            super.eventHandler(event);
        }
    }

    @Override
    protected void initTable() {
        theTable.setAutoCreateRowSorter(false);
        theTable.getTableHeader().setReorderingAllowed(false);

        for (int i = 0; i < theTable.getColumnCount(); i++) {
            theTable.getColumnModel().getColumn(i).
                    setCellRenderer(UiUtil.GENERAL_CELL_RENDERER);
        }

        tradeDatasetFrame.tradeFrames.add(this);
    }

    @Override
    protected boolean isUpdatingAll() {
        return true;
    }

    @Override
    protected Dimension getMaxSize() {
        return new Dimension(600, 600);
    }

    @Override
    protected Object[][] getRows() {
        List<Object[]> columns = new ArrayList<Object[]>();

        columns.add(new Object[]{
                    "Symbol",
                    "Order ID",
                    "Submitted",
                    "Side",
                    "Order Type",
                    "Order Price",
                    "Status",
                    "Executed",
                    "Filled Qty.",
                    "Remaining Qty.",
                    "Avg. Filled Price",
                    "Commission",
                    "Why Held"
                });

        OrderRecord[] orderRecords = new OrderRecord[]{
            trade.getEnterOrderRecord(),
            trade.getExitOrderRecord()};

        for (OrderRecord orderRecord : orderRecords) {
            if (orderRecord == null) {
                continue;
            }

            List<Object> values = new ArrayList<Object>();

            String symbol = null;
            try {
                symbol = orderRecord.getContractDetails().m_summary.m_localSymbol;
            } catch (Exception e) {
            }

            values.add(symbol);

            values.add(orderRecord.getOrderId());
            values.add(orderRecord.getSubmittedTime());
            values.add(orderRecord.getSide());
            values.add(orderRecord.getOrderType());
            values.add(orderRecord.getOrderPrice());
            values.add(orderRecord.getStatus());
            values.add(orderRecord.getExecutedTime());
            values.add(orderRecord.getFilled());
            values.add(orderRecord.getRemaining());
            values.add(orderRecord.getAvgFillPrice());
            values.add(orderRecord.getCommission());
            values.add(orderRecord.getWhyHeld());

            columns.add(values.toArray());
        }

        return GeneralUtil.transpose(columns.toArray(new Object[0][0]));
    }

    @Override
    protected Object[] getColumnHeaders() {
        return new Object[]{
                    "",
                    "Enter",
                    "Exit"
                };
    }

    @Override
    protected void preExit() {
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
