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

import OAT.data.SnapShot;
import OAT.data.TickDataset;
import OAT.data.ChartDataset;
import OAT.data.TimeChart;
import OAT.data.ChartType;
import OAT.data.Tick;
import OAT.data.BarDataset;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EWrapperMsgGenerator;
import com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException;
import java.util.*;
import java.util.logging.Level;
import OAT.event.GenericListener;
import OAT.event.State;
import OAT.trading.Account;
import OAT.trading.Main;
import OAT.trading.TradingHours;
import OAT.trading.client.DataClient;
import OAT.ui.BarChartFrame;
import OAT.ui.util.UiUtil;
import OAT.util.DateUtil;

/**
 * Abstract class for market data handling.
 *
 * @author Antonio Yip
 */
public abstract class DataThread extends BaseThread implements GenericListener {

    //Parameters
    protected String p_Symbol = "";
    protected String p_Security_Type = "";
    protected String p_Exchange = "";
    protected String p_Currency = "";
    protected String p_Trading_Symbol = "";
    protected ChartType p_Chart_Type = ChartType.CONTRACT;
    protected int p_Bar_Size = 1000;
    protected int p_Expiry_Every_n_Months = 3;
    protected int p_Last_Day_Before_Expiry = 8;
    protected boolean p_Ignore_RT_Volume = false;
    //
    protected BarDataset primaryChart;
//    protected BarDataset realTimeChart;
    protected SnapShot snapShot = new SnapShot(this);
    protected List<BarDataset> charts = Collections.synchronizedList(new ArrayList());
    protected Set<BarChartFrame> chartFrames = Collections.synchronizedSet(new HashSet());
    protected Map<Integer, BarDataset> histDataReq = Collections.synchronizedMap(new HashMap());
    protected TradingHours tradingHours = new TradingHours();
    protected Contract contract;
    protected ContractDetails contractDetails;
    protected Contract secondContract;
    protected ContractDetails secondContractDetails;
    protected Contract tradingContract;
    protected ContractDetails tradingContractDetails;
    protected int reloadedConId;
    protected int contractId;
    protected long lastDataTime;
    private TickDataset ticksDataset;
    //
    private boolean updatingQuote; //, realTimeVolume;
    private DataClient dataClient;

    protected void initPrimaryChart() {
        if (primaryChart != null) {
            return;
        }

        primaryChart = BarDataset.newChart(this);
        charts.add(primaryChart);
    }

    public abstract void postContractDetails();

    @Override
    protected void preExit() {
        cancelRealTimeBars();
        cancelMktData();
    }

    @Override
    public void preSleep() {
        cancelRealTimeBars();
        cancelMktData();
    }

    //
    //Getters
    //
    public boolean isDataNotSubscribed() {
        return isStateActive(State.DATA_IS_NOT_SUBSCRIBED);
    }

    public boolean isGettingMarketData() {
        return isStateActive(State.GETTING_MARKET_DATA);
    }

    public boolean isForcingNewBar() {
        return isStateActive(State.FORCING_NEW_BAR);
    }

    public boolean isCdError() {
        return isStateActive(State.CD_ERROR);
    }

    public boolean isDataBroken() {
        return isStateActive(State.DATA_BROKEN);
    }

    public DataClient getDataClient() {
        return dataClient;
    }

    public SnapShot getSnapShot() {
        return snapShot;
    }

    @Override
    public String getName() {
        return super.getName() + " - " + getSymbol();
    }

    public boolean isUpdatingQuote() {
        return updatingQuote;
    }

    public Contract getContract() {
        return contract;
    }

    public ContractDetails getContractDetails() {
        return contractDetails;
    }

    public int getContractId() {
        return contractId;
    }

    public ChartType getChartType() {
        return p_Chart_Type;
    }

    public int getBarSize() {
        return p_Bar_Size;
    }

    public String getCurrency() {
        return p_Currency;
    }

    public String getExchange() {
        return p_Exchange;
    }

    public Contract getSecondContract() {
        return secondContract;
    }

    public ContractDetails getSecondContractDetails() {
        return secondContractDetails;
    }

    public String getSecurityType() {
        return p_Security_Type;
    }

    public String getSymbol() {
        return p_Symbol;
    }

    public String getTradingSymbol() {
        return p_Trading_Symbol;
    }

    public Contract getTradingContract() {
        return tradingContract;
    }

    public ContractDetails getTradingContractDetails() {
        return tradingContractDetails;
    }

    public int getExpiryEveryMonths() {
        return p_Expiry_Every_n_Months;
    }

    public int getLastDayBeforeExpiry() {
        return p_Last_Day_Before_Expiry;
    }

    public TradingHours getTradingHours() {
        return tradingHours;
    }

    public boolean isIgnoreRTVolume() {
        return p_Ignore_RT_Volume;
    }

