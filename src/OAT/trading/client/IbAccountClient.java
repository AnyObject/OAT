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

package OAT.trading.client;

import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import OAT.event.State;
import OAT.trading.AccountPortfolio;
import OAT.trading.AccountPosition;
import OAT.trading.Main;
import OAT.trading.thread.BaseThread;
import OAT.trading.thread.MainThread;
import OAT.util.DateUtil;
import OAT.util.MathUtil;
import OAT.util.TextUtil;
import OAT.util.XMLUtil;

/**
 *
 * @author Antonio Yip
 */
public class IbAccountClient extends IbClient implements AccountClient {

    public IbAccountClient(BaseThread baseThread) {
        super(baseThread);
    }

    @Override
    public int getClientId() {
        return 10;
    }

    public MainThread getAccountThread() {
        return Main.getMainThread();
    }

    //
    //Requests
    //
    @Override
    public void reqAccountUpdates(AccountPortfolio account) {
        if (!eClientSocket.isConnected() || account == null) {
            return;
        }

        if (Main.ibPlatform != Platform.GATEWAY) {
            log(Level.FINE, "Platform is not Gateway. Request account is canceled");
            return;
        }

//        if (getAccountThread().isStateActive(State.SUBSCRIBED_ACCOUNT_UPDATE)) {
//            log(Level.FINE, "Account already subscribed: {0}", account.accountCode);
//            return;
//        }

        if (isLoggable(Level.FINER)) {
            log(Level.FINER, "Requesting account update: {0}", account.accountCode);
        }

        eClientSocket.reqAccountUpdates(true, account.accountCode);

        getAccountThread().setState(true, State.SUBSCRIBED_ACCOUNT_UPDATE);
    }

    @Override
    public void cancelAccountUpdates(AccountPortfolio account) {
        if (!eClientSocket.isConnected() || account == null) {
            return;
        }

        log(Level.INFO, "Cancelling account update: {0}", account.accountCode);

        eClientSocket.reqAccountUpdates(false, account.accountCode);

        getAccountThread().setState(false, State.SUBSCRIBED_ACCOUNT_UPDATE);
    }

    public void requestFA(int faDataType) {
        if (!eClientSocket.isConnected()) {
            return;
        }

        eClientSocket.requestFA(faDataType);
    }

    public void replaceFA(int faDataType, String xml) {
        if (!eClientSocket.isConnected()) {
            return;
        }

        eClientSocket.replaceFA(faDataType, xml);
    }

    public void reqManagedAccts() {
        if (!eClientSocket.isConnected()) {
            return;
        }

        eClientSocket.reqManagedAccts();
    }

    //
    //com.ib.client.Ewrapper
    //
    @Override
    public void error(final int id, final int errorCode, final String errorMsg) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (errorCode == 322) { // Duplicate Id
                    logError(Level.WARNING, id, errorCode, errorMsg);

                } else if (errorCode == 2100) {
                    logError(Level.FINE, id, errorCode, errorMsg);
                    //New ibAccount data requested from TWS.
                    //API client has been unsubscribed from ibAccount data
                    Main.getMainThread().setState(false, State.SUBSCRIBED_ACCOUNT_UPDATE);

                } else {
                    unhandledError(id, errorCode, errorMsg);
                }
            }
        }).start();
    }

    @Override
    public void updatePortfolio(final Contract contract, final int position, final double marketPrice, final double marketValue, final double averageCost, final double unrealizedPNL, final double realizedPNL, final String accountName) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    AccountPortfolio accountPortfolio = getAccountThread().getAccountPortfolio(accountName);
                    double urPNL = MathUtil.zerolize(unrealizedPNL);
                    double PNL = MathUtil.zerolize(realizedPNL);

                    if (accountPortfolio != null) {
                        accountPortfolio.updatePosition(
                                contract, position, marketPrice, marketValue,
                                averageCost, urPNL, PNL,
                                getAccountThread().getAccountPortfolios().indexOf(accountPortfolio) == 0);
                    }

                } catch (Exception e) {
                    log(Level.SEVERE, null, e);
                }
            }
        }).start();

        super.updatePortfolio(contract, position, marketPrice, marketValue, averageCost, unrealizedPNL, realizedPNL, accountName);
    }

    @Override
    public void updateAccountTime(final String timeStamp) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
