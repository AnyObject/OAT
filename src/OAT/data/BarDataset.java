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

import com.ib.client.ContractDetails;
import java.util.TimeZone;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.OHLCDataset;
import OAT.event.BarChartChangeEvent;
import OAT.trading.thread.DataThread;
import OAT.ui.util.Timeline;
import OAT.util.DateUtil;
import OAT.util.MathUtil;
import OAT.util.TextUtil;

/**
 *
 * @author Antonio Yip
 */
public abstract class BarDataset extends ChartDataset<Bar> implements OHLC, OHLCDataset {

    public static final String[] COLUMN_HEADERS = {
        "Time", "Open", "High", "Low", "Close", "Volume"};//, "Count", "WAP"};
    protected String localSymbol;
    protected String subTitle = "";
    protected int barSize;
    protected Tick lastTick;
    protected DataThread dataThread;
    private long lastUpdated;
//    private boolean broken;

    /**
     * Create a new bar chart.
     *
     * @param localSymbol
     * @param barSize
     * @param timeZone
     */
    public BarDataset(String localSymbol, int barSize, TimeZone timeZone) {
        this.localSymbol = localSymbol;
        this.barSize = barSize > 0 ? barSize : getDefaultBarSize();
        this.timeZone = timeZone;
        this.timeline = new Timeline(getTimeLineSegment(), timeZone);
//        this.timeline = Timeline.newDefaultDailyTimeline(Timeline.TICK_TIME_SEGMENT, timeZone);
//        setNewTimeline(true);
    }

    /**
     * Create a new bar chart.
     *
     * @param localSymbol
     * @param chartType
     * @param barSize
     * @param timeZone
     * @return
     */
    public static BarDataset newChart(String localSymbol, ChartType chartType, int barSize, TimeZone timeZone) {
        switch (chartType) {
            case TICK:
                return new TickChart(localSymbol, barSize, timeZone);
            case CONTRACT:
                return new ContractChart(localSymbol, barSize, timeZone);
            case TIME:
                return new TimeChart(localSymbol, barSize, timeZone);
            case REALTIME:
                return new RealTimeChart(localSymbol, timeZone);
            default:
                return null;
        }
    }

    /**
     * Create a new bar chart.
     *
     * @param contractDetails
     * @param chartType
     * @param barSize
     * @return
     */
    public static BarDataset newChart(ContractDetails contractDetails, ChartType chartType, int barSize) {
        if (contractDetails == null) {
            throw new UnsupportedOperationException("null contractDetails");
        }

        return newChart(
                contractDetails.m_summary.m_localSymbol,
                chartType,
                barSize,
                DateUtil.getTimeZone(contractDetails.m_timeZoneId));
    }

    /**
     * Create a new bar chart.
     *
     * @param datathread
     * @return
     */
    public static BarDataset newChart(DataThread datathread) {
        if (datathread == null) {
            throw new UnsupportedOperationException("null dataClient");
        }

        BarDataset barDataset = newChart(
                datathread.getContractDetails(),
                datathread.getChartType(),
                datathread.getBarSize());

        barDataset.dataThread = datathread;
        barDataset.addChangeListener(datathread);

        return barDataset;
    }

    /**
     * Create a new bar chart.
     *
     * @param contractDetails
     * @param chartType
     * @return
     */
    public static BarDataset newChart(ContractDetails contractDetails, ChartType chartType) {
        return newChart(contractDetails, chartType, 0);
    }

    /**
     * Get the smallest segment of the time line.
     *
     * @return
     */
    public abstract int getTimeLineSegment();

    public long getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public Double[] getAxisRange() {
        return null;
    }

    @Override
    public String getTitle() {
        return localSymbol + " @ " + getBarSizeStr() + subTitle;
    }

    @Override
    public Object getSource() {
        return dataThread;
    }

    /**
     * Return the subtitle
     *
     * @return
     */
    public String getSubTitle() {
        return subTitle;
    }

    /**
     *
     * @param chartType
     * @return
     */
    public static int getDefaultBarSize(ChartType chartType) {
        return newChart("", chartType, 0, null).getDefaultBarSize();
    }

    /**
     *
     * @param chartType
     * @param barSize
     * @return
     */
    public static String getUnit(ChartType chartType, int barSize) {
        return newChart("", chartType, barSize, null).getUnitPlural();
    }

    /**
     *
     * @return
     */
    public abstract int getDefaultBarSize();