    public BarDataset getPrimaryChart() {
        return primaryChart;
    }

    public List<BarDataset> getCharts() {
        return charts;
    }

    /**
     *
     * @return
     */
    public TickDataset getTicksDataset() {
        if (ticksDataset == null) {
            return new TickDataset();
        }

        return ticksDataset;
    }

    /**
     *
     * @return
     */
    public Tick getLastTick() {
        if (primaryChart == null) {
            return null;
        }

        return primaryChart.getLastTick();
    }

    /**
     *
     * @return
     */
    public double getLastTickPrice() {
        if (getLastTick() == null) {
            return 0;
        }

        return getLastTick().getPrice();
    }

    /**
     *
     * @return
     */
    public long getLastTickTime() {
        if (getLastTick() == null) {
            return 0;
        }

        return getLastTick().getTime();
    }

    //
    //Setters
    //
    public void setDataClient(DataClient dataClient) {
        this.dataClient = dataClient;
    }

    public void setLastDataTime(long lastDataTime) {
        this.lastDataTime = lastDataTime;
    }

    public void setUpdatingQuote(boolean bool) {
        this.updatingQuote = bool;
    }

    public void setContractId(int contractId) {
        this.contractId = contractId;
    }

    public void setSecondContractDetails(ContractDetails contractDetails) throws Exception {
        if (contractDetails == null) {
            return;
        }

        log(Level.INFO,
                "secondContractDetails = {0}",
                contractDetails.m_summary.m_localSymbol);

        this.secondContractDetails = contractDetails;
        this.secondContract = contractDetails.m_summary;

        //SQL
        if (Main.isSavingDataToSql()) {
            Main.dataSchema.insertContract(contractDetails.m_summary);
            Main.dataSchema.insertContractDetails(contractDetails);
        }
    }

    public void setTradingContractDetails(ContractDetails contractDetails) throws Exception {
        this.tradingContractDetails = contractDetails;
        this.tradingContract = contractDetails.m_summary;

        log(Level.INFO,
                "tradingContractDetails = {0}",
                contractDetails.m_summary.m_localSymbol);

        if (!isBacktesting()) {
            //Update portfolio table
            updatePortfolioTable(contractDetails.m_longName, 2);
            updatePortfolioTable(contractDetails.m_summary.m_localSymbol, 0);
            updatePortfolioTable(contractDetails.m_summary.m_exchange, 1);
            updatePortfolioTable(contractDetails.m_summary.m_currency, 4);

            setState(false, State.REQUESTING_TRADING_CONTRACT_DETAILS, State.SERVER_DELAYED);
            if (!isRequestingContractDetails()) {
                postContractDetails();
            }

            //web
            try {
                Main.webSchema.insertWebSymbol(contractId,
                        contractDetails.m_summary.m_localSymbol);
            } catch (Exception ex) {
                log(Level.SEVERE, null, ex);
            }
            updateWebSymbols("m_currency", contractDetails.m_summary.m_currency);
            updateWebSymbols("m_multiplier", contractDetails.m_summary.m_multiplier);
            updateWebSymbols("m_minTick", contractDetails.m_minTick);
            updateWebSymbols("m_priceMagnifier", contractDetails.m_priceMagnifier);
            updateWebQuote("Last", null);

            //SQL
            if (Main.isSavingDataToSql() && !contractDetails.equals(this.contractDetails)) {
                Main.dataSchema.insertContract(contractDetails.m_summary);
                Main.dataSchema.insertContractDetails(contractDetails);
            }
        }
    }

    public void setContractDetails(ContractDetails contractDetails) {
        try {
            setContractDetails(contractDetails, Main.isSavingDataToSql());
        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }
    }

    public void setContractDetails(ContractDetails contractDetails, boolean saveToSQL) throws Exception {
        boolean backtesting = isBacktesting();

        this.contractDetails = contractDetails;
        this.contract = contractDetails.m_summary;

        log(Level.INFO,
                "contractDetails = {0}",
                contractDetails.m_summary.m_localSymbol);

        if (!backtesting) {
            //Trading hours
            tradingHours.addTradingSessions(contractDetails);
            log(Level.INFO, tradingHours.toString());

            Main.contracts.set(contractId, contract);
            snapShot.setSymbol(contractDetails.m_summary.m_localSymbol);

            //SQL
            if (Main.isSavingDataToSql()) {
                Main.dataSchema.insertContract(contractDetails.m_summary);
                Main.dataSchema.insertContractDetails(contractDetails);
                Main.dataSchema.insertTradingHours(tradingHours);
            }

            setState(false, State.REQUESTING_CONTRACT_DETAILS, State.SERVER_DELAYED);
        }

        if (backtesting
                || getTradingSymbol() == null
                || getTradingSymbol().isEmpty()
                || getTradingSymbol().equalsIgnoreCase(getSymbol())) {
            setTradingContractDetails(contractDetails);

        } else if (!isRequestingTradingContractDetails()) {
            postContractDetails();
        }
    }

