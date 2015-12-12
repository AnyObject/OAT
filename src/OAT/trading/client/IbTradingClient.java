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

import OAT.trading.Trade;
import OAT.trading.OrderRecord;
import OAT.trading.Account;
import OAT.trading.Main;
import OAT.trading.Side;
import com.ib.client.Contract;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import OAT.event.State;
import OAT.trading.thread.BaseThread;
import OAT.trading.thread.TradingThread;
import OAT.util.DateUtil;
import OAT.util.MathUtil;

/**
 *
 * @author Antonio Yip
 */
public class IbTradingClient extends IbClient implements TradingClient {

    private int nextValidId;
    private Map<Integer, TradingThread> reqIdtradingThreadMap = new HashMap<Integer, TradingThread>();
    private Map<Integer, TradingThread> orderIdtradingThreadMap = new HashMap<Integer, TradingThread>();
    private Map<Integer, OrderRecord> orderRecordMap = new ConcurrentHashMap<Integer, OrderRecord>();
    private List<Order> openOrders = new ArrayList<Order>();
    private boolean requestingOpenOrders;
    private boolean assignedNewOrderIdNotUsed;

    public IbTradingClient(BaseThread baseThread) {
        super(baseThread);
    }

    @Override
    public synchronized boolean isInterruptible() {
        return !assignedNewOrderIdNotUsed;
    }

    @Override
    public int getClientId() {
        return 30;
    }

    @Override
    public synchronized int getNextOrderId(TradingThread tradingThread) {
        assignedNewOrderIdNotUsed = true;
        int id = nextValidId++;

        tradingThread.log(Level.FINE, "Next valid order ID: {0}", id);

        return id;
    }

    //
    //Requests
    //
    /**
     * Request executions.
     *
     * @param tradingThread
     */
    @Override
    public void reqExecutions(TradingThread tradingThread) {
        if (!eClientSocket.isConnected()) {
            return;
        }

        if (tradingThread.isRequestingExecutions()) {
            return;
        }

        int reqId = REQ_EXECUTIONS_I + tradingThread.getThreadId();
        reqIdtradingThreadMap.put(reqId, tradingThread);

        tradingThread.setState(false, State.PENDING_REQUEST_EXECUTIONS);
        tradingThread.setState(true, State.REQUESTING_EXECUTIONS);

        long sinceTime;

        if (Main.getAccount() == Account.DEMO) {
            sinceTime = DateUtil.getTimeNow() - DateUtil.HOUR_TIME;
        } else {
            sinceTime = tradingThread.getTradingHours().getLastOpenTime();
        }

        eClientSocket.reqExecutions(
                reqId,
                createFilter(
                0,
                null,
                DateUtil.getTimeStamp(sinceTime, "yyyyMMdd  HH:mm:ss"),
                tradingThread.getSymbol(),
                tradingThread.getSecurityType(),
                null,
                null));
    }

    @Override
    public void reqOpenOrders(TradingThread tradingThread) {
        if (!eClientSocket.isConnected()) {
            return;
        }

        if (requestingOpenOrders) {
            return;
        }

        requestingOpenOrders = true;

        eClientSocket.reqOpenOrders();
    }

