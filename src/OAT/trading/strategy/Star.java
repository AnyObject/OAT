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
public class Star extends StrategyPlugin implements Calculator , Stopper {

    //Parameters
    /**
     * The maximum number of bars to be combined to form a star.
     */
    protected int p_Star_Bars = 2;
    /**
     * The number of previous bars to compare to the star.
     */
    protected int p_Previous_Bars = 6;
    /**
     * The length ratio of tail to head.
     */
    protected double p_Tail_to_Head_Ratio = 2.75;
    /**
     * The length ratio of body to tail.
     */
    protected double p_Body_to_Tail_Ratio = 0;
//    /**
//     * The minimum length of the whole stick.
//     */
//    protected double p_Min_Length = 0;
    /**
     * The position of the midpoint of the star that the previous bars cannot
     * exceed, in order to identify the pattern.
     */
    protected double p_Star_Midpoint = 0.5;
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
        return p_Previous_Bars + p_Star_Bars;
    }

    @Override
    public Side trigger(List<Bar> descendingBars) {
        for (int i = 1; i <= p_Star_Bars; i++) {
//            List<Bar> bars = getDescendingBars(p_Previous_Bars + i);
            //filterBars(descendingBars, p_Previous_Bars + i);
            //descendingBars.subList(0, p_Previous_Bars + i);
            //getDescendingBars(p_Previous_Bars + i);

            if (descendingBars.size() < p_Previous_Bars + i) {
                return Side.NEUTRAL;
            }

//            List<Bar> bars = GeneralUtil.subListMaxSize(descendingBars, p_Previous_Bars + i);

            List<Bar> starBars = descendingBars.subList(0, i);
            List<Bar> prevBars = descendingBars.subList(i, p_Previous_Bars + i);

            Bar starBar = new Bar(starBars);
            Bar prevBar = new Bar(prevBars);

//            if (starBar.getHigh() - starBar.getLow() < bars.get(0).getClose() * p_Min_Length) {
//                continue;
//            }
            //check fluctuation
            if (MathUtil.getFluctuation(prevBars) < p_Min_Range) {
                logOnce(Level.FINE, "Fluctuation too small.");
                return Side.NEUTRAL;
            }

            if (MathUtil.isMorningStar(starBar, p_Tail_to_Head_Ratio, p_Body_to_Tail_Ratio)) {
//                for (int j = 1; j < prevBars.size(); j++) {
//                    if (prevBars.get(i).getLow() < prevBars.get(i - 1).getLow()) {
//                        return Side.NEUTRAL;
//                    }
//                }

                if (prevBar.getLow() > MathUtil.getMidPoint(starBar, p_Star_Midpoint)) {
//                    System.out.println("MS " + starBar.getOpen() + " " + starBar.getHigh() + " " + starBar.getLow() + " " + starBar.getClose());
                    return Side.LONG;
                }

            } else if (MathUtil.isEveningStar(starBar, p_Tail_to_Head_Ratio, p_Body_to_Tail_Ratio)) {
//                for (int j = 1; j < prevBars.size(); j++) {
//                    if (prevBars.get(i).getHigh() < prevBars.get(i - 1).getHigh()) {
//                        return Side.NEUTRAL;
//                    }
//                }
                
                if (prevBar.getHigh() < MathUtil.getMidPoint(starBar, 1 - p_Star_Midpoint)) {
//                    System.out.println("ES " + starBar.getOpen() + " " + starBar.getHigh() + " " + starBar.getLow() + " " + starBar.getClose());
                    return Side.SHORT;
                }
            }
        }

        return Side.NEUTRAL;
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
        return getLastTradeAdverseStop(p_Star_Bars);
    }
}
