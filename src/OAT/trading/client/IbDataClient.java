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

import com.ib.client.ContractDetails;
import com.ib.client.EWrapperMsgGenerator;
import com.ib.client.TickType;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import OAT.data.RealTimeChart;
import OAT.data.SnapShot;
import OAT.data.Tick;
import OAT.event.State;
import OAT.trading.Account;
import OAT.trading.Main;
import OAT.trading.thread.BaseThread;
import OAT.trading.thread.DataThread;
import OAT.util.DateUtil;

/**
 *
 * @author Antonio Yip
 */
public class IbDataClient extends IbClient implements DataClient {

    private Map<Integer, DataThread> dataThreadMap = new HashMap<Integer, DataThread>();
    private Map<Integer, List<ContractDetails>> cdListMap = new HashMap<Integer, List<ContractDetails>>();
    private Set<DataThread> realTimeVolume = new HashSet<DataThread>();
    private Set<Integer> firstCdReqIds = new HashSet<Integer>();
    private Set<Integer> secondCdReqIds = new HashSet<Integer>();

    public IbDataClient(BaseThread baseThread) {
        super(baseThread);
    }

    @Override
    public int getClientId() {
        return 20;
    }

    @Override
    public void disconnectFromIB() {
        super.disconnectFromIB();

        realTimeVolume.clear();

        for (BaseThread thread : Main.currentStrategies) {
            thread.setState(false, State.GETTING_MARKET_DATA);
            thread.setState(false, State.GETTING_REAL_TIME_BAR);
            thread.setState(false, State.REQUESTING_CONTRACT_DETAILS);
        }
    }

    //
    //Requests
    //
    /**
     * Request market data.
     *
     * @param dataThread
     */
    @Override
    public void reqMktData(DataThread dataThread) {
        if (!eClientSocket.isConnected()) {
            return;
        }

        if (dataThread.isGettingMarketData()) {
            dataThread.log(Level.FINE, "Duplicated market data request.");
            return;
        }

        dataThread.log(Level.INFO, "Requesting market data: " + GENERIC_TICK_TYPES);

        int reqId = REQ_MKT_DATA_I + dataThread.getThreadId();
        dataThreadMap.put(reqId, dataThread);
        firstCdReqIds.add(reqId);
        eClientSocket.reqMktData(
                reqId,
                dataThread.getContract(),
                GENERIC_TICK_TYPES, false);

        if (dataThread.getSecondContract() != null) {
            int secondReqId = REQ_2ND_MKT_DATA_I + dataThread.getThreadId();
            dataThreadMap.put(secondReqId, dataThread);
            secondCdReqIds.add(secondReqId);
            eClientSocket.reqMktData(
                    secondReqId,
                    dataThread.getSecondContract(),
                    GENERIC_TICK_TYPES, false);
        }
    }

    @Override
    public void cancelMktData(DataThread dataThread) {
        if (!eClientSocket.isConnected()) {
            return;
        }

        if (dataThread.isDataNotSubscribed()) {
            dataThread.setState(false, State.DATA_IS_NOT_SUBSCRIBED);
            return;
        }

        int reqId = REQ_MKT_DATA_I + dataThread.getThreadId();
        if (firstCdReqIds.contains(reqId)) {
            if (dataThreadMap.containsKey(reqId)) {
                eClientSocket.cancelMktData(reqId);
                firstCdReqIds.remove(reqId);
            }
        }

        int secondReqId = REQ_2ND_MKT_DATA_I + dataThread.getThreadId();
        if (secondCdReqIds.contains(secondReqId)) {
            if (dataThreadMap.containsKey(secondReqId)) {
                eClientSocket.cancelMktData(secondReqId);
                secondCdReqIds.remove(secondReqId);
            }
        }

        dataThread.log(Level.INFO, "Market data cancelled.");
        dataThread.setState(false, State.GETTING_MARKET_DATA);
    }

