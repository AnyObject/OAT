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

package OAT.trading;

import java.util.List;
import OAT.data.Bar;

/**
 * Interface for stopper plug-in.
 *
 * @author Antonio Yip
 */
public interface Stopper extends DataRequirer {

    /**
     * Calculate and return the current stop price using the market data.
     *
     * @param descendingBars 
     * @return the current stop price
     */
    public abstract double calculateStopPrice(List<Bar> descendingBars);

    /**
     * Test if the stopper is active.
     *
     * @return true or false
     */
    public boolean isActive();
}
