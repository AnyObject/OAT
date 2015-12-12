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

import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import OAT.data.Tick;
import OAT.data.TickDataset;
import OAT.trading.TradingHours;
import OAT.util.DateUtil;

/**
 *
 * @author Antonio Yip
 */
public class DataSchema extends SqlConnect {

    @Override
    public void createTables() throws SQLException, IOException, ClassNotFoundException {
        runScriptFromResources("createDataTables.sql");

        try {
            runScriptFromResources("alterDataTables.sql");
        } catch (SQLException e) {
        }

        try {
            runScriptFromResources("populateCurrency.sql");
        } catch (SQLException e) {
        }
    }

    @Override
    public void createProcedures() throws SQLException, IOException, ClassNotFoundException {
        runScriptFromResources("createDataProcedures.sql");
    }

    public void insertTick(Contract contract, Tick tick) throws SQLException, ClassNotFoundException {
        if (contract == null || contract.m_conId <= 0) {
            throw new UnsupportedOperationException("Empty contract.");
        }

        if (tick.getSize() <= 0 || tick.getPrice() <= 0) {
            return;
        }

        callProcedure("insertTick", contract.m_conId, tick.getTime(), tick.getPrice(), tick.getSize(), tick.isForcedNew());
    }

    public void insertTicks(Contract contract, List<Tick> ticks) throws SQLException, ClassNotFoundException {
        for (Tick tick : ticks) {
            insertTick(contract, tick);
        }
    }

    public void insertContract(Contract contract) throws SQLException, ClassNotFoundException {
        executeUpdate(
                "REPLACE INTO `contracts`"
                + " (m_symbol, m_secType, m_expiry, m_strike, m_right, m_multiplier,"
                + " m_exchange, m_currency, m_localSymbol, m_primaryExch, m_includeExpired,"
                + " m_comboLegsDescrip, m_comboLegs, m_conId, m_secIdType, m_secId)"
                + " VALUES("
                + SqlUtil.formatValues(
                contract.m_symbol,
                contract.m_secType,
                contract.m_expiry,
                contract.m_strike,
                contract.m_right,
                contract.m_multiplier,
                contract.m_exchange,
                contract.m_currency,
                contract.m_localSymbol,
                contract.m_primaryExch,
                contract.m_includeExpired,
                contract.m_comboLegsDescrip,
                contract.m_comboLegs,
                contract.m_conId,
                contract.m_secIdType,
                contract.m_secId)
                + " );");
    }

    public void insertContractDetails(ContractDetails contractDetails) throws SQLException, ClassNotFoundException {
        executeUpdate(
                "REPLACE INTO `contract_details`"
                + " (m_conId, m_marketName, m_tradingClass, m_minTick, m_priceMagnifier,"
                + " m_orderTypes, m_validExchanges, m_underConId, m_longName, m_cusip,"
                + " m_ratings, m_descAppend, m_bondType, m_couponType, m_callable,"
                + " m_putable, m_coupon, m_convertible, m_maturity, m_issueDate, m_nextOptionDate,"
                + " m_nextOptionType, m_nextOptionPartial, m_notes, m_contractMonth, m_industry,"
                + " m_category, m_subcategory, m_timeZoneId, m_tradingHours, m_liquidHours)"
                + " VALUES("
                + SqlUtil.formatValues(
                contractDetails.m_summary.m_conId,
                contractDetails.m_marketName,
                contractDetails.m_tradingClass,
                contractDetails.m_minTick,
                contractDetails.m_priceMagnifier,
                contractDetails.m_orderTypes,
                contractDetails.m_validExchanges,
                contractDetails.m_underConId,
                contractDetails.m_longName,
                contractDetails.m_cusip,
                contractDetails.m_ratings,
                contractDetails.m_descAppend,
                contractDetails.m_bondType,
                contractDetails.m_couponType,
                contractDetails.m_callable,
                contractDetails.m_putable,
                contractDetails.m_coupon,
                contractDetails.m_convertible,
                contractDetails.m_maturity,
                contractDetails.m_issueDate,
                contractDetails.m_nextOptionDate,
                contractDetails.m_nextOptionType,
                contractDetails.m_nextOptionPartial,
                contractDetails.m_notes,
                contractDetails.m_contractMonth,
                contractDetails.m_industry,
                contractDetails.m_category,
                contractDetails.m_subcategory,
                contractDetails.m_timeZoneId,
                contractDetails.m_tradingHours,
                contractDetails.m_liquidHours)
                + " );");
    }

    public void insertTradingHours(TradingHours tradingHours) throws SQLException, ClassNotFoundException {
        for (int i = 0; i < tradingHours.getTradingSessions().size(); i++) {
            Calendar open = tradingHours.getTradingSessions().get(i).getOpen();
            Calendar close = tradingHours.getTradingSessions().get(i).getClose();

            executeUpdate(
                    "REPLACE INTO `trading_hours`"
                    + " (m_exchange, open_long, close_long, timeZone, gmt_offset, open_local, close_local)"
                    + " VALUES("
                    + SqlUtil.formatValues(
                    tradingHours.getExchange(),
                    open.getTimeInMillis(),
                    close.getTimeInMillis(),
                    tradingHours.getTimeZone().getDisplayName(),
                    (double) (DateUtil.getTimeDiffFromGMT(open) / DateUtil.HOUR_TIME),
                    DateUtil.getForeignTimeStamp(open),
                    DateUtil.getForeignTimeStamp(close))
                    + " );");
        }
    }