    @Override
    public void reqContractDetails(DataThread dataThread) {
        if (!eClientSocket.isConnected()) {
            return;
        }

        if (dataThread.isRequestingContractDetails()) {
            dataThread.log(Level.FINE, "Duplicated contract details request.");
            return;
        }

        dataThread.setState(false, State.CD_ERROR);
        dataThread.setState(true, State.REQUESTING_CONTRACT_DETAILS);

        reqContractDetails(
                REQ_CONTRACT_DETAIL_I + dataThread.getThreadId(),
                dataThread,
                dataThread.getSymbol(),
                dataThread.getSecurityType(),
                dataThread.getExchange(),
                dataThread.getCurrency());

        if (dataThread.getTradingSymbol() != null
                && !dataThread.getTradingSymbol().isEmpty()) {
            dataThread.setState(true, State.REQUESTING_TRADING_CONTRACT_DETAILS);

            reqContractDetails(
                    REQ_TRADING_CONTRACT_DETAIL_I + dataThread.getThreadId(),
                    dataThread,
                    dataThread.getTradingSymbol(),
                    dataThread.getSecurityType(),
                    dataThread.getExchange(),
                    dataThread.getCurrency());
        }
    }

    /**
     * Request real-time bar.
     *
     * @param dataThread
     */
    @Override
    public void reqRealTimeBars(DataThread dataThread) {
        if (!eClientSocket.isConnected()) {
            return;
        }

        if (dataThread.isStateActive(State.GETTING_REAL_TIME_BAR)) {
            dataThread.log(Level.FINE, "Duplicated real-time bars request.");
            return;
        }

        int reqId = REQ_REAL_TIME_BARS_I + dataThread.getThreadId();

        dataThreadMap.put(reqId, dataThread);
        dataThread.log(Level.INFO, "Requesting real-time bars.");

        eClientSocket.reqRealTimeBars(
                reqId,
                dataThread.getContract(),
                RealTimeChart.BAR_SIZE,
                "TRADES", true);
    }

    @Override
    public void cancelRealTimeBars(DataThread dataThread) {
        if (!eClientSocket.isConnected()) {
            return;
        }

        int reqId = REQ_REAL_TIME_BARS_I + dataThread.getThreadId();

        if (dataThreadMap.containsKey(reqId)) {
            eClientSocket.cancelRealTimeBars(reqId);
        }

        dataThread.log(Level.INFO, "Real time bars cancelled.");
        dataThread.setState(false, State.GETTING_REAL_TIME_BAR);
    }

    //
    //com.ib.client.Ewrapper
    //
    @Override
    public void error(final int id, final int errorCode, final String errorMsg) {
//        new Thread(new Runnable() {

//            @Override
//            public void run() {
        DataThread dataThread = dataThreadMap.get(id);
        int typeId = getReqTypeId(id);

        Level level;

        if (errorCode == 2104 || errorCode == 1102 || errorCode == 2119) { //connected || restored || connecting
            level = Level.FINE;
        } else {
            level = Level.WARNING;
        }

        if (errorCode == 502) { // 
            if (Main.getMainThread().isInitialized()) {
                logError(level, id, errorCode, errorMsg);
            }
            return;
        }

        if (errorCode == 322) { // Duplicate Id
            logError(level, id, errorCode, errorMsg);
            return;
        }

        if (dataThread == null) {
//                    log(Level.WARNING, "error(): dataThread == null");
            logError(level, id, errorCode, errorMsg);
            return;
        }

//                } else {
        switch (typeId) {
            case REQ_CONTRACT_DETAIL_I:
                logError(level, id, errorCode, errorMsg);

                dataThread.setState(false, State.REQUESTING_CONTRACT_DETAILS);
                return;

            case REQ_MKT_DATA_I:
                if (errorMsg != null
                        && errorMsg.contains("market data is not subscribed")) {
                    dataThread.setState(true, State.DATA_IS_NOT_SUBSCRIBED);

                } else {
                    logError(level, id, errorCode, errorMsg);
                }

                dataThread.setState(false, State.GETTING_MARKET_DATA);
                return;

            case REQ_REAL_TIME_BARS_I:
                logError(level, id, errorCode, errorMsg);

                dataThread.setState(false, State.GETTING_REAL_TIME_BAR);
                return;

            default:
                unhandledError(id, errorCode, errorMsg);
        }
//            }
//            }
//        }).start();
    }

