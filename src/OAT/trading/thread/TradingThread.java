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

import OAT.trading.StrategyPlugin;
import OAT.trading.IndicatorDataset;
import OAT.trading.Trade;
import OAT.trading.OrderRecord;
import OAT.trading.Account;
import OAT.trading.Main;
import OAT.trading.AccountPosition;
import OAT.trading.Calculator;
import OAT.trading.TradeDataset;
import OAT.trading.Side;
import OAT.trading.AccountPortfolio;
import OAT.trading.Session;
import OAT.event.MarketDataChangeEvent;
import OAT.event.PositionExitEvent;
import OAT.event.AccountPortfolioChangeEvent;
import OAT.event.PositionEnterEvent;
import OAT.event.OrderChangeEvent;
import OAT.event.PositionChangeEvent;
import OAT.event.State;
import OAT.event.TradeHighLowChangeEvent;
import OAT.event.BarChartChangeEvent;
import com.ib.client.Execution;
import java.util.*;
import java.util.logging.Level;
import OAT.data.Bar;
import OAT.data.BarDataset;
import OAT.data.SnapShot;
import OAT.trading.client.TradingClient;
import OAT.ui.BarChartFrame;
import OAT.ui.ParametersFrame;
import OAT.ui.util.UiUtil;
import OAT.util.DateUtil;
import OAT.util.Tabulable;
import OAT.util.TextUtil;
import OAT.util.Waiting;

/**
 * Abstract class for trading and account handling.
 *
 * @author Antonio Yip
 */
public abstract class TradingThread extends DataThread implements Tabulable {

    public static final String[] COLUMN_HEADERS = {
        "Product", "Strategy", "Chart",
        "Position", "Enter", "Stop", "Exit", "U/R Points", "Bal. Points"};
    //
    //Trading Variables
    //
    /**
     * Active strategy.
     */
    protected boolean p_Active = true;
    /**
     * Backtest only strategy.
     */
    protected boolean p_Backtest_Only = false;
    /**
     * Avoid chasing at the same direction of the previous trade.
     */
    protected boolean p_Avoid_Chasing = true;
    /**
     * Account group for advisor account.
     */
    protected String p_Account_Group = "";
    /**
     * The default quantity of all trades.
     */
    protected int p_Default_Qty = 1;
    /**
     * To determine the limit price of a new order.
     *
     * <p> limit price = order price + side * p_Max_Limit_Ticks * getMinTick()
     */
    protected int p_Max_Limit_Ticks = 10;
    /**
     * Minimum number of minute before market close to enter new position.
     */
    protected int p_Last_Order_Minute = 20;
    /**
     * Number of minute before market close to kill all positions.
     */
    protected int p_Closing_Minute = 1;
    /**
     * Maximum loss ratio to the last price per trade.
     *
     * <p> p_Max_Stop_Factor = max loss / last price
     *
     */
    protected double p_Max_Stop_Factor = 0.004;
    /**
     * Maximum loss ratio to the last price per day.
     *
     * <p> p_Max_Daily_Loss_Factor = max day loss / last price
     *
     */
    protected double p_Max_Daily_Loss_Factor = 0.01;
    /**
     * Maximum number of consecutive loss.
     */
    protected int p_Max_Consec_Loss = 4;
    /**
     * Stop order trigger method.
     *
     * <p>0=Default, 1=Double_Bid_Ask, 2=Last, 3=Double_Last, 4=Bid_Ask,
     * 7=Last_or_Bid_Ask, 8=Mid-point
     */
    protected final int Trigger_Method = 0;
    //
    private int prevSessionIndex = -1;
    private double prevSessionGap;
    //
    private int currentPosition;
    private int adjustedPosition;
    private double currentValue;
    private List<AccountPortfolio> faAccounts;
    private Session currentSession;
    private AccountPosition accountPosition;
    private Stack<OrderRecord> orderRecords = new Stack<OrderRecord>();
    private List<Execution> executions = new ArrayList<Execution>();
    private List<IndicatorDataset> indicators = new ArrayList<IndicatorDataset>();
    private TradeDataset trades = new TradeDataset(this);
    private TradingClient tradingClient;
//    private List<Order> openOrders = new LinkedList<Order>();
    //
    private ParametersFrame variablesFrame;
    private long lastClearWebTime;
    private PendingPosition pendingPosition;
    private boolean firstChecked; //real time only

    /**
     *
     */
    public static enum Field {

        SYMBOL(0),
        STRATEGY(1),
        CHART(2),
        POS(3),
        ENTER(4),
        STOP(5),
        EXIT(6),
        GROSS(7),
        BAL_PTS(8);
        public final int column;

        Field(int column) {
            this.column = column;
        }
    }

    private class PendingPosition {

        Side side;
        int qty;
        double limit;
        boolean force;

        private PendingPosition(Side side, int qty, double limit, boolean force) {
            this.side = side;
            this.qty = qty;
            this.limit = limit;
            this.force = force;
        }
    }

    /**
     * Calculate and return the current stop price using the market data.
     *
     * @return the current stop price
     */
    public abstract double calculateStopPrice();

    /**
     * Calculate long and short positions.
     *
     * @return
     */
    public abstract Side trigger();

    /**
     * Update indicators/triggers.
     *
     */
    public abstract void update();

    /**
     * Returns a map that records the sides calculated by the trading
     * strategies.
     *
     * @return
     */
    public abstract Map<Calculator, Side> getLastCalculations();