    /**
     *
     * @return
     */
    public abstract String getUnit();

    /**
     *
     * @return
     */
    public String getUnitPlural() {
        if (barSize > 1) {
            return getUnit() + "s";
        } else {
            return getUnit();
        }
    }

    /**
     *
     * @return
     */
    public String getBarSizeStr() {
        return "" + barSize + " " + getUnitPlural();
    }

    /**
     *
     * @return
     */
    public Tick getLastTick() {
        return lastTick;
    }

    /**
     *
     * @return
     */
    public int getBarSize() {
        return barSize;
    }

    /**
     *
     * @return
     */
    public String getLocalSymbol() {
        return localSymbol;
    }

    /**
     *
     * @return
     */
    public TimeZone getTimeZone() {
        return timeZone;
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

        Bar bar = get(index);

        return new Object[]{
                    bar.getTime(),
                    bar.getOpen(),
                    bar.getHigh(),
                    bar.getLow(),
                    bar.getClose(),
                    bar.getVolume()
//                    bar.getTickCount(),
//                    bar.getWap()
                };
    }

    @Override
    public Object[] getRow(Bar bar) {
        int index = indexOf(bar);
        return getRow(index);
    }

    @Override
    public synchronized void clear() {
        super.clear();

        lastTick = null;
        resetTimeline(true);
    }

    @Override
    public synchronized void clear(long before) {
        super.clear(before);

        if (getLast() == null) {
            lastTick = null;
        } else {
            lastTick = new Tick(getLast().getClose(), getLast().getCloseTime());
        }
        updateTimeLine(true);
    }

    /**
     * Add a new bar from a tick.
     *
     * @param tick
     * @param notify
     * @return
     */
    public Bar add(Tick tick, boolean notify) {
        Bar prevBar = getLast();
        long roundedTime;
        Bar updatedBar;

        if (prevBar == null
                || tick.getTime() > prevBar.getOpenTime() + getTimeLineSegment()) {
            roundedTime = DateUtil.roundTime(tick.getTime(), getTimeLineSegment());
        } else {
            roundedTime = prevBar.getOpenTime() + getTimeLineSegment();
        }

        updatedBar = new Bar(roundedTime);
        updatedBar.addTick(tick);

        add(updatedBar, notify);

        return updatedBar;
    }

    /**
     *
     * @param tick
     * @param notify
     */
    public abstract void addTick(Tick tick, boolean notify);

    /**
     *
     * @param tick
     */
    public void addTick(Tick tick) {
        addTick(tick, true);
    }

    /**
     *
     * @param tickDataset
     * @param notify
     */
    public void addTicks(TickDataset tickDataset, boolean notify) {
        if (tickDataset != null) {
            for (Tick tick : tickDataset) {
                addTick(tick, false);
            }
        }

        if (notify) {
            fireDatasetChanged();
        }
    }

    @Override
    public double getClose() {
        if (isEmpty()) {
            return Double.NaN;
        }

        return getLast().getClose();
    }

    @Override
    public long getCloseTime() {
        if (isEmpty()) {
            return 0;
        }

        return getLast().getCloseTime();
    }

    @Override
    public double getHigh() {
        return MathUtil.getHigh(this);
    }

    @Override
    public long getHighTime() {
        return MathUtil.getHighTime(this);
    }

    @Override
    public double getLow() {
        return MathUtil.getLow(this);
    }

    @Override
    public long getLowTime() {
        return MathUtil.getLowTime(this);
    }

    @Override
    public double getOpen() {
        if (isEmpty()) {
            return Double.NaN;
        }

        return getFirst().getOpen();
    }

    @Override
    public long getOpenTime() {
        if (isEmpty()) {
            return 0;
        }

        return getFirst().getOpenTime();
    }

    @Override
    public double getWap() {
        double p = 0;
        long s = 0;

        for (Bar bar : this) {
            p += bar.getWap() * bar.getSize();
            s += bar.getSize();
        }

        return p / s;
    }

    @Override
    public int getTickCount() {
        int ticks = 0;

        for (Bar bar : this) {
            ticks += bar.getTickCount();
        }

        return ticks;
    }

    @Override
    public long getVolume() {
        long volume = 0;

        for (Bar bar : this) {
            volume += bar.getSize();
        }

        return volume;
    }

    @Override
    public double getPrice() {
        return getClose();
    }

    @Override
    public long getSize() {
        return getVolume();
    }

