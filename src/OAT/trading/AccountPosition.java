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

import com.ib.client.Contract;
import OAT.event.Listenable;
import OAT.event.PositionChangeEvent;

/**
 *
 * @author Antonio Yip
 */
public class AccountPosition extends Listenable {

    private Contract contract;
    private int position;
    private double marketPrice;
    private double marketValue;
    private double averageCost;
    private double unrealizedPNL;
    private double realizedPNL;
    private long lastUpdated;
    private boolean fired;

    public AccountPosition(Contract contract) {
        this.contract = contract;
    }

    public double getAverageCost() {
        return averageCost;
    }

    public Contract getContract() {
        return contract;
    }

    public double getMarketPrice() {
        return marketPrice;
    }

    public double getMarketValue() {
        return marketValue;
    }

    public int getPosition() {
        return position;
    }

    public double getRealizedPNL() {
        return realizedPNL;
    }

    public double getUnrealizedPNL() {
        return unrealizedPNL;
    }

    public double getDayPNL() {
        return realizedPNL + unrealizedPNL;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setAverageCost(double averageCost, boolean notify) {
        this.averageCost = averageCost;

        if (notify) {
            firePositionChanged();
        }
    }

    public void setContract(Contract contract, boolean notify) {
        this.contract = contract;

        if (notify) {
            firePositionChanged();
        }
    }

    public void setMarketPrice(double marketPrice, boolean notify) {
        this.marketPrice = marketPrice;

        if (notify) {
            firePositionChanged();
        }
    }

    public void setMarketValue(double marketValue, boolean notify) {
        this.marketValue = marketValue;

        if (notify) {
            firePositionChanged();
        }
    }

    public void setPosition(int position, boolean notify) {
        this.position = position;

        if (notify) {
            firePositionChanged();
        }
    }

    public void setRealizedPNL(double realizedPNL, boolean notify) {
        this.realizedPNL = realizedPNL;

        if (notify) {
            firePositionChanged();
        }
    }

    public void setUnrealizedPNL(double unrealizedPNL, boolean notify) {
        this.unrealizedPNL = unrealizedPNL;

        if (notify) {
            firePositionChanged();
        }
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
        fired = false;
    }

    public boolean isFired() {
        return fired;
    }

    public void firePositionChanged() {
        notifyListeners(new PositionChangeEvent(this));
        fired = true;
    }

    @Override
    public String toString() {
        return "" + position + ". Market price: " + marketPrice;// + " " + marketValue;
    }
}
