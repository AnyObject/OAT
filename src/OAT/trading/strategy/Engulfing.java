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
import OAT.trading.Trend;
import OAT.trading.Calculator;
import OAT.trading.Side;
import java.util.List;
import java.util.logging.Level;
import OAT.data.Bar;
import OAT.util.MathUtil;

/**
 *
 * @author Antonio Yip
 */
public class Engulfing extends StrategyPlugin implements Calculator , Stopper {

    //Parameters
    /**
     * The number of previous bars to compare to form engulfing.
     */
    protected int p_Previous_Bars = 2;
    /**
     * Minimum length of the engulfing bar.
     */
    protected double p_Min_Length_Ratio = 2;
    protected double p_Min_Range = 0.004;

    @Override
    public String[] getKeys() {
        return null;
    }

    @Override
    public Double[] getRange() {
        return null;
    }

    @Override
    public int getRequiredBars() {
        return p_Previous_Bars + 2;
    }

    @Override
    public Side trigger(List<Bar> bars) {
//        List<Bar> bars = getDescendingBars(getRequiredBars());
//        List<Bar> bars = descendingBars;

        if (bars.size() < getRequiredBars()) {
            return Side.NEUTRAL;
        }

        //check fluctuation
        if (MathUtil.getFluctuation(bars.subList(2, bars.size())) < p_Min_Range) {
            logOnce(Level.FINE, "Fluctuation too small.");
            return Side.NEUTRAL;
        }

        if (bars.get(0).getHigh() <= bars.get(1).getHigh()
                || bars.get(0).getLow() >= bars.get(1).getLow()) {
            return Side.NEUTRAL;
        }

        if (bars.get(0).getHigh() - bars.get(0).getLow()
                < (bars.get(1).getHigh() - bars.get(1).getLow()) * p_Min_Length_Ratio) {
            return Side.NEUTRAL;
        }

        if (Math.abs(bars.get(0).getClose() - bars.get(0).getOpen())
                <= Math.abs(bars.get(1).getClose() - bars.get(1).getOpen())) {
            return Side.NEUTRAL;
        }

        if (MathUtil.isWhite(bars.get(0)) && MathUtil.isBlack(bars.get(1))) {
            for (int i = 2; i < p_Previous_Bars + 2; i++) {
                if (bars.get(i).getLow() < bars.get(i - 1).getLow()) {
                    return Side.NEUTRAL;
                }
            }

            return Side.LONG;

        } else if (MathUtil.isBlack(bars.get(0)) && MathUtil.isWhite(bars.get(1))) {
            for (int i = 2; i < p_Previous_Bars + 2; i++) {
                if (bars.get(i).getHigh() > bars.get(i - 1).getHigh()) {
                    return Side.NEUTRAL;
                }
            }

            return Side.SHORT;

        } else {
            return Side.NEUTRAL;
        }
    }

    @Override
    public void update(List<Bar> descendingBars) {
    }

    @Override
    public Trend getTrend() {
        return Trend.OSCILLATOR;
    }

    @Override
    public double calculateStopPrice(List<Bar> descendingBars) {
        return getLastTradeAdverseStop(1);
    }
}