//                getAccountThread().log(Level.FINE, "updateAccountTime: {0}.", timeStamp);
                    getAccountThread().setLastAccountUpdate(DateUtil.getTimeNow());

                } catch (Exception e) {
                    log(Level.SEVERE, null, e);
                }
            }
        }).start();

        super.updateAccountTime(timeStamp);
    }

    @Override
    public void updateAccountValue(final String key, final String value, final String currency, final String accountName) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    AccountPortfolio acct = getAccountThread().getAccountPortfolio(accountName);

                    if (acct != null) {
                        acct.putAccountValue(key, value, currency);
                    }

                } catch (Exception e) {
                    log(Level.SEVERE, null, e);
                }
            }
        }).start();

        super.updateAccountValue(key, value, currency, accountName);
    }

    @Override
    public void accountDownloadEnd(final String accountName) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    getAccountThread().log(Level.INFO, "Account details downloaded for {0}.", accountName);

                    AccountPortfolio accountPortfolio = getAccountThread().getAccountPortfolio(accountName);

                    if (accountPortfolio != null) {
                        for (Map.Entry<String, AccountPosition> entry : accountPortfolio.accountPositions.entrySet()) {
                            String string = entry.getKey();
                            AccountPosition accountPosition = entry.getValue();

                            //To do
                        }

                        accountPortfolio.fireAccountPortfolioChanged();
                    }

                } catch (Exception e) {
                    log(Level.SEVERE, null, e);
                }
            }
        }).start();

        super.accountDownloadEnd(accountName);
    }

    @Override
    public void managedAccounts(final String accountsList) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    StringTokenizer st = new StringTokenizer(accountsList, ",");

                    getAccountThread().setAdvisorAccount(new AccountPortfolio(st.nextToken()));
                    getAccountThread().log(Level.INFO,
                            "Advisor: {0}",
                            getAccountThread().getAdvisorAccount());

                    while (st.hasMoreTokens()) {
                        String acct = st.nextToken();
                        getAccountThread().addSubAccount(acct);
                    }

                    getAccountThread().log(Level.INFO,
                            "Sub-accounts: {0}",
                            TextUtil.toString(getAccountThread().getAccountPortfolios()));

                    requestFA(EClientSocket.GROUPS);

                } catch (Exception e) {
                    log(Level.SEVERE, null, e);
                }
            }
        }).start();

        super.managedAccounts(accountsList);
    }

    @Override
    public void receiveFA(final int faDataType, final String xml) {
        if (xml == null) {
            return;
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

                    DocumentBuilder db = null;
                    try {
                        db = dbf.newDocumentBuilder();
                    } catch (ParserConfigurationException ex) {
                        getAccountThread().log(Level.SEVERE, null, ex);
                    }
                    if (db == null) {
                        return;
                    }

                    InputSource is = new InputSource();
                    is.setCharacterStream(new StringReader(xml));

                    Document doc = null;
                    try {
                        doc = db.parse(is);
                    } catch (Exception ex) {
                        getAccountThread().log(Level.WARNING, null, ex);
                    }
                    if (doc == null) {
                        return;
                    }

                    NodeList nodes;

                    switch (faDataType) {
                        case EClientSocket.GROUPS:
                            getAccountThread().setDefaultAccountGroup(null);
                            getAccountThread().getAccountGroupMap().clear();

                            nodes = doc.getElementsByTagName("Group");

                            for (int i = 0; i < nodes.getLength(); i++) {
                                Element element = (Element) nodes.item(i);

                                NodeList name = element.getElementsByTagName("name");
                                String group = XMLUtil.getCharacterDataFromElement((Element) name.item(0));
                                List<AccountPortfolio> accounts = new ArrayList<AccountPortfolio>();

                                NodeList listOfAccts = element.getElementsByTagName("String");
                                for (int j = 0; j < listOfAccts.getLength(); j++) {
                                    accounts.add(getAccountThread().getAccountPortfolio(
                                            XMLUtil.getCharacterDataFromElement(
                                            (Element) listOfAccts.item(j))));
                                }

                                getAccountThread().getAccountGroupMap().put(group, accounts);

                                if (getAccountThread().getDefaultAccountGroup() == null) {
                                    getAccountThread().setDefaultAccountGroup(group);
                                }

                                getAccountThread().log(Level.INFO, "{0}: {1}",
                                        new Object[]{group, TextUtil.toString(accounts)});
                            }
                            break;

                        case EClientSocket.ALIASES:
                            break;

                        case EClientSocket.PROFILES:
                            break;

                        default:
                            getAccountThread().log(Level.WARNING, "Unknow faDataType.");
                    }

                } catch (Exception e) {
                    log(Level.SEVERE, null, e);
                }
            }
        }).start();

        super.receiveFA(faDataType, xml);
    }
}
