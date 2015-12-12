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

package OAT.event;

import javax.swing.event.ChangeEvent;
import OAT.trading.Trade;

/**
 *
 * @author Antonio Yip
 */
public class TradeHighLowChangeEvent extends ChangeEvent {

    protected Trade trade;

    public TradeHighLowChangeEvent(Trade source) {
        super(source);
        this.trade = source;
    }

    public Trade getTrade() {
        return trade;
    }

    public long getLowTime() {
        return trade.getLowTime();
    }

    public double getLow() {
        return trade.getLow();
    }

    public long getHighTime() {
        return trade.getHighTime();
    }

    public double getHigh() {
        return trade.getHigh();
    }
}
