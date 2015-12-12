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

import OAT.event.PositionExitEvent;
import OAT.event.PositionEnterEvent;
import OAT.event.OrderChangeEvent;
import OAT.event.Listenable;
import OAT.event.NewExecutionEvent;
import OAT.event.TradeHighLowChangeEvent;
import OAT.event.GenericListener;
import com.ib.client.ContractDetails;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import OAT.data.Bar;
import OAT.data.Chartable;
import OAT.data.Price;
import OAT.data.Tick;
import OAT.trading.classification.Prediction;
import OAT.util.DateUtil;
import OAT.util.GeneralUtil;
import OAT.util.MathUtil;
import OAT.util.TextUtil;

/**
 *
 * @author Antonio Yip
 */
public class Trade extends Listenable implements Chartable<Trade>, GenericListener {

//    @Deprecated
//    private OrderRecord enterOrderRecord;
//    private OrderRecord exitOrderRecord;
    private Stack<OrderRecord> enterOrderRecords = new Stack<OrderRecord>();
    private Stack<OrderRecord> exitOrderRecords = new Stack<OrderRecord>();
    private Price high;
    private Price low;
    private Price last;
    private long createdTime;
    private boolean emailed;
    private boolean forced;
    private boolean exitOnClose;
    private transient Map<Calculator, Side> calculations;
//    private transient StrategyPlugin stopper;

