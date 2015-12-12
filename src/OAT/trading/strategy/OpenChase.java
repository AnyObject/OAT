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
import OAT.data.Bar;
import OAT.trading.Calculator;
import OAT.trading.Side;
import OAT.trading.StrategyPlugin;
import OAT.trading.Trend;
import OAT.util.GeneralUtil;
import OAT.util.MathUtil;

/**
 *
 * @author Antonio Yip
 */
public class OpenChase extends StrategyPlugin implements Calculator {

    protected int p_Chase_Bar = 4;
    protected int p_Max_Bar = 6;
    protected boolean p_Same_color_Bars = false;

    @Override
    public Trend getTrend() {
        return Trend.FOLLOWER;
    }

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
        return Math.max(p_Chase_Bar, p_Max_Bar + 1);
    }

    @Override
    public Side trigger(List<Bar> descendingBars) {
//        List<Bar> bars = getDescendingBars(p_Max_Bar + 1);
        List<Bar> bars = GeneralUtil.subListMaxSize(descendingBars, p_Max_Bar + 1);
        //filterBars(descendingBars, p_Max_Bar + 1);
        //descendingBars.subList(0, p_Max_Bar + 1);
        //getDescendingBars(p_Max_Bar + 1);

        if (bars.size() < p_Chase_Bar || bars.size() > p_Max_Bar) {
            return Side.NEUTRAL;
        }

        Bar lastBar = bars.get(0);
        double midPoint = MathUtil.getMidPoint(lastBar, 0.5);

        if (MathUtil.isWhite(lastBar)
                && lastBar.getClose() > midPoint) {
            for (int i = 1; i < p_Chase_Bar; i++) {
                if (p_Same_color_Bars && !MathUtil.isWhite(bars.get(i))) {
                    return Side.NEUTRAL;
                }

                if (bars.get(i - 1).getHigh() < bars.get(i).getHigh() // || bars.get(i - 1).getLow() < bars.get(i).getLow()
                        ) {
                    return Side.NEUTRAL;
                }
            }

            return Side.LONG;

        } else if (MathUtil.isBlack(lastBar)
                && lastBar.getClose() < midPoint) {
            for (int i = 1; i < p_Chase_Bar; i++) {
                if (p_Same_color_Bars && !MathUtil.isWhite(bars.get(i))) {
                    return Side.NEUTRAL;
                }

                if (bars.get(i - 1).getLow() > bars.get(i).getLow() // || bars.get(i - 1).getHigh() > bars.get(i).getHigh()
                        ) {
                    return Side.NEUTRAL;
                }
            }

            return Side.SHORT;
        }

        return Side.NEUTRAL;
    }

    @Override
    public void update(List<Bar> descendingBars) {
    }
}
