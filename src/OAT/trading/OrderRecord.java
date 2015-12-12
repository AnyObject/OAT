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

import com.ib.client.*;
import java.util.List;
import OAT.data.Bar;
import OAT.event.Listenable;
import OAT.event.NewExecutionEvent;
import OAT.util.DateUtil;
import OAT.util.MathUtil;

/*
 *
 * @author Antonio Yip
 */
public class OrderRecord extends Listenable {

    //from IB
    private Order order;
    private OrderState orderState;
    private Execution execution;
    private ContractDetails contractDetails;
    private int filled, remaining;
    private double avgFillPrice, lastFillPrice;
    private double commission;
    private String status = "";
    private String whyHeld = "";
    //
    private Bar bar;
    private List<Bar> previousBars;
    private boolean enterOrder;
    private long submittedTime; //time

    public OrderRecord() {
    }

    public OrderRecord(Order order, ContractDetails contractDetails) {
        this.order = order;
        this.contractDetails = contractDetails;
    }

    public void newOrder(Order order, ContractDetails contractDetails, long submittedTime, String status) {
        this.order = order;
        this.contractDetails = contractDetails;
        this.submittedTime = submittedTime;
        this.status = status;

//        fireChanged();
    }

//    public void clear(boolean notify) {
//        order = new Order();
//        orderState = null;
//        execution = null;
//        filled = 0;
//        remaining = 0;
//        avgFillPrice = 0;
//        lastFillPrice = 0;
//        status = "";
//        whyHeld = "";
//        submitted = 0;
//        enterOrder = false;
//        contractDetails = null;
//        bar = null;
//        pattern = null;
//
//        if (notify) {
//            fireChanged();
//        }
//    }
    public boolean isFullyFilled() {
        return execution != null
                && execution.m_cumQty > 0 && getRemaining() == 0;
//        return (execution != null && order != null
//                && execution.m_cumQty == order.m_totalQuantity);
//                || (filled > 0 && remaining == 0);
    }

    public boolean isPartiallyFilled() {
        return execution != null
                && execution.m_cumQty > 0 && getRemaining() > 0;
//        return (execution != null && order != null
//                && execution.m_cumQty < order.m_totalQuantity);
//                || (filled > 0 && remaining > 0);
    }

    public boolean isEmpty() {
        return order == null;
    }

//    public boolean isEnterOrder() {
//        return enterOrder;
//    }

    public boolean isFilled() {
        return isFullyFilled() || isPartiallyFilled();
    }

    public boolean isCanceled() {
        return status.toLowerCase().contains("cancel");
    }

    public boolean isCanceledWithError() {
        return isCanceled() && status.toLowerCase().contains("error");
    }

    public boolean isSubmitted() {
        return status.toLowerCase().contains("submit");
    }

    public boolean isNotFound() {
        return status.toLowerCase().contains("notfound");
    }

    public boolean isSending() {
        return status.toLowerCase().contains("sending");
    }

    public boolean isError() {
        return status.toLowerCase().contains("error");
    }

    public boolean isLimitOrder() {
        return order != null && "LMT".equalsIgnoreCase(order.m_orderType);
    }

    public boolean isStopOrder() {
        return order != null
                && order.m_orderType != null
                && order.m_orderType.contains("STP");
    }

    public boolean isMarketOrder() {
        return order != null && "MKT".equalsIgnoreCase(order.m_orderType);
    }

    public boolean isTrailOrder() {
        return order != null && "TRAIL".equalsIgnoreCase(order.m_orderType);
    }

    /**
     * Test if the order is either being sent or partially filled.
     *
     * @return true or false
     */
    public boolean isWorking() {
        return isSending() || isPartiallyFilled();
    }

    public double getAvgFillPrice() {
        if (execution != null) {
            return execution.m_avgPrice;
        }

        return Double.NaN;
    }

    public ContractDetails getContractDetails() {
        return contractDetails;
    }