    /**
     * Place new order.
     *
     * @param tradingThread
     * @param orderRecord
     * @param side
     * @param qty
     * @param orderType
     * @param lmtPrice
     * @param auxPrice
     */
    @Override
    public synchronized void placeOrder(TradingThread tradingThread, OrderRecord orderRecord, Side side, int qty, String orderType, double lmtPrice, double auxPrice) {
        if (orderRecord == null
                || side == Side.NEUTRAL
                || tradingThread.isSleeping()) {
            return;
        }

        String faMethod = "";
        String faPercentage = "";

        if (Main.isAdvisorAccount()) {
            if (tradingThread.getCurrentPosition() == 0) {
                faMethod = "EqualQuantity";

            } else if (Side.valueOf(tradingThread.getCurrentPosition()) != side
                    && orderType.equals("MKT")) {
                faMethod = "PctChange";
                faPercentage = "-100";

            } else {
                faMethod = "EqualQuantity";
            }
        }
         
        int orderId = getNextOrderId(tradingThread);

        orderRecordMap.put(orderId, orderRecord);

        String faGroup = Main.getAccount() == Account.DEMO ? "All" : tradingThread.getAccountGroup();
        int quantity;

        if (faMethod != null && faMethod.equalsIgnoreCase("PctChange")) {
            quantity = 0;
        } else {
            quantity = Math.abs(qty);
        }

        Order newOrder = createOrder(
                getClientId(),
                orderId,
                side.actionName,
                quantity,
                orderType,
                MathUtil.roundStep(lmtPrice, side, tradingThread.getMinTick()),
                MathUtil.roundStep(auxPrice, side, tradingThread.getMinTick()),
                tradingThread.getTriggerMethod(),
                faGroup,
                faMethod,
                faPercentage);

        orderRecord.newOrder(
                newOrder,
                tradingThread.getTradingContractDetails(),
                tradingThread.isBacktesting() ? 
                tradingThread.getLastTickTime() : DateUtil.getTimeNow(),
                "Sending");

        orderIdtradingThreadMap.put(orderId, tradingThread);

        tradingThread.log(Level.INFO, "Placing Order: {0}", orderRecord);

        if (eClientSocket != null) {
            eClientSocket.placeOrder(orderId, orderRecord.getContract(), newOrder);
        }
        
        assignedNewOrderIdNotUsed = false;
    }

    @Override
    public boolean changeOrder(TradingThread tradingThread, OrderRecord orderRecord, Side side, int qty, String orderType, double lmtPrice, double auxPrice) {
        if (orderRecordMap.get(orderRecord.getOrderId()) == null
                || tradingThread.isSleeping()) {
            return false;
        }

        if (!orderType.equals(orderRecord.getOrder().m_orderType)) {
            tradingThread.log(Level.WARNING,
                    "ChangeOrder: type is different, canceling the existing one.");
            tradingThread.cancelOrder(orderRecord);

            if (!orderRecord.isFullyFilled() && !orderRecord.isPartiallyFilled()) {
                placeOrder(tradingThread, orderRecord, side, qty, orderType, lmtPrice, auxPrice);
                return true;

            } else {
                tradingThread.log(Level.WARNING,
                        "Order #{0} has been filled.",
                        orderRecord.getOrderId());
                cancelOrder(tradingThread, qty);
                return false;
            }
        }

        Order revisedOrder = createOrder(
                getClientId(),
                orderRecord.getOrderId(),
                side.actionName,
                Math.abs(qty),
                orderType,
                MathUtil.roundStep(lmtPrice, orderRecord.getSide(), tradingThread.getMinTick()),
                MathUtil.roundStep(auxPrice, orderRecord.getSide(), tradingThread.getMinTick()),
                tradingThread.getTriggerMethod(),
                orderRecord.getOrder().m_faGroup,
                orderRecord.getOrder().m_faMethod,
                orderRecord.getOrder().m_faPercentage);

        if (orderRecord.getOrder().m_action.equals(revisedOrder.m_action)
                && orderRecord.getOrder().m_totalQuantity == revisedOrder.m_totalQuantity
                && (orderRecord.getOrder().m_lmtPrice == revisedOrder.m_lmtPrice
                || revisedOrder.m_lmtPrice <= 0)
                && orderRecord.getOrder().m_auxPrice == revisedOrder.m_auxPrice) {
            return false;
        }

        tradingThread.log(Level.FINE,
                "Original order: {0} \n\tRevised order to be sent: {1}",
                //                new OrderRecord(revisedOrder));
                new Object[]{
                    orderRecord,
                    new OrderRecord(revisedOrder,
                    orderRecord.getContractDetails())
                });

        orderRecord.setOrder(revisedOrder, true);

        if (eClientSocket != null) {
            eClientSocket.placeOrder(orderRecord.getOrderId(), orderRecord.getContract(), revisedOrder);
        }

        tradingThread.log(Level.INFO, "Change Order: {0}", orderRecord);

        return true;
    }