    /**
     * Create a trade using the specified created time.
     *
     * @param createdTime milliseconds
     */
    public Trade(long createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public double getX() {
        double exitTime = getX(1);

        return !Double.isNaN(exitTime) ? exitTime : getX(0);
    }

    @Override
    public double getX(int series) {
        long x = 0;
        Bar bar;

        switch (series) {
            case 0:
                bar = getEnterBar();
                x = bar != null ? bar.getTime() : getEnterTime();
                break;

            case 1:
                bar = getExitBar();
                x = bar != null ? bar.getTime() : getExitTime();
                break;

            default:
        }

        if (x > 0) {
            return x;
        }

        return Double.NaN;
    }

    @Override
    public double getY(int series) {
        double y = Double.NaN;

        switch (series) {
            case 0:
                y = getEnterPrice();
                break;

            case 1:
                y = getExitPrice();
                break;

            default:
        }

        if (y > 0) {
            return y;
        }

        return Double.NaN;
    }

    public Stack<OrderRecord> getEnterOrderRecords() {
        return enterOrderRecords;
    }

    public Stack<OrderRecord> getExitOrderRecords() {
        return exitOrderRecords;
    }

    /**
     *
     * @param orderRecord
     * @return
     */
    public boolean isEnterOrder(OrderRecord orderRecord) {
        return enterOrderRecords.contains(orderRecord);
    }

    /**
     *
     * @param orderRecord
     * @return
     */
    public boolean isExitOrder(OrderRecord orderRecord) {
        return exitOrderRecords.contains(orderRecord);
    }

    /**
     *
     * @return
     */
    public OrderRecord getEnterOrderRecord() {
//        return enterOrderRecord;
        if (enterOrderRecords.isEmpty()) {
            return null;
        }

        return enterOrderRecords.peek();
    }

    /**
     *
     * @return
     */
    public OrderRecord getExitOrderRecord() {
//        return exitOrderRecord;
        if (exitOrderRecords.isEmpty()) {
            return null;
        }

        return exitOrderRecords.peek();
    }

    /**
     *
     * @return
     */
    public Map<Calculator, Side> getCalculations() {
        return calculations;
    }

    /**
     *
     * @param calculations
     */
    public void setCalculations(Map<Calculator, Side> calculations) {
        this.calculations = calculations;
    }

    /**
     *
     * @param trend
     * @return
     */
    public boolean isTrend(Trend trend) {
        for (Map.Entry<Calculator, Side> entry : calculations.entrySet()) {
            Calculator calculator = entry.getKey();
            Side side = entry.getValue();

            if (calculator.getTrend() == trend && getSide() == side) {
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @param orderRecord
     * @param notify
     */
    public void addEnterOrderRecord(OrderRecord orderRecord, boolean notify) {
//        this.enterOrderRecord = orderRecord;
        if (!enterOrderRecords.contains(orderRecord)) {
            enterOrderRecords.add(orderRecord);
            orderRecord.addChangeListener(this);

            if (notify) {
                fireOrderChanged(orderRecord);
            }
        }
    }

    /**
     *
     * @param orderRecord
     * @param notify
     */
    public void addExitOrderRecord(OrderRecord orderRecord, boolean notify) {
//        this.exitOrderRecord = orderRecord;
        if (!exitOrderRecords.contains(orderRecord)) {
            exitOrderRecords.add(orderRecord);
            orderRecord.addChangeListener(this);

            if (notify) {
                fireOrderChanged(orderRecord);
            }
        }
    }

    public int getNetPosition() {
        int position = 0;

        for (OrderRecord enterOrderRecord : enterOrderRecords) {
            if (enterOrderRecord.isFilled()) {
                position += enterOrderRecord.getSide().sign * enterOrderRecord.getFilled();
            }
        }

        for (OrderRecord exitOrderRecord : exitOrderRecords) {
            if (exitOrderRecord.isFilled()) {
                position += exitOrderRecord.getSide().sign * exitOrderRecord.getFilled();
            }
        }

        return position;
    }

    /**
     *
     * @return
     */
    public boolean isOpen() {
//        if (enterOrderRecord != null && !enterOrderRecord.isCanceled() && enterOrderRecord.isFilled()) {
//            if (exitOrderRecord == null
//                    || enterOrderRecord.getFilled() != exitOrderRecord.getFilled()) {
//                return true;
//            }
//        }
//
//        return false;

        return getNetPosition() != 0;
    }

    /**
     *
     * @return
     */
    public boolean isProtected() {
//        if (enterOrderRecord != null
//                && exitOrderRecord != null
//                && (exitOrderRecord.isSubmitted() || exitOrderRecord.isSending())) {
//
//            if (0 == exitOrderRecord.getSide().sign
//                    * (exitOrderRecord.getRemaining() + exitOrderRecord.getFilled())
//                    + enterOrderRecord.getSide().sign * enterOrderRecord.getFilled()) {
//                return true;
//            }
//        }
//
//        return false;


        return getNetPosition() + getCovered() == 0;
    }
    
    public int getCovered() {
        int covered = 0;

        for (OrderRecord exitOrderRecord : exitOrderRecords) {
            if (exitOrderRecord.isSubmitted() || exitOrderRecord.isWorking()) {
                covered += exitOrderRecord.getSide().sign
                        * (exitOrderRecord.getRemaining() + exitOrderRecord.getFilled());
            } 
        }

        return covered;
    }

    /**
     *
     * @return
     */
    public boolean hasStopOrder() {
//        if (exitOrderRecord != null && exitOrderRecord.isSubmitted()) {
//            return true;
//        }

        for (OrderRecord exitOrderRecord : exitOrderRecords) {
            if (exitOrderRecord.isSubmitted()) {
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @return
     */
    public boolean isCompleted() {
//        if (enterOrderRecord == null) {
//            if (exitOrderRecord == null) {
//                return false;
//            }
//
//            if (exitOrderRecord.isFullyFilled()) {
//                return true;
//            }
//
//        } else {
//            if (exitOrderRecord == null) {
//                return false;
//            }
//
//            if (enterOrderRecord.isSubmitted() || exitOrderRecord.isSubmitted()) {
//                return false;
//            }
//
//            if (enterOrderRecord.isWorking() || exitOrderRecord.isWorking()) {
//                return false;
//            }
//
//            if (0 == exitOrderRecord.getSide().sign * exitOrderRecord.getFilled()
//                    + enterOrderRecord.getSide().sign * enterOrderRecord.getFilled()) {
//                return true;
//            }
//        }
        if (enterOrderRecords.isEmpty()) {
            if (exitOrderRecords.isEmpty()) {
                return false;
            }

            for (OrderRecord exitOrderRecord : exitOrderRecords) {
                if (!exitOrderRecord.isFullyFilled()) {
                    return false;
                }
            }

            return true;
        }

        for (OrderRecord enterOrderRecord : enterOrderRecords) {
            if (enterOrderRecord.isSubmitted() || enterOrderRecord.isWorking()) {
                return false;
            }
        }

        for (OrderRecord exitOrderRecord : exitOrderRecords) {
            if (exitOrderRecord.isSubmitted() || exitOrderRecord.isWorking()) {
                return false;
            }
        }

        if (getNetPosition() == 0) {
            return true;
        }

        return false;
    }

    /**
     *
     * @return
     */
    public boolean isCeased() {
//        if (enterOrderRecord == null) {
//            return true;
//        }
//
//        if (enterOrderRecord.isCanceled()) {
//            return true;
//        }

//        return false;

        if (enterOrderRecords.isEmpty()) {
            return true;
        }

        boolean allCanceled = true;
        for (OrderRecord enterOrderRecord : enterOrderRecords) {
            allCanceled &= enterOrderRecord.isCanceled();
        }

        return allCanceled;

    }

    /**
     * Test if the order is either being sent or partially filled.
     *
     * @return true or false
     */
    public boolean isWorking() {
//        if (enterOrderRecord != null && enterOrderRecord.isWorking()) {
//            return true;
//        }
//
//        if (exitOrderRecord != null && exitOrderRecord.isWorking()) {
//            return true;
//        }
        for (OrderRecord enterOrderRecord : enterOrderRecords) {
            if (enterOrderRecord.isWorking()) {
                return true;
            }
        }

        for (OrderRecord exitOrderRecord : exitOrderRecords) {
            if (exitOrderRecord.isWorking()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Test if the order is just sent in the past interval defined by {@link Main.p_Submitted_Order_Wait}.
     *
     * @return true or false
     */
    public boolean isJustSubmitted() {
//        if (enterOrderRecord != null && enterOrderRecord.isJustSubmitted()) {
//            return true;
//        }
//
//        if (exitOrderRecord != null && exitOrderRecord.isJustSubmitted()) {
//            return true;
//        }
        for (OrderRecord enterOrderRecord : enterOrderRecords) {
            if (enterOrderRecord.isJustSubmitted()) {
                return true;
            }
        }

        for (OrderRecord exitOrderRecord : exitOrderRecords) {
            if (exitOrderRecord.isJustSubmitted()) {
                return true;
            }
        }


        return false;
    }

    /**
     *
     * @return
     */
    public boolean isEmailed() {
        return emailed;
    }

    /**
     *
     * @return
     */
    public boolean isForced() {
        return forced;
    }

    /**
     *
     * @return
     */
    public boolean isExitOnClose() {
        return exitOnClose;
    }

    /**
     *
     * @return
     */
    public ContractDetails getContractDetails() {
//        if (enterOrderRecord != null) {
//            return enterOrderRecord.getContractDetails();
//        }
//
//        if (exitOrderRecord != null) {
//            return exitOrderRecord.getContractDetails();
//        }

        if (!enterOrderRecords.isEmpty()) {
            return enterOrderRecords.peek().getContractDetails();
        }

        if (!exitOrderRecords.isEmpty()) {
            return exitOrderRecords.peek().getContractDetails();
        }

        return null;
    }

    /**
     *
     * @return
     */
    public long getCreatedTime() {
        return createdTime;
    }

    /**
     *
     * @return
     */
    public int getEnterQty() {
//        if (enterOrderRecord != null) {
//            return enterOrderRecord.getFilled();
//        }
//        
//        return 0;

        int qty = 0;

        for (OrderRecord enterOrderRecord : enterOrderRecords) {
            qty += enterOrderRecord.getFilled();
        }

        return qty;
    }

    /**
     *
     * @return
     */
    public double getEnterPrice() {
//        if (enterOrderRecord != null) {
//            return enterOrderRecord.getAvgFillPrice();
//        }

//        return Double.NaN;

        int qty = getEnterQty();

        if (qty == 0) {
            return Double.NaN;
        }

        double value = 0;

        for (OrderRecord enterOrderRecord : enterOrderRecords) {
            if (enterOrderRecord.isFilled()) {
                value += enterOrderRecord.getAvgFillPrice() * enterOrderRecord.getFilled();
            }
        }

        return value / qty;
    }

    /**
     *
     * @return
     */
    public long getEnterTime() {
//        if (enterOrderRecord != null) {
//            return enterOrderRecord.getExecutedTime();
//        }
//
//        return 0;

        long time = 0;

        for (OrderRecord orderRecord : enterOrderRecords) {
            if (orderRecord.getExecutedTime() > time) {
                time = orderRecord.getExecutedTime();
            }
        }

        return time;
    }

    /**
     *
     * @return
     */
    public Bar getEnterBar() {
        if (getEnterOrderRecord() == null) {
            return null;
        }

        if (getEnterOrderRecord().getBar() == null) {
            return new Bar(getEnterOrderRecord().getExecutedTime());
        }

        return getEnterOrderRecord().getBar();
    }

    /**
     *
     * @return
     */
    public List<Bar> getEnterPreviousBars() {
        if (getEnterOrderRecord() == null) {
            return null;
        }

        return getEnterOrderRecord().getPreviousBars();
    }

    /**
     *
     * @return
     */
    public int getExitQty() {
//        if (exitOrderRecord != null) {
//            return exitOrderRecord.getFilled();
//        }
//
//        return 0;

        int qty = 0;

        for (OrderRecord exitOrderRecord : exitOrderRecords) {
            qty += exitOrderRecord.getFilled();
        }

        return qty;
    }

    /**
     *
     * @return
     */
    public double getExitPrice() {
//        if (exitOrderRecord != null) {
//            return exitOrderRecord.getAvgFillPrice();
//        }
//
//        return Double.NaN;

        int qty = getExitQty();

        if (qty == 0) {
            return Double.NaN;
        }

        double value = 0;

        for (OrderRecord exitOrderRecord : exitOrderRecords) {
            if (exitOrderRecord.isFilled()) {
                value += exitOrderRecord.getAvgFillPrice() * exitOrderRecord.getFilled();
            }
        }

        return value / qty;
    }

    /**
     *
     * @return
     */
    public long getExitTime() {
//        if (exitOrderRecord != null) {
//            return exitOrderRecord.getExecutedTime();
//        }
//
//        return 0;

        long time = 0;

        for (OrderRecord orderRecord : exitOrderRecords) {
            if (orderRecord.getExecutedTime() > time) {
                time = orderRecord.getExecutedTime();
            }
        }

        return time;
    }

    /**
     *
     * @return
     */
    public Bar getExitBar() {
        if (getExitOrderRecord() == null) {
            return null;
        }

        if (getExitOrderRecord().getBar() == null) {
            return new Bar(getExitOrderRecord().getExecutedTime());
        }

        return getExitOrderRecord().getBar();
    }

    /**
     *
     * @return
     */
    public List<Bar> getExitPreviousBars() {
        if (getExitOrderRecord() == null) {
            return null;
        }

        return getExitOrderRecord().getPreviousBars();
    }

    /**
     *
     * @return
     */
    public double getStopPrice() {
        if (getExitOrderRecord() == null || !getExitOrderRecord().isSubmitted()) {
            return 0;
        }

        if (getExitOrderRecord().isTrailOrder()) {
            double auxPrice = getExitOrderRecord().getOrder().m_auxPrice;
            if (getSide() == Side.LONG) {
                return getHigh() - auxPrice;
            } else if (getSide() == Side.SHORT) {
                return getLow() + auxPrice;
            }

        } else if (getExitOrderRecord().isStopOrder()) {
            return getExitOrderRecord().getOrder().m_auxPrice;

        } else if (getExitOrderRecord().isLimitOrder()) {
            return getExitOrderRecord().getOrder().m_lmtPrice;
        }

        return 0;
    }

    /**
     *
     * @return
     */
    public Side getSide() {
        if (getEnterOrderRecord() != null) {
            return getEnterOrderRecord().getSide();

        } else if (getExitOrderRecord() != null) {
            return getExitOrderRecord().getSide().reverse();
        }

        return Side.NEUTRAL;
    }

    /**
     *
     * @return
     */
    public double getProfit() {
        double enter = getEnterPrice();
        double exit = getExitPrice();

        return ((Double.isNaN(exit) ? getLast() : exit) - enter) * getSide().sign;
    }

    /**
     *
     * @return
     */
    public double getNetAmount() {
        return GeneralUtil.getContractValue(getContractDetails(), getProfit())
                - getCommission();
    }

    /**
     *
     * @return
     */
    public long getDuration() {
        if (getExitTime() == 0) {
            return 0;
        }

        return getExitTime() - getEnterTime();
    }

    /**
     *
     * @return
     */
    public double getHigh() {
        if (high == null) {
            return Double.NaN;
        }

        return high.getPrice();
    }

    /**
     *
     * @return
     */
    public double getLow() {
        if (low == null) {
            return Double.NaN;
        }

        return low.getPrice();
    }

    /**
     *
     * @return
     */
    public double getLast() {
        if (last == null) {
            return Double.NaN;
        }

        return last.getPrice();
    }

    /**
     *
     * @return
     */
    public long getHighTime() {
        if (high == null) {
            return 0;
        }

        return high.getTime();
    }

    /**
     *
     * @return
     */
    public long getLowTime() {
        if (low == null) {
            return 0;
        }

        return low.getTime();
    }

    /**
     *
     * @return
     */
    public double getLastTime() {
        if (last == null) {
            return 0;
        }

        return last.getTime();
    }

    /**
     *
     * @return
     */
    public double getCommission() {
        double commission = 0;

        for (OrderRecord enterOrderRecord : enterOrderRecords) {
            commission += enterOrderRecord.getCommission();// / enterOrderRecord.getFilled();
        }

        for (OrderRecord exitOrderRecord : exitOrderRecords) {
            commission += exitOrderRecord.getCommission();// / exitOrderRecord.getFilled();
        }

        return commission;
    }

    /**
     *
     * @return
     */
    public int getMaxId() {
        int max = 0;

        for (OrderRecord enterOrderRecord : enterOrderRecords) {
            if (enterOrderRecord.getOrderId() > max) {
                max = enterOrderRecord.getOrderId();
            }
        }


        for (OrderRecord exitOrderRecord : exitOrderRecords) {
            if (exitOrderRecord.getOrderId() > max) {
                max = exitOrderRecord.getOrderId();
            }
        }

        return max;

//        return Math.max(
//                enterOrderRecord == null ? 0 : enterOrderRecord.getOrderId(),
//                exitOrderRecord == null ? 0 : exitOrderRecord.getOrderId());
    }

    /**
     *
     * @param size
     * @param predictionMode
     * @return
     */
    public Object[] getEnterBarsPattern(int size, boolean predictionMode) {
        if (getEnterOrderRecord() == null) { //|| bars > enterOrderRecord.getPreviousBars().size()) {
            return null;
        }

        List<Bar> pattern = getEnterOrderRecord().getPreviousBars();

        double output;

        if (predictionMode) {
            output = getProfit();
        } else {
            output = getPrediction() == Prediction.LOSS ? 0 : 1;
        }

        Object[] vector = MathUtil.getBarsPattern(pattern, getSide(), size, output);

        return vector;
    }

    /**
     *
     * @return
     */
    public Prediction getPrediction() {
        if (!isCompleted()) {
            return Prediction.NOT_APPLICABLE;
        }

        if (getProfit() / getEnterPrice() > Main.p_Prediction_Win_Margin) {
            return Prediction.WIN;
        } else {
            return Prediction.LOSS;
        }
    }

    /**
     *
     * @param price
     */
    public void setPrice(Price price) {
        setPrice(price, true);
    }

    /**
     *
     * @param price
     * @param notify
     */
    public void setPrice(Price price, boolean notify) {
        if (price != null && isOpen()) {
            if (last == null || price.getPrice() != last.getPrice()) {
                last = price;
                fireChanged();
            }

            if (high == null || price.getPrice() > high.getPrice()) {
                high = price;
                fireHighLowChanged();

            } else if (low == null || price.getPrice() < low.getPrice()) {
                low = price;
                fireHighLowChanged();
            }
        }
    }

    /**
     *
     * @param emailed
     */
    public void setEmailed(boolean emailed) {
        this.emailed = emailed;
    }

    /**
     *
     * @param forced
     */
    public void setForced(boolean forced) {
        this.forced = forced;
    }

    /**
     *
     * @param exitOnClose
     */
    public void setExitOnClose(boolean exitOnClose) {
        this.exitOnClose = exitOnClose;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getSide().executionName).append(" ").append(getEnterPrice()).
                append(" @ ").append(DateUtil.getTimeStamp(getEnterTime(), DateUtil.TRADE_DATETIME_FORMAT)).
                append(TextUtil.LINE_SEPARATOR).
                append("EXITED ").append(getExitPrice()).append(" @ ").
                append(DateUtil.getTimeStamp(getExitTime(), DateUtil.TRADE_DATETIME_FORMAT)).
                append(TextUtil.LINE_SEPARATOR).
                append("PTS = ").append(TextUtil.PRICE_CHANGE_FORMATTER.format(getProfit()));

        if (getContractDetails() != null && getContractDetails().m_summary != null) {
            sb.append(" P/L = ").append(getContractDetails().m_summary.m_currency).
                    append(" ").append(TextUtil.CURRENCY_FORMATTER.format(getNetAmount()));
        }

        sb.append(TextUtil.LINE_SEPARATOR);
        sb.append("CREATED ").append(DateUtil.getTimeStamp(getCreatedTime(), DateUtil.TRADE_DATETIME_FORMAT));
        sb.append(TextUtil.LINE_SEPARATOR);

        return sb.toString().toUpperCase();
    }

    /**
     *
     * @param t
     * @return
     */
    @Override
    public int compareTo(Trade t) {
        return (int) (getCreatedTime() - t.getCreatedTime());
    }

    /**
     *
     * @param event
     */
    @Override
    public void eventHandler(EventObject event) {
        if (event instanceof NewExecutionEvent) {
            NewExecutionEvent e = (NewExecutionEvent) event;

//            if (e.getOrderRecord() == enterOrderRecord) {
            if (enterOrderRecords.contains(e.getOrderRecord())) {
                fireEntered();

            } else if (exitOrderRecords.contains(e.getOrderRecord())) {
                fireExited();
            }

        } else if (event.getSource() instanceof OrderRecord) {
            fireOrderChanged((OrderRecord) event.getSource());
        }
    }

    /**
     *
     */
    protected void fireEntered() {
        setPrice(new Tick(getEnterPrice(), getEnterTime()), false);
        notifyListeners(new PositionEnterEvent(this));
    }

    /**
     *
     */
    protected void fireExited() {
        setPrice(new Tick(getExitPrice(), getExitTime()), false);
        notifyListeners(new PositionExitEvent(this));
    }

    /**
     *
     * @param orderRecord
     */
    protected void fireOrderChanged(OrderRecord orderRecord) {
        notifyListeners(new OrderChangeEvent(this, orderRecord));
    }

    /**
     *
     */
    protected void fireHighLowChanged() {
        notifyListeners(new TradeHighLowChangeEvent(this));
    }
}
