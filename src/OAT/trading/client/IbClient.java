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

import com.ib.client.*;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import OAT.event.State;
import OAT.trading.Main;
import OAT.trading.thread.BaseThread;
import OAT.util.DateUtil;
import OAT.util.GeneralUtil;
import OAT.util.ThreadLogger;
import OAT.util.Waiting;

/**
 *
 * @author Antonio Yip
 */
public abstract class IbClient extends BaseClient implements EWrapper {

    private static final int MAX_CLIENTS = 8;
    private static final Set<IbClient> CONNECTED_CLIENTS = Collections.synchronizedSet(new HashSet<IbClient>());
    //
    protected static final String GENERIC_TICK_TYPES = "100,101,104,106,225,233,291,318";
    protected static final int REQ_BASE = 1000;
    protected static final int REQ_CONTRACT_DETAIL_I = 1 * REQ_BASE;
    protected static final int REQ_TRADING_CONTRACT_DETAIL_I = 2 * REQ_BASE;
    protected static final int REQ_MKT_DATA_I = 3 * REQ_BASE;
    protected static final int REQ_REAL_TIME_BARS_I = 4 * REQ_BASE;
    protected static final int REQ_HISTORICAL_DATA_I = 5 * REQ_BASE;
    protected static final int REQ_2ND_MKT_DATA_I = 6 * REQ_BASE;
    protected static final int REQ_EXECUTIONS_I = 7 * REQ_BASE;
    protected static final int REQ_OPEN_ORDERS_I = 8 * REQ_BASE;
    protected static final int REQ_PORTFOLIO_I = 9 * REQ_BASE;
    //
    protected EClientSocket eClientSocket;
    //
    private long connectionTime;
    private boolean pending;
    private boolean restarting;
    private boolean initialized;

    public IbClient(BaseThread baseThread) {
        super(baseThread);
    }

    public static enum Platform {

        GATEWAY("localhost", 4001),
        TWS("localhost", 7496),
        TWS_WEB("localhost", 7496),
        TWS_LINUX("10.0.1.102", 7496),
        TWS_WINDOWS("10.0.1.103", 7496);
        public final String ip;
        public final int port;

        Platform(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }
    }

    protected static Contract createContract(String symbol, String securityType,
            String exchange, String currency) {
        Contract contract = new Contract();

        contract.m_symbol = symbol;
        contract.m_secType = securityType;
        contract.m_exchange = exchange;
        contract.m_currency = currency;

        return contract;
    }

    protected static Order createOrder(int clientId, int orderId, String action,
            int quantity, String orderType, double lmtPrice, double auxPrice,
            int triggerMethod, String faGroup, String faMethod, String faPrecentage) {
        Order order = new Order();

        order.m_clientId = clientId;
        order.m_orderId = orderId;
        order.m_action = action;
        order.m_totalQuantity = quantity;
        order.m_orderType = orderType;
        order.m_lmtPrice = lmtPrice;
        order.m_auxPrice = auxPrice;
        order.m_transmit = true;
        order.m_triggerMethod = triggerMethod;
        order.m_faGroup = faGroup;
        order.m_faMethod = faMethod;
        order.m_faPercentage = faPrecentage;

        return order;
    }

    protected static ExecutionFilter createFilter(int p_clientId, String p_acctCode,
            String p_time, String p_symbol, String p_secType, String p_exchange,
            String p_side) {
        ExecutionFilter filter = new ExecutionFilter();

        filter.m_clientId = p_clientId;
        filter.m_acctCode = p_acctCode;
        filter.m_time = p_time;
        filter.m_symbol = p_symbol;
        filter.m_secType = p_secType;
        filter.m_exchange = p_exchange;
        filter.m_side = p_side;

        return filter;
    }

    public long getConnectionTime() {
        return connectionTime;
    }

    protected int getReqTypeId(int id) {
        return id - (id % REQ_BASE);
    }

    @Override
    public synchronized void connect() {
        connectToIB();
    }

    @Override
    public void disconnect() {
        disconnectFromIB();
    }

    @Override
    public boolean isConnected() {
        return eClientSocket != null && eClientSocket.isConnected() && initialized;
    }