    public Contract getContract() {
        return contractDetails.m_summary;
    }

    public Execution getExecution() {
        return execution;
    }

    public int getFilled() {
        if (execution != null) {
            return execution.m_cumQty;
        }

        return 0;
    }

    public Order getOrder() {
        return order;
    }

    public String getOrderType() {
        if (order == null || order.m_orderType == null) {
            return "";
        }

        return order.m_orderType;
    }

    public double getOrderPrice() {
        if (order != null && order.m_orderType != null) {
            if (order.m_orderType.contains("STP")) {
                return order.m_auxPrice;
            } else if (order.m_orderType.contains("LMT")) {
                return order.m_lmtPrice;
            }
        }

        return 0;
    }

    public int getOrderId() {
        if (order == null) {
            return -1;
        }

        return order.m_orderId;
    }

    public OrderState getOrderState() {
        return orderState;
    }

    public int getRemaining() {
        if (order != null) {
            if (order.m_totalQuantity == 0
                    && order.m_orderType.equalsIgnoreCase("MKT")) {
                return remaining;
            }

            return order.m_totalQuantity - getFilled();
        }

        return 0;
    }

    public Bar getBar() {
        return bar;
    }

    public List<Bar> getPreviousBars() {
        return previousBars;
    }

    public Side getSide() {
        if (execution != null) {
            return Side.valueOfAction(execution.m_side);
        }

        if (order != null) {
            return Side.valueOfAction(order.m_action);
        }

        return Side.NEUTRAL;
    }

    public long getExecutedTime() {
        if (execution == null || execution.m_time == null) {
            return 0;
        }

        return DateUtil.getTime(execution.m_time);
    }

    public String getStatus() {
        return status;
    }

    public String getWhyHeld() {
        return whyHeld;
    }

    public long getSubmittedTime() {
        return submittedTime;
    }

    /**
     * Test if the order is just sent in the past interval defined by {@link Main.p_Submitted_Order_Wait}.
     *
     * @return true or false
     */
    public boolean isJustSubmitted() {
        return isSubmitted()
                && DateUtil.getTimeNow() - getSubmittedTime() <= Main.p_Submitted_Order_Wait;
    }

    public double getCommission() {
        if (orderState != null && MathUtil.isValid(orderState.m_commission)) {
            return orderState.m_commission;
        }

        return commission;
    }

    public void setCommission(double commission) {
        this.commission = commission;
    }

    public void setContractDetails(ContractDetails contractDetails, boolean notify) {
        this.contractDetails = contractDetails;

        if (notify) {
            fireChanged();
        }
    }

    public void setSubmittedTime(long submitted) {
        this.submittedTime = submitted;
    }

    public void setEnterOrder(boolean enterOrder) {
        this.enterOrder = enterOrder;
    }

    public void setExecution(Execution execution, boolean notify) {
        this.execution = execution;

        setStatus("Filled", false);

        if (notify) {
            fireNewExecution();
        }
    }

    public void setExecution(Execution execution) {
        setExecution(execution, true);
    }

    public void setOpenOrder(Order order, OrderState orderState, boolean notify) {
        if (order != null) {
            this.order = order;
        }

        setOrderState(orderState, false);

        if (notify) {
            fireChanged();
        }
    }

    public void setOpenOrder(Order order, OrderState orderState) {
        setOpenOrder(order, orderState, true);
    }

    public void setOrder(Order order, boolean notify) {
        this.order = order;

        if (notify) {
            fireChanged();
        }
    }

