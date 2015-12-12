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

import java.util.Collection;
import OAT.util.MathUtil;

/**
 *
 * @author Antonio Yip
 */
public class Bar implements Chartable<Bar>, OHLC {

    private long openTime = 0;
    private long highTime = 0;
    private long lowTime = 0;
    private long closeTime = 0;
    private double open = -1;
    private double high = -1;
    private double low = -1;
    private double close = -1;
    private int tickCount = 0;
    private long volume = 0;
    private double wap = -1; //weighted average price
//    private boolean sessionBreak;

    public Bar() {
    }

    public Bar(Bar bar) {
        this.openTime = bar.openTime;
        this.open = bar.open;
        this.high = bar.high;
        this.highTime = bar.highTime;
        this.low = bar.low;
        this.lowTime = bar.lowTime;
        this.close = bar.close;
        this.closeTime = bar.closeTime;
        this.tickCount = bar.tickCount;
        this.volume = bar.volume;
        this.wap = bar.wap;

    }

    public Bar(Collection<? extends OHLC> prices) {
        this(MathUtil.getBar(prices));
    }

    public Bar(long openTime) {
        this.openTime = openTime;
    }

    /**
     * Create a bar with data.
     *
     * @param openTime
     * @param open
     * @param high
     * @param low
     * @param close
     * @param volume
     * @param wap
     * @param count
     */
    public Bar(long openTime, double open, double high, double low, double close, long volume, double wap, int count) {
        this.openTime = openTime;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.wap = wap;
        this.tickCount = count;
    }

    @Override
    public int getTickCount() {
        return tickCount;
    }

    @Override
    public long getVolume() {
        return volume;
    }

    @Override
    public double getWap() {
        return wap;
    }

    @Override
    public double getPrice() {
        return close;
    }

    @Override
    public long getSize() {
        return volume;
    }

    @Override
    public long getTime() {
        return openTime;
    }

    @Override
    public double getClose() {
        return close;
    }

    @Override
    public long getCloseTime() {
        return closeTime;
    }

    @Override
    public double getHigh() {
        return high;
    }

    @Override
    public long getHighTime() {
        return highTime;
    }

    @Override
    public double getLow() {
        return low;
    }

    @Override
    public long getLowTime() {
        return lowTime;
    }

    @Override
    public double getOpen() {
        return open;
    }

    @Override
    public long getOpenTime() {
        return openTime;
    }

//    public boolean isSessionBreak() {
//        return sessionBreak;
//    }

    public void addTick(Tick tick) {
        if (open == -1) {
            open = tick.getPrice();

            if (openTime == 0) {
                openTime = tick.getTime();
            }
        }

        if (high == -1 || tick.getPrice() > high) {
            high = tick.getPrice();
            highTime = tick.getTime();
        }

        if (low == -1 || tick.getPrice() < low) {
            low = tick.getPrice();
            lowTime = tick.getTime();
        }

        close = tick.getPrice();
        closeTime = tick.getTime();

        wap = (wap * volume + tick.getPrice() * tick.getSize()) / (volume + tick.getSize());
        volume = volume + tick.getSize();

//        if (tick.isForcedNew()) {
//            firstItem = true;
//        }

        tickCount++;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public void setTickCount(int tickCount) {
        this.tickCount = tickCount;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    public void setWap(double wap) {
        this.wap = wap;
    }

    public void setTime(long time) {
        this.closeTime = time;
    }

    public void setCloseTime(long closeTime) {
        this.closeTime = closeTime;
    }

    public void setHighTime(long highTime) {
        this.highTime = highTime;
    }

    public void setLowTime(long lowTime) {
        this.lowTime = lowTime;
    }

    public void setOpenTime(long openTime) {
        this.openTime = openTime;
    }

//    public void setSessionBreak(boolean sessionBreak) {
//        this.sessionBreak = sessionBreak;
//    }

    @Override
    public int compareTo(Bar o) {
        return (int) (getTime() - o.getTime());
    }

    @Override
    public double getX() {
        return closeTime;
    }

    @Override
    public double getX(int series) {
        return openTime;
    }

    @Override
    public double getY(int series) {
        return close;
    }

    public boolean isIn(long time) {
        return time >= openTime && time <= closeTime;
    }
}