    public double getCommission(String symbol) throws SQLException, IOException, ClassNotFoundException {
        double commission = 0;

        ResultSet resultSet = callProcedure("getLastTraded",
                symbol);

        while (resultSet.next()) {
            commission = resultSet.getDouble("commission");
        }

        return commission;
    }

    public Contract getContract(String localSymbol) throws SQLException, ClassNotFoundException {
        Contract contract = null;

        ResultSet resultSet = executeQuery(
                "SELECT * FROM `contracts`"
                + " WHERE m_localSymbol = '" + localSymbol + "'"
                + ";");

        while (resultSet.next()) {
            contract = new Contract(
                    resultSet.getInt("m_conId"),
                    resultSet.getString("m_symbol"),
                    resultSet.getString("m_secType"),
                    resultSet.getString("m_expiry"),
                    resultSet.getDouble("m_strike"),
                    resultSet.getString("m_right"),
                    resultSet.getString("m_multiplier"),
                    resultSet.getString("m_exchange"),
                    resultSet.getString("m_currency"),
                    resultSet.getString("m_localSymbol"),
                    null,
                    resultSet.getString("m_primaryExch"),
                    resultSet.getBoolean("m_includeExpired"),
                    resultSet.getString("m_secIdType"),
                    resultSet.getString("m_secId"));
        }

        return contract;
    }

    public ContractDetails getContractDetails(String localSymbol) throws SQLException, ClassNotFoundException {
        ContractDetails contractDetails = null;

        ResultSet resultSet = executeQuery(
                "SELECT * FROM `contract_details`"
                + " WHERE m_conId = "
                + " (SELECT m_conId FROM `contracts`"
                + " WHERE m_localSymbol = '" + localSymbol + "'"
                + " LIMIT 1)"
                + ";");

        while (resultSet.next()) {
            contractDetails = new ContractDetails(
                    getContract(localSymbol),
                    resultSet.getString("m_marketName"),
                    resultSet.getString("m_tradingClass"),
                    resultSet.getDouble("m_minTick"),
                    resultSet.getString("m_orderTypes"),
                    resultSet.getString("m_validExchanges"),
                    resultSet.getInt("m_underConId"),
                    resultSet.getString("m_longName"),
                    resultSet.getString("m_contractMonth"),
                    resultSet.getString("m_industry"),
                    resultSet.getString("m_category"),
                    resultSet.getString("m_subcategory"),
                    resultSet.getString("m_timeZoneId"),
                    resultSet.getString("m_tradingHours"),
                    resultSet.getString("m_liquidHours"));

            contractDetails.m_priceMagnifier = resultSet.getInt("m_priceMagnifier");
        }

        return contractDetails;
    }

    public int getConId(String symbol, int lastDayBeforeExpiry) throws SQLException, IOException, ClassNotFoundException {
        int conId = 0;

        ResultSet resultSet = callProcedure("getConId",
                symbol, lastDayBeforeExpiry);

        while (resultSet.next()) {
            conId = resultSet.getInt("m_conId");
        }

        return conId;
    }

    public String getLocalSymbol(String symbol, String exchange, int lastDayBeforeExpiry) throws SQLException, IOException, ClassNotFoundException {
        String localSymbol = null;

        ResultSet resultSet = callProcedure("getLocalSymbol",
                symbol, exchange, lastDayBeforeExpiry);

        while (resultSet.next()) {
            localSymbol = resultSet.getString("m_localSymbol");
        }

        return localSymbol;
    }

    public TimeZone getTimeZone(String symbol) throws SQLException, IOException, ClassNotFoundException {
        TimeZone timeZone = null;

        ResultSet resultSet = callProcedure("getTimeZone",
                symbol);

        while (resultSet.next()) {
            timeZone = DateUtil.getTimeZone(resultSet.getString("timeZone"));
        }

        return timeZone;
    }

    public List<Calendar[]> getTradingHours(String symbol, long since) throws SQLException, IOException, ClassNotFoundException {
        List<Calendar[]> sessions = new ArrayList<Calendar[]>();

        ResultSet resultSet = callProcedure("getTradingHours",
                symbol, since);

        while (resultSet.next()) {
            Calendar[] hours = new Calendar[]{
                getCalendar(resultSet, "open_long"),
                getCalendar(resultSet, "close_long")};

            sessions.add(hours);
        }

        return sessions;
    }

    public TickDataset getTicksData(Contract contract, Calendar since, Calendar end) throws SQLException, ClassNotFoundException {
        return getTicksData(contract, since.getTimeInMillis(), end.getTimeInMillis());
    }

