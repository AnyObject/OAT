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

import OAT.util.Waiting;
import OAT.util.GeneralUtil;
import OAT.util.DateUtil;
import OAT.util.FileUtil;
import OAT.util.TextUtil;
import OAT.trading.TradingHours;
import OAT.trading.Trade;
import OAT.trading.Account;
import OAT.trading.Main;
import OAT.trading.Trend;
import OAT.trading.Parameters;
import com.ib.client.ContractDetails;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import javax.swing.ProgressMonitor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.DefaultXYDataset;
import OAT.data.TickDataset;
import OAT.event.GenericListener;
import OAT.event.State;
import OAT.event.StateChangeEvent;
import OAT.sql.BacktestSchema;
import OAT.sql.DataSchema;
import OAT.trading.classification.Predictor;
import OAT.trading.classification.TrainingSample;
import OAT.trading.client.BacktestDataClient;
import OAT.trading.client.BacktestTradingClient;
import OAT.trading.client.DataClient;
import OAT.trading.client.TradingClient;
import OAT.ui.BacktestDayBreakdownFrame;
import OAT.ui.BacktestSummaryFrame;
import OAT.ui.BasicChartFrame;

/**
 *
 * @author Antonio Yip
 */
public final class BacktestThread extends BaseThread implements GenericListener {

    public String symbol;
    public BacktestSummaryFrame backtestSummaryFrame;
    public List<BacktestDayBreakdownFrame> breakdownFrames;
    public boolean suppressClassifiers;
    public boolean suppressStoppers;
    public int trainingSize;
    public boolean training;
    public boolean predictionMode;
    public boolean appendResults;
    //
    private long startTime, endTime, progressStartTime, totalTime, timeLeft;
    private int totalBacktests = 1;
    private int totalTicks;
    private int finishedBacktests;
    private int daysCount;
    private int expiryMonths;
    private int lastDay;
    private Level logLevel;
    private BackDate backDate;
    private List<BacktestStrategy> strategies;
    private String[] keys;
    private List[] steps;
    private Object[][] combinations;
    private TreeMap<String, ContractDetails> contractDetailsMap;
    private ProgressMonitor progressMonitor;
    private int threadCount = 0;
    private boolean logged;
    private double commission;
    private DataClient dataClient = new BacktestDataClient(this);
    private TradingClient tradingClient = new BacktestTradingClient(this);

    //
    public static enum BackDate {

        TODAY(DateUtil.DAY_TIME),
        ONE_WEEK(DateUtil.WEEK_TIME),
        ONE_MONTH(DateUtil.MONTH_TIME),
        TWO_MONTH(DateUtil.MONTH_TIME * 2),
        THREE_MONTH(DateUtil.QUARTER_TIME),
        FOUR_MONTH(DateUtil.MONTH_TIME * 4),
        SIX_MONTH(DateUtil.QUARTER_TIME * 2),
        NINE_MONTH(DateUtil.QUARTER_TIME * 3),
        ONE_YEAR(DateUtil.YEAR_TIME),
        TWO_YEAR(DateUtil.YEAR_TIME * 2),
        THREE_YEAR(DateUtil.YEAR_TIME * 3);
        final long time;

        BackDate(long time) {
            this.time = time;
        }

        @Override
        public String toString() {
            switch (this) {
                case TODAY:
                    return "Today";
                case ONE_WEEK:
                    return "Last Week";
                case ONE_MONTH:
                    return "Last Month";
                case TWO_MONTH:
                    return "Last 2 Months";
                case THREE_MONTH:
                    return "Last 3 Months";
                case FOUR_MONTH:
                    return "Last 4 Months";
                case SIX_MONTH:
                    return "Last 6 Months";
                case NINE_MONTH:
                    return "Last 9 Months";
                case ONE_YEAR:
                    return "Last Year";
                case TWO_YEAR:
                    return "Last 2 Years";
                case THREE_YEAR:
                    return "Last 3 Years";
                default:
                    return name();
            }
        }
    }

    public class BacktestData {

        public String symbol;
        private long time;
        private BackDate backDate;
        private ContractDetails[] contractDetailses;
        private TradingHours[] tradingHourses;
        private TickDataset[] tickDatasets;
        private int ticksDataCount = 0;
        private ProgressMonitor dataProgressMonitor;

