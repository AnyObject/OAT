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

package OAT.trading.client;

import OAT.trading.Main;
import OAT.trading.OrderRecord;
import OAT.trading.Side;
import OAT.trading.thread.BaseThread;
import OAT.trading.thread.TradingThread;
import OAT.util.ThreadLogger;

/**
 *
 * @author Antonio Yip
 */
public class BacktestTradingClient extends IbTradingClient {

    public BacktestTradingClient(BaseThread baseThread) {
        super(baseThread);
    }

    @Override
    public boolean isInterruptible() {
        return true;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void connect() {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public ThreadLogger getLogger() {
        return Main.backtestThread.getLogger();
    }

    @Override
    public String getLogPrefix() {
        return "";
    }

    @Override
    public int getClientId() {
        return 930;
    }

    @Override
    public void reqExecutions(TradingThread tradingThread) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void reqOpenOrders(TradingThread tradingThread) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void placeOrder(TradingThread tradingThread, OrderRecord orderRecord, Side side, int qty, String orderType, double lmtPrice, double auxPrice) {
        super.placeOrder(tradingThread, orderRecord, side, qty, orderType, lmtPrice, auxPrice);
        orderRecord.setStatus("Submitted");
    }

    @Override
    public boolean changeOrder(TradingThread tradingThread, OrderRecord orderRecord, Side side, int qty, String orderType, double lmtPrice, double auxPrice) {
        boolean b = super.changeOrder(tradingThread, orderRecord, side, qty, orderType, lmtPrice, auxPrice);
        orderRecord.setStatus("Submitted");
        return b;
    }

    @Override
    public void cancelOrder(TradingThread tradingThread, int orderId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