    public void setOrderState(OrderState orderState, boolean notify) {
        if (orderState == null) {
            return;
        }

        if (this.orderState == null) {
            this.orderState = orderState;

        } else {
            if (MathUtil.isValid(orderState.m_commission)) {
                this.orderState.m_commission = orderState.m_commission;
            }

            if (MathUtil.isValid(orderState.m_maxCommission)) {
                this.orderState.m_maxCommission = orderState.m_maxCommission;
            }

            if (MathUtil.isValid(orderState.m_minCommission)) {
                this.orderState.m_minCommission = orderState.m_minCommission;
            }

            if (orderState.m_commissionCurrency != null) {
                this.orderState.m_commissionCurrency = orderState.m_commissionCurrency;
            }

            if (orderState.m_equityWithLoan != null) {
                this.orderState.m_equityWithLoan = orderState.m_equityWithLoan;
            }

            if (orderState.m_initMargin != null) {
                this.orderState.m_initMargin = orderState.m_initMargin;
            }

            if (orderState.m_maintMargin != null) {
                this.orderState.m_maintMargin = orderState.m_maintMargin;
            }

            if (orderState.m_status != null) {
                this.orderState.m_status = orderState.m_status;
            }

            if (orderState.m_warningText != null) {
                this.orderState.m_warningText = orderState.m_warningText;
            }
        }

        if (notify) {
            fireChanged();
        }
    }

    public void setOrderStatus(String status, int filled, int remaining, double avgFillPrice, double lastFillPrice, String whyHeld, boolean notify) {
        this.status = status;
        this.filled = filled;
        this.remaining = remaining;
        this.avgFillPrice = avgFillPrice;
        this.lastFillPrice = lastFillPrice;
        this.whyHeld = whyHeld;

        if (notify) {
            fireChanged();
        }
    }

    public void setOrderStatus(int orderId, String status, int filled, int remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {
        setOrderStatus(status, filled, remaining, avgFillPrice, lastFillPrice, whyHeld, true);
    }

    public void setStatus(String status) {
        setStatus(status, true);
    }

    public void setStatus(String status, boolean notify) {
        if (status != null && !status.equalsIgnoreCase(this.status)) {
            this.status = status;

            if (notify) {
                fireChanged();
            }
        }
    }

    public void setWhyHeld(String whyHeld, boolean notify) {
        if (!whyHeld.equals(this.whyHeld)) {
            this.whyHeld = whyHeld;

            if (notify) {
                fireChanged();
            }
        }
    }

    public void setBar(Bar bar, boolean notify) {
        this.bar = bar;

        if (notify) {
            fireChanged();
        }
    }

    public void setPreviousBars(List<Bar> pattern, boolean notify) {
        this.previousBars = pattern;

        if (notify) {
            fireChanged();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof OrderRecord)) {
            return false;
        }

        OrderRecord theOther = (OrderRecord) obj;

        if (this.getClass() != theOther.getClass()) {
            return false;
        }

        if (this.contractDetails == null) {
            if (theOther.contractDetails != null) {
                return false;
            }
        } else if (!this.contractDetails.equals(theOther.contractDetails)) {
            return false;
        }

        if (this.order == null) {
            if (theOther.order != null) {
                return false;
            }
        } else if (!this.order.equals(theOther.order)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        if (order == null) {
            return "empty order";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("#").append(order.m_orderId).append(" ").
                append(order.m_action).append(" ").
                append(contractDetails.m_summary.m_localSymbol).append(" ").
                append("QTY=").append(order.m_totalQuantity).append(" @ ").
                append(order.m_lmtPrice > 0 ? order.m_lmtPrice + " " : "").
                append(order.m_orderType).append(" ").
                append(order.m_auxPrice > 0 ? order.m_auxPrice + " " : "").
                append(order.m_faGroup).append(" ").
                append(order.m_faMethod).append(" ").
                append(order.m_faPercentage != null ? order.m_faPercentage : "").append(" | ").append(getStatus());

        if (execution != null) {
            sb.append(" | ").append(execution.m_side).append(" ").
                    append("CUMQTY=").append(execution.m_cumQty).append(" ").
                    append("AVG=").append(execution.m_avgPrice);
        }

        return sb.toString();
    }

    protected void fireNewExecution() {
        notifyListeners(new NewExecutionEvent(this));
    }
}