        public BacktestData(String symbol) {
            this.symbol = symbol;
        }

        public void getTicksData(int expiryMonths, int lastDay, BackDate backDate) throws IOException, SQLException, ClassNotFoundException {
            this.backDate = backDate;
            this.time = DateUtil.getTimeNow();

            getBacktestSchema().clearBacktestDaysTable();

            final Object[][] backdatedSeesions =
                    getDataSchema().getBackdatedSessions(
                    symbol, expiryMonths, lastDay, backDate.time);

            contractDetailses = new ContractDetails[backdatedSeesions.length];
            tradingHourses = new TradingHours[backdatedSeesions.length];
            tickDatasets = new TickDataset[backdatedSeesions.length];

            if (dataProgressMonitor == null) {
                dataProgressMonitor = new ProgressMonitor(Main.frame,
                        "Loading data...",
                        "", 0, tickDatasets.length);
            }

            for (int n = 0; n < backdatedSeesions.length; n++) {
                if (dataProgressMonitor.isCanceled()) {
                    throw new UnsupportedOperationException("Cancelled");
                }

                final int i = n;

                waitForThread(Main.p_Backtest_Max_Thread);

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        String localSymbol = (String) backdatedSeesions[i][0];
                        TimeZone timeZone = DateUtil.getTimeZone((String) backdatedSeesions[i][1]);
                        long dayOpen = (Long) backdatedSeesions[i][2];
                        long dayClose = (Long) backdatedSeesions[i][3];
                        long breakStart = (Long) backdatedSeesions[i][4];
                        long breakEnd = (Long) backdatedSeesions[i][5];

                        try {
                            ContractDetails contractDetails;
                            if (contractDetailsMap.containsKey(localSymbol)) {
                                contractDetails = contractDetailsMap.get(localSymbol);
                            } else {
                                contractDetails = getDataSchema().getContractDetails(localSymbol);
                                contractDetailsMap.put(localSymbol, contractDetails);
                            }

                            TradingHours tradingHours = new TradingHours();

                            if (breakStart > 0 && breakEnd > 0) {
                                tradingHours.addTradingSession(dayOpen, breakStart, timeZone);
                                tradingHours.addTradingSession(breakEnd, dayClose, timeZone);
                            } else {
                                tradingHours.addTradingSession(dayOpen, dayClose, timeZone);
                            }

                            contractDetailses[i] = contractDetails;
                            tradingHourses[i] = tradingHours;
                            tickDatasets[i] = (getDataSchema().getTicksData(contractDetails.m_summary, dayOpen, dayClose));

                            dataProgressMonitor.setProgress(++ticksDataCount);
                            threadCount--;
                        } catch (Exception ex) {
                            log(Level.SEVERE, null, ex);
                        }
                    }
                }).start();

