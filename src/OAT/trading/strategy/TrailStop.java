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
import OAT.trading.Trade;
import OAT.util.TextUtil;

/**
 *
 * @author Antonio Yip
 */
public class TrailStop extends StrategyPlugin implements Stopper {

    protected double p_Trail_Stop_Factor = 0.005;
    protected double p_Trigger = 0.003;

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
        return 0;
    }

    @Override
    public double calculateStopPrice(List<Bar> descendingBars) {
        Side side = getTradingThread().getCurrentSide();
        Trade lastTrade = getTradingThread().getLastTrade();

        if (lastTrade != null && side != Side.NEUTRAL) {
            double stopPrice;

            if (side == Side.LONG) {
                if (lastTrade.getHigh() / lastTrade.getEnterPrice() - 1 < p_Trigger) {
                    return Double.NaN;
                }

                stopPrice = lastTrade.getHigh() * (1 - p_Trail_Stop_Factor);

            } else {
                 if (1 - lastTrade.getLow() / lastTrade.getEnterPrice() < p_Trigger) {
                    return Double.NaN;
                }

                stopPrice = lastTrade.getLow() * (1 + p_Trail_Stop_Factor);
            }

            log(Level.FINE, "Stop = " + TextUtil.PRICE_FORMATTER.format(stopPrice));
            return stopPrice;
        }

        return Double.NaN;
    }
}
