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

package OAT.trading.strategy;

import OAT.trading.StrategyPlugin;
import OAT.trading.Indicator;
import OAT.trading.Trend;
import OAT.trading.Calculator;
import OAT.trading.Side;
import java.util.List;
import OAT.data.Bar;
import OAT.util.MathUtil;

/**
 *
 * @author Antonio Yip
 * @deprecated 
 */
public class Pattern extends StrategyPlugin implements Calculator {

    //Parameters
    protected int p_Pattern_Bars = 3;
    protected double p_Midpoint_Trigger = 1;
    protected int p_Break_Bars = 12;

    //
//    protected transient boolean isHigher, isLower;
//    protected transient double longTrigger = Double.MAX_VALUE;
//    protected transient double shortTrigger = Double.MIN_VALUE;
    @Override
    public Trend getTrend() {
        return Trend.FOLLOWER;
    }

    @Override
    public String[] getKeys() {
        return new String[]{"Pattern"};
    }

    @Override
    public Double[] getRange() {
        return new Double[]{-1.0, 1.0, 1.0};
    }

    @Override
    public int getRequiredBars() {
        return Math.max(p_Break_Bars + 1, p_Pattern_Bars);
    }

    @Override
    public Side trigger(List<Bar> descendingBars) {
        Indicator indicator = getLastIndicator();

        if (indicator == null) {
            return Side.NEUTRAL;
        }

        double signal = indicator.getValue(0);

        if (signal == 1) {
            return Side.LONG;
        } else if (signal == -1) {
            return Side.SHORT;
        }

        return Side.NEUTRAL;
    }

    @Override
    public void update(List<Bar> descendingBars) {
//        List<Bar> bars = getDescendingBars();
        List<Bar> bars = descendingBars;
        //filterBars(descendingBars, getRequiredBars());
        //descendingBars.subList(0, getRequiredBars());
        //getDescendingBars(Math.max(p_Break_Bars + 1, p_Pattern_Bars));

        if (bars.isEmpty()) {
            return;
        }

        if (bars.size() < p_Break_Bars + 1
                || bars.size() < p_Pattern_Bars) {
            addIndicator(new Indicator(bars.get(0)));
            return;
        }

        if (isHigher(bars)) {
            addIndicator(new Indicator(bars.get(0), 1D));
        } else if (isLower(bars)) {
            addIndicator(new Indicator(bars.get(0), -1D));
        }
    }

//    @Override
//    public double calculateStopPrice() {
//        ArrayList<Bar> stopBars = getTradingThread().getDescendingBars((int) Math.ceil(stopBarsCount));
//
//        double factor = stopBarsCount % 1;
//
//        if (!stopBars.isEmpty()) {
//            Side currentSide = getTradingThread().getCurrentSide();
//
//            if (currentSide == Side.LONG) {
//                double low = Double.MAX_VALUE;
//
//                for (int i = 0; i < stopBars.barSize(); i++) {
//                    if (factor > 0 && i == stopBars.barSize() - 1) {
//                        Bar b = stopBars.get(i);
//                        low = Math.min(low, b.high - (b.high - b.low) * factor);
//                        continue;
//                    }
//
//                    low = Math.min(low, stopBars.get(i).low);
//                }
//
//                return low;
//
//            } else if (currentSide == Side.SHORT) {
//                double high = Double.MIN_VALUE;
//
//                for (int i = 0; i < stopBars.barSize(); i++) {
//                    if (factor > 0 && i == stopBars.barSize() - 1) {
//                        Bar b = stopBars.get(i);
//                        high = Math.max(high, b.low + (b.high - b.low) * factor);
//                        continue;
//                    }
//
//                    high = Math.max(high, stopBars.get(i).high);
//                }
//
//                return high;
//            }
//        }
//
//        return Double.NaN;
//    }
    protected boolean isHigher(List<Bar> bars) {
        double midPoint = MathUtil.getMidPoint(bars.get(0), p_Midpoint_Trigger);

        for (int i = 1; i < p_Pattern_Bars; i++) {
            //all other bars are white
            //if (!MathUtil.isWhite(bars.get(i))) {
            //    return false;
            //}

            //all other bars cannot be black
            if (MathUtil.isBlack(bars.get(i))) {
                return false;
            }
        }

        for (int i = 0; i < p_Pattern_Bars - 1; i++) {
            //last bar's high is the highest
            if (bars.get(0).getHigh() <= bars.get(i + 1).getHigh()) {
                return false;
            }

            //all bars higher than or equals to first bar's low
            if (bars.get(i).getLow() < bars.get(p_Pattern_Bars - 1).getLow()) {
                return false;
            }
            //if (bars.get(i).high < bars.get(bars.barSize() - 1).high) {
            //    return false;
            //}
        }

        Bar breakRange = new Bar(bars.subList(1, p_Break_Bars + 1));

        if (bars.get(0).getHigh() < breakRange.getHigh()) {
            return false;
        }

        if (bars.get(0).getClose() < bars.get(1).getClose() // last bar's close above prev bar's close
                || bars.get(0).getClose() < midPoint //last bar's close above its midpoint
                || !MathUtil.isWhite(bars.get(0))
                || getTradingThread().getLastTickPrice() < bars.get(1).getClose()
                || getTradingThread().getLastTickPrice() < midPoint) { //market price higher than trigger price
//            longTrigger = bars.get(0).high;

            return false;
        }

        return true;
    }

    protected boolean isLower(List<Bar> bars) {
        double midPoint = MathUtil.getMidPoint(bars.get(0), 1 - p_Midpoint_Trigger);

        for (int i = 1; i < p_Pattern_Bars; i++) {
            //all other bars are black
            //if (!MathUtil.isBlack(bars.get(i))) {
            //    return false;
            //}

            //all other bars cannot be white
            if (MathUtil.isWhite(bars.get(i))) {
                return false;
            }
        }

        for (int i = 0; i < p_Pattern_Bars - 1; i++) {
            //last bar's low is the lowest
            if (bars.get(0).getLow() >= bars.get(i + 1).getLow()) {
                return false;
            }

            //all bars lower than or equals first bar's high
            if (bars.get(i).getHigh() > bars.get(p_Pattern_Bars - 1).getHigh()) {
                return false;
            }
            //if (bars.get(i).low > bars.get(bars.barSize() - 1).low) {
            //    return false;
            //}
        }

        Bar breakRange = new Bar(bars.subList(1, p_Break_Bars + 1));

        if (bars.get(0).getLow() > breakRange.getLow()) {
            return false;
        }

        if (bars.get(0).getClose() > bars.get(1).getClose() // last bar's close below prev bar's close
                || bars.get(0).getClose() > midPoint //last bar's close below its midpoint
                || !MathUtil.isBlack(bars.get(0))
                || getTradingThread().getLastTickPrice() > bars.get(1).getClose()
                || getTradingThread().getLastTickPrice() > midPoint) { //market price lower than trigger price
//            shortTrigger = bars.get(0).low;

            return false;
        }

        return true;
    }
}
