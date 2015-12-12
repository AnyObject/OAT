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

package OAT.sql;

import com.ib.client.ContractDetails;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import OAT.data.TickDataset;
import OAT.trading.Main;
import OAT.trading.Trade;
import OAT.trading.TradingHours;
import OAT.trading.thread.BacktestThread.BacktestData;

/**
 *
 * @author Antonio Yip
 */
public class BacktestSchema extends SqlConnect {

    public int backtestId;

    public void dropTables() throws SQLException, IOException, ClassNotFoundException {
        runScriptFromResources("dropBacktestTables.sql");
    }

    public void truncateTables() throws SQLException, IOException, ClassNotFoundException {
        runScriptFromResources("truncateBacktestTables.sql");
    }

    @Override
    public void createTables() throws SQLException, IOException, ClassNotFoundException {
        runScriptFromResources("createBacktestTables.sql");
    }

    @Override
    public void createProcedures() throws SQLException, IOException, ClassNotFoundException {
        runScriptFromResources("createBacktestProcedures.sql");
    }

    public void clearBacktestDaysTable() throws IOException, SQLException, ClassNotFoundException {
        callProcedure("clearBacktestDaysTable");
    }

    public void insertBacktestStrategy(int strategyId, String variables, ContractDetails cd, double commission) throws SQLException, ClassNotFoundException {
        callProcedure("insertBacktestStrategy",
                backtestId,
                strategyId,
                variables,
                cd.m_summary.m_symbol,
                cd.m_summary.m_exchange,
                cd.m_summary.m_currency,
                Integer.valueOf(cd.m_summary.m_multiplier) / cd.m_priceMagnifier,
                commission);
    }

    public void insertBacktestDay(BacktestData backtestData) throws SQLException, ClassNotFoundException {
        for (int i = 0; i < backtestData.size(); i++) {
            insertBacktestDay(i, backtestData.getTradingHours(i), backtestData.getTickDataset(i));
        }
    }

    public void insertBacktestDay(int dayId, TradingHours tradingHours, TickDataset ticksData) throws SQLException, ClassNotFoundException {
        callProcedure("insertBacktestDay",
                backtestId,
                dayId,
                tradingHours.getFirstSession().getOpenTime(),
                tradingHours.getLastSession().getCloseTime(),
                ticksData.getDayVolume(),
                ticksData.size());
    }

    public void insertBacktestTrade(int strategyId, int dayId, int tradeId, Trade trade) throws SQLException, ClassNotFoundException, IOException {
        callProcedure("insertTrade",
                backtestId,
                strategyId,
                dayId,
                tradeId,
                trade.getSide().sign,
                trade.getEnterTime(),
                trade.getEnterPrice(),
                trade.getExitTime(),
                trade.getExitPrice());
    }

    public Object[][] getBacktestSummary() throws SQLException, IOException, ClassNotFoundException {
        ResultSet resultSet = callProcedure("getBacktestSummary", backtestId);

        Object[][] table = getTable(resultSet,
                new String[]{
                    "id",
                    //                    "currency",
                    "net",
                    "gross",
                    "margin",
                    "day_count",
                    "day_max",
                    "day_min",
                    "day_ratio",
                    "day_avg",
                    "day_stdev",
                    "day_sharpe",
                    "trade_count",
                    "trade_max",
                    "trade_min",
                    "trade_ratio",
                    "trade_avg",
                    "trade_stdev",
                    "trade_sharpe"
                });

        for (int i = 0; i < table[0].length; i++) {
            String s = (String) table[0][i];
            table[0][i] = s.replace("day", "d").
                    replace("trade", "t");
        }

        return table;
    }

    public Object[][] getBacktestDayBreakdown(int strategyId) throws SQLException, IOException, ClassNotFoundException {
        ResultSet resultSet = callProcedure("getBacktestBreakdown", backtestId, strategyId);

        return getTable(resultSet,
                new String[]{
                    "day",
                    "open",
                    "close",
                    "volume",
                    "ticks",
                    "sum",
                    "count",
                    "max",
                    "min",
                    "win_ratio",
                    "avg",
                    "stdev",
                    "sharpe"
                });
    }

    public String getStrategyVariables(int strategyId) throws SQLException, ClassNotFoundException {
        ResultSet resultSet = callProcedure("getStrategyVariables", backtestId, strategyId);

        String variables = "";

        while (resultSet.next()) {
            variables = resultSet.getString("variables");
        }

        return variables;
    }

    public String getCurrency(int strategyId) throws SQLException, ClassNotFoundException {
        String currency = "";

        ResultSet resultSet = executeQuery(
                "SELECT * FROM `backtest_strategies`"
                + " WHERE strategy_id = '" + strategyId + "'"
                + " AND backtest_id = '" + backtestId + "'"
                + " LIMIT 1"
                + " ;");

        while (resultSet.next()) {
            currency = resultSet.getString("currency");
        }

        return currency;
    }

