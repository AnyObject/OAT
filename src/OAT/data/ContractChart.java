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
import OAT.ui.util.Timeline;

/**
 *
 * @author Antonio Yip
 */
public class ContractChart extends BarDataset {

//    public static final int DEFAULT_BAR_SIZE = 500;
//    public static final String DEFAULT_UNIT = "contract";

    public ContractChart(String localSymbol, int barSize, TimeZone timeZone) {
        super(localSymbol, barSize, timeZone);
    }

    @Override
    public int getDefaultBarSize() {
        return 500;
    }

    @Override
    public String getUnit() {
        return "contract";
    }

    @Override
    public int getTimeLineSegment() {
        return Timeline.TICK_TIME_SEGMENT;
    }

    @Override
    public synchronized void addTick(Tick tick, boolean notify) {
        if (tick.getSize() <= 0 || tick.getPrice() <= 0) {
            return;
        }

        boolean lastPriceChanged = getClose() != tick.getPrice();
        lastTick = tick;
        int prevBarsCount = getItemCount();

        Bar prevBar = getLast();
        Bar updatedBar;

//        long roundedTime;
//        if (prevBar == null || tick.getTime() > prevBar.getOpenTime() + Timeline.TICK_TIME_SEGMENT) {
//            roundedTime = DateUtil.roundTime(tick.getTime(), Timeline.TICK_TIME_SEGMENT);
//        } else {
//            roundedTime = prevBar.getOpenTime() + Timeline.TICK_TIME_SEGMENT;
//        }

        if (isEmpty()
                || tick.isForcedNew()
                || (prevBar.getVolume() % barSize == 0 && prevBar.getHigh() != prevBar.getLow())) {
//            updatedBar = new Bar(roundedTime);
//            updatedBar.addTick(tick);
//            add(updatedBar, false);

            updatedBar = add(tick, false);
        } else {
//            if (prevBar.high == -1 || tick.getPrice() > prevBar.high) {
//                prevBar.high = tick.getPrice();
//                prevBar.highTime = tick.getTime();
//            }
//            if (prevBar.low == -1 || tick.getPrice() < prevBar.low) {
//                prevBar.low = tick.getPrice();
//                prevBar.lowTime = tick.getTime();
//            }
//            prevBar.close = tick.getPrice();

            long filledSize;

            if ((prevBar.getVolume() % barSize) + tick.getSize() <= barSize) {
                filledSize = tick.getSize();
            } else {
                filledSize = barSize - (prevBar.getVolume() % barSize);
            }

            Tick filledTick = new Tick(tick);
            filledTick.setSize(filledSize);

            Tick remainedTick = new Tick(tick);
            remainedTick.setSize(tick.getSize() - filledSize);

//            prevBar.wap = (prevBar.wap * (prevBar.volume % barSize) + tick.getPrice() * filledSize) / ((prevBar.volume % barSize) + filledSize);
//            prevBar.volume += filledSize;
//            prevBar.tickCount++;


            if (remainedTick.getSize() > 0) {
                prevBar.addTick(filledTick);
                updateChart(prevBar, lastPriceChanged, getItemCount() > prevBarsCount, false, false);

//                updatedBar = new Bar(roundedTime);
//                updatedBar.addTick(remainedTick);
//                add(updatedBar, false);

                updatedBar = add(remainedTick, false);
            } else {
                prevBar.addTick(tick);
                updatedBar = prevBar;
            }
        }

        updateChart(updatedBar, lastPriceChanged, getItemCount() > prevBarsCount, notify, true);
    }
}