                threadCount++;
            }

            new Waiting(Main.p_Backtest_Time_Out, Main.p_Wait_Interval, logger) {

                @Override
                public boolean waitWhile() {
                    return ticksDataCount < size();
                }

                @Override
                public void retry() {
                }

                @Override
                public String message() {
                    return "Loading data from database...";
                }

                @Override
                public void timeout() {
                    throw new UnsupportedOperationException("Unable to load data.");
                }
            };

        }

        public TickDataset getTickDataset(int dayId) {
            return tickDatasets[dayId];
        }

        public ContractDetails getContractDetails(int dayId) {
            return contractDetailses[dayId];
        }

        public TradingHours getTradingHours(int dayId) {
            return tradingHourses[dayId];
        }

        public BackDate getBackDate() {
            return backDate;
        }

        public long getDuration() {
            return DateUtil.getTimeNow() - time;
        }

        public int size() {
            if (tickDatasets == null) {
                return 0;
            }

            return tickDatasets.length;
        }

        public void clear() {
            contractDetailses = null;
            tradingHourses = null;
            tickDatasets = null;
        }
    }

    public BacktestThread(String symbol, int expiryMonths, int lastDay, String[] keys, List[] steps, BackDate backDate, Level logLevel) {
        this.symbol = symbol;
        this.expiryMonths = expiryMonths;
        this.lastDay = lastDay;
        this.keys = keys;
        this.steps = steps;
        this.backDate = backDate;
        this.logLevel = logLevel;
    }

    @Override
    public void init() {
        super.init();

        try {
            if (Main.backtestSchema == null) {
                log(Level.INFO, "Initializing backtestSchema...");
                Main.backtestSchema = new BacktestSchema();
                Main.backtestSchema.connect(getAccount().backtestSchema);
                Main.backtestSchema.init();
            } else {
                Main.backtestSchema.checkConnection();
            }
            Main.backtestSchema.backtestId = (int) DateUtil.getSecondsOfDay();

            if (Main.backtestDataSchema == null) {
                log(Level.INFO, "Initializing dataSchema...");
                Main.backtestDataSchema = new DataSchema();
                Main.backtestDataSchema.connect(getAccount().dataSchema);
            } else {
                Main.backtestDataSchema.checkConnection();
            }

        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }

        breakdownFrames = new ArrayList();
        contractDetailsMap = new TreeMap();

        try {
            commission = getDataSchema().getCommission(symbol);
        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void preSleep() {
    }

    @Override
    public void postWakeUp() {
    }

    @Override
    public void postConnection() {
    }

    @Override
    public void run() {
        if (symbol == null || keys == null || steps == null || backDate == null) {
            return;
        }

        setState(true, State.BACKTESTING);
        threadCount = 0;

        Main.frame.selectLogTextPane(getDefaultLoggerName());

        cleanup();

        logger.setLevel(logLevel);

        startTime = DateUtil.getTimeNow();

        log(Level.INFO, "Starting Backtest {0} for {1}...",
                new Object[]{symbol, backDate.toString().toLowerCase()});

        //Preparing
        if (strategies != null) {
            for (BacktestStrategy strategy : strategies) {
                strategy.removeChangeListener(this);
                strategy = null;
            }

            strategies = null;
        }

        // clear all backtest tables
        if (!appendResults) {
            try {
                getBacktestSchema().truncateTables();
            } catch (Exception ex) {
                log(Level.SEVERE, null, ex);
            }
        }

        strategies = new ArrayList<BacktestStrategy>();

        // get data
        if (getBacktestData() != null
                && symbol.equals(getBacktestData().symbol)
                && backDate != BackDate.TODAY
                && backDate == getBacktestData().getBackDate()
                && getBacktestData().getDuration() < DateUtil.DAY_TIME) {
        } else {

            setBacktestData(new BacktestData(symbol));

            try {
                getBacktestData().getTicksData(expiryMonths, lastDay, backDate);

            } catch (UnsupportedOperationException e) {
                log(Level.INFO, "User canceled.");
                setBacktestData(null);
                return;

            } catch (Exception e) {
                log(Level.SEVERE, null, e);
            }

        }

        try {
            getBacktestSchema().insertBacktestDay(getBacktestData());
        } catch (Exception e) {
            log(Level.SEVERE, null, e);
        }

        daysCount = getBacktestData().size();

        String dayCountString = TextUtil.BASIC_FORMATTER.format(daysCount);
        log(Level.INFO, "{0} trading sessions found.", dayCountString);

        int tickCount = 0;
        for (int i = 0; i < getBacktestData().size(); i++) {
            tickCount += getBacktestData().getTickDataset(i).size();

        }

        log(Level.INFO, "Calculating combinations...");

        combinations = GeneralUtil.extractCombinations(steps);

        totalTicks = tickCount * combinations.length;
        totalBacktests = getBacktestData().size() * combinations.length;
        finishedBacktests = 0;

        progressStartTime = DateUtil.getTimeNow();
        progressMonitor = new ProgressMonitor(Main.frame,
                "Running Backtest.                                           ",
                "", 0, totalBacktests);
        //progressMonitor.setProgress(0);

        //log combinations
        StringBuilder sb = new StringBuilder("Combinations:\n");
        for (int i = 0; i < combinations.length; i++) {
            sb.append("[").append(i).append("]  ").
                    append(TextUtil.toString(combinations[i], ",")).
                    append("\n");
        }
        log(Level.FINE, sb.toString());

        String count = TextUtil.BASIC_FORMATTER.format(combinations.length);

        log(Level.INFO,
                "\n\tTotal Combinations = {0}"
                + "\n\tTotal number of ticks = {1} x {2} = {3}"
                + "\n\tTotal number of backtests = {4} x {5} = {6}",
                new Object[]{
                    combinations.length,
                    TextUtil.BASIC_FORMATTER.format(tickCount),
                    count,
                    TextUtil.BASIC_FORMATTER.format(totalTicks),
                    dayCountString,
                    count,
                    totalBacktests});

        new Thread(new Runnable() {

            @Override
            public void run() {
                for (int j = 0; j < combinations.length; j++) {
                    for (int i = 0; i < getBacktestData().size(); i++) {
                        if (progressMonitor.isCanceled()) {
                            break;
                        }

                        final BacktestStrategy strategy = new BacktestStrategy();
                        final TickDataset tickDataset = getBacktestData().getTickDataset(i);
                        ContractDetails contractDetails = getBacktestData().getContractDetails(i);
                        TradingHours tradingHours = getBacktestData().getTradingHours(i);

                        try {
                            Parameters[] params = GeneralUtil.getParameters(keys, combinations[j]);
                            strategy.setLogger(logger);
                            strategy.setThreadId(strategies.size());
                            strategy.setDataClient(dataClient);
                            strategy.setTradingClient(tradingClient);
                            strategy.setParameters(params);


                            if (contractDetails == null) {
                                log(Level.WARNING,
                                        "Contract not found for dayId: {0}",
                                        i);
                            }

                            strategy.initBacktest(i, j,
                                    //                                    tickDataset,
                                    contractDetails,
                                    tradingHours,
                                    commission);

                            strategies.add(strategy);

                            if (tickDataset.getNewBarCount() > tradingHours.getSessions().size()) {
                                totalBacktests--;
                                progressMonitor.setMaximum(totalBacktests);

                                continue;
                            }

                            waitForThread(Main.p_Backtest_Max_Thread);

                            strategy.setTicksDataset(tickDataset);
                            new Thread(strategy).start();


                            threadCount++;

                        } catch (Exception ex) {
                            log(Level.SEVERE, null, ex);
                        }
                    }
                }

            }
        }).start();
    }

    public Account getAccount() {
        return Account.BACKTEST;
    }

    @Override
    public String getChildrenNodeName() {
        return null;
    }

    public HybridStrategy getStrategy(int strategyId, int dayId) {
        return strategies.get(strategyId * daysCount + dayId);
    }

    public BacktestData getBacktestData() {
        return Main.backtestData;
    }

    public void setBacktestData(BacktestData backtestData) {
        Main.backtestData = backtestData;
    }

    public BacktestSchema getBacktestSchema() {
        return Main.backtestSchema;
    }

    public DataSchema getDataSchema() {
        return Main.backtestDataSchema;
    }

    public Level getLogLevel() {
        return logLevel;
    }

    public void showSummary() {
        Object[][] table = null;

        try {
            table = getBacktestSchema().getBacktestSummary();
        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }

        if (backtestSummaryFrame != null && backtestSummaryFrame.isDisplayable()) {
            backtestSummaryFrame.dispose();
        }

        backtestSummaryFrame = new BacktestSummaryFrame(Main.frame, table);
        backtestSummaryFrame.setVisible(true);
    }

    public void showDayBreakdown(final int strategyId) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < breakdownFrames.size(); i++) {
                    BacktestDayBreakdownFrame breakdownFrame = breakdownFrames.get(i);

                    if (breakdownFrame.strategyId == strategyId) {
                        breakdownFrame.toFront();
                        return;
                    }
                }

                Object[][] table = null;

                try {
                    table = getBacktestSchema().getBacktestDayBreakdown(strategyId);
                } catch (Exception ex) {
                    log(Level.SEVERE, null, ex);
                }

                BacktestDayBreakdownFrame breakdownFrame =
                        new BacktestDayBreakdownFrame(table, strategyId);

                breakdownFrames.add(breakdownFrame);

                breakdownFrame.setVisible(true);
            }
        });
    }

    public void showDayProfitsDistribution(int strategyId) {
        double[] values = null;

        try {
            values = getBacktestSchema().getDayProfits(strategyId);
        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }

        showHistogram("Day Profits", values);
    }

    public void showTradeProfitsDistribution(int strategyId) {
        double[] values = null;

        try {
            values = getBacktestSchema().getTradeProfits(strategyId);
        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }

        showHistogram("Trade Profits", values);
    }

    public void showEquityChart(int strategyId) {
        double[][] values = null;
        String currency = "";

        try {
            values = getBacktestSchema().getCumulatedDayNet(strategyId);
            currency = getBacktestSchema().getCurrency(strategyId);

        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }

        showEquityChart("Equity (" + currency + ")", values);
    }

    public void showPortfolioEquityChart(int strategyId) {
        double[][] values = null;

        try {
            values = getBacktestSchema().getCumulatedPortfolioDayNet(strategyId);
        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }

        showEquityChart("Portfolio Equity (" + Main.p_Base_Currency + ")", values);
    }

    private void showHistogram(String title, double[] data) {
        if (data == null || data.length == 0) {
            return;
        }

        HistogramDataset histogramDataset = new HistogramDataset();
        histogramDataset.addSeries(title, data, 15);

        JFreeChart chart = ChartFactory.createHistogram(
                "", "", "", histogramDataset,
                PlotOrientation.VERTICAL, false, false, false);

        new BasicChartFrame(title, chart).setVisible(true);
    }

    private void showEquityChart(String title, double[][] data) {
        if (data == null || data.length == 0) {
            return;
        }

        DefaultXYDataset dataset = new DefaultXYDataset();
        dataset.addSeries("", data);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "", "", "", dataset,
                false, false, false);


        new BasicChartFrame(title, chart).setVisible(true);
    }

    @Override
    public String getDefaultLoggerName() {
        return "Backtest";
    }

    @Override
    public String getDefaultNodeName() {
        return null;
    }

    @Override
    protected void preExit() {
        cleanup();

        if (backtestSummaryFrame != null) {
            backtestSummaryFrame.setVisible(false);
        }

        try {
            getBacktestSchema().disconnect();
        } catch (SQLException ex) {
            log(Level.SEVERE, null, ex);
        }
        
        try {
            getDataSchema().disconnect();
        } catch (SQLException ex) {
            log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();

        totalBacktests = 0;

        if (backtestSummaryFrame != null) {
            backtestSummaryFrame.setVisible(false);
        }

        exited();
    }

    //
    //Events
    //
    @Override
    public void eventHandler(EventObject event) {
        if (event instanceof StateChangeEvent) {
            stateChanged((StateChangeEvent) event);
        }
    }

    protected synchronized void stateChanged(StateChangeEvent evt) {
        if (evt.getState() == State.BACKTEST_FINISHED) {
            if (progressMonitor.isCanceled() && !logged) {
                logged = true;
                log(Level.INFO, "User canceled.");

                for (BacktestStrategy strategy : strategies) {
                    strategy.isDead = true;
                }

                return;
            }

            threadCount--;

            progressMonitor.setProgress(++finishedBacktests);

            double finished = (double) finishedBacktests / totalBacktests;

            if (finishedBacktests % 50 == 0) {
                long timeUsed = DateUtil.getTimeNow() - progressStartTime;
                timeLeft = DateUtil.roundTime(
                        (long) (timeUsed / finished) - timeUsed, DateUtil.SECOND_TIME);
            }

            progressMonitor.setNote(
                    String.format("Completed %d%% ", (int) (100 * finished))
                    + "\tEst. time left: "
                    + DateUtil.getSimpleDurationStr(timeLeft));
        }

        if (finishedBacktests >= totalBacktests) {
            finish();
        }
    }

    public long getEndTime() {
        return endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getStrategyVariables(int strategyId) {
        try {
            return getBacktestSchema().getStrategyVariables(strategyId);
        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public void writeCSV(List<? extends Object[]> list, String name) {
        if (!list.isEmpty()) {
            String st = TextUtil.toString(list.toArray(new Object[0][0]), "\n", ",");

            File csvFile = new File(FileUtil.getFolder(Main.tradingFolder, "Training")
                    + DateUtil.getTimeStamp(getClientStartTime(), "yyyyMMddHHmmss")
                    + "_" + TextUtil.convertSymbol(symbol) + "_" + name + ".csv");

            try {
                FileUtil.saveToFile(st, csvFile);
                log(Level.INFO, "Exported {0}", csvFile);
            } catch (IOException ex) {
                log(Level.SEVERE, null, ex);
            }
        }
    }

    protected void finish() {
        //
        // still working
        if (training) {
            String csvPrefix;

            if (predictionMode) {
                csvPrefix = "PREDICTION";
            } else {
                csvPrefix = "CLASSIFICATION";
            }

            for (Trend trend : Trend.values()) {
                List<Object[]> trainingSet = new ArrayList<Object[]>();
                List<Object[]> tradeRecords = new ArrayList<Object[]>();

                for (BacktestStrategy strategy : strategies) {
                    for (Trade trade : strategy.getTrades()) {


                        if (trade.isTrend(trend)) {
                            Object[] row = trade.getEnterBarsPattern(trainingSize, predictionMode);

                            if (row != null) {
                                trainingSet.add(row);
                                tradeRecords.add(
                                        new Object[]{
                                            DateUtil.getDateTimeString(trade.getEnterTime()),
                                            trade.getSide().sign,
                                            trade.getEnterPrice(),
                                            DateUtil.getDateTimeString(trade.getExitTime()),
                                            trade.getExitPrice(),
                                            trade.getProfit()
                                        });
                            }
                        }
                    }
                }

                writeCSV(trainingSet, csvPrefix + "_" + trend.name());
                writeCSV(tradeRecords, "TRADES_" + trend.name());

                for (Predictor predictor : strategies.get(0).getPredictors()) {
                    if (predictor.getTrend() != trend) {
                        continue;
                    }

                    List<TrainingSample> trainingSamples = new ArrayList<TrainingSample>();

                    for (Object[] row : trainingSet) {
                        trainingSamples.add(
                                new TrainingSample(
                                Arrays.copyOf(row, row.length - 1),
                                row[row.length - 1]));
                    }

                    predictor.train(trainingSamples);
                    predictor.crossValidate(trainingSamples);
                }
            }

        } else {
            log(Level.INFO, "Calculating results...");

            //get results
            showSummary();
        }

        //post logging
        if (!logged) {
            logged = true;

            StringBuilder sb = new StringBuilder("Results:\n\t");

            for (BacktestStrategy strategy : strategies) {
                sb.append("\t").append(strategy.primaryChart.getLocalSymbol()).
                        append(" [").
                        append(strategy.strategyId).
                        append("]").
                        append(" [").
                        append(strategy.dayId).
                        append(": ").
                        append(DateUtil.getTimeStamp(
                        strategy.tradingHours.getLastOpenTime(strategy.primaryChart.getOpenTime()),
                        DateUtil.DATE_FORMAT)).
                        append("]").
                        append("  Trades=").
                        append(strategy.getTrades().getItemCount()).
                        append("  Gross=").
                        append(TextUtil.PRICE_CHANGE_FORMATTER.format(strategy.getGross())).
                        append("  Average=").
                        append(TextUtil.PRICE_CHANGE_FORMATTER.format(strategy.getGrossAverage())).
                        append("  Time=").
                        append(DateUtil.getDurationStr(strategy.getDuration())).
                        append("\n");
            }
            log(Level.FINE, sb.toString());

            endTime = DateUtil.getTimeNow();
            totalTime = endTime - startTime;

            log(Level.INFO,
                    "Finished backtest."
                    + "\n\tTotal time = {0}"
                    + "\n\tAverage ticks per second = {1}",
                    new Object[]{
                        DateUtil.getDurationStr(totalTime),
                        TextUtil.BASIC_FORMATTER.format(1000D * totalTicks / totalTime)});


            setState(false, State.BACKTESTING);
            getLogger().pushLog();
        }
    }

    private void waitForThread(final int maxThread) {
        new Waiting(Main.p_Backtest_Time_Out, Main.p_Backtest_Wait_Interval, logger) {

            @Override
            public boolean waitWhile() {
                return threadCount >= maxThread;
            }

            @Override
            public void retry() {
            }

            @Override
            public String message() {
                return null;
            }

            @Override
            public void timeout() {
            }
        };
    }
}