    public void connectToIB() {
        if (eClientSocket == null) {
            eClientSocket = new EClientSocket(this);
        }

        if (eClientSocket.isConnected()) {
            return;
        }

        pending = false;

        new Waiting(10000, 1000, baseThread.getLogger()) {

            @Override
            public boolean waitWhile() {
                return CONNECTED_CLIENTS.size() >= MAX_CLIENTS;
            }

            @Override
            public void retry() {
            }

            @Override
            public String message() {
                return "Waiting for free socket... ("
                        + CONNECTED_CLIENTS.size() + " in use)";
            }

            @Override
            public void timeout() {
                pending = true;
            }
        };

        if (pending) {
            restart();
            return;
        }

        CONNECTED_CLIENTS.add(this);

        new Waiting(90000, 1000, baseThread.getLogger()) {

            @Override
            public boolean waitWhile() {
                return !eClientSocket.isConnected();
            }

            @Override
            public void retry() {
                eClientSocket.eConnect(
                        Main.ibPlatform.ip,
                        Main.ibPlatform.port,
                        getClientId());
            }

            @Override
            public String message() {
                return null;
            }

            @Override
            public void timeout() {
                pending = true;
            }
        };

        if (pending) {
            restart();
            return;
        }

        if (eClientSocket.isConnected()) {
            log(Level.FINE, "Connection count = {0}", CONNECTED_CLIENTS.size());

            String loginTimeStamp = eClientSocket.TwsConnectionTime();

            try {
                connectionTime = DateUtil.getCalendarDate(
                        loginTimeStamp, "yyyyMMdd HH:mm:ss").getTimeInMillis();
            } catch (ParseException ex) {
                connectionTime = DateUtil.getTimeNow();
            }

            log(Level.INFO, "Connected to {0} server version {1} at {2}",
                    new Object[]{
                        Main.ibPlatform.name(),
                        eClientSocket.serverVersion(),
                        loginTimeStamp
                    });
        } else {
            throw new UnsupportedOperationException("Cannot connect to IB.");
        }

        initialized = true;
        fireStateChanged(State.INITIALIZED, true);
    }

    public void disconnectFromIB() {
        initialized = false;
        fireStateChanged(State.INITIALIZED, false);

        CONNECTED_CLIENTS.remove(this);

        if (eClientSocket == null) {
            return;
        }

        if (pending) {
            pending = false;
            return;
        }

        if (eClientSocket.isConnected()) {
            eClientSocket.eDisconnect();
        } else {
            return;
        }

        log(Level.INFO, "Disconnected from IB.");
    }

    public void restart() {
        if (restarting) {
            return;
        }

        restarting = true;

        disconnectFromIB();

        GeneralUtil.pause(
                Main.p_Restart_Client_Wait,
                baseThread.getLogger(),
                "Waiting to restart...");

        restarting = false;

        connectToIB();
    }

    @Override
    public void log(Level level, String string) {
        getLogger().log(level, getLogPrefix() + string);
    }

    @Override
    public void log(Level level, String string, Object param1) {
        getLogger().log(level, getLogPrefix() + string, param1);
    }

    @Override
    public void log(Level level, String string, Object[] params) {
        getLogger().log(level, getLogPrefix() + string, params);
    }

    @Override
    public void log(Level level, String msg, Throwable throwable) {
        getLogger().log(level, msg, throwable);
    }

    @Override
    public String getLogPrefix() {
        return this.getClass().getSimpleName() + ": ";
    }

    @Override
    public ThreadLogger getLogger() {
        return baseThread.getLogger();
    }

    @Override
    public boolean isLoggable(Level level) {
        return baseThread.isLoggable(level);
    }

    protected void logError(Level level, int id, int errorCode, String errorMsg) {
        if (isLoggable(level)) {
            log(level, EWrapperMsgGenerator.error(id, errorCode, errorMsg));
        }
    }