    @Override
    public void contractDetails(final int reqId, final ContractDetails contractDetails) {
//        new Thread(new Runnable() {

//            @Override
//            public void run() {
        try {
            if (contractDetails == null) {
                return;
            }

            DataThread dataThread = dataThreadMap.get(reqId);
            List<ContractDetails> cdList = cdListMap.get(reqId);

            if (dataThread == null) {
                log(Level.WARNING, "contractDetails(): dataThread == null");
            } else {

                if (dataThread != null) {
                    if (cdList != null) {
                        cdList.add(contractDetails);
                    } else {
                        dataThread.log(Level.WARNING, "Unhandled contract: {0}",
                                contractDetails.m_summary.m_localSymbol);
                    }

                    dataThread.log(Level.FINE, "contractDetails downloaded: {0}"
                            + "\n\tTrading Hours: {1}"
                            + "\n\tLiquid Hours: {2}",
                            new Object[]{
                                contractDetails.m_summary.m_localSymbol,
                                contractDetails.m_tradingHours,
                                contractDetails.m_liquidHours});

                    dataThread.setState(false, State.SERVER_DELAYED);
                }
            }

        } catch (Exception e) {
            log(Level.SEVERE, null, e);
        }
//            }
//        }).start();

        super.contractDetails(reqId, contractDetails);
    }

    @Override
    public void contractDetailsEnd(final int reqId) {
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
        try {
            DataThread dataThread = dataThreadMap.get(reqId);
            List<ContractDetails> cdList = cdListMap.get(reqId);

            if (dataThread == null) {
                log(Level.WARNING, "contractDetailsEnd(): dataThread == null");
            } else {

                ContractDetails cd = getLatestContractDetailsForDataThread(reqId, dataThread, cdList);

                int reqTypeId = getReqTypeId(reqId);

                if (cd != null) {
                    if (reqTypeId == REQ_CONTRACT_DETAIL_I) {
                        dataThread.setContractDetails(cd);

                        ContractDetails secondCd = getLatestContractDetailsForDataThread(reqId, dataThread, cdList);
                        dataThread.setSecondContractDetails(secondCd);

                    } else if (reqTypeId == REQ_TRADING_CONTRACT_DETAIL_I) {
                        dataThread.setTradingContractDetails(cd);
                    }

                } else {
                    dataThread.log(Level.WARNING, "Cannot load Contract Details.");
                    dataThread.setState(true, State.CD_ERROR);
                }
            }

        } catch (Exception e) {
            log(Level.SEVERE, null, e);
        }
//            }
//        }).start();

        super.contractDetailsEnd(reqId);
    }

    @Override
    public void tickGeneric(final int tickerId, final int tickType, final double value) {
        try {
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
            final DataThread dataThread = dataThreadMap.get(tickerId);

            if (dataThread == null) {
                log(Level.WARNING, "tickGeneric: dataThread == null");
            } else {

                switch (tickType) {
                    case 318:
                        dataThread.log(Level.INFO, EWrapperMsgGenerator.tickGeneric(tickerId, tickType, value));
                        break;

                    default:
                }
            }
//        }).start();
//            }

        } catch (Exception e) {
            log(Level.SEVERE, null, e);
        }

        super.tickGeneric(tickerId, tickType, value);
    }

