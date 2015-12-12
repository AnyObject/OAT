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
import OAT.util.GeneralUtil;
import OAT.util.MathUtil;

/**
 *
 * @author Antonio Yip
 */
public class RSI extends StrategyPlugin implements Calculator {

    //Parameters
    protected int p_RSI_Bars = 10;
    protected int p_Range_Bars = 7;
    protected double p_Over_Bought = 0.8;
    protected double p_Over_Sold = 0.2;
    //

    @Override
    public Trend getTrend() {
        return Trend.OSCILLATOR;
    }

    @Override
    public String[] getKeys() {
        return new String[]{"RSI"};
    }

    @Override
    public Double[] getRange() {
        return new Double[]{0.0, 1.0, 0.2};
    }

    @Override
    public int getRequiredBars() {
        return Math.max(p_Range_Bars + 1, p_RSI_Bars);
    }

    @Override
    public Side trigger(List<Bar> descendingBars) {
        Indicator indicator = getLastIndicator();

        if (indicator == null) {
            return Side.NEUTRAL;
        }


//        if (bars.getItemCount() < p_RSI_Bars
//                || bars.isEmpty()) {
//            return Side.NEUTRAL;
//        }
//
//        double rsi = MathUtil.RSI(bars);
//
//        indicatorDataset.add(new Indicator(bars.get(0), rsi));

        double rsi = indicator.getValue(0);

//        List<Bar> rangeBars = getDescendingBars(p_RSI_Bars + 1);
        List<Bar> rangeBars = GeneralUtil.subListMaxSize(descendingBars, p_RSI_Bars + 1);
//        filterBars(descendingBars, p_RSI_Bars + 1);
        //getDescendingBars(p_Range_Bars + 1);
        List<Bar> bars = rangeBars.subList(0, 2);//getDescendingBars(2);

        rangeBars.remove(0);
        Bar range = new Bar(rangeBars);

        if (rsi >= p_Over_Bought) {
            if (!MathUtil.isBlack(bars.get(0))
                    && !MathUtil.isBlack(bars.get(1))
                    && bars.get(0).getHigh() <= range.getHigh()
                    && getTradingThread().getPrimaryChart().getLast().getOpen() <= range.getHigh()) {
                return Side.SHORT;
            }

        }

        if (rsi <= p_Over_Sold) {
            if (!MathUtil.isWhite(bars.get(0))
                    && !MathUtil.isWhite(bars.get(1))
                    && bars.get(0).getLow() >= range.getLow()
                    && getTradingThread().getPrimaryChart().getLast().getOpen() >= range.getLow()) {
                return Side.LONG;
            }
        }

        return Side.NEUTRAL;
    }

    @Override
    public void update(List<Bar> descendingBars) {
//        List<Bar> bars = getDescendingBars(p_RSI_Bars);
        List<Bar> bars = GeneralUtil.subListMaxSize(descendingBars, p_RSI_Bars);
        //filterBars(descendingBars, p_RSI_Bars);
        //descendingBars.subList(0, p_RSI_Bars);
        //getDescendingBars(p_RSI_Bars);

        if (bars.isEmpty()) {
            return;
        }

        if (bars.size() < p_RSI_Bars) {
            addIndicator(new Indicator(bars.get(0)));
            return;
        }

        addIndicator(new Indicator(bars.get(0), MathUtil.RSI(bars)));
    }
//    @Override
//    public double calculateStopPrice() {
//        ArrayList<Bar> bars = getTradingThread().getDescendingBars(p_RSI_Bars);
//
//        if (bars.barSize() >= p_RSI_Bars) {
//            rsi = MathUtil.RSI(bars);
//            Bar range = new Bar(getTradingThread().getDescendingBars(p_Range_Bars, true));
//            Side currentSide = getTradingThread().getCurrentSide();
//
//            if (currentSide == Side.LONG) {
//                if (rsi >= p_Over_Bought) {
//                    return getTradingThread().getPrimaryChart().getLast().open;
//
//                } else if (rsi <= p_Over_Sold) {
//                    return range.low;
//                }
//
//                return bars.get(0).low;
//
//            } else if (currentSide == Side.SHORT) {
//                if (rsi >= p_Over_Bought) {
//                    return range.high;
//
//                } else if (rsi <= p_Over_Sold) {
//                    return getTradingThread().getPrimaryChart().getLast().open;
//                }
//
//                return bars.get(0).high;
//            }
//        }
//
//        return Double.NaN;
//    }
}
