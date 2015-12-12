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
import java.util.ArrayList;
import java.util.List;
import OAT.data.Bar;

/**
 *
 * @author Antonio Yip
 */
public class MovingAverage extends StrategyPlugin implements Calculator {

    //Parameters
    protected int p_Fast_MA = 3;
    protected int p_Slow_MA = 10;
    //
    protected List<Double> fastMAs = new ArrayList<Double>();
    protected List<Double> slowMAs = new ArrayList<Double>();

    @Override
    public String getDefaultNodeName() {
        return "MA";
    }

    @Override
    public Trend getTrend() {
        return Trend.FOLLOWER;
    }

    @Override
    public String[] getKeys() {
        return new String[]{
                    "MA1",
                    "MA2"
                };
    }

    @Override
    public Double[] getRange() {
        return null;
    }

    @Override
    public int getRequiredBars() {
        return Math.max(p_Fast_MA, p_Slow_MA);
    }

    @Override
    public Side trigger(List<Bar> descendingBars) {
        //        ArrayList<Bar> bars = getTradingThread().getDescendingBars(Math.max(p_Fast_MA, p_Slow_MA));
////        ArrayList<Bar> ma2Bars = getTradingThread().getDescendingBars(p_Slow_MA); //slow MA
//
//        double fastMA = 0;
//        double slowMA = 0;
//        double prevFastMA = fastMAs.get(fastMAs.size() - 1);
//        double prevSlowMA = slowMAs.get(slowMAs.size() - 1);
//
//        //fast MA
//        if (bars.size() >= p_Fast_MA) {
//            for (int i = 0; i < p_Fast_MA; i++) {
//                fastMA += bars.get(i).getBar();
//            }
//
//            fastMA /= p_Fast_MA;
//        } else {
//            fastMA = Double.NaN;
//        }
//        fastMAs.add(fastMA);
//
//        //slow MA
//        if (bars.size() >= p_Slow_MA) {
//            for (int i = 0; i < p_Slow_MA; i++) {
//                slowMA += bars.get(i).getBar();
//            }
//
//            slowMA /= p_Slow_MA;
//        } else {
//            slowMA = Double.NaN;
//        }
//        slowMAs.add(slowMA);
//
//        indicatorDataset.add(new Indicator(bars.get(0), new Double[]{fastMA, slowMA}));

        int fastMAsize = fastMAs.size();
        int slowMAsize = slowMAs.size();

        if (fastMAsize < 2 || slowMAsize < 2) {
            return Side.NEUTRAL;
        }

        double fastMA = fastMAs.get(fastMAsize - 1);
        double slowMA = slowMAs.get(slowMAsize - 1);
        double prevFastMA = fastMAs.get(fastMAsize - 2);
        double prevSlowMA = slowMAs.get(slowMAsize - 2);


        if (fastMAsize >= p_Fast_MA && slowMAsize >= p_Slow_MA) {
            if (fastMA > slowMA && prevFastMA < prevSlowMA) {
                return Side.LONG;

            } else if (fastMA < slowMA && prevFastMA > prevSlowMA) {
                return Side.SHORT;
            }
        }

        return Side.NEUTRAL;
    }

    @Override
    public void update(List<Bar> descendingBars) {
//        List<Bar> bars = getDescendingBars();
        List<Bar> bars = descendingBars;
        //filterBars(descendingBars, getRequiredBars());
        //descendingBars.subList(0, getRequiredBars());
        //getDescendingBars(Math.max(p_Fast_MA, p_Slow_MA));

        if (bars.isEmpty()) {
            return;
        }

        double fastMA = 0;
        double slowMA = 0;

        //fast MA
        if (bars.size() >= p_Fast_MA) {
            for (int i = 0; i < p_Fast_MA; i++) {
                fastMA += bars.get(i).getPrice();
            }

            fastMA /= p_Fast_MA;
        } else {
            fastMA = Double.NaN;
        }
        fastMAs.add(fastMA);

        //slow MA
        if (bars.size() >= p_Slow_MA) {
            for (int i = 0; i < p_Slow_MA; i++) {
                slowMA += bars.get(i).getPrice();
            }

            slowMA /= p_Slow_MA;
        } else {
            slowMA = Double.NaN;
        }
        slowMAs.add(slowMA);

        addIndicator(new Indicator(bars.get(0), new Double[]{fastMA, slowMA}));
    }
}
