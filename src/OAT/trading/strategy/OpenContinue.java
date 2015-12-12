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
import OAT.trading.Trade;
import OAT.trading.Main;
import OAT.trading.Trend;
import OAT.trading.Calculator;
import OAT.trading.Side;
import java.util.List;
import OAT.data.Bar;
import OAT.data.Tick;

/**
 *
 * @author Antonio Yip
 */
public class OpenContinue extends StrategyPlugin implements Calculator {

    protected int p_Previous_Session_Bars = 5;
//    protected double p_Within_Range = 0.002;
//    protected long p_Time_Window = 60000;

//    public OpenContinue() {
//        p_Ignore_Break = true;
//    }
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
        return p_Previous_Session_Bars;
    }

    @Override
    public Side trigger(List<Bar> descendingBars) {
        Trade lastTrade = getTradingThread().getLastTrade();

        if (lastTrade == null
                || !lastTrade.isExitOnClose()
                || lastTrade.getProfit() < 0) {
            return Side.NEUTRAL;
        }

        Tick lastTick = getPrimaryChart().getLastTick();

        if (lastTick.getTime() - getCurrentSession().getOpenTime() > Main.p_Task_Interval) {
            return Side.NEUTRAL;
        }

//        List<Bar> bars = getDescendingBars();
        List<Bar> bars = descendingBars;

        if (bars.size() < p_Previous_Session_Bars) {
            return Side.NEUTRAL;
        }

        if (getCurrentSession().isIn(bars.get(0).getTime())) {
            return Side.NEUTRAL;
        }

        Bar lastCloseBar = bars.get(0);

        if (lastTrade.getSide() == Side.LONG) {
            if (lastTick.getPrice() < lastCloseBar.getLow()) {
                return Side.NEUTRAL;
            }

            for (int i = 1; i < p_Previous_Session_Bars; i++) {
                if (bars.get(i - 1).getHigh() < bars.get(i).getHigh()) {
                    return Side.NEUTRAL;
                }
            }

        } else if (lastTrade.getSide() == Side.SHORT) {
            if (lastTick.getPrice() > lastCloseBar.getHigh()) {
                return Side.NEUTRAL;
            }

            for (int i = 1; i < p_Previous_Session_Bars; i++) {
                if (bars.get(i - 1).getLow() > bars.get(i).getLow()) {
                    return Side.NEUTRAL;
                }
            }
        }

//        if (Math.abs(lastTick.getPrice() / lastCloseBar.getClose() - 1) <= p_Within_Range) {
            return lastTrade.getSide();
//        }

//        return Side.NEUTRAL;
    }

    @Override
    public void update(List<Bar> descendingBars) {
    }

    @Override
    public Trend getTrend() {
        return Trend.FOLLOWER;
    }
}