    /**
     *
     * @param tradingThread
     * @param orderId
     */
    @Override
    public void cancelOrder(TradingThread tradingThread, int orderId) {
        if (orderId >= 0) {
            tradingThread.log(Level.INFO, "Canceling Order: #{0}", orderId);

            if (eClientSocket != null) {
                eClientSocket.cancelOrder(orderId);
            }
        }
    }

    //
    //com.ib.client.Ewrapper
    //
    @Override
    public void error(final int id, final int errorCode, final String errorMsg) {
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
                if (errorCode == 322) { // Duplicate Id
                    logError(Level.WARNING, id, errorCode, errorMsg);

                } else if (errorCode == 201) { //order rejected
                    logError(Level.WARNING, id, errorCode, errorMsg);

                    TradingThread tradingThread = orderIdtradingThreadMap.get(id);
                    OrderRecord orderRecord = orderRecordMap.get(id);

                    if (tradingThread != null) {
//                        tradingThread.log(Level.WARNING,
//                                "Order #{0} was rejected: {1}",
//                                new Object[]{id, errorMsg});

                        if (errorMsg.contains("MUST EXCEED")
                                && errorMsg.contains("THE MARGIN REQ")) {
//                            tradingThread.log(Level.WARNING, "Not enough margin.");
                            tradingThread.setState(true, State.TRADING_SUSPENDED);
                        }

//                        if (tradingThread.getLastTrade().isExitOrder(orderRecord)) {
//                            tradingThread.setState(false, State.CLOSING_POSITION);
//                        }
                    }

                    if (orderRecord != null) {
                        orderRecord.setStatus("Cancelled - " + errorMsg);
                    }

                } else if (errorCode == 202) { //order cancelled
                    logError(Level.INFO, id, errorCode, errorMsg);

                    TradingThread tradingThread = orderIdtradingThreadMap.get(id);
                    OrderRecord orderRecord = orderRecordMap.get(id);

                    if (orderRecord != null) {
                        //2013.01.16
//                        if (tradingThread.getLastTrade().isExitOrder(orderRecord)) {
//                            tradingThread.setState(false, State.CLOSING_POSITION);
//                        }

                        orderRecord.setStatus("Cancelled");

                        if (orderRecord.getFilled() != 0) {
                            tradingThread.log(Level.WARNING,
                                    "Order #{0} was partially filled. Closing position",
                                    id);

                            tradingThread.closePosition();

//                        } else {
//                            tradingThread.log(Level.INFO,
//                                    "Order #{0} was cancelled.",
//                                    id);
                        }
                    }

//        } else if (errorCode == 200) {
//            logError(Level.WARNING, id, errorCode, errorMsg);
//
//            TradingThread tradingThread = reqIdtradingThreadMap.get(id);
//            tradingThread.log(Level.WARNING, errorMsg);


                } else if (errorCode == 103) {
                    logError(Level.WARNING, id, errorCode, errorMsg);

                    TradingThread tradingThread = orderIdtradingThreadMap.get(id);

                    if (tradingThread != null) {
                        tradingThread.log(Level.WARNING,
                                "Duplicated order #{0}.", id);
                    }

                    orderRecordMap.get(id).setStatus("Cancelled with error - " + errorMsg);

                } else if (errorCode == 135) {
                    logError(Level.WARNING, id, errorCode, errorMsg);

//                    TradingThread tradingThread = orderIdtradingThreadMap.get(id);

                    OrderRecord orderRecord = orderRecordMap.get(id);

                    if (orderRecord != null) {
                        if (!orderRecord.isFilled()) {
                            orderRecord.setStatus("NotFound");
                        }
                    } else {
                        baseThread.log(Level.WARNING,
                                "Order #{0} was not found.", id);
                    }

                } else if (errorCode == 434 || errorCode == 321) {
                    //order getItemCount cannot be zero.
                    logError(Level.FINE, id, errorCode, errorMsg);

                    TradingThread tradingThread = orderIdtradingThreadMap.get(id);
                    OrderRecord orderRecord = orderRecordMap.get(id);

                    if (orderRecord != null) {
                        orderRecord.setStatus("Cancelled with error - " + errorMsg);
                    }

                    if (tradingThread != null) {
//                        tradingThread.setState(false, State.CLOSING_POSITION);

                        if (tradingThread.getCurrentPosition() != 0) {
                            tradingThread.setCurrentPosition(0);
                        }

                        if (tradingThread.getAccountPosition() != null
                                && tradingThread.getAccountPosition().getPosition() != 0) {
                            tradingThread.log(Level.INFO, "Position was cleared.");
                            tradingThread.resetAccountPosition();
                        }
//
//                        if (tradingThread.getCurrentPosition() == 0) {
//                            tradingThread.log(Level.INFO, "Position cleared.");
//                            tradingThread.setState(false, State.CLOSING_POSITION);
//                            tradingThread.resetAccountPosition();
//
//                        } else if (tradingThread.getAccountPosition() == null
//                                || tradingThread.getAccountPosition().getPosition() == 0) {
//                            tradingThread.setCurrentPosition(0);
//                            tradingThread.setState(false, State.CLOSING_POSITION);
//                        }
                    }

                } else if (errorCode == 110 || (errorCode > 434 && errorCode <= 436)) {
                    logError(Level.WARNING, id, errorCode, errorMsg);

                    TradingThread tradingThread = orderIdtradingThreadMap.get(id);

                    String orderStatus = "Error " + errorCode;
                    cancelOrder(tradingThread, id);

                    try {
                        Main.tradingSchema.updateOrderStatus(getClientId(), id, orderStatus);
                    } catch (Exception ex) {
                        log(Level.SEVERE, null, ex);
                    }

                    OrderRecord orderRecord = orderRecordMap.get(id);

                    if (orderRecord != null) {
                        orderRecord.setStatus(orderStatus, false);
                        orderRecord.setWhyHeld(errorMsg, true);
                    }

//        } else if (errorCode > 0 && errorMsg != null
//                && ((errorCode >= 320 && errorCode <= 323)
//                || errorMsg.toLowerCase().contains("error")
//                || errorMsg.toLowerCase().contains("failed")
//                || (!errorMsg.toLowerCase().contains("duplicate") && errorCode != 165))) {
//            logError(Level.WARNING, id, errorCode, errorMsg);
//
//            requestError(id, false);

                } else {
                    TradingThread tradingThread = reqIdtradingThreadMap.get(id);
                    int typeId = getReqTypeId(id);

                    switch (typeId) {
                        case REQ_EXECUTIONS_I:
                            logError(Level.WARNING, id, errorCode, errorMsg);
                            tradingThread.log(Level.WARNING, errorMsg);

                            tradingThread.setState(false, State.REQUESTING_EXECUTIONS);

//                    if (retry) {
//                        tradingThread.setState(true, State.PENDING_REQUEST_EXECUTIONS);
//                    }
                            return;

                        case REQ_PORTFOLIO_I:
                            logError(Level.WARNING, id, errorCode, errorMsg);
                            tradingThread.log(Level.WARNING, errorMsg);

                            return;

                        default:
                            unhandledError(id, errorCode, errorMsg);
                    }
                }
//            }
//        }).start();
    }