    protected void unhandledError(final int id, final int errorCode, final String errorMsg) {
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {


                if (errorCode == 2107 || errorCode == 2108) { // data farm is inactive
                    logError(Level.FINE, id, errorCode, errorMsg);

                } else if (errorCode == 2106) {
                    logError(Level.INFO, id, errorCode, errorMsg);

                } else if (errorCode == 2104 || errorCode == 2119) {
                    logError(Level.FINE, id, errorCode, errorMsg);

                } else if (errorCode == 1100) {
                    logError(Level.WARNING, id, errorCode, errorMsg);

                } else if (errorCode == 1101 || errorCode == 1102) {
                    logError(Level.WARNING, id, errorCode, errorMsg);

                } else if (errorCode == 502) {
                    if (Main.getMainThread().isInitialized()) {
                        logError(Level.WARNING, id, errorCode, errorMsg);
                    }

                } else if (errorCode == 503) { //needs upgrade
                    logError(Level.SEVERE, id, errorCode, errorMsg);

                } else if (errorCode == 504) {
                    logError(Level.SEVERE, id, errorCode, errorMsg);

                    if (initialized) {
                        restart();
                    }

                } else {
                    //unhandled error
                    logError(Level.WARNING, id, errorCode, "Unhandled error: " + errorMsg);
                }
//            }
//        }).start();
    }

    //
    //com.ib.client.Ewrapper
    //
    @Override
    public void connectionClosed() {
        log(Level.SEVERE, EWrapperMsgGenerator.connectionClosed());

        if (initialized) {
            restart();
        }
    }

    @Override
    public void error(Exception e) {
        log(Level.WARNING, EWrapperMsgGenerator.error(e), e);
    }

    @Override
    public void error(String str) {
        log(Level.WARNING, EWrapperMsgGenerator.error(str));
    }

    @Override
    public void nextValidId(int orderId) {
        log(Level.FINE, EWrapperMsgGenerator.nextValidId(orderId));
    }

    @Override
    public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
        if (isLoggable(Level.FINEST)) {
            log(Level.FINEST, EWrapperMsgGenerator.tickPrice(tickerId, field, price, canAutoExecute));
        }
    }

    @Override
    public void tickSize(int tickerId, int field, int size) {
        if (isLoggable(Level.FINEST)) {
            log(Level.FINEST, EWrapperMsgGenerator.tickSize(tickerId, field, size));
        }
    }

