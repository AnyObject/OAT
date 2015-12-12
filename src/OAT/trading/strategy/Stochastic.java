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
import java.util.logging.Level;
import OAT.data.Bar;

/**
 *
 * @author Antonio Yip
 */
public class Stochastic extends StrategyPlugin implements Calculator {

    //Parameters
    protected int p_K_Bars = 5;
    protected int p_D_Bars = 15;
    protected double p_Over_Bought = 0.75;
    protected double p_Over_Sold = 0.25;
    protected int p_Trigger_Bars = 1;
//    protected boolean p_Ignore_Channel = true;
//    protected int p_Min_Bars = 5;
    //
//    protected ArrayList<Double> Ks = new ArrayList<Double>();
//    protected ArrayList<Double> Ds = new ArrayList<Double>();

    @Override
    public Trend getTrend() {
        return Trend.OSCILLATOR;
    }

    @Override
    public String[] getKeys() {
        return new String[]{"%K", "%D"};
    }

    @Override
    public Double[] getRange() {
        return new Double[]{0.0, 1.0, 0.2};
    }

    @Override
    public int getRequiredBars() {
        return p_K_Bars;
    }

    @Override
    public Side trigger(List<Bar> descendingBars) {
        List<Indicator> indicators = getDescendingIndicators(p_Trigger_Bars + 1);

        if (indicators.size() < p_Trigger_Bars + 1) {
            logOnce(Level.FINE, "Not enough trigger bars.");
            return Side.NEUTRAL;
        }

//        System.out.println(indicators.size());

        double lastK = indicators.get(indicators.size() - 1).getValue(0);
        double lastD = indicators.get(indicators.size() - 1).getValue(1);

        if (lastK >= p_Over_Bought
                && lastD >= p_Over_Bought
                && lastK > lastD) {

            for (int i = 0; i < indicators.size() - 1; i++) {
                Indicator indicator = indicators.get(i);
                double K = indicator.getValue(0);
                double D = indicator.getValue(1);

                if (K >= D) {
                    logOnce(Level.FINE, "Over-bought but %K still >= %D");
                    return Side.NEUTRAL;
                }
            }

//            if (p_Ignore_Channel && isLastTradeRunningOn(Channel.class, Side.LONG)) {
//                return Side.NEUTRAL;
//            }

            return Side.SHORT;
        }

        if (lastK <= p_Over_Sold
                && lastD <= p_Over_Sold
                && lastK < lastD) {
            for (int i = 0; i < indicators.size() - 1; i++) {
                Indicator indicator = indicators.get(i);
                double K = indicator.getValue(0);
                double D = indicator.getValue(1);

                if (K <= D) {
                    logOnce(Level.FINE, "Over-sold but %K still <= %D");
                    return Side.NEUTRAL;
                }
            }

//            if (p_Ignore_Channel && isLastTradeRunningOn(Channel.class, Side.SHORT)) {
//                return Side.NEUTRAL;
//            }

            return Side.LONG;
        }

        return Side.NEUTRAL;
    }

    @Override
    public void update(List<Bar> descendingBars) {
//        List<Bar> bars = getDescendingBars();
        List<Bar> bars = descendingBars;
        //filterBars(descendingBars, p_K_Bars);

        if (bars.isEmpty()) {
            return;
        }

        double K;
        double D;

        if (bars.size() < p_K_Bars) {
            K = Double.NaN;
        } else {
            Bar range = new Bar(bars);

            if (range.getHigh() == range.getLow()) {
                K = 0;
            } else {
                K = (bars.get(0).getClose() - range.getLow()) / (range.getHigh() - range.getLow());
            }
        }

        double sumK = K;
        int countK = 1;

        for (Indicator indicator : getDescendingIndicators()) {
            if (countK == p_D_Bars) {
                break;
            }

            sumK += indicator.getValue(0);
            countK++;
        }

        if (countK < p_D_Bars) {
            D = Double.NaN;
        } else {
            D = sumK / countK;
        }

        addIndicator(new Indicator(bars.get(0), new Double[]{K, D}));
    }
}
