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
import OAT.data.Bar;
import OAT.util.MathUtil;

/**
 *
 * @author Antonio Yip
 */
public class ThreeMethods extends StrategyPlugin implements Calculator, Stopper {

    //Parameters
    protected int p_Method_Bars = 3;
    /**
     * The length ratio of body over shadow.
     */
    protected double p_Body_to_Shadow_Ratio = 0.5;

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
        return p_Method_Bars;
    }

    @Override
    public Trend getTrend() {
        return Trend.FOLLOWER;
    }

    @Override
    public Side trigger(List<Bar> descendingBars) {
//        List<Bar> descendingBars = getDescendingBars();

        if (descendingBars.size() < p_Method_Bars) {
            return Side.NEUTRAL;
        }

        Side side = Side.NEUTRAL;

        for (int i = 0; i < p_Method_Bars; i++) {
            Bar bar = descendingBars.get(i);

            if (MathUtil.getBodyToShadowRatio(bar) >= p_Body_to_Shadow_Ratio) {
                if (MathUtil.isWhite(bar)) {
                    if (i == 0) {
                        side = Side.LONG;

                    } else if (side != Side.LONG) {
                        return Side.NEUTRAL;

                    } else if (bar.getHigh() > descendingBars.get(i - 1).getHigh()
                            || bar.getLow() > descendingBars.get(i - 1).getLow()) {
//                            || bar.getHigh() > descendingBars.get(i - 1).getClose()) {
                        return Side.NEUTRAL;
                    }

                } else if (MathUtil.isBlack(bar)) {
                    if (i == 0) {
                        side = Side.SHORT;

                    } else if (side != Side.SHORT) {
                        return Side.NEUTRAL;

                    } else if (bar.getLow() < descendingBars.get(i - 1).getLow()
                            || bar.getHigh() < descendingBars.get(i - 1).getHigh()) {
//                            || bar.getLow() < descendingBars.get(i - 1).getClose()) {
                        return Side.NEUTRAL;
                    }

                } else {
                    return Side.NEUTRAL;
                }

            } else {
                return Side.NEUTRAL;
            }
        }

        return side;
    }

    @Override
    public void update(List<Bar> descendingBars) {
    }

    @Override
    public double calculateStopPrice(List<Bar> descendingBars) {
        return getLastTradeAdverseStop(1);
    }
}