    public double[] getDayProfits(int strategyId) throws SQLException, ClassNotFoundException {
        ResultSet resultSet = callProcedure("getDayProfits", backtestId, strategyId);

        List<Double> profits = new ArrayList<Double>();

        while (resultSet.next()) {
            profits.add(resultSet.getDouble("profit"));
        }

        double[] data = new double[profits.size()];

        for (int i = 0; i < profits.size(); i++) {
            data[i] = profits.get(i);
        }

        return data;
    }

    public double[] getTradeProfits(int strategyId) throws SQLException, ClassNotFoundException {
        ResultSet resultSet = callProcedure("getTradeProfits", backtestId, strategyId);

        List<Double> profits = new ArrayList<Double>();

        while (resultSet.next()) {
            profits.add(resultSet.getDouble("profit"));
        }

        double[] data = new double[profits.size()];

        for (int i = 0; i < profits.size(); i++) {
            data[i] = profits.get(i);
        }

        return data;
    }

    public double[][] getCumulatedDayNet(int strategyId) throws SQLException, ClassNotFoundException {
        ResultSet resultSet = callProcedure("getDayNet", backtestId, strategyId);

        List<Double> dates = new ArrayList<Double>();
        List<Double> profits = new ArrayList<Double>();

        while (resultSet.next()) {
            dates.add(resultSet.getDouble("date"));
            profits.add(resultSet.getDouble("profit"));
        }

        double[][] data = new double[2][dates.size()];
        double cumProfit = 0;

        for (int i = 0; i < profits.size(); i++) {
            data[0][i] = dates.get(i);
            data[1][i] = cumProfit += profits.get(i);
        }

        return data;
    }

    public double[][] getCumulatedPortfolioDayNet(int strategyId) throws SQLException, ClassNotFoundException {
        ResultSet resultSet = callProcedure("getPortfolioDayNet", strategyId, Main.p_Base_Currency);

        List<Double> dates = new ArrayList<Double>();
        List<Double> profits = new ArrayList<Double>();

        while (resultSet.next()) {
            dates.add(resultSet.getDouble("date"));
            profits.add(resultSet.getDouble("profit"));
        }

        double[][] data = new double[2][dates.size()];
        double cumProfit = 0;

        for (int i = 0; i < profits.size(); i++) {
            data[0][i] = dates.get(i);
            data[1][i] = cumProfit += profits.get(i);
        }

        return data;
    }

//    public void insertBacktestTradeBars(int strategyId, int dayId, LinkedList<Trade> trades, File file) throws IOException, SQLException, ClassNotFoundException {
//        for (int i = 0; i < trades.size(); i++) {
//            Trade trade = trades.get(i);
//            double enterPrice = trade.getEnterPrice();
//
//            StringBuilder barsString = new StringBuilder();
//            long prevBarTime = trade.getEnterTime();
//            double prevDuration = prevBarTime - trade.getPrevBars().get(0).time;
//            double prevCount = trade.getPrevBars().get(0).count;
//
////            barsString.append(formatValues(
////                        trade.getEnterTime(),trade.getEnterPrice(),
////                        trade.getExitTime(),trade.getExitPrice(),
////                        trade.getSide(),trade.getProfit()));
////
////            barsString.append(", ");
//
//            //extract bars
//            for (int j = 0; j < trade.getPrevBars().size(); j++) {
//                Bar bar = trade.getPrevBars().get(j);
//
//                //bar duration
//                long duration = prevBarTime - bar.time; // in milliseconds
//                prevBarTime = bar.time;
//
//                //sql
//                insertBar(strategyId, dayId, i, j,
//                        duration, bar.count,
//                        bar.open, bar.high, bar.low, bar.close);
//
//                if (file != null) {
//
//                    //duration, count
//                    if (j > 0) {
//                        barsString.append(formatValues(
//                                (double) duration / prevDuration,
//                                (double) bar.count / prevCount)).
//                                append(", ");
//                    }
//
//                    //price normalisation
//                    double openN = bar.open / enterPrice;
//                    double highN = bar.high / enterPrice;
//                    double lowN = bar.low / enterPrice;
//                    double closeN = bar.close / enterPrice;
//
//                    barsString.append(formatValues(
//                            openN, highN, lowN, closeN));
//
//
////                    //indicators
////                    if (bar.indicators != null) {
////                        for (double d : bar.indicators) {
////                            barsString.append(", ").append(d);
////                        }
////                    }
//
//                    barsString.append(", ");
//                }
//            }
//
//            if (file != null) {
//                barsString.append(trade.getTradeLabel().value);
//                barsString.append("\n");
//
//                FileUtil.appendToFile(barsString.toString(), file);
//            }
//        }
//    }
    protected void insertBar(int strategyId, int dayId, int tradeId, int barId, long time, long count, double open, double high, double low, double close) throws SQLException, ClassNotFoundException {
        executeUpdate(
                "INSERT INTO `backtest`.`backtest_bars`"
                + " (`backtest_id`, `strategy_id`, `day_id`, `trade_id`, `bar_id`,"
                + " `duration`, `count`, `open`, `high`, `low`, `close` )"
                + " VALUES("
                + SqlUtil.formatValues(
                backtestId,
                strategyId,
                dayId,
                tradeId,
                barId,
                time,
                count,
                open,
                high,
                low,
                close)
                + ");");
    }
}
