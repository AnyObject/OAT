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
import OAT.trading.Presister;
import OAT.trading.Indicator;
import OAT.trading.Trend;
import OAT.trading.Calculator;
import OAT.trading.Side;
import OAT.trading.Session;
import java.util.ArrayList;
import java.util.List;
import OAT.data.Bar;
import OAT.data.Tick;

/**
 *
 * @author Antonio Yip
 */
public class OBV extends StrategyPlugin implements Calculator, Presister { // , Stopper {

    protected int p_OBV_x = 15;
    protected int p_OBV_y = 11;
//    protected int p_OBV = 500;
//    protected int p_Prev_Bars = 3;
    //
    private long obv;
    private List<Long> obvs = new ArrayList<Long>();
    private Tick prevTick;
    private boolean isUpdatedBar;
    private Session prevSession;
    private Side lastChangedSide = Side.NEUTRAL;
    private Side presistentSide = Side.NEUTRAL;

    @Override
    public String[] getKeys() {
        return new String[]{"OBV"};
    }

    @Override
    public Double[] getRange() {
        return new Double[]{};
    }

    @Override
    public int getRequiredBars() {
        return 1;
    }

    @Override
    public Trend getTrend() {
        return Trend.FOLLOWER;
    }

    @Override
    public Side trigger(List<Bar> descendingBars) {
        Session currentSession = getTradingThread().getCurrentSession();
        Tick lastTick = getPrimaryChart().getLastTick();
        
        if (prevSession == null
                || prevSession != currentSession) {
            obv = 0;
            obvs.clear();
            prevTick = null;
        }


        if (prevTick != null) {
            double priceChange = lastTick.getPrice() - prevTick.getPrice();

            if (priceChange != 0) {
                lastChangedSide = Side.valueOf(priceChange);
            }

            obv += lastTick.getSize() * lastChangedSide.sign;

            obvs.add(obv);
        }

        prevSession = currentSession;
        prevTick = lastTick;

//        if (descendingBars.size() < p_Prev_Bars || obvs.size() - 1 < p_OBV_x) {
//            return Side.NEUTRAL;
//        }

        if (isUpdatedBar) {
            isUpdatedBar = false;

//            return getSide();
        }

        return Side.NEUTRAL;
    }

    @Override
    public void update(List<Bar> descendingBars) {
        if (descendingBars.isEmpty()) {
            return;
        }

        addIndicator(new Indicator(descendingBars.get(0), (double) obv));

        isUpdatedBar = true;
    }

//    @Override
//    public double calculateStopPrice(List<Bar> descendingBars) {
//        return triggerStopPrice(getSide());
//    }
    @Override
    public Side getPresistentSide() {
        if (obvs.size() - 1 < p_OBV_x) {
            return Side.NEUTRAL;
        }

        long obvDiff = obv - obvs.get(obvs.size() - 1 - p_OBV_x);

        if (obvDiff >= p_OBV_y) {
            presistentSide = Side.LONG;

        } else if (obvDiff <= -p_OBV_y) {
            presistentSide = Side.SHORT;
        }

        return presistentSide;
    }
//    private Side getSide() {
//        if (obvs.size() - 1 < p_OBV_x) {
//            return Side.NEUTRAL;
//        }
//
//        long obvDiff = obv - obvs.get(obvs.size() - 1 - p_OBV_x);
//
//        if (obvDiff >= p_OBV_y) {
//            presistentSide = Side.LONG;
//
//        } else if (obvDiff <= -p_OBV_y) {
//            presistentSide = Side.SHORT;
//
////        } else {
////            presistentSide = Side.NEUTRAL;
//        }
//
////        if (obv > p_OBV) {
////            presistentSide = Side.LONG;
////
////        } else if (obv < -p_OBV) {
////            presistentSide = Side.SHORT;
////
////        } else {
////            presistentSide = Side.NEUTRAL;
////        }
//
//        return presistentSide;
//    }
}