    @Override
    public void nextValidId(int orderId) {
        if (orderId > nextValidId) {
            nextValidId = orderId;
        }

        super.nextValidId(orderId);
    }

    @Override
    public void execDetails(final int reqId, final Contract contract, final Execution execution) {
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
                try {
                    TradingThread tradingThread = Main.getStrategy(contract);

                    if (tradingThread != null) {
//                        && contract.m_symbol.equals(tradingThread.getTradingSymbol())
//                        && contract.m_secType.equals(tradingThread.getSecurityType())) {

                        OrderRecord orderRecord;

                        if (tradingThread.addExecution(execution)) {
                            orderRecord = orderRecordMap.get(execution.m_orderId);

                            if (orderRecord != null) {
                                orderRecord.setBar(tradingThread.getPrimaryChart().getLast(), false);
                                orderRecord.setExecution(execution);
                            }
                        }

                        Main.tradingSchema.insertExecution(contract, execution);

                        tradingThread.insertWebExecution(execution);
                    }
                } catch (Exception e) {
                    log(Level.SEVERE, null, e);
                }
//            }
//        }).start();

        super.execDetails(reqId, contract, execution);
    }

    @Override
    public void execDetailsEnd(final int reqId) {
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
                try {
                    TradingThread tradingThread = reqIdtradingThreadMap.get(reqId);

                    if (tradingThread != null) {
                        tradingThread.log(Level.FINE, "Executions downloaded.");
                        tradingThread.setState(false, State.REQUESTING_EXECUTIONS);
                    }
                } catch (Exception e) {
                    log(Level.SEVERE, null, e);
                }
//            }
//        }).start();

        super.execDetailsEnd(reqId);
    }

