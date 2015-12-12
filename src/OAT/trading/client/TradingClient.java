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

import OAT.trading.OrderRecord;
import OAT.trading.Side;
import OAT.trading.thread.TradingThread;

/**
 *
 * @author Antonio Yip
 */
public interface TradingClient extends Connectable {

    public void reqExecutions(TradingThread tradingThread);

    public void reqOpenOrders(TradingThread tradingThread);

    public void placeOrder(TradingThread tradingThread, OrderRecord orderRecord, Side side, int qty, String orderType, double lmtPrice, double auxPrice);

    public boolean changeOrder(TradingThread tradingThread, OrderRecord orderRecord, Side side, int qty, String orderType, double lmtPrice, double auxPrice);

    public void cancelOrder(TradingThread tradingThread, int orderId);

    public int getNextOrderId(TradingThread tradingThread);
    
    public boolean isInterruptible();
}