    @Override
    public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice, double pvDividend, double gamma, double vega, double theta, double undPrice) {
        if (isLoggable(Level.FINEST)) {
            log(Level.FINEST, EWrapperMsgGenerator.tickOptionComputation(tickerId, field, impliedVol, delta, optPrice, pvDividend, gamma, vega, theta, undPrice));
        }
    }

    @Override
    public void tickGeneric(int tickerId, int tickType, double value) {
        if (isLoggable(Level.FINEST)) {
            log(Level.FINEST, EWrapperMsgGenerator.tickGeneric(tickerId, tickType, value));
        }
    }

    @Override
    public void tickString(int tickerId, int tickType, String value) {
        if (isLoggable(Level.FINEST)) {
            log(Level.FINEST, EWrapperMsgGenerator.tickString(tickerId, tickType, value));
        }
    }

    @Override
    public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints, double impliedFuture, int holdDays, String futureExpiry, double dividendImpact, double dividendsToExpiry) {
        if (isLoggable(Level.FINEST)) {
            log(Level.FINEST, EWrapperMsgGenerator.tickEFP(tickerId, tickType, basisPoints, formattedBasisPoints, impliedFuture, holdDays, futureExpiry, dividendImpact, dividendsToExpiry));
        }
    }

    @Override
    public void orderStatus(int orderId, String status, int filled, int remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {
        log(Level.FINE, EWrapperMsgGenerator.orderStatus(orderId, status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld));
    }

    @Override
    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
        if (isLoggable(Level.FINER)) {
            log(Level.FINER, EWrapperMsgGenerator.openOrder(orderId, contract, order, orderState));
        }
    }

    @Override
    public void openOrderEnd() {
        log(Level.FINE, EWrapperMsgGenerator.openOrderEnd());
    }

    @Override
    public void updateAccountValue(String key, String value, String currency, String accountName) {
        log(Level.FINE, EWrapperMsgGenerator.updateAccountValue(key, value, currency, accountName));
    }

    @Override
    public void updatePortfolio(Contract contract, int position, double marketPrice, double marketValue, double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
        log(Level.FINE, EWrapperMsgGenerator.updatePortfolio(contract, position, marketPrice, marketValue, averageCost, unrealizedPNL, realizedPNL, accountName));
    }

    @Override
    public void updateAccountTime(String timeStamp) {
        log(Level.FINE, EWrapperMsgGenerator.updateAccountTime(timeStamp));
    }

    @Override
    public void accountDownloadEnd(String accountName) {
        log(Level.FINE, EWrapperMsgGenerator.accountDownloadEnd(accountName));
    }

    @Override
    public void contractDetails(int reqId, ContractDetails contractDetails) {
        if (isLoggable(Level.FINER)) {
            log(Level.FINER, EWrapperMsgGenerator.contractDetails(reqId, contractDetails));
        }
    }

    @Override
    public void bondContractDetails(int reqId, ContractDetails contractDetails) {
        log(Level.FINE, EWrapperMsgGenerator.bondContractDetails(reqId, contractDetails));
    }

    @Override
    public void contractDetailsEnd(int reqId) {
        if (isLoggable(Level.FINER)) {
            log(Level.FINER, EWrapperMsgGenerator.contractDetailsEnd(reqId));
        }
    }

    @Override
    public void execDetails(int reqId, Contract contract, Execution execution) {
        if (isLoggable(Level.FINER)) {
            log(Level.FINER, EWrapperMsgGenerator.execDetails(reqId, contract, execution));
        }
    }

    @Override
    public void execDetailsEnd(int reqId) {
        if (isLoggable(Level.FINER)) {
            log(Level.FINER, EWrapperMsgGenerator.execDetailsEnd(reqId));
        }
    }

    @Override
    public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {
        log(Level.FINE, EWrapperMsgGenerator.updateMktDepth(tickerId, position, operation, side, price, size));
    }

    @Override
    public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price, int size) {
        log(Level.FINE, EWrapperMsgGenerator.updateMktDepthL2(tickerId, position, marketMaker, operation, side, price, size));
    }

    @Override
    public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
        log(Level.FINE, EWrapperMsgGenerator.updateNewsBulletin(msgId, msgType, message, origExchange));
    }

    @Override
    public void managedAccounts(String accountsList) {
        log(Level.FINE, EWrapperMsgGenerator.managedAccounts(accountsList));
    }

    @Override
    public void receiveFA(int faDataType, String xml) {
        log(Level.FINE, EWrapperMsgGenerator.receiveFA(faDataType, xml));
    }

    @Override
    public void historicalData(int reqId, String date, double open, double high, double low, double close, int volume, int count, double WAP, boolean hasGaps) {
        if (isLoggable(Level.FINER)) {
            log(Level.FINER, EWrapperMsgGenerator.historicalData(reqId, date, open, high, low, close, volume, count, WAP, hasGaps));
        }
    }

    @Override
    public void scannerParameters(String xml) {
        log(Level.FINE, EWrapperMsgGenerator.scannerParameters(xml));
    }

    @Override
    public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance, String benchmark, String projection, String legsStr) {
        log(Level.FINE, EWrapperMsgGenerator.scannerData(reqId, rank, contractDetails, distance, benchmark, projection, legsStr));
    }

    @Override
    public void scannerDataEnd(int reqId) {
        log(Level.FINE, EWrapperMsgGenerator.scannerDataEnd(reqId));
    }

    @Override
    public void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume, double wap, int count) {
        if (isLoggable(Level.FINER)) {
            log(Level.FINER, EWrapperMsgGenerator.realtimeBar(reqId, time, open, high, low, close, volume, wap, count));
        }
    }

    @Override
    public void currentTime(long time) {
        log(Level.FINE, EWrapperMsgGenerator.currentTime(time));
    }

    @Override
    public void fundamentalData(int reqId, String data) {
        log(Level.FINE, EWrapperMsgGenerator.fundamentalData(reqId, data));
    }

    @Override
    public void deltaNeutralValidation(int reqId, UnderComp underComp) {
        log(Level.FINE, EWrapperMsgGenerator.deltaNeutralValidation(reqId, underComp));
    }

    @Override
    public void tickSnapshotEnd(int reqId) {
        log(Level.FINE, EWrapperMsgGenerator.tickSnapshotEnd(reqId));
    }
}
