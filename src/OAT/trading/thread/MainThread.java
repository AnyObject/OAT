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

package OAT.trading.thread;

import OAT.trading.Trade;
import OAT.trading.Account;
import OAT.trading.Main;
import OAT.trading.AccountPosition;
import OAT.trading.TradeDataset;
import OAT.trading.Side;
import OAT.trading.AccountPortfolio;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import OAT.event.GenericListener;
import OAT.trading.client.AccountClient;
import OAT.util.DateUtil;
import OAT.util.TextUtil;

/**
 *
 * @author Antonio Yip
 */
public abstract class MainThread extends BaseThread implements GenericListener {

    protected transient long lastAccountUpdate;
    protected transient AccountPortfolio advisorAccount;
    protected transient List<AccountPortfolio> accountPortfolios = new ArrayList<AccountPortfolio>();
    protected transient HashMap<String, List<AccountPortfolio>> accountGroupMap = new HashMap();
    protected transient String defaultAccountGroup;

    public void reqAccountUpdates() {
        getClient().reqAccountUpdates(Main.frame.getSelectedAccount());
    }

    public void cancelAccountUpdates() {
        getClient().cancelAccountUpdates(Main.frame.getSelectedAccount());
    }

    @Override
    public void cleanup() {
        super.cleanup();

        for (int i = Main.chartFrames.size() - 1; i >= 0; i--) {
            Main.chartFrames.get(i).setVisible(false);
        }
    }

    @Override
    public void postConnection() {
    }

    @Override
    public int getThreadId() {
        return 1;
    }

    public AccountClient getClient() {
        return Main.getAccountClient();
    }

    public long getLastAccountUpdate() {
        return lastAccountUpdate;
    }

    public AccountPortfolio getAdvisorAccount() {
        return advisorAccount;
    }

    public AccountPortfolio getAccountPortfolio(String accountCode) {
        for (AccountPortfolio accountPortfolio : accountPortfolios) {
            if (accountPortfolio.isSameCode(accountCode)) {
                return accountPortfolio;
            }
        }

        return null;
    }

    public String getAccountGroupString(String group) {
        if (accountGroupMap.containsKey(group)
                || Main.getAccount() != Account.CASH) {
            return group;
        } else {
            return defaultAccountGroup;
        }
    }

    public List<AccountPortfolio> getAccountPortfolios(String group) {
        return accountGroupMap.get(getAccountGroupString(group));
    }

    public List<AccountPortfolio> getAccountPortfolios() {
        return accountPortfolios;
    }

    public String getDefaultAccountGroup() {
        return defaultAccountGroup;
    }

    public List<AccountPortfolio> getSubAccounts() {
        return accountPortfolios;
    }

    public HashMap<String, List<AccountPortfolio>> getAccountGroupMap() {
        return accountGroupMap;
    }

    public void setLastAccountUpdate(long lastAccountUpdate) {
        this.lastAccountUpdate = lastAccountUpdate;
    }

    public void setAdvisorAccount(AccountPortfolio advisorAccount) {
        this.advisorAccount = advisorAccount;
    }

    public void setDefaultAccountGroup(String defaultAccountGroup) {
        this.defaultAccountGroup = defaultAccountGroup;
    }

    public void addSubAccount(String accountCode) {
        for (AccountPortfolio accountPortfolio : accountPortfolios) {
            if (accountPortfolio.isSameCode(accountCode)) {
                return;
            }
        }

        AccountPortfolio newAccount = new AccountPortfolio(accountCode);
        newAccount.addChangeListener(this);
        accountPortfolios.add(newAccount);
        Main.frame.addAccountCombo(newAccount);
    }

    public String getAccountSummary(AccountPortfolio accountPortfolio) {
        if (accountPortfolio == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        sb.append("-- Account Summary --\n");
        sb.append("As at ").append(DateUtil.getCalendarDate().getTime()).append("\n");
        sb.append("Account: ");
        sb.append(accountPortfolio.accountCode).append("\n");
        sb.append("\nCurrency\tBalance\t\tP&L\n");

        for (String key : accountPortfolio.accountValues.keySet()) {
            if (key == null || key.isEmpty()) {
                continue;
            }

            Properties prop = accountPortfolio.accountValues.get(key);
            String balance = prop.get("CashBalance").toString();
            String pnL = prop.get("RealizedPnL").toString();

            sb.append(key).append("\t");
            sb.append(balance).append("\t");

            if (balance.length() < 10) {
                sb.append("\t");
            }

            sb.append(pnL);
            sb.append("\n");
        }

        sb.append("\nContract\tPosition\tCurrency\tP&L\n");

        for (String key : accountPortfolio.accountPositions.keySet()) {
            if (key == null || key.isEmpty()) {
                continue;
            }

            AccountPosition position = accountPortfolio.accountPositions.get(key);

            if (position.getDayPNL() == 0) {
                continue;
            }

            sb.append(key.replace(" ", "")).append("\t");
            sb.append(position.getPosition()).append("\t");
            sb.append(position.getContract().m_currency).append("\t");
            sb.append(TextUtil.CURRENCY_FORMATTER.format(
                    position.getDayPNL())).append("\n");
        }


        for (TradingThread tradingThread : Main.currentStrategies) {
            TradeDataset trades = tradingThread.getTrades();

            if (trades.isEmpty()) {
                continue;
            }

            String localSymbol = tradingThread.getContract().m_localSymbol;
            String currency = tradingThread.getContract().m_currency;

            sb.append("\n").append(localSymbol);
            sb.append(" Activities");
            sb.append("\n");

            sb.append("Side\tEnter\tTime\tExit\tTime\tP/L");
            sb.append("\n");

            for (int i = 0; i < trades.getItemCount(); i++) {
                Trade trade = trades.get(i);

                if (trade == null
                        || trade.isCeased()
                        || trade.getSide() == Side.NEUTRAL) {
                    continue;
                }

                sb.append(trade.getSide()).append("\t");
                sb.append(trade.getEnterPrice()).append("\t");
                sb.append(DateUtil.getTimeStamp(trade.getEnterTime(),
                        "EEE " + DateUtil.MINUTE_TIME_FORMAT)).append("\t");
                sb.append(trade.getExitPrice()).append("\t");
                sb.append(DateUtil.getTimeStamp(trade.getExitTime(),
                        "EEE " + DateUtil.MINUTE_TIME_FORMAT)).append("\t");
                sb.append(TextUtil.CURRENCY_FORMATTER.format(
                        trade.getNetAmount())).append(" ");
                sb.append(currency);
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}