    @Override
    public long getTime() {
        return getOpenTime();
    }

    /**
     *
     * @param subTitle
     */
    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    /**
     *
     * @param updatedBar
     * @param lastPriceChanged
     * @param newBarAdded
     * @param notify
     * @param updateTimeline
     */
    protected void updateChart(Bar updatedBar, boolean lastPriceChanged, boolean newBarAdded, boolean notify, boolean updateTimeline) {
        lastUpdated = DateUtil.getTimeNow();

        //Update time line
        if (updateTimeline) {
            updateTimeLine(updatedBar, false);
        }

        //Notify strategy
        if (notify) {
            fireBarChartChanged(lastPriceChanged, newBarAdded);
        }
    }

    private void updateTimeLine(Bar bar, boolean notify) {
        if (timeline == null) {
            return;
        }

        Bar prevBar = getPrev(bar);

        if (prevBar == null) {
            return;
        }

        if (prevBar.getOpenTime() > 0
                && bar.getOpenTime() > prevBar.getOpenTime() + 2 * getTimeline().getSegmentSize()) {
            long execeptionFrom = prevBar.getOpenTime() + getTimeline().getSegmentSize();
            long execeptionTo = bar.getOpenTime() - getTimeline().getSegmentSize();

            if (timeline.containsDomainRange(execeptionFrom, execeptionTo)) {
                timeline.addException(execeptionFrom, execeptionTo);
            }
        }

        if (notify) {
            fireBarChartChanged(false, false);
        }
    }

    private void updateTimeLine(boolean notify) {
        resetTimeline(false);

        for (Bar bar : this) {
            updateTimeLine(bar, false);
        }

        if (notify) {
            fireBarChartChanged(false, false);
        }
    }

    private void resetTimeline(boolean notify) {
        timeline.clearExceptionSegments();

        if (notify) {
            fireDatasetChanged();
        }
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return localSymbol + " " + getBarSizeStr() + " " //+ getBarStructure() + " "
                + getOpen() + " " + getHigh() + " " + getLow() + " " + getClose() + " " + getVolume() + " "
                + TextUtil.SIMPLE_FORMATTER.format(getWap()) + " " + getItemCount() + " "
                + DateUtil.getCalendarDate((getLast().getOpenTime())).getTime() + " ";
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        if (!(obj instanceof BarDataset)) {
            return false;
        }

        BarDataset otherChart = (BarDataset) obj;

        if (localSymbol != null) {
            if (!localSymbol.equalsIgnoreCase(otherChart.localSymbol)) {
                return false;
            }
        } else if (otherChart.localSymbol != null) {
            return false;
        }

        if (barSize != otherChart.barSize) {
            return false;
        }

        if (dataThread != otherChart.dataThread) {
            return false;
        }

        return true;
    }

    @Override
    public String[] getKeys() {
        return new String[]{localSymbol};
    }

    @Override
    public Number getHigh(int series, int item) {
        if (series != 0) {
            return Double.NaN;
        }

        return get(item).getHigh();
    }

    @Override
    public double getHighValue(int series, int item) {
        return getHigh(series, item).doubleValue();
    }

    @Override
    public Number getLow(int series, int item) {
        if (series != 0) {
            return Double.NaN;
        }

        return get(item).getLow();
    }

    @Override
    public double getLowValue(int series, int item) {
        return getLow(series, item).doubleValue();
    }

    @Override
    public Number getOpen(int series, int item) {
        return get(item).getOpen();
    }

    @Override
    public double getOpenValue(int series, int item) {
        return getOpen(series, item).doubleValue();
    }

    @Override
    public Number getClose(int series, int item) {
        return get(item).getClose();
    }

    @Override
    public double getCloseValue(int series, int item) {
        return getClose(series, item).doubleValue();
    }

    @Override
    public Number getVolume(int series, int item) {
        if (series != 0) {
            return Double.NaN;
        }

        return get(item).getVolume();
    }

    @Override
    public double getVolumeValue(int series, int item) {
        return getVolume(series, item).doubleValue();
    }

    /**
     *
     * @param lastPriceChanged
     * @param newBarAdded
     */
    public void fireBarChartChanged(boolean lastPriceChanged, boolean newBarAdded) {
        notifyListeners(new BarChartChangeEvent(this, lastPriceChanged, newBarAdded));
        notifyListeners(new DatasetChangeEvent(this, this));
    }
}