    public TickDataset getTicksData(Contract contract, long since, long end) throws SQLException, ClassNotFoundException {
        if (contract == null || contract.m_conId <= 0) {
            throw new UnsupportedOperationException("Invalid contract.");
        }

        return getTicksData(contract.m_conId, since, end);
    }

    public TickDataset getTicksData(int conId, long since, long end) throws SQLException, ClassNotFoundException {

        TickDataset ticksData = new TickDataset();// localSymbol);//, since, end);
        long tickVolume = 0;
        double tickWap = 0;

        ResultSet resultSet = callProcedure("getTicksData",
                conId, since, end);

        while (resultSet.next()) {
            Tick tick = new Tick();
            tick.setTime(resultSet.getLong("tick_time_long"));
            tick.setPrice(resultSet.getDouble("tick_price"));
            tick.setSize(resultSet.getLong("tick_size"));
            tick.setForcedNew(resultSet.getInt("tick_newBar") != 0);

            tickWap = (tickWap * tickVolume + tick.getPrice() * tick.getSize())
                    / (tickVolume += tick.getSize());
            tick.setDayVolume(tickVolume);
            tick.setDayWap(tickWap);

            ticksData.add(tick);
        }

        return ticksData;
    }

    /**
     * Returns an array of information about backdated sessions for backtesting.
     *
     * @param symbol the contract symbol e.g.: "HSI", "NQ"
     * @param expiryMonths number of months that a contract expiry
     * @param lastDay number of days before expiry to roll over
     * @param backdatedTime backdated time in milliseconds
     * @return an array with columns: "m_localSymbol", "timeZone", "day_open",
     * "day_close", "break_start", "break_end"
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public Object[][] getBackdatedSessions(String symbol, int expiryMonths, int lastDay, long backdatedTime) throws SQLException, ClassNotFoundException {
        List<Object[]> al = new ArrayList<Object[]>();

        ResultSet resultSet = callProcedure("getBackDays",
                symbol, expiryMonths, lastDay, backdatedTime);

        while (resultSet.next()) {
            al.add(new Object[]{
                        resultSet.getString("m_localSymbol"),
                        resultSet.getString("timeZone"),
                        resultSet.getLong("day_open"),
                        resultSet.getLong("day_close"),
                        resultSet.getLong("break_start"),
                        resultSet.getLong("break_end")
                    });
        }

        return al.toArray(new Object[0][0]);
    }
//    public ArrayList<TickDataset> getHistData(String symbol, int expiryMonths, int lastDay, long backDateTime) throws SQLException, IOException, ClassNotFoundException {
//        ArrayList<TickDataset> ticksDatas = new ArrayList();
//
//        ResultSet resultSet = callProcedure("getBackdatedSessions",
//                symbol, expiryMonths, lastDay, backDateTime);
//
////        ResultSet resultSet = executeQueryFromScript(
////                new File(Main.scriptsFolder + "tradingDaysQuery.sql"),
////                new String[]{
////                    dataSchema,
////                    symbol,
////                    String.valueOf((int) (backDateTime / 1000))});
////            System.out.println(symbol + " " + expiryMonths + " " + backDateTime);
//
////        ArrayList<Object[]> al = new ArrayList<Object[]>();
//
//        while (resultSet.next()) {
////            System.out.println(resultSet.getString("m_localSymbol"));
////            System.out.print(" " + resultSet.getString("open_local"));
////            System.out.print(" " + resultSet.getString("close_local"));
//
////            al.add(new Object[]{resultSet.getInt("m_conId"),
////                        resultSet.getString("m_localSymbol"),
////                        resultSet.getLong("open_long"),
////                        resultSet.getLong("close_long")});
//
//            ticksDatas.add(getTicksData(
//                    resultSet.getInt("m_conId"),
//                    resultSet.getString("m_localSymbol"),
//                    resultSet.getLong("open_long"),
//                    resultSet.getLong("close_long")));
//        }
//
//
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//        }).start();
//
//
////        for (int i = 0; i < ticksDatas.size(); i++) {
////            insertBacktestDay(i, ticksDatas.get(i));
////        }
//
//        return ticksDatas;
//    }
//
//    public ArrayList<TickDataset> getHistData(ArrayList<Object[]> al) {
//        final ArrayList<TickDataset> ticksDatas = new ArrayList();
//
//        for (final Object[] a : al) {
//            new Thread(new Runnable() {
//
//                @Override
//                public void run() {
//                    try {
//                        ticksDatas.add(getTicksData(
//                                (Integer) a[0],
//                                (String) a[1],
//                                (Long) a[2],
//                                (Long) a[3]));
//                    } catch (SQLException ex) {
//                        Logger.getLogger(DataSchema.class.getName()).log(Level.SEVERE, null, ex);
//                    } catch (ClassNotFoundException ex) {
//                        Logger.getLogger(DataSchema.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            }).start();
//        }
//
//        return ticksDatas;
//    }
//    
}
