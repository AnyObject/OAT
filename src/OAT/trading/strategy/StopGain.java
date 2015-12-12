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
import OAT.trading.Stopper;
import OAT.trading.StrategyPlugin;

/**
 *
 * @author Antonio Yip
 */
public class StopGain extends StrategyPlugin implements Stopper{

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
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
