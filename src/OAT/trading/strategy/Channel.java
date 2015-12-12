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
import OAT.trading.Stopper;
import OAT.trading.Indicator;
import OAT.trading.Trend;
import OAT.trading.Calculator;
import OAT.trading.Side;
import java.util.List;
import java.util.logging.Level;
import OAT.data.Bar;
import OAT.util.GeneralUtil;
import OAT.util.MathUtil;

/**
 *
 * @author Antonio Yip
 */
public class Channel extends StrategyPlugin implements Calculator, Stopper {

    //Parameters
    protected int p_Channel_Bars = 7;
    protected int p_Min_Bars = 7;
    protected int p_Confirm_Bars = 0;
    protected double p_Min_Range = 0.004;
    protected int p_Stop_Bars = 1;
//    protected int p_OBV_Trigger = 500;

    @Override
    public Trend getTrend() {
        return Trend.FOLLOWER;
    }

    @Override
    public String[] getKeys() {
        return new String[]{"Lower", "Upper"};
    }

    @Override
    public Double[] getRange() {
        return null;
    }

    @Override
    public int getRequiredBars() {
        return Math.max(p_Min_Bars, p_Channel_Bars);
    }

    @Override
    public Side trigger(List<Bar> descendingBars) {
//        List<Bar> bars = getDescendingBars();

        if (descendingBars.size() < p_Min_Bars) {
            return Side.NEUTRAL;
        }

        List<Bar> channelBars = GeneralUtil.subListMaxSize(descendingBars, p_Channel_Bars);
//        bars.subList(0, Math.min(bars.size(), p_Channel_Bars));

        if (channelBars.isEmpty()) {
            return Side.NEUTRAL;
        }

        Indicator indicator = getLastIndicator();

        if (indicator == null) {
            return Side.NEUTRAL;
        }

//        if (Math.abs(getTradingThread().getObv()) < p_OBV_Trigger) {
//            logOnce(Level.FINE, "OBV too small.");
//            return Side.NEUTRAL;
//        } else {
//        }

        double lowerTrigger = indicator.getValue(0);
        double upperTrigger = indicator.getValue(1);

//        long lowTime = MathUtil.getLowTime(channelBars);
//        long highTime = MathUtil.getHighTime(channelBars);

        //check fluctuation
        if (MathUtil.getFluctuation(upperTrigger, lowerTrigger) < p_Min_Range) {
            logOnce(Level.FINE, "Fluctuation too small.");
            return Side.NEUTRAL;
        }

//        if ((lowTime < highTime && upperTrigger / lowerTrigger - 1 < p_Min_Range)
//                || (highTime < lowTime && 1 - lowerTrigger / upperTrigger < p_Min_Range)) {
//            return Side.NEUTRAL;
//        }

        double lastPrice = getPrimaryChart().getLastTick().getPrice();
        List<Bar> confirmBars = channelBars.subList(0, p_Confirm_Bars); //getDescendingBars(p_Confirm_Bars);
//        double dayHigh = getPrimaryChart().getHigh();
//        double dayLow = getPrimaryChart().getLow();

        if (lastPrice < lowerTrigger) {
            for (Bar bar : confirmBars) {
                if (getIndicator(bar) == null || bar.getLow() >= getIndicator(bar).getValue(0)) {
                    logOnce(Level.FINE, "Not enough confirm bars.");
                    return Side.NEUTRAL;
                }
            }

            return Side.SHORT;

        } else if (lastPrice > upperTrigger) {
            for (Bar bar : confirmBars) {
                if (getIndicator(bar) == null || bar.getHigh() <= getIndicator(bar).getValue(1)) {
                    logOnce(Level.FINE, "Not enough confirm bars.");
                    return Side.NEUTRAL;
                }
            }

            return Side.LONG;
        }

        return Side.NEUTRAL;
    }

    @Override
    public void update(List<Bar> descendingBars) {
//        List<Bar> bars = getDescendingBars(p_Channel_Bars);
        List<Bar> bars = GeneralUtil.subListMaxSize(descendingBars, p_Channel_Bars);

        if (getPrimaryChart().isEmpty()) {
            return;
        }

        Indicator indicator;

        if (bars.size() < p_Channel_Bars) {
            indicator = new Indicator(getPrimaryChart().getLast());

        } else {
            indicator = new Indicator(getPrimaryChart().getLast(),
                    MathUtil.getLow(bars), MathUtil.getHigh(bars));
        }

        addIndicator(indicator);
    }

    @Override
    public double calculateStopPrice(List<Bar> descendingBars) {
        return getLastTradeAdverseStop(p_Stop_Bars);
    }
}