    public void setTicksDataset(TickDataset ticksDataset) {
        this.ticksDataset = ticksDataset;
    }

    //
    //Updaters
    //
    protected void updateTimeCharts() {
        for (BarChartFrame barChartFrame : chartFrames) {
            if (barChartFrame.isVisible()) {
                ChartDataset chartData = barChartFrame.getDataset(0);

                if (chartData instanceof TimeChart) {
                    reqHistoricalData((TimeChart) chartData);
                }
            }
        }
    }

    protected void updateContractsTable(Object value, int columnId) {
        UiUtil.updateTable(
                value,
                getContractId(),
                columnId,
                Main.frame.getContractTable());
    }

    protected void updatePortfolioTable(Object value, int columnId) {
        UiUtil.updateTable(
                value,
                getContractId(),
                columnId,
                Main.frame.getPortfolioTable());
    }

    protected void updateWebSymbols(String field, Object value) {
        updateWeb("symbols", field, value);
    }

    protected void updateWebQuote(String field, Object value) {
        updateWeb("quotes", field, value);
    }

    protected void updateWeb(String table, String field, Object value) {
        try {
            Main.webSchema.updateWebTable(table, contractId, field, value);
        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }
    }

    //
    //Charting
    //
    public void addTickDataset(TickDataset ticksData) {
        for (Tick tick : ticksData) {
            addTick(tick);
        }
    }

    public void addTick(Tick tick) {
        if (tick == null) {
            return;
        }

        boolean backtesting = isBacktesting();

        if (!backtesting) {
            if (!tradingHours.isOpen(tick.getTime())) {
                logOnce(Level.WARNING, "Tick is not in current session.");
                return;
            }
        }

        if (isForcingNewBar()) {
            tick.setForcedNew(true);
            setState(false, State.FORCING_NEW_BAR);

            if (!primaryChart.isEmpty()
                    && tradingHours.getCurrentSession(tick.getTime())
                    == tradingHours.getCurrentSession(primaryChart.getLastTick().getTime())) {
                setState(true, State.DATA_BROKEN);
            } else {
                setState(false, State.DATA_BROKEN);
            }
        }

        List<Tick> newTicks = new LinkedList<Tick>();
        long sizeDiff;

        if (primaryChart.getLastTick() == null) {
            sizeDiff = 0;
        } else {
            sizeDiff = tick.getDayVolume()
                    - tick.getSize()
                    - primaryChart.getLastTick().getDayVolume();
        }

        if (sizeDiff < 0) {
            if (tick.getSize() > -sizeDiff) {
                tick.setSize(tick.getSize() + sizeDiff);

                log(Level.FINE, "Tick volume adjusted {0} < {1} + {2}",
                        new Object[]{
                            tick.getDayVolume(),
                            tick.getSize(),
                            primaryChart.getLastTick().getDayVolume()});
            }

        } else if (sizeDiff > 0) {
            //additional tick to fill gap
            Tick fillTick = new Tick(
                    tick.getPrice(),
                    sizeDiff,
                    tick.getTime(),
                    tick.getDayVolume() - tick.getSize(),
                    tick.getDayWap(),
                    tick.isB(),
                    tick.isForcedNew());

            newTicks.add(fillTick);

            tick.setTime(tick.getTime() + 1);
            tick.setForcedNew(false);
        }

        //the original tick
        newTicks.add(tick);

        //add all adjusted ticks
        for (Tick newTick : newTicks) {
            addToCharts(newTick, true);
        }

        if (!backtesting) {
            for (Tick t : newTicks) {
                getTicksDataset().add(t);
            }

            if (Main.isSavingDataToSql()) {
                try {
                    Main.dataSchema.insertTicks(contract, newTicks);

                } catch (MySQLNonTransientConnectionException ex) {
                    log(Level.WARNING, "{0}\n{1}",
                            new Object[]{ex.getSQLState(), ex.getMessage()});

                } catch (Exception ex) {
                    log(Level.SEVERE, null, ex);
                }
            }

            if (isLoggable(Level.FINEST)) {
                log(Level.FINEST,
                        "{0} last={1} size={2} time={3} volume={4} wap={5} b={6}",
                        new Object[]{
                            snapShot.getSymbol(),
                            tick.getPrice(),
                            tick.getSize(),
                            DateUtil.getTimeStamp(tick.getTime()),
                            tick.getDayVolume(),
                            tick.getDayWap(),
                            tick.isB()});
            }
        }
    }

