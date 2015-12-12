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

package OAT.trading;

import java.util.EventObject;
import OAT.data.Bar;
import OAT.data.ChartDataset;
import OAT.event.GenericListener;
import OAT.trading.thread.TradingThread;

/**
 *
 * @author Antonio Yip
 */
public class TradeDataset extends ChartDataset<Trade> implements GenericListener {

    public static final String[] KEYS = new String[]{"Enter", "Exit"};
    private TradingThread tradingThread;

    public TradeDataset(TradingThread tradingClient) {
        this.tradingThread = tradingClient;
    }

    @Override
    public String getTitle() {
        return tradingThread.getSymbol() + " Trades";
    }

    @Override
    public Double[] getAxisRange() {
        return null;
    }

    @Override
    public String[] getKeys() {
        return KEYS;
    }

    @Override
    public Object getSource() {
        return tradingThread;
    }

//    @Override
//    public Number getX(int series, int item) {
//        if (!items.isEmpty() && currentSession.isSameDay(items.get(item).getX(series))) {
//            return super.getX(series, item);
//        } else {
//            return Double.NaN;
//        }
//    }
//
//    @Override
//    public Number getY(int series, int item) {
//        if (currentSession.isSameDay(items.get(item).getX(series))) {
//            return super.getY(series, item);
//        } else {
//            return Double.NaN;
//        }
//    }
    @Override
    public Object[] getRow(Bar bar) {
        Object[] array = new Object[getSeriesCount()];


//        for (Iterator<Trade> it = descendingIterator(); it.hasNext();) {
//            Trade trade = it.next();

        for (Trade trade : getDescendingItems()) {
            if (trade.getEnterBar() == bar
                    || (trade.getEnterBar() == null
                    && bar.isIn(trade.getEnterTime()))) {
                if (trade.getEnterPrice() > 0) {
                    array[0] = trade.getEnterPrice() * trade.getSide().sign;
                }
            }

            if (trade.getExitBar() == bar
                    || (trade.getEnterBar() == null
                    && bar.isIn(trade.getExitTime()))) {
                if (trade.getExitPrice() > 0) {
                    array[1] = trade.getExitPrice() * -trade.getSide().sign;
                }
            }
        }

        return array;
    }

    public boolean isLast(Object o) {
        if (getLast() == null) {
            return false;
        }
        return getLast().equals(o);
    }

    @Override
    public void eventHandler(EventObject event) {
        if (isLast(event.getSource())) {
            tradingThread.eventHandler(event);
        }

        super.eventHandler(event);
    }
//    public void removeLast() {
//        if (!isEmpty()) {
//            items.remove(items.getItemCount() - 1);
//        }
//    }

  
}
