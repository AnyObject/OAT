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

import OAT.trading.TradingHours;
import OAT.trading.Trade;
import OAT.trading.OrderRecord;
import OAT.trading.Main;
import OAT.trading.Side;
import com.ib.client.ContractDetails;
import com.ib.client.Execution;
import java.util.logging.Level;
import OAT.data.BarDataset;
import OAT.data.TickDataset;
import OAT.event.BarChartChangeEvent;
import OAT.event.State;
import OAT.ui.BarChartFrame;
import OAT.util.DateUtil;
import OAT.util.TextUtil;

/**
 *
 * @author Antonio Yip
 */
public class BacktestStrategy extends HybridStrategy {

//    private Backtest backtest;
    public int dayId, strategyId;
    public long threadEndTime;
    public double commission;
    public boolean isDead;

    @Override
    public void run() {
        threadStartTime = DateUtil.getTimeNow();
        addTickDataset(getTicksDataset());
        finish();
    }

    public void runBacktest(TickDataset ticksData) {
        setTicksDataset(new TickDataset(ticksData.getLast()));
        threadStartTime = DateUtil.getTimeNow();
        addTickDataset(ticksData);
        finish();
    }

    /**
     * Initialise a backtest based on this strategy.
     *
     * @param dayId
     * @param strategyId
     * @param contractDetails
     * @param tradingHours
     * @param commission
     */
    public void initBacktest(int dayId, int strategyId, ContractDetails contractDetails, TradingHours tradingHours, double commission) {
        setState(true, State.BACKTESTING);

        this.dayId = dayId;
        this.strategyId = strategyId;
        this.commission = commission;

        this.contractDetails = contractDetails;
        this.tradingContractDetails = contractDetails;

        if (contractDetails != null) {
            this.contract = contractDetails.m_summary;
            this.tradingContract = contractDetails.m_summary;
        }

        this.tradingHours = tradingHours;

        initPrimaryChart();
        primaryChart.setBacktest(true);

        addChangeListener(Main.backtestThread);
    }

    public long getDuration() {
        return threadEndTime - threadStartTime;
    }

    @Override
    public boolean isLoggable(Level level) {
        if (Main.backtestThread.getLogLevel().intValue() > Level.FINE.intValue()) {
            return level.intValue() >= Level.SEVERE.intValue();
        } else {
            return super.isLoggable(level);
        }
    }

    @Override
    public BarChartFrame getChartFrame(BarDataset chartData) {
        chartData.setSubTitle(" " + DateUtil.getTimeStamp(
                chartData.getOpenTime(), DateUtil.DATE_FORMAT)
                + " [" + strategyId + "]");

        return super.getChartFrame(chartData);
    }

    @Override
    public void closePosition() {
        super.closePosition();

        if (getLastTick() == getTicksDataset().getLast()) {
            executeOrder(getLastTrade().getExitOrderRecord());
        }
    }

    @Override
    protected void barChartChanged(BarChartChangeEvent event) {
        super.barChartChanged(event);

        // backtest market closing
        if (getCurrentPosition() != 0) {
            if (isMarketClosing(getLastTickTime())
                    || getLastTick() == getTicksDataset().getLast()) {
                closePosition();
                getLastTrade().setExitOnClose(true);
            }
        }
    }

    @Override
    protected void lastPriceChanged() {
        if (getLastTrade() != null) {
            if (getCurrentPosition() != 0) {
                executeOrder(getLastTrade().getExitOrderRecord());
            }

            executeOrder(getLastTrade().getEnterOrderRecord());
        }

        super.lastPriceChanged();
    }

    private void finish() {
        setTicksDataset(null);

        if (isDead) {
            return;
        }

        try {
            Object[][] params = getParametersArray();
            Object[] paramValues = new Object[params.length];

            for (int i = 0; i < params.length; i++) {
                Object o = params[i][1];
                paramValues[i] = o;
            }

            Main.backtestSchema.insertBacktestStrategy(
                    strategyId,
                    TextUtil.toString(paramValues),
                    contractDetails,
                    commission);

            int i = 0;
            for (Trade trade : getTrades()) {
                if (trade != null && trade.getEnterOrderRecord().isFilled()) {
                    Main.backtestSchema.insertBacktestTrade(strategyId, dayId, i, trade);
//                        modelSchema.insertTrade(trade);
                }

                i++;
            }

        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }

        threadEndTime = DateUtil.getTimeNow();

        setState(true, State.BACKTEST_FINISHED);
        
//        removeChangeListener(Main.backtestThread);
//        primaryChart.removeChangeListener(this);
//        snapShot.removeChangeListener(this);
//        
//        for (Trade trade : getTrades()) {
//            for (OrderRecord orderRecord : trade.getEnterOrderRecords()) {
//                orderRecord.removeChangeListener(trade);
//            }
//            
//            for (OrderRecord orderRecord : trade.getExitOrderRecords()) {
//                orderRecord.removeChangeListener(trade);
//            }
//            
//            trade.removeChangeListener(this);
//        }
    }

    private void executeOrder(OrderRecord orderRecord) {
        if (!orderRecord.isSubmitted()) {
            return;
        }

        Side side = orderRecord.getSide();
        String orderType = orderRecord.getOrder().m_orderType;
        long lastTickTime = getLastTickTime();
        double lastPrice = getLastTickPrice();

        if (orderType.contains("STP")
                && side.sign * lastPrice < side.sign * orderRecord.getOrder().m_auxPrice) {
            return;
        }

        if (orderType.contains("LMT")
                && side.sign * lastPrice > side.sign * orderRecord.getOrder().m_lmtPrice) {
            return;
        }

        orderRecord.setCommission(commission);

        Execution execution = new Execution(
                orderRecord.getOrderId(),
                getThreadId(),
                "" + orderRecord.getOrderId(),
                "" + lastTickTime,
                "",
                contract.m_exchange,
                side.executionSimpleName,
                p_Default_Qty, lastPrice, 0, 0, p_Default_Qty, lastPrice);

        if (addExecution(execution)) {
            orderRecord.setBar(primaryChart.getLast(), false);
            orderRecord.setExecution(execution);
        }
    }
}