    @Override
    public void openOrder(final int orderId, final Contract contract, final Order order, final OrderState orderState) {
        try {
            openOrders.add(order);
        } catch (Exception e) {
            log(Level.SEVERE, null, e);
        }

//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
                try {
                    TradingThread tradingThread = Main.getStrategy(contract);

//                    if (tradingThread == null) {
//                        log(Level.WARNING, "Unknown contract: " + contract.m_localSymbol);
//                    } else {

                    OrderRecord orderRecord = orderRecordMap.get(order.m_orderId);
                    boolean enterOrder = false;

                    if (orderRecord != null) {
                        orderRecord.setOpenOrder(order, orderState);


//                    for (ListIterator<Trade> listIterator = tradingThread.getTrades().iterator(); listIterator.hasNext();) {
//                        Trade trade = listIterator.next();
                        for (Trade trade : tradingThread.getTrades()) {
                            if (trade.isEnterOrder(orderRecord)) {
                                enterOrder = true;
                                break;
                            }
                        }

                        if (tradingThread.isOrphanOrder(orderRecord)) {
                            tradingThread.cancelOrder(orderRecord);
                        }
                    }

                    Main.tradingSchema.insertOrder(contract, order, orderState, enterOrder);
                    Main.webSchema.updateWebCommission(tradingThread.getContractId(), order, orderState);
//                    }

                } catch (Exception ex) {
                    log(Level.SEVERE, null, ex);
                }
//            }
//        }).start();

        super.openOrder(orderId, contract, order, orderState);
    }

    @Override
    public void openOrderEnd() {
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
                try {
                    log(Level.FINE, "Open Orders downloaded.");

                    log(Level.FINE, "Checking for unknown orders.");
                    for (OrderRecord orderRecord : orderRecordMap.values()) {
                        if (isUnknownOrder(orderRecord)) {
                            eClientSocket.cancelOrder(orderRecord.getOrderId());
//                        orderRecord.setStatus("NotFound");
                        }
                    }

                    openOrders.clear();
                    requestingOpenOrders = false;

                } catch (Exception e) {
                    log(Level.SEVERE, null, e);
                }
//            }
//        }).start();

        super.openOrderEnd();
    }

    @Override
    public void orderStatus(final int orderId, final String status, final int filled, final int remaining, final double avgFillPrice, final int permId, final int parentId, final double lastFillPrice, final int clientId, final String whyHeld) {
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
                try {
                    OrderRecord orderRecord = orderRecordMap.get(orderId);

                    if (orderRecord == null) {
                        if (status.toLowerCase().contains("submit")) {
                            eClientSocket.cancelOrder(orderId); //cancel any unknown order
                        }
                    } else {
                        orderRecord.setOrderStatus(orderId, status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld);
                    }

                    Main.tradingSchema.updateOrderStatus(orderId, status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld);

                } catch (Exception ex) {
                    log(Level.SEVERE, null, ex);
                }
//            }
//        }).start();

        super.orderStatus(orderId, status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld);
    }

    private boolean isUnknownOrder(OrderRecord orderRecord) {
        if (orderRecord == null
                || !orderRecord.isSubmitted()
                || orderRecord.isWorking()) {
            return false;
        }

        int orderId = orderRecord.getOrderId();

        for (Order order : openOrders) {
            if (orderId == order.m_orderId) {
                return false;
            }
        }

        log(Level.WARNING, "Unknown order is found: #{0}", orderId);
        return true;
    }
}
