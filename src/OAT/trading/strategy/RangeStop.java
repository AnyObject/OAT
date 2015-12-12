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

import java.util.List;
import java.util.logging.Level;
import OAT.data.Bar;
import OAT.trading.Side;
import OAT.trading.Stopper;
import OAT.trading.StrategyPlugin;
import OAT.util.MathUtil;

/**
 *
 * @author Antonio Yip
 */
public class RangeStop extends StrategyPlugin implements Stopper {

    protected int p_Stop_Bars = 6;
//    protected double p_Stop_Midpoint = 0.5;

    @Override
    public String[] getKeys() {
//        return new String[]{"Stop"};
        return null;
    }

    @Override
    public Double[] getRange() {
        return null;
    }

    @Override
    public int getRequiredBars() {
        return p_Stop_Bars;
    }

    @Override
    public double calculateStopPrice(List<Bar> descendingBars) {
//        List<Bar> bars = getDescendingBars();
        List<Bar> bars = descendingBars;
        //filterBars(descendingBars, p_Stop_Bars);
        //descendingBars.subList(0, p_Stop_Bars);
        //getDescendingBars(p_Stop_Bars);
        double stop = Double.NaN;

        if (bars.size() == p_Stop_Bars) {
            Side currentSide = getTradingThread().getCurrentSide();
            double low = MathUtil.getLow(bars);
            double high = MathUtil.getHigh(bars);

            if (currentSide == Side.LONG) {
                stop = low;// + (high - low) * p_Stop_Midpoint;
//                addIndicator(new Indicator(getPrimaryChart().getLast(), stop));

            } else if (currentSide == Side.SHORT) {
                stop = high;// - (high - low) * p_Stop_Midpoint;
//                addIndicator(new Indicator(getPrimaryChart().getLast(), stop));

            } else {
                return Double.NaN;
            }

            log(Level.FINE, "Range [Lower, Upper] = [{0}, {1}], stop = {2}",
                    new Object[]{low, high, stop});
        }

        return stop;
    }
}
