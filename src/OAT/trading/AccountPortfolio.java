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
import java.util.HashMap;
import java.util.Properties;
import OAT.event.AccountPortfolioChangeEvent;
import OAT.event.Listenable;
import OAT.util.DateUtil;

/**
 *
 * @author Antonio Yip
 */
public class AccountPortfolio extends Listenable {

    public String accountCode;
    public HashMap<String, Properties> accountValues = new HashMap();
    public HashMap<String, AccountPosition> accountPositions = new HashMap();

    public AccountPortfolio(String accountCode) {
        this.accountCode = accountCode;
    }

    public void putAccountValue(String key, String value, String currency) {
        if (!accountValues.containsKey(currency)) {
            accountValues.put(currency, new Properties());
        }

        accountValues.get(currency).put(key, value);
    }

    public AccountPosition addContract(Contract contract) {
        if (!accountPositions.containsKey(contract.m_localSymbol)) {
            AccountPosition position = new AccountPosition(contract);

            accountPositions.put(contract.m_localSymbol, position);
        }

        return getPosition(contract);
    }

    public void updatePosition(Contract contract, int positionI, double marketPrice, double marketValue, double averageCost, double unrealizedPNL, double realizedPNL, boolean notify) {
        AccountPosition position = getPosition(contract);

        if (position == null) {
            return;
        }

        position.setLastUpdated(DateUtil.getTimeNow());
        position.setMarketPrice(marketPrice, false);
        position.setMarketValue(marketValue, false);
        position.setAverageCost(averageCost, false);
        position.setUnrealizedPNL(unrealizedPNL, false);
        position.setRealizedPNL(realizedPNL, false);

        position.setPosition(positionI, notify);

        fireAccountPortfolioChanged();
    }

    public AccountPosition getPosition(Contract contract) {
        if (contract == null) {
            return null;
        }

        return accountPositions.get(contract.m_localSymbol);
    }

    public boolean isSameCode(String accountCode) {
        return this.accountCode.equalsIgnoreCase(accountCode);
    }

//    public int addContract(String localSymbol) {
//        return accountPositions.containsKey(localSymbol)
//                ? accountPositions.get(localSymbol).position : 0;
//    }
//    public double getUnrealizedPNL(String localSymbol) {
//        return accountPositions.containsKey(localSymbol)
//                ? accountPositions.get(localSymbol).unrealizedPNL : 0;
//    }
//
//    public double getRealizedPNL(String localSymbol) {
//        return accountPositions.containsKey(localSymbol)
//                ? accountPositions.get(localSymbol).realizedPNL : 0;
//    }
    @Override
    public String toString() {
        return accountCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof AccountPortfolio)) {
            return false;
        }

        AccountPortfolio theOther = (AccountPortfolio) obj;

        if (this.getClass() != theOther.getClass()) {
            return false;
        }

        if (!this.accountCode.equalsIgnoreCase(theOther.accountCode)) {
            return false;
        }

        return true;
    }

    public void fireAccountPortfolioChanged() {
        notifyListeners(new AccountPortfolioChangeEvent(this));
    }
}