    /**
     * Test if last trade is working.
     *
     * @return
     */
    public boolean isLastTradeWorking() {
        Trade lastTrade = getLastTrade();

        if (lastTrade == null) {
            return false;
        }

        if (getCurrentPosition() != 0) {
            if (lastTrade.getEnterOrderRecord() != null
                    && lastTrade.getEnterOrderRecord().getOrderId() >= 0
                    && !lastTrade.getEnterOrderRecord().isCanceled()
                    && (lastTrade.getEnterOrderRecord().isSubmitted()
                    || !lastTrade.getEnterOrderRecord().isFullyFilled())) {
                return true;
            }

            if (lastTrade.getExitOrderRecord() != null
                    && lastTrade.getExitOrderRecord().isMarketOrder()
                    && !lastTrade.getEnterOrderRecord().isCanceled()
                    && (lastTrade.getExitOrderRecord().isSubmitted()
                    || !lastTrade.getExitOrderRecord().isFullyFilled())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a list of indicators.
     *
     * @return
     */
    public List<IndicatorDataset> getIndicators() {
        return indicators;
    }

    @Override
    public void init() {
//    public void connect() {
        super.init();

//        if ((isInitializing() && !isPending()) || isWakingUp()) {
        log(Level.INFO, "Symbol: {0}", p_Symbol);

        //reload data and orders from SQL
        try {
            reloadConId();
            reloadTimeZone();
            reloadTradingHours();
            reloadExecutions();
            reloadCurrentPosition();

            if (tradingHours.isIntradayBreak() || currentPosition != 0) {
                reloadCurrentValue();
                reloadOrders();
            }
        } catch (Exception e) {
            log(Level.SEVERE, null, e);
        }

        updateTradingTable();
//        }
    }

    @Override
    public void postConnection() {
        //        connectToIB();
//
//        if (isPending()) {
//            return;
//        }

        //Request contract
//        reqContractDetails(createContract(p_Symbol, p_Security_Type, p_Exchange, p_Currency));
        reqContractDetails();
    }

    @Override
    public void postContractDetails() {
//        setCurrentSession();

        if (isCdError()) {
            log(Level.WARNING, "Error requesting contractDetails.");
//            restart();
            // to add error handling
            reqContractDetails();

            return;
        }

//        if (getContractDetails() == null) { //|| contractDetails.m_summary == null) {
//            log(Level.INFO, "Current contract: {0}", contractDetails.m_summary.m_localSymbol);
//        } else {
//            throw new UnsupportedOperationException("contractDetails error. Cannot initialize chart.");
//        }

//        if (isInitializing()) {
        initPrimaryChart();

        if (!getTradingHours().isEmpty()) {
            if (!getTradingHours().isIntradayBreak()) {
//                    setAccountPosition(0);
//                    setUnrealizedPNL(0);
//                    setDayPNL(0);

                //Chart
                try {
                    reloadChartData(getTradingHours().getLastOpen());
                } catch (Exception e) {
                    log(Level.SEVERE, null, e);
                }
            }
        }
//        }

//        postConnection();
//        if (contractDetails == null) {
//            reqContractDetails();
//        }


        reqExecutions();
        reqOpenOrders();

        setAccountGroup(p_Account_Group);

//        if (!isInitializing()) {
//        reqMktData();
//        }

        updateTradingTable();

        if (!isActive() && Main.getAccount() == Account.CASH) {
//            if (Main.getAccount() == Account.CASH) {
            log(Level.WARNING, "This strategy is inactive for trading.");
//            }
//
//            if (Main.getAccount() == Account.DEMO) {
//                wakeUpUntil(DateUtil.getTimeNow() + DateUtil.FIVE_MINUTE_TIME);
//            }
        }

        checkValidity();

//        if (Main.getAccount() == Account.DEMO) {
//            clearPrevSessionItems();
//        }

        setState(false, State.TRADING_SUSPENDED);
        setState(true, State.LOADED, State.INITIALIZED, State.FORCING_NEW_BAR);
    }

    private void checkValidity() {
        List<String> errorMsgs = new LinkedList<String>();

        if (getPrimaryChart() == null) {
            errorMsgs.add("null primaryChart");
        }

        if (getTradingContractDetails() == null) {
            errorMsgs.add("null contractDetails");
        }

        if (errorMsgs.size() > 0) {
            throw new UnsupportedOperationException(TextUtil.toString(errorMsgs));
        }
    }

    @Override
    public void goSleep() {
        if (isClosingPosition()) {
            return;
        }

        super.goSleep();
    }

    @Override
    public void postWakeUp() {
        reqContractDetails();
    }

    @Override
    public void preSleep() {
        super.preSleep();

        if (isPendingCheckPosition()) {
            checkPosition();
        }
    }

    @Override
    protected void preExit() {
        if (!isInitialized()) {
            return;
        }

        cancelEnterOrder();
        Trade lastTrade = getLastTrade();

        if (isSleeping()
                || getCurrentPosition() == 0
                || lastTrade == null || lastTrade.isCeased()
                || lastTrade.isCompleted() || lastTrade.isProtected()) {
            super.preExit();
            exited();
        } else {
            setupStop();
            super.preExit();
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();

        getTicksDataset().clear();
        snapShot.clear();

        currentValue = 0;
        cancelAllOrders();

        updateTradingTablePosition();
        updateTradingTableProfit();
    }

    /**
     * Clear items that does not belong to the current sessions.
     */
    public void clearPrevSessionItems() { //real time only
        long cutoff = DateUtil.getMidnightTime(getTradingHours().getNextOpen());
//                getTradingHours().getLastCloseTime();

//        if (currentSession == null) {
//            if (Main.getAccount() == Account.DEMO) {
//                cutoff = DateUtil.getTimeNow() - DateUtil.HOUR_TIME * 4;
//            } else {
//                log(Level.WARNING, "currentSession == null ");
//                return;
//            }
//        } else {
//            cutoff = currentSession.getOpenTime();
//        }

        if (!charts.isEmpty()) {
            logOnce(Level.INFO,
                    "Cleaning previous session items. cutoff = "
                    + DateUtil.getDateTimeString(cutoff));
        }

        trades.clear(cutoff);

        for (StrategyPlugin strategyPlugin : getStrategyPlugins()) {
            strategyPlugin.getIndicatorDataset().clear(cutoff);
        }

        for (BarDataset chart : charts) {
            chart.clear(cutoff);
        }

        prevSessionIndex = -1;
        prevSessionGap = 0;


//        Calendar nextOpenDate = tradingHours.getNextOpen();
//
//        Calendar cutOff = DateUtil.getMidnight(nextOpenDate);
//
//        try {
//            Calendar clearTime = DateUtil.getCalendarDate(
//                    Main.p_Clear_Balance_Time, DateUtil.MINUTE_TIME_FORMAT);
//            cutOff.add(Calendar.HOUR_OF_DAY, clearTime.get(Calendar.HOUR_OF_DAY));
//            cutOff.add(Calendar.MINUTE, clearTime.get(Calendar.MINUTE));
//        } catch (ParseException ex) {
//        }

//        if (new Session(cutOff, nextOpenDate).isInNow()) {
        try {
            dropWebExecutionBefore(cutoff);
            clearWebQuote();

            updatePortfolioTable(0, 6);
        } catch (Exception e) {
            log(Level.SEVERE, null, e);
        }
//        }
    }

    //
    //Tasks
    //
    @Override
    protected void intervalTask() {
        if (tradingHours.isOpen()) {
            marketTimeTask();
        } else {
            afterMarketTimeTask();
        }

        super.intervalTask();
    }

    /**
     * Routines to run every defined interval during market time.
     */
    protected void marketTimeTask() {
//        setCurrentSession();

        long now = DateUtil.getTimeNow();
        long open = tradingHours.getLastOpenTime();

        if (DateUtil.isNow(open)) { //Market just opened
            setState(true, State.MARKET_JUST_OPENED);

//            reqMktData();

            if (getCurrentPosition() != 0) {
                log(Level.WARNING, "Closing odd position when market opens.");
                closePosition();
            }

            if (isActive() && Main.getAccount() == Account.CASH) {
                displayPrimaryChart();
            }

        } else {
            setState(false, State.MARKET_JUST_OPENED);

//            if (!isClosingPosition()) {
////                closePosition();
////            } else {
//                if (getLastTrade() != null
//                        && getLastTrade().isCompleted()
//                        && getCurrentPosition() == 0
//                        && getAccountPosition() != null
//                        && getAccountPosition().getPosition() != 0) {
//                    
//                    
//                    closePosition();
//                }
//            }

            if (isLastTradeWorking()) {
                log(Level.FINE, "Last trade is working. Checking delayed.");
            } else {
                if (isPendingReqExecutions()
                        && !isServerDelayed()
                        && !isClosingPosition()) {
                    reqExecutions();
                    reqOpenOrders();
                }

                if (getCurrentPosition() == 0
                        //                        && getLastTrade() != null
                        //                        && getLastTrade().isCompleted()
                        && getAccountPosition() != null
                        && getAccountPosition().getPosition() != 0) {
                    closePosition();

                } else {
                    if (isPendingCheckPosition()) {
                        checkPosition();
                    }
                }
            }

            if (isGettingMarketData()
                    && now - lastDataTime
                    >= Math.max(Main.p_Task_Interval, Main.p_Market_Data_Time_Out)) {

                if (currentPosition != 0) {
                    log(Level.WARNING, "Closing position because lack of real time data.");
                    closePosition();
                }

                log(Level.INFO, "Restarting market data.");
                cancelMktData();
//                reqMktData();
//            } else {
            }

            reqMktData();
        }

        //lastTrade may change anytime
//        Trade lastTrade;
//        lastTrade = getLastTrade();
        if (getLastTrade() != null
                && getLastTrade().getEnterOrderRecord() != null
                && getLastTrade().getEnterOrderRecord().isSubmitted()) {
            long created = getLastTrade().getCreatedTime();

            if (now - created >= Main.p_Task_Interval) {
                log(Level.INFO, "Cancelling unfinished position.");
                cancelEnterOrder();
                closePosition();
            }
        }

//        lastTrade = getLastTrade(); 
        if (isLastInterval(now)) {
            log(Level.INFO, "Cleaning position at last minute.");
            closePosition();

        } else if (isMarketClosing(now)) {
            if (currentPosition != 0) {
                log(Level.INFO, "Session closing, closing position by market order.");
                closePosition();

                if (getLastTrade() != null) {
                    getLastTrade().setExitOnClose(true);
                }
            }

        } else if (currentPosition != 0
                && getLastTrade() != null
                && getLastTrade().getStopPrice() > 0
                && getLastTickPrice() > 0
                && currentPosition * getLastTickPrice()
                <= currentPosition * getLastTrade().getStopPrice()) {
            log(Level.WARNING,
                    "Stop price {0} triggered (Last: {1}), closing position by market order.",
                    new Object[]{
                        getLastTrade().getStopPrice(),
                        getLastTickPrice()
                    });

            closePosition();

        } else {
            if (currentPosition != 0
                    //                    && accountPosition != null
                    //                    && accountPosition.getPosition() != 0
                    //                    && !isServerDelayed()
                    && getLastTrade() != null
                    && getLastTrade().isOpen()
                    //                    && !getLastTrade().isWorking()
                    && !getLastTrade().isProtected()) {
                log(Level.WARNING, "Last trade is not protected. Position = {0}, Covered = {1}",
                        new Object[]{
                            getLastTrade().getNetPosition(),
                            getLastTrade().getCovered()
                        });
                setupStop();
//                setState(true, State.PENDING_CHECK_POSITION);
            }
        }

//        if (realTimeChart != null) {
//            long duration = now - realTimeChart.getLast().getOpenTime();
//            if (duration > 30000) {
//                reqHistoricalData(realTimeChart,
//                        Math.min(2 * DateUtil.HOUR_TIME,
//                        duration));
//            }
//        }

        if (DateUtil.isMinuteInterval(5)) {
            updateTimeCharts();
        }
    }

    /**
     * Routines to run every defined interval after market time.
     */
    protected void afterMarketTimeTask() {
        long nextOpenTime = tradingHours.getNextOpenTime();
        long wakeUpTime = nextOpenTime - Main.p_Wake_up_before_Open;

        if (!isExiting()) {
            //clean up prev day
            if (!tradingHours.isIntradayBreak()) {
                if (DateUtil.isNow(wakeUpTime)) {
                    cleanup();

                } else {
                    long now = DateUtil.getTimeNow();

                    if (now >= wakeUpTime && now < nextOpenTime) {
                        clearPrevSessionItems();
                    }
                }
            }

            wakeUpUntil(wakeUpTime);

            if (!isSleeping()) {
                reqMktData();
            }
        }
    }

    //
    //Orders
    //
    public void cancelOrder(int orderId) {
        getTradingClient().cancelOrder(this, orderId);
    }

    /**
     * Cancel all orders.
     */
    public void cancelAllOrders() {
        pendingPosition = null;

        List<Integer> ids = new ArrayList<Integer>();

        for (OrderRecord orderRecord : orderRecords) {
            if (orderRecord == null
                    || orderRecord.getOrderId() < 0
                    || orderRecord.isFullyFilled()
                    || orderRecord.isCanceled()
                    || orderRecord.isNotFound()) {
                continue;
            }

            ids.add(orderRecord.getOrderId());
        }

        for (Integer id : ids) {
            cancelOrder(id);
        }
    }

    /**
     * Cancel any enter order.
     */
    public void cancelEnterOrder() {
        pendingPosition = null;

        Trade lastTrade = getLastTrade();

        if (lastTrade != null
                && lastTrade.getEnterOrderRecord() != null
                && lastTrade.getEnterOrderRecord().isSubmitted()) {
            log(Level.FINE, "Canceling enter order");
            cancelOrder(lastTrade.getEnterOrderRecord());
        }
    }

    /**
     * Cancel any exit order.
     */
    public void cancelExitOrder() {
        Trade lastTrade = getLastTrade();

        if (lastTrade != null
                && lastTrade.getExitOrderRecord() != null
                && lastTrade.getExitOrderRecord().isSubmitted()) {
            log(Level.FINE, "Canceling exit order");
            cancelOrder(lastTrade.getExitOrderRecord());
        }
    }

    /**
     * Cancel an order record.
     *
     * @param orderRecord
     */
    public void cancelOrder(final OrderRecord orderRecord) {
        if (orderRecord == null) {
            return;
        }

        if (orderRecord.isCanceled()
                || orderRecord.isFullyFilled()
                || orderRecord.isNotFound()
                || orderRecord.isError()) {
            return;
        }

        if (isBacktesting()) {
            if (!orderRecord.isFilled()) {
                orderRecord.setStatus("Cancelled");
            }

            return;
        }

        cancelOrder(orderRecord.getOrderId());
    }

    /**
     * Place new order.
     *
     * @param orderRecord
     * @param side
     * @param qty
     * @param orderType
     * @param lmtPrice
     * @param auxPrice
     */
    public void placeOrder(OrderRecord orderRecord, Side side, int qty, String orderType, double lmtPrice, double auxPrice) {
        new Waiting(10000, 500, getLogger()) {

            @Override
            public boolean waitWhile() {
                return !getTradingClient().isInterruptible();
            }

            @Override
            public void retry() {
            }

            @Override
            public String message() {
                return "Waiting for tradingClient to finish...";
            }

            @Override
            public void timeout() {
            }
        };

        getTradingClient().placeOrder(this, orderRecord, side, qty, orderType, lmtPrice, auxPrice);
    }

    private boolean changeOrder(OrderRecord orderRecord, Side side, int qty, String orderType, double lmtPrice, double auxPrice) {
        return getTradingClient().changeOrder(this, orderRecord, side, qty, orderType, lmtPrice, auxPrice);
    }

    //
    //Requests
    //
    public void reqOpenOrders() {
        getTradingClient().reqOpenOrders(this);
    }

    /**
     * Request executions.
     */
    public void reqExecutions() {
        getTradingClient().reqExecutions(this);
    }

    private void reloadCurrentPosition() throws Exception {
        long reloadSince = tradingHours.getLastOpen().getTimeInMillis();

        currentPosition = (Main.tradingSchema.getCurrentPosition(
                getThreadId(),
                reloadedConId,
                reloadSince,
                getAccountPrefix()));

        log(Level.INFO,
                "Reloaded POS ({0}) = {1}",
                new Object[]{getAccountPrefix(), currentPosition});
    }

    private void reloadCurrentValue() throws Exception {
        long reloadSince = tradingHours.getLastOpen().getTimeInMillis();

        currentValue = Main.tradingSchema.getCurrentValue(
                getThreadId(),
                reloadedConId,
                reloadSince,
                getAccountPrefix());
    }

    private void reloadExecutions() throws Exception {
        long reloadSince = tradingHours.getLastOpen().getTimeInMillis();

        executions = Main.tradingSchema.getExecutions(
                getThreadId(),
                reloadedConId,
                reloadSince);

        for (Execution execution : executions) {
            insertWebExecution(execution);
        }

        log(Level.INFO, "Reloaded executions: {0}", executions.size());
    }

    private void reloadOrders() throws Exception {
        long reloadSince = tradingHours.getLastOpen().getTimeInMillis();

        if (currentPosition != 0) {
            OrderRecord enterOrderRecord = createNewEnterOrderRecord();
            OrderRecord exitOrderRecord = getLastExitOrderRecord();

            Main.tradingSchema.getLastFilledOrder(
                    getThreadId(),
                    reloadedConId,
                    reloadSince,
                    enterOrderRecord);

            Main.tradingSchema.getSubmittedOrder(
                    getThreadId(),
                    reloadedConId,
                    reloadSince,
                    exitOrderRecord);

            for (Execution execution : executions) {
                OrderRecord orderRecord = getOrderRecord(execution.m_orderId);

                if (orderRecord != null) {
                    orderRecord.setExecution(execution, false);
                }
            }
        }
    }

    private void reloadTimeZone() throws Exception {
        tradingHours.setTimeZone(Main.dataSchema.getTimeZone(p_Symbol));

        if (tradingHours.getTimeZone() != null) {
            log(Level.INFO,
                    "Reloaded time zone: {0}",
                    tradingHours.getTimeZone().getDisplayName());
        }
    }

    private void reloadTradingHours() throws Exception {
        tradingHours.addTradingSession(Main.dataSchema.getTradingHours(p_Symbol,
                DateUtil.getMidnight(
                DateUtil.getLastWorkingDay(),
                tradingHours.getTimeZone()).getTimeInMillis()));

        log(Level.INFO,
                "Reloading from SQL since: {0}",
                tradingHours.getLastOpen().getTime());
    }

    public boolean addExecution(Execution newExecution) { //execution may come from outside API
        if (newExecution == null
                || executions.contains(newExecution)) {
            return false;
        }

        if (!isBacktesting() && Main.getAccount() != Account.DEMO) {
            if (primaryChart != null
                    && !primaryChart.isEmpty()
                    && tradingHours != null
                    && DateUtil.getTime(newExecution.m_time)
                    < tradingHours.getLastOpenTime(primaryChart.getLast().getOpenTime())) {
                return false;
            }
        }

        boolean isNew = executions.add(newExecution);
        boolean isValid = isValidExecution(newExecution);

        if (isValid) {
            int side = Side.valueOfAction(newExecution.m_side).sign;
            int shares = newExecution.m_shares;

            currentPosition += side * shares;
            currentValue -= side * shares * newExecution.m_price;

            if (currentPosition == 0) {
//                setState(false, State.CLOSING_POSITION);

                if (pendingPosition != null) {
                    log(Level.INFO, "Placing pending position {0}.", pendingPosition.side);

                    enterPosition(
                            pendingPosition.side,
                            pendingPosition.qty,
                            pendingPosition.limit,
                            pendingPosition.force);

                    pendingPosition = null;
                }
            }
        }

        return isNew && isValid;
    }

    private void dropWebExecutionBefore(long time) throws Exception {
        if (time == lastClearWebTime) {
            return;
        }

        updateWebSymbols(Field.STOP.toString(), null);
        Main.webSchema.dropWebExecutions(contractId, time);

        lastClearWebTime = time;

        log(Level.INFO, "Web executions cleared.");
    }

    private void dropWebExecutionBefore(Calendar date) throws Exception {
        dropWebExecutionBefore(date.getTimeInMillis());
    }

    private void clearWebQuote() throws Exception {
        Main.webSchema.clearWebQuote(contractId);
    }

    //
    //Getters
    //
    /**
     * Get strategy plugins.
     *
     * @return
     */
    public abstract StrategyPlugin[] getStrategyPlugins();

    /**
     * Test if the trading client is active.
     *
     * @return
     */
    public boolean isActive() {
        return p_Active;
    }

    /**
     * Test if it is a backtest strategy.
     *
     * @return
     */
    public boolean isBacktestOnly() {
        return p_Backtest_Only;
    }

    @Override
    public String getDefaultLoggerName() {
        return p_Symbol;
    }

    public TradingClient getTradingClient() {
        return tradingClient;
    }

    /**
     * Returns the contract minimal increment of price.
     *
     * @return
     */
    public double getMinTick() {
        if (getTradingContractDetails().m_priceMagnifier == 0) {
            return getTradingContractDetails().m_minTick;
        }

        return getTradingContractDetails().m_minTick
                * getTradingContractDetails().m_priceMagnifier;
    }

    /**
     * Returns the strategy ID.
     *
     * @return
     */
    public int getStrategyId() {
        return Main.currentStrategies.indexOf(this);
    }

    /**
     *
     * @return
     */
    public int getDefaultQty() {
        return p_Default_Qty;
    }

    /**
     *
     * @return
     */
    public double getURPoints() {
        Trade lastTrade = getLastTrade();

        if (lastTrade != null && lastTrade.isOpen()) {
            return lastTrade.getProfit() * getGroupSize();
        }

        return Double.NaN;
    }

    /**
     *
     * @return
     */
    public double getBalPoints() {
        if (currentPosition != 0 && getLastTickPrice() > 0) {
            return currentPosition * getLastTickPrice() + currentValue;
        }

        return currentValue;
    }

    /**
     *
     * @return
     */
    public double getEnterPrice() {
        Trade lastTrade = getLastTrade();
        if (lastTrade != null && lastTrade.isOpen() && currentPosition != 0) {
            return lastTrade.getEnterPrice();
        }

        return Double.NaN;
    }

    /**
     *
     * @return
     */
    public double getExitPrice() {
        Trade lastTrade = getLastTrade();

        if (lastTrade != null
                && lastTrade.getExitOrderRecord() != null
                && lastTrade.getExitOrderRecord().isFilled()) {
            return lastTrade.getExitPrice();
        }

        return Double.NaN;
    }

    /**
     *
     * @return
     */
    public double getStopPrice() {
        Trade lastTrade = getLastTrade();

        if (lastTrade != null
                && lastTrade.getExitOrderRecord() != null
                && lastTrade.getExitOrderRecord().isSubmitted()) {
            return lastTrade.getStopPrice();
        }

        return Double.NaN;
    }

    public AccountPosition getAccountPosition() {
        return accountPosition;
    }

    /**
     *
     * @return
     */
    public String getAccountPrefix() {
        return (Main.getAccount() == Account.DEMO || Main.getAccount() == Account.PAPER
                ? "D" : "")
                + (Main.isAdvisorAccount() ? "F" : "");
    }

    public String getAccountGroup() {
        if (Main.getAccount() == Account.CASH) {
            return p_Account_Group;
        } else {
            String group = Main.getMainThread().getDefaultAccountGroup();

            if (group == null) {
                return "";
            }

            return group;
        }
    }

    public int getTriggerMethod() {
        return Trigger_Method;
    }

    /**
     *
     * @return
     */
    public int getGroupSize() {
        if (isBacktesting() || faAccounts == null || faAccounts.isEmpty()) {
            return 1;
        } else {
            return faAccounts.size();
        }
    }

    /**
     *
     * @return
     */
    public int getCurrentPosition() {
        return currentPosition;
    }

    /**
     *
     * @return
     */
    public double getCurrentValue() {
        return currentValue;
    }

    /**
     *
     * @return
     */
    public Side getCurrentSide() {
        return Side.valueOf(currentPosition);
    }

    /**
     *
     * @return
     */
    public List<Execution> getExecutions() {
        return executions;
    }

    /**
     *
     * @return
     */
    public Execution getLastExecution() {
        if (executions.isEmpty()) {
            return null;
        }

        return executions.get(executions.size() - 1);
    }

    /**
     *
     * @return
     */
    public TradeDataset getTrades() {
        return trades;
    }

    /**
     *
     * @return
     */
    public Trade getLastTrade() {
        return trades.getLast();
    }

    /**
     *
     * @return
     */
    public double getGross() {
        int gross = 0;

        for (Trade trade : trades) {
            gross += trade.getProfit();
        }

        return gross;
    }

    /**
     *
     * @return
     */
    public double getGrossAverage() {
        return getGross() / trades.getItemCount();
    }

    /**
     *
     * @return
     */
    public ParametersFrame getVariablesFrame() {
        return variablesFrame;
    }

    @Override
    public String[] getColumnHeaders() {
        return COLUMN_HEADERS;
    }

    @Override
    public Object[] getRow(int index) {
        if (index >= getItemCount()) {
            return null;
        }

        return new Object[]{
                    p_Symbol,
                    this.getClass().getSimpleName(),
                    primaryChart == null ? null : primaryChart.getBarSizeStr(),
                    currentPosition,
                    getEnterPrice(),
                    getStopPrice(),
                    getExitPrice(),
                    getURPoints(),
                    getBalPoints()
                };
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public double getPrevSessionGap() {
        return prevSessionGap;
    }

    public int getPrevSessionIndex() {
        return prevSessionIndex;
    }

    /**
     *
     * @param orderId
     * @return
     */
    protected OrderRecord getOrderRecord(int orderId) {
        for (OrderRecord orderRecord : orderRecords) {
            if (orderId == orderRecord.getOrderId()) {
                return orderRecord;
            }
        }

        return null;
    }

    public Stack<OrderRecord> getOrderRecords() {
        return orderRecords;
    }

    /**
     *
     * @return
     */
    public Session getCurrentSession() {
        return currentSession;
    }

    /**
     *
     * @param other
     * @return
     */
    public boolean isSameContract(TradingThread other) {
        if (this.contract != null && this.contract.equals(other.contract)) {
            return true;
        }

        return this.p_Symbol.equalsIgnoreCase(other.p_Symbol)
                && this.p_Last_Day_Before_Expiry == other.p_Last_Day_Before_Expiry
                && this.p_Expiry_Every_n_Months == other.p_Expiry_Every_n_Months;
    }

    protected boolean isLastOrderPassed(long time) {
        return isMarketClosingIn(DateUtil.MINUTE_TIME * p_Last_Order_Minute, time);
    }

    protected boolean isMarketClosing(long time) {
        return isMarketClosingIn(DateUtil.MINUTE_TIME * p_Closing_Minute, time);
    }

    protected boolean isLastInterval(long time) {
        return isMarketClosingIn(Main.p_Task_Interval, time);
    }

    protected boolean isMarketClosingIn(long timeBeforeClose, long time) {
        long close = tradingHours.getNextCloseTime(time);

        return time >= close - timeBeforeClose && time < close;
    }

    protected boolean isValidExecution(Execution execution) {
        if (isBacktesting() || !Main.isAdvisorAccount()) {
            return true;
        }

        if ((execution.m_shares > 0 && execution.m_acctNumber.contains("F"))
                || execution.m_clientId == 0) {
            return true;
        }

        return false;
    }

    //
    //Setters
    //
    public void setTradingClient(TradingClient tradingClient) {
        this.tradingClient = tradingClient;
    }

    /**
     *
     * @param string
     */
    public void setAccountGroup(String string) {
        faAccounts = Main.getMainThread().getAccountPortfolios(string);

        if (faAccounts == null) {
            if (Main.getAccount() == Account.CASH) {
                log(Level.SEVERE, "faAccounts == null");
                return;
            } else {
                faAccounts = Main.getMainThread().getAccountPortfolios("Group1");
                if (faAccounts == null) {
                    faAccounts = new LinkedList<AccountPortfolio>();
                }
            }
        }

        log(Level.INFO, "{0}: {1}",
                new Object[]{
                    Main.getMainThread().getAccountGroupString(string),
                    TextUtil.toString(faAccounts)});

        log(Level.INFO,
                "Purchase size = {0}{1}",
                new Object[]{
                    getGroupSize(),
                    p_Default_Qty > 1 ? " x " + p_Default_Qty : ""});

        if (Main.getMainThread().isInitialized()) {
            AccountPortfolio advisor = Main.getMainThread().getAdvisorAccount();

            updateWebSymbols("m_group",
                    (advisor != null ? advisor.toString() + ", " : "")
                    + TextUtil.toString(faAccounts));
        }

        for (AccountPortfolio accountPortfolio : faAccounts) {
            AccountPosition position = accountPortfolio.addContract(getTradingContract());

            position.addChangeListener(this);
            accountPortfolio.addChangeListener(this);
        }
    }

    /**
     * Calculate the current session from tradingHours.
     */
    public void setCurrentSession() {
        setCurrentSession(tradingHours.getCurrentSession());
    }

    /**
     * Set current session from UNIX time.
     *
     * @param time milliseconds
     */
    public void setCurrentSession(long time) {
        setCurrentSession(tradingHours.getCurrentSession(time));
    }

    /**
     * Set current session.
     *
     * @param session
     */
    public void setCurrentSession(Session session) {
        if (session == null || session.equals(currentSession)) {
            return;
        }

        this.currentSession = session;

        log(Level.INFO, "Current session: {0}", session);

        //cancel trading suspension when session changed
        setState(false, State.TRADING_SUSPENDED);
    }

    //
    // Account Position (real-time only)
    //
    /**
     *
     * @param accountPosition
     */
    public void setAccountPosition(AccountPosition accountPosition) { //real time only
        boolean checkNow = this.accountPosition == null && accountPosition != null;

        this.accountPosition = accountPosition;

        if (checkNow) {
            checkPosition();
        } else {
            setState(true, State.PENDING_CHECK_POSITION);
        }
    }

    public void resetAccountPosition() { //real time only
        if (accountPosition == null) {
            return;
        }

        log(Level.WARNING, "Reset account position.");

        double realizedPNL = 0;

        for (Trade trade : getTrades()) {
            realizedPNL += trade.getNetAmount();
        }

        accountPosition.setRealizedPNL(realizedPNL, false);
        accountPosition.setUnrealizedPNL(0, false);
        accountPosition.setPosition(0, true);
    }

    public void checkPosition() { //real time only
        if (isSleeping()) {
            return;
        }

        if (isLastTradeWorking()) {
            log(Level.FINE, "Check position delayed.");
            return;
        }

//        boolean fixed = false;

        Trade lastTrade = getLastTrade();

        if (lastTrade != null) {
            if (lastTrade.isWorking() || lastTrade.isJustSubmitted()) {
                log(Level.FINE, "Last trade is working, position was not checked.");
                return;
            }
        }

        log(Level.FINE, "Checking position...");
        setState(false, State.PENDING_CHECK_POSITION);

        int acctPosition;

        if (accountPosition == null) {
            acctPosition = 0;
        } else {
            acctPosition = accountPosition.getPosition();
        }

        if (currentPosition > getDefaultQty() * getGroupSize()
                || acctPosition > getDefaultQty()) {
            log(Level.SEVERE,
                    "{0} Excess quantity: "
                    + "accountPosition = {1}, currentPosition = {2}",
                    new Object[]{
                        getTradingContract().m_localSymbol,
                        acctPosition,
                        currentPosition});

            log(Level.WARNING, "Excess quantity, closing position.");
            closePosition();

        } else if (currentPosition != acctPosition * getGroupSize()) {
            log(Level.SEVERE,
                    "{0} Inconsistent quantity: "
                    + "accountPosition = {1}, currentPosition = {2}",
                    new Object[]{
                        getTradingContract().m_localSymbol,
                        acctPosition,
                        currentPosition});

//            reqExecutions();
//            reqOpenOrders();

            if (firstChecked) {
                if ((getLastTrade() == null || !getLastTrade().isWorking())
                        && (currentPosition != 0 && acctPosition == 0)) {
                    // server may delay don't do anything here.
                    log(Level.WARNING, "Inconsistent quantity persists, server may be delayed.");

                } else {
                    log(Level.WARNING, "Inconsistent quantity, closing position.");
                    closePosition();
                }

            } else {
                if (currentPosition == 0 && acctPosition != 0) {
//                adjustedPosition = currentPosition;
                    cancelAllOrders();

                    setCurrentPosition(acctPosition * getGroupSize());
//                logger.log(Level.WARNING, "currentPosition changed to {0}", currentPosition);
//                adjustedPosition -= currentPosition;
//                setupStop();

//                fixed = true;

//            } else if (currentPosition != 0 && acctPosition == 0) {
//                adjustedPosition = currentPosition;
//                currentPosition = 0;
//                logger.log(Level.WARNING, "currentPosition changed to {0}", currentPosition);
//                setCurrentPosition(0);
//                closePosition();
//                fixed = true;
                } else {
                    log(Level.WARNING, "Inconsistent quantity, closing position.");
                    closePosition();
                }
            }


//            if (!isServerDelayed()) {
//                adjustedPosition = 0;
//                logger.log(Level.FINE,
//                        "adjustedPosition reset to 0, currentPosition = {0}",
//                        currentPosition);
//            }

//        } else if (currentPosition == 0
//                && accountPosition != null
//                && accountPosition.getPosition() == 0) {
//            adjustedPosition = 0;
//            logger.log(Level.FINE,
//                    "adjustedPosition reset to 0, currentPosition = {0}",
//                    currentPosition);
        }

////        check last exit order
//        if (lastTrade != null) {
//            if (lastTrade.getExitTime() > 0
//                    && lastTrade.getExitTime() < lastTrade.getEnterTime()) {
//                lastTrade.addExitOrderRecord(new OrderRecord(), true);
//                log(Level.WARNING, "Deleted invalid exit order.");
//
//
//                updateTradingTablePosition();
//                updateTradingTableProfit();
//            }
//        }

        firstChecked = true;
        log(Level.FINE, "Position checked.");

        updateTradingTable();
//        setupStop();
    }

    public void setCurrentPosition(int newPosition) { //real time only
        if (currentPosition == newPosition) {
            return;
        }

        if (isServerDelayed()) {
            adjustedPosition = newPosition - currentPosition;
        }

        currentPosition = newPosition;

//        if (currentPosition == 0) {
//            setState(false, State.CLOSING_POSITION);
//        }

        updateTradingTablePosition();
        updateTradingTableProfit();

        log(Level.WARNING,
                "currentPosition changed to {0}", //, adjustedPosition = {1}",
                new Object[]{currentPosition});//, adjustedPosition});
    }

    //
    // Trading
    //
    /**
     * Trigger calculators and enter position.
     */
    public void enterPosition() {
        Side side = trigger(); //trigger all calculators
        boolean newPosition =
                side != Side.NEUTRAL
                && side != Side.valueOf(currentPosition);
//                && (trades.isEmpty() || trades.getLast().getEnterOrderRecord() == null
//                || trades.getLast().getEnterOrderRecord().getBar() != primaryChart.getLast());

        if (newPosition) {
//                Level level = isTradingSuspended() || !isActive() ? Level.FINE : Level.INFO;

            if (!isBacktesting()) {
                log(Level.FINE, "Calculated position: {0} by {1}",
                        new Object[]{
                            side.sign,
                            TextUtil.toString(getLastCalculations())});
            }

            enterPosition(side, p_Default_Qty,
                    getLastTickPrice() + side.sign * p_Max_Limit_Ticks * getMinTick());
        }
    }

    /**
     *
     * @param side
     * @param qty
     * @param limit
     */
    public void enterPosition(Side side, int qty, double limit) {
        enterPosition(side, qty, limit, false);
    }

    /**
     *
     * @param side
     * @param qty
     * @param limit
     * @param force
     */
    public void enterPosition(Side side, int qty, double limit, boolean force) {
        if (side == Side.NEUTRAL || qty <= 0) {
            return;
        }

        boolean backtesting = isBacktesting();

//        if (isClosingPosition()) {
//            if (!isBacktesting()) {
//                logger.warning("Waiting for position to close.");
//                setState(true,
//                        State.PENDING_REQUEST_EXECUTIONS,
//                        State.PENDING_CHECK_POSITION);
//            }
//            
//            if (getLastTrade().getExitOrderRecord().isWorking()) {
//                return;
//            }
//        }

        int multiplier = getGroupSize();
        int netPosition = side.sign * qty * multiplier;

        //Do nothing if net position == 0
        if (netPosition == currentPosition) {
            return;
        }

        //Real-time errors
        if (!backtesting) {
            //Position conflicts
            if (!force && Math.abs(netPosition + currentPosition) > p_Default_Qty * multiplier) {
                log(Level.WARNING,
                        "Calculated position ({0} + {1}) exceeded default quantity "
                        + "({2} x {3}), order was not send.",
                        new Object[]{netPosition,
                            currentPosition,
                            p_Default_Qty,
                            multiplier
                        });
                return;
            }


            //Initialzing or exiting
            if (isExiting() || !isInitialized()) {
                log(Level.INFO,
                        "Cannot place order during initializing or exiting: {0} QTY={1}",
                        new Object[]{side.actionName, qty});
                return;
            }

            //Waiting position to close
            if (isClosingPosition() && getCurrentPosition() != 0) {
                log(Level.FINE, "Waiting position to close, order cancelled.");
                return;
            }

            //Inactive
            if (Main.getAccount() == Account.CASH) {
                if (!force && !isActive()) {
                    log(Level.FINER,
                            "Inactive strategy. Order suspended: {0} @ {1}",
                            new Object[]{side.actionName, limit});
                    return;
                }
            }

//            //Disconnected
//            if (!isConnected()) {
//                logger.warning("Disconnected. Order canceled.");
//                return;
//            }

            //Another order is in place
            if (getLastTrade() != null && getLastTrade().getSide() == side) {
                OrderRecord enterOrderRecord = getLastTrade().getEnterOrderRecord();

                if (enterOrderRecord != null && !enterOrderRecord.isCanceled()) {
                    if (enterOrderRecord.isSubmitted() || enterOrderRecord.isWorking()) {
                        log(Level.WARNING, "Another enter order is in place.");
                        return;
                    }
                }
            }
        }

        //Cancel any previous enter order
        cancelEnterOrder();

        //Close position before reverse order
        if (currentPosition != 0) {
            log(Level.INFO, "Entering opposite position, closing current position.");

            closePosition();

            pendingPosition = new PendingPosition(side, qty, limit, force);
            return;
        }

        //Trading is suspended
        if (!force && isTradingSuspended()) {
            log(Level.FINE, "Trading has been suspended.");
            return;
        }

        //Server delayed - real time only
        if (!backtesting && !force && isServerDelayed()) {
            log(Level.INFO,
                    "Server response is delayed. Trading will be resumed once back to normal.");
            return;
        }

        //Data broken
        if (backtesting || Main.getAccount() != Account.DEMO) {
            if (!force && isDataBroken()) {
                log(Level.INFO, "Market data is broken.");
                setState(true, State.TRADING_SUSPENDED);
                return;
            }
        }

        //Last order time passed
        if (!force && isLastOrderPassed(getLastTickTime())) {
            log(Level.INFO, "Last order time has passed.");
            setState(true, State.TRADING_SUSPENDED);
            return;
        }

//        //Too Frequent
//        if (!force) {
//            Trade lastTrade = getLastTrade();
//            if (lastTrade != null
//                    && lastTrade.getExitBar() != null
//                    && primaryChart.indexOf(lastTrade.getExitBar())
//                    >= primaryChart.getItemCount() - 2) {
//                if (!backtesting) {
//                    logger.info("Too frequet trade.");
//                }
//                return;
//            }
//        }

        //Reached max consecutive loss
        if (backtesting || Main.getAccount() != Account.DEMO) {
            int consecLoss = 0;
            double dailyLoss = 0;

            for (Trade trade : trades) {
                if (trade.getProfit() < 0) {
                    consecLoss++;
                    dailyLoss += trade.getProfit();

                    if (consecLoss >= p_Max_Consec_Loss) {
                        log(Level.INFO, "Reached maximum consecutive loss.");
                        setState(true, State.TRADING_SUSPENDED);
                        return;
                    }

                    if (dailyLoss <= -getLastTickPrice() * p_Max_Daily_Loss_Factor) {
                        log(Level.INFO, "Reached daily loss.");
                        setState(true, State.TRADING_SUSPENDED);
                        return;
                    }

                } else {
                    consecLoss = 0;
                }
            }
        }

        //Avoid chasing
        if (p_Avoid_Chasing && !force && getLastTrade() != null) {
            Side lastSide = getLastTrade().getSide();
            Bar lastExitBar = getLastTrade().getExitBar();

            if (lastSide == side && lastExitBar == getPrimaryChart().getLast()) {
                log(Level.FINE,
                        "Position enter cancelled to avoid chasing.");
                return;
            }
        }

        //Lastly, no error, place the order
        if (currentPosition == 0) {
            placeOrder(createNewEnterOrderRecord(),
                    Side.valueOf(netPosition - currentPosition),
                    Math.abs(netPosition - currentPosition),
                    limit > 0 ? "LMT" : "MKT",
                    limit,
                    0);
        }
    }

    public boolean isClosingPosition() {
        Trade lastTrade = getLastTrade();

        if (lastTrade == null) {
            return false;
        }

        OrderRecord lastExitOrderRecord = lastTrade.getExitOrderRecord();

        if (lastExitOrderRecord == null) {
            return false;
        }

        return lastExitOrderRecord.isWorking()
                //                || (!lastTrade.isProtected()
                //                && lastExitOrderRecord.isCanceled())
                || (lastExitOrderRecord.isMarketOrder()
                && !lastExitOrderRecord.isFullyFilled());
    }

    /**
     *
     */
    public void closePosition() {
        boolean backtesting = isBacktesting();

        if (!backtesting && isClosingPosition()) {
//                && getLastTrade() != null
//                && getLastTrade().getExitOrderRecord() != null) {
//
//            if (getLastTrade().getExitOrderRecord().isWorking()
//                    || getLastTrade().getExitOrderRecord().isSubmitted() //2013.01.16
//                    || (getLastTrade().getExitOrderRecord().isMarketOrder()
//                    && !getLastTrade().getExitOrderRecord().isFullyFilled())) {
            log(Level.FINE, "Another exit Order is in progress.");
            return;
//            }
        }

//        if (!isBacktesting() && isClosingPosition()) {
//            if (currentPosition == 0
//                    && (accountPosition == null
//                    || accountPosition.getPosition() == 0)) {
//                setState(false, State.CLOSING_POSITION);
//
//                return;
//            } else {
//                cancelAllOrders();
//            }
//        }

        Side side;
        final OrderRecord orderRecord;

        if (getCurrentPosition() == 0) {
            //to exit any un-recoreded position
            if (backtesting || isSleeping()) {
                return;
            }

            cancelAllOrders();

            Trade lastTrade = getLastTrade();

            if (lastTrade != null) {
                side = lastTrade.getSide();
            } else {
                return;
            }

            orderRecord = createNewOrderRecord();

        } else {
            cancelExitOrder();

            side = Side.valueOf(currentPosition);
            orderRecord = getLastExitOrderRecord();
        }

        if (!backtesting) {
//            setState(true, State.CLOSING_POSITION);

            //wait exit order to cancell
            if (getLastTrade() != null) {
                final OrderRecord exitOrderRecord = getLastTrade().getExitOrderRecord();

                if (exitOrderRecord != null && !exitOrderRecord.isCanceled()) {
                    Waiting waiting = new Waiting(10000, 500, getLogger()) {

                        @Override
                        public boolean waitWhile() {
                            return exitOrderRecord.isSubmitted() || exitOrderRecord.isWorking();
                        }

                        @Override
                        public void retry() {
                        }

                        @Override
                        public String message() {
                            return "Waiting exit order to work...";
                        }

                        @Override
                        public void timeout() {
                        }
                    };

                    if (waiting.isTimeout()) {
                        log(Level.WARNING, "An exit order is still working.");
                        return;
                    }
                }
            }
        }

        placeOrder(orderRecord,
                side.reverse(),
                0,
                "MKT",
                0,
                0);
    }

    /**
     *
     */
    public void setupStop() {
        boolean backtesting = isBacktesting();

        if (!backtesting && Main.getAccount() != Account.DEMO) {
            if (!firstChecked || !isInitialized()) {// || isStateActive(State.CLOSING_POSITION)
                return;
            }
        }

        if (currentPosition != 0) {
            OrderRecord exitOrderRecord = getLastExitOrderRecord();

            if (!backtesting) {
                if (exitOrderRecord.isSending()) {
                    if (DateUtil.getTimeNow() - exitOrderRecord.getSubmittedTime()
                            >= Main.p_Send_Order_Time_Out) {
                        log(Level.WARNING,
                                "Stop order #{0} sending time out.",
                                exitOrderRecord.getOrderId());

                        cancelOrder(exitOrderRecord);

//                        if (!exitOrderRecord.isCanceled()
//                                && !exitOrderRecord.isNotFound()) {
//                            log(Level.WARNING,
//                                    "Can't cancel pervious stop order #{0}.",
//                                    exitOrderRecord.getOrderId());
//                            return;
//                        }
                    } else {
                        log(Level.FINE,
                                "Stop Order #{0} has just been sent.",
                                exitOrderRecord.getOrderId());
                        return;
                    }
                }

                if (Math.abs(currentPosition) > p_Default_Qty * getGroupSize()) {
                    log(Level.WARNING, "Exceed quantity, closing position.");
                    closePosition();
                    return;
                }
            }

            String orderType;
            Side side = getCurrentSide();
            double auxPrice;
            double stopPrice;
            double limitPrice;

            if (exitOrderRecord.isSubmitted()
                    && getStopPrice() > 0
                    && exitOrderRecord.isStopOrder()) {
                stopPrice = getStopPrice();
            } else {
                stopPrice = getEnterPrice() * (1 - side.sign * p_Max_Stop_Factor);
            }

            double calculatedStopPrice = calculateStopPrice();

            if (Double.isNaN(stopPrice)
                    || (calculatedStopPrice > 0
                    && side.sign * calculatedStopPrice > side.sign * stopPrice)) {
                stopPrice = calculatedStopPrice - side.sign * getMinTick();
            }

            if (Double.isNaN(stopPrice) || stopPrice <= getMinTick()) { //safty net
                orderType = "TRAIL";
                limitPrice = 0;
                auxPrice = p_Max_Stop_Factor * (getLastTickPrice() > 0
                        ? getLastTickPrice()
                        : getEnterPrice());

            } else {
                orderType = "STP LMT";
                limitPrice = stopPrice - side.sign * p_Max_Limit_Ticks * getMinTick();
                auxPrice = stopPrice;
            }

            Side currentSide = Side.valueOf(currentPosition);
            boolean changed;

            if (exitOrderRecord.isSubmitted() || exitOrderRecord.isPartiallyFilled()) {
                changed = changeOrder(exitOrderRecord,
                        currentSide.reverse(),
                        currentPosition,
                        orderType,
                        limitPrice,
                        auxPrice);
            } else {
                cancelOrder(exitOrderRecord);

                placeOrder(exitOrderRecord,
                        currentSide.reverse(),
                        currentPosition,
                        orderType,
                        limitPrice,
                        auxPrice);

                changed = true;
            }

            if (!backtesting && changed) {
                log(Level.FINE, "Stop order: {0}", exitOrderRecord);
            }
        } else {
            cancelExitOrder();
        }
    }

    protected Trade createNewTrade() {
        Trade trade = new Trade(
                isBacktesting() ? getLastTickTime() : DateUtil.getTimeNow());

        trade.setCalculations(getLastCalculations());
        trades.add(trade, false);

        return trade;
    }

    protected OrderRecord createNewEnterOrderRecord() {//boolean needsPattern) {
        OrderRecord orderRecord = createNewOrderRecord();
        createNewTrade().addEnterOrderRecord(orderRecord, false);

        return orderRecord;
    }

    protected OrderRecord getLastExitOrderRecord() {//boolean needsPattern) {//, boolean notify) {
        Trade lastTrade = getLastTrade();

        if (lastTrade == null) {
            lastTrade = createNewTrade();
        }

        OrderRecord exitOrder = lastTrade.getExitOrderRecord();

        if (exitOrder == null
                || (!exitOrder.isSubmitted() && !exitOrder.isWorking())) {
            exitOrder = createNewOrderRecord();
            lastTrade.addExitOrderRecord(exitOrder, false);
        }

        if (lastTrade.getEnterOrderRecord() == null) {
            lastTrade.addEnterOrderRecord(new OrderRecord(), false);
            Execution execution = new Execution();
            execution.m_side = getCurrentSide().toString();
            lastTrade.getEnterOrderRecord().setExecution(execution, false);
        }

//        if (notify) {
//            exitOrder.fireChanged();
//        }

        return exitOrder;
    }

    private OrderRecord createNewOrderRecord() {//boolean needsPattern) {//, boolean notify) {
        OrderRecord orderRecord = new OrderRecord();

        if (primaryChart != null) {
            orderRecord.setBar(primaryChart.getLast(), false);

//            if (needsPattern && !primaryChart.isEmpty()) {
            if (!primaryChart.isEmpty()) {
                List<Bar> descendingItems = new LinkedList<Bar>(primaryChart.getDescendingItems());

                orderRecord.setPreviousBars(descendingItems.subList(1, descendingItems.size()), false);
            }
        }

        orderRecords.add(orderRecord);

//        if (notify) {
//            orderRecord.fireChanged();
//        }

        return orderRecord;
    }

    //
    //Updaters
    //
    /**
     *
     */
    public void updateTradingTable() {
        Object[] array = getRow(0);
        Field[] fields = Field.values();

        for (int i = 0; i < array.length; i++) {
            updateTradingTable(array[i], fields[i]);
        }
    }

    private void updateTradingTable(Object value, Field field) {
        UiUtil.updateTable(
                value,
                getStrategyId(),
                field.column,
                Main.frame.getStrategyTable());

//        if (contract == null) {
//            return;
//        }

        if (field == Field.ENTER || field == Field.STOP || field == Field.EXIT) {
            updateWebSymbols(field.toString(),
                    Double.parseDouble(value.toString()) == 0 ? null : value);
        }
    }

    private void updateTradingTablePosition() {
        updateTradingTable(currentPosition, Field.POS);
        updateTradingTable(getEnterPrice(), Field.ENTER);
        updateTradingTable(getStopPrice(), Field.STOP);
        updateTradingTable(getExitPrice(), Field.EXIT);
    }

    private void updateTradingTableProfit() {
        updateTradingTable(getURPoints(), Field.GROSS);
        updateTradingTable(getBalPoints(), Field.BAL_PTS);
    }

    public void insertWebExecution(Execution execution) {
        if (execution.m_shares < 0) {
            return;
        }

        if (DateUtil.getTime(execution.m_time)
                >= DateUtil.getMidnightTime(tradingHours.getTimeZone())) {
            try {
                Main.webSchema.insertWebExecution(contractId, execution);
            } catch (Exception ex) {
                log(Level.SEVERE, null, ex);
            }
        }
    }

    public boolean isOrphanOrder(OrderRecord orderRecord) {
        if (orderRecord == null) {
            return false;
        }

        if (!orderRecord.isSubmitted() || orderRecord.isFilled()) {
            return false;
        }

        if (getLastTrade() != null
                && (getLastTrade().isEnterOrder(orderRecord)
                || getLastTrade().isExitOrder(orderRecord))) {
            return false;
        }

        log(Level.WARNING, "Orphan order is found: #{0}", orderRecord.getOrderId());
        return true;
    }

    private void logExecution(String string, Execution execution) {
        if (string == null || execution == null) {
            return;
        }

        log(Level.INFO,
                "{0} #{8} {3} QTY={4} @ {5} CUMQTY={6} CURPOS={1} {7} at {2}",
                new Object[]{
                    string,
                    currentPosition,
                    execution.m_time == null ? "" : execution.m_time.substring(10, execution.m_time.length()),
                    execution.m_side,
                    execution.m_shares,
                    execution.m_price,
                    execution.m_cumQty,
                    execution.m_acctNumber,
                    execution.m_orderId});
    }

    //
    //Overriding Object
    //
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof TradingThread)) {
            return false;
        }

        TradingThread theOther = (TradingThread) obj;

        if (this.getClass() != theOther.getClass()) {
            return false;
        }

        if (this.p_Symbol == null && theOther.p_Symbol == null) {
            return true;
        }

        if (this.p_Symbol == null
                || !this.p_Symbol.equalsIgnoreCase(theOther.p_Symbol)
                || !this.p_Security_Type.equalsIgnoreCase(theOther.p_Security_Type)) {
            return false;
        }

        if (this.p_Backtest_Only != theOther.p_Backtest_Only) {
            return false;
        }

        return true;
    }

    //
    //Listeners
    //
    @Override
    public void eventHandler(EventObject event) {
        if (event instanceof MarketDataChangeEvent) {
            marketDataChanged((MarketDataChangeEvent) event);

        } else if (event instanceof BarChartChangeEvent) {
            barChartChanged((BarChartChangeEvent) event);

        } else if (event instanceof PositionEnterEvent) {
            positionEntered((PositionEnterEvent) event);

        } else if (event instanceof PositionExitEvent) {
            positionExited((PositionExitEvent) event);

        } else if (event instanceof TradeHighLowChangeEvent) {
            tradeHighLowChanged((TradeHighLowChangeEvent) event);

        } else if (event instanceof OrderChangeEvent) {
            orderChanged((OrderChangeEvent) event);

        } else if (event instanceof PositionChangeEvent) {
            positionChanged((PositionChangeEvent) event);

        } else if (event instanceof AccountPortfolioChangeEvent) {
            accountPortfolioChanged((AccountPortfolioChangeEvent) event);
        }
    }

    /**
     *
     * @param event
     */
    protected void accountPortfolioChanged(AccountPortfolioChangeEvent event) {
        AccountPosition position = event.getAccountPortfolio().getPosition(
                getTradingContractDetails().m_summary);

        if (!isSleeping() || position.getPosition() != 0) {
            log(Level.INFO, "Portfolio changed: {0}", position);
        }

        updatePortfolioTable(position);
    }

    /**
     *
     * @param event
     */
    protected void marketDataChanged(MarketDataChangeEvent event) {
        if (isBacktesting() || !isUpdatingQuote()) {
            return;
        }

        updateContractsTable(event.getValue(), event.getField().column);

        if (event.getField() == SnapShot.Field.HIGH
                || event.getField() == SnapShot.Field.LOW
                || event.getField() == SnapShot.Field.LAST
                || event.getField() == SnapShot.Field.CHANGE
                || event.getField() == SnapShot.Field.VOLUME) {

            updateWebQuote(event.getField().simpleName(),
                    ((Number) event.getValue()).intValue() > 0
                    || event.getField() == SnapShot.Field.CHANGE
                    ? event.getValue() : null);
        }
    }

    /**
     *
     * @param event
     */
    protected void barChartChanged(BarChartChangeEvent event) {
        if (event.getSource() != primaryChart) {
            return;
        }

        if (event.isNewBarAdded()) {
            log(Level.FINE, "New bar added. Day low/high: [{0}, {1}]",
                    new Object[]{
                        getPrimaryChart().getLow(),
                        getPrimaryChart().getHigh()});

            newBarAdded();
        }

        if (event.isLastPriceChanged() || event.isNewBarAdded()) {
            log(Level.FINER, "Last price changed: {0}", getLastTickPrice());

            lastPriceChanged();
        }
    }

    protected void newBarAdded() {
        Session session = tradingHours.getCurrentSession(getLastTickTime());

        if (session != null && !session.equals(getCurrentSession())) {
            if (primaryChart.isEmpty()) {
                prevSessionIndex = -1;
                prevSessionGap = 0;

            } else if (primaryChart.size() > 1
                    && !session.equals(
                    tradingHours.getCurrentSession(
                    primaryChart.getPrev(primaryChart.getLast()).getTime()))) {

                prevSessionIndex = primaryChart.size() - 2;
                prevSessionGap = Math.abs(
                        primaryChart.getLast().getOpen()
                        / primaryChart.getPrev(primaryChart.getLast()).getClose() - 1);
            }

            log(Level.FINE,
                    "[prevSessionIndex, prevSessionGap] = [{0}, {1}]",
                    new Object[]{prevSessionIndex, prevSessionGap});

            setCurrentSession(session);
        }

        update();
        setupStop();
    }

    protected void lastPriceChanged() {
        if (getLastTrade() != null) {
            getLastTrade().setPrice(primaryChart.getLastTick());
        }

        enterPosition();

        if (!isBacktesting() && getCurrentPosition() != 0) {
            updateTradingTableProfit();
        }
    }

    /**
     *
     * @param event
     */
    protected void positionEntered(PositionEnterEvent event) {
        boolean backtesting = isBacktesting();

        if (!backtesting && !isInitialized()) {
            return;
        }

        log(Level.INFO, "Position entered @ {0}. Last price = {1}",
                new Object[]{
                    DateUtil.getSimpleTimeStamp(event.getEnterOrderRecord().getExecutedTime()),
                    getLastTickPrice()});

        if (!backtesting) {
            setState(true, State.PENDING_REQUEST_EXECUTIONS);

            updateTradingTablePosition();
            updateTradingTableProfit();

            OrderRecord orderRecord = event.getEnterOrderRecord();

            logExecution("Entered", orderRecord.getExecution());
        }

        setupStop();
    }

    /**
     *
     * @param event
     */
    protected void positionExited(PositionExitEvent event) {
        boolean backtesting = isBacktesting();

        if (!backtesting && !isInitialized()) {
            return;
        }

        log(Level.INFO, "Position exited @ {0}. Last price = {1}",
                new Object[]{
                    DateUtil.getSimpleTimeStamp(event.getExitOrderRecord().getExecutedTime()),
                    getLastTickPrice()});

        OrderRecord orderRecord = event.getExitOrderRecord();

        if (!backtesting) {
            updateTradingTablePosition();
            updateTradingTableProfit();

            logExecution("Exited", orderRecord.getExecution());

        }

        if (orderRecord.isFullyFilled()) {
//            setState(false, State.CLOSING_POSITION);
            setState(true, State.PENDING_REQUEST_EXECUTIONS);
        }
    }

    private void orderChanged(OrderChangeEvent event) {
        boolean backtesting = isBacktesting();

        if (!backtesting) {
            if (!isInitialized()) {
                return;
            }

//            if (event.getTrade().isExitOrder(event.getOrderRecord())
//                    && event.getOrderRecord().isCanceledWithError()) {
//                setState(false, State.CLOSING_POSITION);
//            }

            updateTradingTablePosition();
            updateTradingTableProfit();

            if (isExiting()) {
                if (event.getTrade().isCeased()
                        || event.getTrade().isCompleted()
                        || event.getTrade().isProtected()) {
                    exited();
                }
            }
        }

        OrderRecord orderRecord = event.getOrderRecord();
//        String s;
//
//        if (orderRecord.isCanceled()) {
//            s = "cancelled";
//        } else {
//            s = "changed";
//        }

        if (orderRecord != null && !orderRecord.isEmpty()) {
            log(Level.FINE, "Order changed: {0}",
                    //                    new Object[]{
                    orderRecord.toString());
//                        orderRecord.getStatus()});
        }
    }

    /**
     *
     * @param event
     */
    protected void tradeHighLowChanged(TradeHighLowChangeEvent event) {
        if (!isBacktesting() && !isInitialized()) {
            return;
        }

        log(Level.INFO, "Trade low/high changed: [{0}, {1}]",
                new Object[]{
                    event.getLow(),
                    event.getHigh()});

        if (getLastTrade().hasStopOrder()) {
            setupStop();
        }
    }

    /**
     *
     * @param event
     */
    protected void positionChanged(PositionChangeEvent event) {
        AccountPosition position = event.getPosition();

        if (isSleeping() && position.getPosition() == 0 && getCurrentPosition() == 0) {
            return;
        }

        if (getTradingContract().equals(position.getContract())) {
            log(Level.INFO, "Account position changed: {0}", position);

            setAccountPosition(position);
            updatePortfolioTable(position);

        } else {
            log(Level.SEVERE, "Unexpected contract: {0}",
                    event.getPosition().getContract().m_localSymbol);
        }
    }

    /**
     *
     * @param position
     */
    protected void updatePortfolioTable(AccountPosition position) {
        updatePortfolioTable(position.getPosition(), 3);
        updatePortfolioTable(position.getUnrealizedPNL(), 5);
        updatePortfolioTable(position.getDayPNL(), 6);
    }

    //
    //Display frames
    //
    /**
     * Display the exiting parameters frame if found or else create a new one.
     */
    public void displayParametersFrame() {
        if (variablesFrame == null || !variablesFrame.isDisplayable()) {
            variablesFrame = new ParametersFrame(Main.frame.getStrategyTable(), this);
        }

        variablesFrame.setVisible(true);
    }

    @Override
    public BarChartFrame getChartFrame(BarDataset chartData) {
        return super.getChartFrame(chartData);
    }
}