    public void addSecoundCdTick(Tick tick) {
        if (tick == null) {
            return;
        }

        if (Main.isSavingDataToSql()) {
            try {
                Main.dataSchema.insertTick(secondContract, tick);

            } catch (MySQLNonTransientConnectionException ex) {
                log(Level.WARNING, "{0}\n{1}",
                        new Object[]{ex.getSQLState(), ex.getMessage()});

            } catch (Exception ex) {
                log(Level.SEVERE, null, ex);
            }
        }
    }

    private void addToCharts(TickDataset ticksData, boolean notify) {
        for (BarDataset chart : charts) {
            chart.addTicks(ticksData, notify);
        }
    }

    private void addToCharts(Tick tick, boolean notify) {
        for (BarDataset chart : charts) {
            chart.addTick(tick, notify);
        }
    }

//    public void displayChart(ChartType chartType) {
//        if (chartType == p_Chart_Type) {
//            getPrimaryChartFrame().setVisible(true);
//        } else {
//            getChartFrame(BarDataset.newChart(contractDetails, chartType)).setVisible(true);
//        }
//    }
    public void displayHistoricalChart() {
        getChartFrame(BarDataset.newChart(contractDetails, ChartType.TIME)).setVisible(true);
    }

    public void displayPrimaryChart() {
        getPrimaryChartFrame().setVisible(true);
    }

    public BarChartFrame getPrimaryChartFrame() {
        return getChartFrame(primaryChart);
    }

    public BarChartFrame getChartFrame(final BarDataset chartData) {
        BarChartFrame chartFrame = Main.getChartFrame(chartData);

        if (chartFrame != null) {
            if (chartData != primaryChart) {
                charts.add(chartData);

                chartFrame.addComponentListener(new java.awt.event.ComponentAdapter() {

                    @Override
                    public void componentHidden(java.awt.event.ComponentEvent evt) {
                        charts.remove(chartData);
                    }
                });
            }

            if (chartData != primaryChart && chartData instanceof TimeChart) {
                long duration = Main.getAccount() == Account.DEMO
                        ? DateUtil.HOUR_TIME * 4
                        : DateUtil.getTimeNow() - DateUtil.getMidnight(
                        tradingHours.getTimeZone()).getTimeInMillis();

                reqHistoricalData(chartData, duration);
            }

            chartFrames.add(chartFrame);
        }

        return chartFrame;
    }

    //
    //SQL Reloader
    //
    protected void reloadConId() throws Exception {
        reloadedConId = Main.dataSchema.getConId(p_Symbol, p_Last_Day_Before_Expiry);
    }

    protected void reloadContractDetails() throws Exception {
        log(Level.INFO, "Reloading contractDetails for {0} from SQL...", p_Symbol);

        setContractDetails(Main.dataSchema.getContractDetails(
                Main.dataSchema.getLocalSymbol(p_Symbol, p_Exchange, p_Last_Day_Before_Expiry)), false);

        if (contractDetails != null) {

            log(Level.WARNING, "ContractDetails reloaded from SQL.");

            log(Level.INFO, EWrapperMsgGenerator.contractDetails(0, contractDetails));
            log(Level.INFO, tradingHours.toString());
        }
    }

    protected void reloadChartData(Calendar since, Calendar end) throws Exception {
        setTicksDataset(Main.dataSchema.getTicksData(contract, since, end));
        addToCharts(getTicksDataset(), false);
    }

    protected void reloadChartData(Calendar since) throws Exception {
        reloadChartData(since, DateUtil.getCalendarDate());
    }

    //
    //Requests
    //
    /**
     * Request market data.
     */
    public void reqMktData() {
        if (isGettingMarketData() || isDataNotSubscribed()) {
            return;
        }

        getDataClient().reqMktData(this);
    }

    /**
     * Request contract details using the saved contract.
     */
    public void reqContractDetails() {
        getDataClient().reqContractDetails(this);
    }

    /**
     * Request historical data.
     *
     * @param chart
     * @param end
     * @param duration
     */
    public void reqHistoricalData(BarDataset chart, long end, long duration) {
        Main.getHistDataClient().reqHistoricalData(this, chart, end, duration);
    }

    /**
     * Request historical data.
     *
     * @param chart
     */
    public void reqHistoricalData(TimeChart chart) {
        reqHistoricalData(chart,
                Math.min(DateUtil.HOUR_TIME * 4,
                2 * Math.max(chart.getBarSize(),
                DateUtil.getTimeNow() - chart.getLastUpdated())));
    }

    /**
     * Request historical data.
     *
     * @param chart
     * @param duration
     */
    public void reqHistoricalData(BarDataset chart, long duration) {
        reqHistoricalData(chart, DateUtil.getTimeNow(), duration);
    }

    /**
     * Request real-time bar.
     */
    public void reqRealTimeBars() {
        getDataClient().reqRealTimeBars(this);
    }

    public void cancelMktData() {
        getDataClient().cancelMktData(this);
    }

    public void cancelRealTimeBars() {
        getDataClient().cancelRealTimeBars(this);
    }
}