    @Override
    public void tickPrice(final int tickerId, final int field, final double price, final int canAutoExecute) {
        try {
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {

            final DataThread dataThread = dataThreadMap.get(tickerId);
            final boolean isFirstCd = firstCdReqIds.contains(tickerId);

            if (dataThread == null) {
                log(Level.WARNING, "tickPrice(): dataThread == null");
            } else {

                if (isFirstCd) {

                    boolean marketData = dataThread.isGettingMarketData();

                    if (!marketData) {
                        dataThread.setState(true, State.GETTING_MARKET_DATA);

                        if (dataThread.isIgnoreRTVolume()) {
                            dataThread.log(Level.INFO, "Ignoring RT Volume.");
                        }
                    }

                    SnapShot snapShot = dataThread.getSnapShot();

                    if (snapShot == null) {
                        return;
                    }

                    switch (field) {
                        case TickType.BID:
                            snapShot.setBid(price);
                            break;

                        case TickType.ASK:
                            snapShot.setAsk(price);
                            break;

                        case TickType.LAST:
                            snapShot.setLast(price);

                            if (!marketData && (dataThread.getTradingHours().isOpen()
                                    || Main.getAccount() == Account.DEMO)) {

                                if (price > 0) {
                                    snapShot.setSessionOpen(price);
                                }
                            }

                            if (price > 0 && marketData) {
                                if (snapShot.getOpen() <= 0) {
                                    snapShot.setOpen(price);
                                }

                                if (snapShot.getClose() > 0) {
                                    snapShot.setChange(price - snapShot.getClose());
                                }
                            }

                            break;

                        case TickType.OPEN:
                            snapShot.setOpen(price);

                            if (snapShot.getSessionOpen() < 0) {
                                snapShot.setSessionOpen(price);
                            }

                            if (snapShot.getHigh() < 0) {
                                snapShot.setHigh(price);
                            }

                            if (snapShot.getLow() < 0) {
                                snapShot.setLow(price);
                            }

                            break;

                        case TickType.HIGH:
                            snapShot.setHigh(price);
                            break;

                        case TickType.LOW:
                            snapShot.setLow(price);
                            break;

                        case TickType.CLOSE:
                            snapShot.setClose(price);
                            break;

                        case 318:
                            dataThread.log(Level.INFO,
                                    EWrapperMsgGenerator.tickPrice(tickerId, field, price, canAutoExecute));
                            break;

                        default:
                    }
                }

                dataThread.setLastDataTime(DateUtil.getTimeNow());
            }

        } catch (Exception e) {
            log(Level.SEVERE, null, e);
        }
//            }
//        }).start();

        super.tickPrice(tickerId, field, price, canAutoExecute);
    }

    @Override
    public void tickSize(final int tickerId, final int field, final int size) {
        try {
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
            final DataThread dataThread = dataThreadMap.get(tickerId);
            final boolean isFirstCd = firstCdReqIds.contains(tickerId);

            if (dataThread == null) {
                log(Level.WARNING, "tickSize(): dataThread == null");
            } else {

                if (isFirstCd) {
                    SnapShot snapShot = dataThread.getSnapShot();

                    if (snapShot == null) {
                        return;
                    }

                    switch (field) {
                        case TickType.BID_SIZE:
                            snapShot.setBidSize(size);
                            break;

                        case TickType.ASK_SIZE:
                            snapShot.setAskSize(size);
                            break;

                        case TickType.LAST_SIZE:
                            snapShot.setLastSize(size);
                            break;

                        case TickType.VOLUME:
                            snapShot.setVolume(size);

//                        int lastSize = snapShot.getLastSize();
//                        double last = snapShot.getLast();
//                        boolean marketData = dataThread.isGettingMarketData();
//                        boolean forcedNewBar = dataThread.isForcingNewBar();
//                        BarDataset primaryChart = dataThread.getPrimaryChart();
//
//                        if (!realTimeVolume.contains(dataThread)
//                                && marketData
//                                && lastSize > 0
//                                && last > 0
//                                && dataThread.getTradingHours().isOpen(time)) {
//                            if (primaryChart.getLastTick() != null
//                                    && getItemCount <= primaryChart.getLastTick().getDayVolume()) {
//                                if (!forcedNewBar) {
//                                    break;
//                                }
//
//                                dataThread.log(Level.WARNING,
//                                        "Invalid volume adjusted. {0} <= {1}",
//                                        new Object[]{
//                                            getItemCount,
//                                            primaryChart.getLastTick().getDayVolume()});
//
//                                primaryChart.getLastTick().setDayVolume(getItemCount - lastSize);
//                            }
//
//                            dataThread.addTick(new Tick(
//                                    last,
//                                    lastSize,
//                                    time,
//                                    getItemCount,
//                                    last,
//                                    false,
//                                    forcedNewBar));
//
//                            dataThread.setState(false, State.FORCING_NEW_BAR);
//                        }

                            break;

                        case 318:
                            dataThread.log(Level.INFO, EWrapperMsgGenerator.tickSize(tickerId, field, size));
                            break;

                        default:
                    }
                }

                dataThread.setLastDataTime(DateUtil.getTimeNow());
            }
//        }).start();
//            }

        } catch (Exception e) {
            log(Level.SEVERE, null, e);
        }

        super.tickSize(tickerId, field, size);
    }

    @Override
    public void tickString(final int tickerId, final int tickType, final String value) {
        try {
            final DataThread dataThread = dataThreadMap.get(tickerId);
            final boolean isFirstCd = firstCdReqIds.contains(tickerId);
            final boolean isSecondCd = secondCdReqIds.contains(tickerId);

            if (dataThread == null) {
                log(Level.WARNING, "tickString(): dataThread == null");
            } else {

                switch (tickType) {
                    case TickType.RT_VOLUME:
                        if (dataThread.isIgnoreRTVolume()
                                || value == null
                                || value.matches("\\D*;.*")) {
                            break;
                        }

                        if (isFirstCd) {
                            dataThread.setState(true, State.GETTING_MARKET_DATA);
                        }
//                dataThread.log(Level.INFO, "Getting RT_VOLUME ticks.");

                        StringTokenizer st = new StringTokenizer(value, ";");
                        final double last = Double.parseDouble(st.nextToken());
                        final long size = Long.parseLong(st.nextToken());
                        final long time = Long.parseLong(st.nextToken());
                        final long volume = Long.parseLong(st.nextToken());
                        final double wap = Double.parseDouble(st.nextToken());
                        final boolean b = Boolean.parseBoolean(st.nextToken());

//                        if (!realTimeVolume.contains(dataThread)) {
//                            realTimeVolume.add(dataThread);
//                dataThread.setState(true, State.GETTING_MARKET_DATA);
//                dataThread.log(Level.INFO, "Getting RT_VOLUME ticks.");
//                        }

//                        if ((dataThread.getTradingHours().isOpen(time))
                        //                                || Main.getAccount() == Account.DEMO)
//                                && getItemCount > 0 && last > 0) {
                        if (size > 0 && last > 0) {
//                    new Thread(new Runnable() {
//
//                        @Override
//                        public void run() {
                            if (isFirstCd) {
                                dataThread.addTick(new Tick(
                                        last,
                                        size,
                                        time,
                                        volume,
                                        wap,
                                        b,
                                        false));
//                                    dataThread.isForcingNewBar()));

//                            dataThread.setState(false, State.FORCING_NEW_BAR);
//                        }
//                        }
//                    }).start();
                            } else if (isSecondCd) {
                                dataThread.addSecoundCdTick(new Tick(
                                        last,
                                        size,
                                        time,
                                        volume,
                                        wap,
                                        b,
                                        false));
                            }
                        }

                        break;

                    case TickType.LAST_TIMESTAMP:
                        if (isFirstCd) {
                            dataThread.getSnapShot().setLastTimeStamp(value);
                        }
                        break;

                    case 318:
                        dataThread.log(Level.INFO, EWrapperMsgGenerator.tickString(tickerId, tickType, value));
                        break;

                    default:
                }

                dataThread.setLastDataTime(DateUtil.getTimeNow());
            }

        } catch (Exception e) {
            log(Level.SEVERE, null, e);
        }

        super.tickString(tickerId, tickType, value);
    }

    @Override
    public void realtimeBar(final int reqId, final long time, final double open, final double high, final double low, final double close, final long volume, final double wap, final int count) {
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
        try {
            DataThread dataThread = dataThreadMap.get(reqId);

//                if (dataThread == null) {
//                    log(Level.WARNING, "realTimeBar(): dataThread == null");
//                    return;
//                }

            if (dataThread != null) {
                dataThread.setState(true, State.GETTING_REAL_TIME_BAR);
            }
        } catch (Exception e) {
            log(Level.SEVERE, null, e);
        }
//            }
//        }).start();

        super.realtimeBar(reqId, time, open, high, low, close, volume, wap, count);
    }

    private void reqContractDetails(int reqId, DataThread dataThread, String symbol, String secType, String exchange, String currency) {
        dataThreadMap.put(reqId, dataThread);
        cdListMap.put(reqId, Collections.synchronizedList(new ArrayList<ContractDetails>()));

        eClientSocket.reqContractDetails(
                reqId,
                createContract(symbol, secType, exchange, currency));
    }

    private ContractDetails getLatestContractDetailsForDataThread(int reqId, DataThread dataThread, List<ContractDetails> cdList) {
        ContractDetails newCd = null;

        for (int i = 0; i < cdList.size(); i++) {
            ContractDetails cd = cdList.get(i);
            if (cd.m_summary == null
                    || cd.m_summary.m_expiry == null
                    || cd.m_contractMonth == null) {
                dataThread.log(Level.WARNING, "Empty contract received.\n{0}",
                        EWrapperMsgGenerator.contractDetails(reqId, cd));
                continue;
            }

            if (Integer.parseInt(cd.m_contractMonth.substring(4, 6)) % dataThread.getExpiryEveryMonths() != 0) {
                continue;
            }

            Calendar lastTradingDay;

            try {
                (lastTradingDay = DateUtil.getCalendarDate(
                        cd.m_summary.m_expiry, "yyyyMMdd",
                        DateUtil.getTimeZone(cd.m_timeZoneId))).add(
                        Calendar.DAY_OF_YEAR, 1 - dataThread.getLastDayBeforeExpiry());
            } catch (ParseException ex) {
                dataThread.log(Level.SEVERE, null, ex);
                continue;
            }

            if (DateUtil.getCalendarDate().after(lastTradingDay)) {
                dataThread.log(Level.INFO, "lastTradingDay: {0}",
                        lastTradingDay.getTime()); //for debugging
                continue;
            }

            if (newCd == null
                    || cd.m_summary.m_conId == newCd.m_summary.m_conId) {
                newCd = cd;
                continue;
            }

            if (cd.m_summary.m_expiry.compareTo(newCd.m_summary.m_expiry) > 0) {
                continue;
            }

            String exchange = dataThread.getExchange();
            String currency = dataThread.getCurrency();

            if ((exchange == null || exchange.equals("")
                    || exchange.equalsIgnoreCase(cd.m_summary.m_exchange)
                    || exchange.equalsIgnoreCase(cd.m_summary.m_primaryExch))
                    && (currency == null || currency.equals("")
                    || currency.equalsIgnoreCase(cd.m_summary.m_currency))) {

                newCd = cd;
            } else {
                dataThread.log(Level.WARNING,
                        "No matching exchange: {0} or currency: {1} found.",
                        new Object[]{exchange, currency});
            }
        }

        cdList.remove(newCd);

        return newCd;
    }
}
