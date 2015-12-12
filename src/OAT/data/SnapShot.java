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

package OAT.data;

import com.ib.client.TickType;
import OAT.event.GenericListener;
import OAT.event.Listenable;
import OAT.event.MarketDataChangeEvent;
import OAT.util.DateUtil;
import OAT.util.Tabulable;

/**
 *
 * @author Antonio Yip
 */
public final class SnapShot extends Listenable implements Price, Tabulable {

    public static final String[] COLUMN_HEADERS = {
        "Symbol", "Size", "Bid", "Ask", "Size", "Last", "L.Size",
        "Change", "Volume", "Open", "High", "Low"};
    private double close = -1, open = -1, high = -1, low = -1, bid = -1,
            ask = -1, last = -1, change = 0, sessionOpen = -1;
    private int bidSize = 0, askSize = 0, lastSize = 0;
    private long volume = 0;
    private String symbol = "";
    private String lastTimeStamp = "";

    public SnapShot(GenericListener listener) {
        addChangeListener(listener);
    }

    public static enum Field {

        SYMBOL(-1, 0),
        BID_SIZE(TickType.BID_SIZE, 1),
        BID(TickType.BID, 2),
        ASK(TickType.ASK, 3),
        ASK_SIZE(TickType.ASK_SIZE, 4),
        LAST(TickType.LAST, 5),
        LAST_SIZE(TickType.LAST_SIZE, 6),
        CHANGE(-1, 7),
        VOLUME(TickType.VOLUME, 8),
        OPEN(TickType.OPEN, 9),
        HIGH(TickType.HIGH, 10),
        LOW(TickType.LOW, 11),
        PREV_CLOSE(TickType.CLOSE, -1),
        SESSION_OPEN(-1, -1);
        public final int tickType;
        public final int column;

        Field(int tickType, int column) {
            this.tickType = tickType;
            this.column = column;
        }

        public String simpleName() {
            switch (this) {
                case CHANGE:
                    return "CHG";
                case VOLUME:
                    return "VOL";
                default:
                    return this.name();
            }
        }
    }

    @Override
    public String[] getColumnHeaders() {
        return COLUMN_HEADERS;
    }

    @Override
    public Object[] getRow(int index) {
        if (index >= getItemCount()) {
            return null;
        }

        return new Object[]{
                    symbol, bidSize, bid, ask, askSize, last, lastSize,
                    change, volume, open, high, low};
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public void clear() {
        setClose(-1);
        setOpen(-1);
        setHigh(-1);
        setLow(-1);
        setBid(-1);
        setAsk(-1);
        setLast(-1);
        setChange(0);
        setBidSize(0);
        setAskSize(0);
        setLastSize(0);
        setVolume(0);
        setLastTimeStamp("");
    }

    @Override
    public double getPrice() {
        return getLast();
    }

    @Override
    public long getSize() {
        return getVolume();
    }

    @Override
    public long getTime() {
        return DateUtil.getTime(getLastTimeStamp());
    }

    public double getAsk() {
        return ask;
    }

    public int getAskSize() {
        return askSize;
    }

    public double getBid() {
        return bid;
    }

    public int getBidSize() {
        return bidSize;
    }

    public double getChange() {
        return change;
    }

    public double getClose() {
        return close;
    }

    public double getHigh() {
        return high;
    }

    public double getLast() {
        return last;
    }

    public int getLastSize() {
        return lastSize;
    }

    public String getLastTimeStamp() {
        return lastTimeStamp;
    }

    public double getLow() {
        return low;
    }

    public double getMidPoint() {
        return (ask + bid) / 2;
    }

    public double getOpen() {
        return open;
    }

    public double getSessionOpen() {
        return sessionOpen;
    }

    public String getSymbol() {
        return symbol;
    }

    public long getVolume() {
        return volume;
    }

    public void setAsk(double ask) {
        if (this.ask == ask) {
            return;
        }

        this.ask = ask;
        fireMarketDataChanged(Field.ASK, ask);
    }

    public void setAskSize(int askSize) {
        if (this.askSize == askSize) {
            return;
        }

        this.askSize = askSize;
        fireMarketDataChanged(Field.ASK_SIZE, askSize);
    }

    public void setBid(double bid) {
        if (this.bid == bid) {
            return;
        }

        this.bid = bid;
        fireMarketDataChanged(Field.BID, bid);
    }

    public void setBidSize(int bidSize) {
        if (this.bidSize == bidSize) {
            return;
        }

        this.bidSize = bidSize;
        fireMarketDataChanged(Field.BID_SIZE, bidSize);
    }

    public void setChange(double change) {
        if (this.change == change) {
            return;
        }

        this.change = change;
        fireMarketDataChanged(Field.CHANGE, change);
    }

    public void setClose(double close) {
        if (this.close == close) {
            return;
        }

        this.close = close;
        fireMarketDataChanged(Field.PREV_CLOSE, close);
    }

    public void setHigh(double high) {
        if (this.high == high) {
            return;
        }

        this.high = high;
        fireMarketDataChanged(Field.HIGH, high);
    }

    public void setLast(double last) {
        if (this.last == last) {
            return;
        }

        this.last = last;
        fireMarketDataChanged(Field.LAST, last);
    }

    public void setLastSize(int lastSize) {
        if (this.lastSize == lastSize) {
            return;
        }

        this.lastSize = lastSize;
        fireMarketDataChanged(Field.LAST_SIZE, lastSize);
    }

    public void setLastTimeStamp(String lastTimeStamp) {
        this.lastTimeStamp = lastTimeStamp;
    }

    public void setLow(double low) {
        if (this.low == low) {
            return;
        }

        this.low = low;
        fireMarketDataChanged(Field.LOW, low);
    }

    public void setOpen(double open) {
        if (this.open == open) {
            return;
        }

        this.open = open;
        fireMarketDataChanged(Field.OPEN, open);
    }

    public void setSessionOpen(double sessionOpen) {
        if (this.sessionOpen == sessionOpen) {
            return;
        }

        this.sessionOpen = sessionOpen;
        fireMarketDataChanged(Field.SESSION_OPEN, sessionOpen);
    }

    public void setSymbol(String symbol) {
        if (this.symbol.equals(symbol)) {
            return;
        }

        this.symbol = symbol;
        fireMarketDataChanged(Field.SYMBOL, symbol);
    }

    public void setVolume(long volume) {
        if (this.volume == volume) {
            return;
        }

        this.volume = volume;
        fireMarketDataChanged(Field.VOLUME, volume);
    }

    protected void fireMarketDataChanged(Field field, Object value) {
        notifyListeners(new MarketDataChangeEvent(this, field, value));
    }
}
