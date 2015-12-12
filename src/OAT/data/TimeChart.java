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

import java.util.TimeZone;
import OAT.util.DateUtil;

/**
 *
 * @author Antonio Yip
 */
public class TimeChart extends BarDataset {

//    public static final int DEFAULT_BAR_SIZE = 300000; //5 minutes
//    public static final String DEFAULT_UNIT = "ms";
//    private long lastUpdated;

    public TimeChart(String localSymbol, int barSize, TimeZone timeZone) {
        super(localSymbol, barSize, timeZone);
    }

  

    @Override
    public int getDefaultBarSize() {
        return 300000; //5 minutes
    }

    @Override
    public String getUnit() {
        return "ms";
    }

    @Override
    public int getTimeLineSegment() {
        return barSize;
    }

    @Override
    public String getUnitPlural() {
        return getUnit(); //no s
    }

    @Override
    public String getBarSizeStr() {
        return DateUtil.getSimpleDurationStr(barSize);
    }

    @Override
    public synchronized boolean add(Bar bar, boolean notify) {
        if (bar.getOpenTime() % barSize != 0) {
            return false; // incompatible barSize or openTime
        }

        boolean lastPriceChanged = getClose() != bar.getPrice();
        int prevBarsCount = getItemCount();

        Bar updatedBar;
        boolean b = super.add(bar, false);

        updatedBar = bar;
        updateChart(updatedBar,
                lastPriceChanged,
                getItemCount() > prevBarsCount,
                notify,
                getPrev(bar) != null
                && bar.getOpenTime() - getPrev(bar).getOpenTime() != barSize);

        return b;
    }

    @Override
    public void addTick(Tick tick, boolean notify) {
        if (tick.getSize() <= 0 || tick.getPrice() <= 0) {
            return;
        }

        boolean hasDataThread = dataThread != null;
        boolean lastPriceChanged = getClose() != tick.getPrice();
        lastTick = tick;
        int prevBarsCount = getItemCount();

        Bar prevBar = getLast();
        Bar updatedBar;

        if (isEmpty()
                || tick.getTime() >= prevBar.getOpenTime() + barSize) {

            updatedBar = add(tick, false);
        } else {
            prevBar.addTick(tick);
            updatedBar = prevBar;
        }

        updateChart(updatedBar, lastPriceChanged, getItemCount() > prevBarsCount, notify, hasDataThread);
    }
}
