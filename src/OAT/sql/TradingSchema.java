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
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import OAT.trading.OrderRecord;
import OAT.util.DateUtil;

/**
 *
 * @author Antonio Yip
 */
public class TradingSchema extends SqlConnect {

    @Override
    public void createProcedures() throws SQLException, IOException, ClassNotFoundException {
        runScriptFromResources("createTradingProcedures.sql");
    }

    @Override
    public void createTables() throws SQLException, IOException, ClassNotFoundException {
        runScriptFromResources("createTradingTables.sql");
    }

    public void insertExecution(Contract contract, Execution execution) throws SQLException, ClassNotFoundException {
        executeUpdate(
                "REPLACE INTO `executions`"
                + " (m_execId, m_permId, m_conId, m_orderId, m_clientId,  m_time,"
                + " m_acctNumber, m_exchange, m_side, m_shares, m_price,"
                + " m_liquidation, m_cumQty, m_avgPrice, exe_time_long)"
                + " VALUES("
                + SqlUtil.formatValues(
                execution.m_execId,
                execution.m_permId,
                contract.m_conId,
                execution.m_orderId,
                execution.m_clientId,
                execution.m_time,
                execution.m_acctNumber,
                execution.m_exchange,
                execution.m_side,
                execution.m_shares,
                execution.m_price,
                execution.m_liquidation,
                execution.m_cumQty,
                execution.m_avgPrice,
                DateUtil.getTime(execution.m_time))
                + " );");
    }

    public void insertOrder(Contract contract, Order order, OrderState orderState, boolean enterOrder) throws SQLException, ClassNotFoundException {
        executeBatch(
                "INSERT INTO `orders`"
                + " (m_permId, m_conId, m_orderId, m_clientId, m_action, m_totalQuantity,"
                + " m_orderType, m_lmtPrice, m_auxPrice, m_tif, m_ocaGroup, m_ocaType,"
                + " m_transmit, m_parentId, m_triggerMethod, m_outsideRth, m_goodAfterTime,"
                + " m_goodTillDate, m_allOrNone, m_minQty, m_volatilityType, m_trailStopPrice,"
                + " m_whatIf, enterOrder)"
                + " VALUES("
                + SqlUtil.formatValues(
                order.m_permId,
                contract.m_conId,
                order.m_orderId,
                order.m_clientId,
                order.m_action,
                order.m_totalQuantity,
                order.m_orderType,
                order.m_lmtPrice,
                order.m_auxPrice,
                order.m_tif,
                order.m_ocaGroup,
                order.m_ocaType,
                order.m_transmit,
                order.m_parentId,
                order.m_triggerMethod,
                order.m_outsideRth,
                order.m_goodAfterTime,
                order.m_goodTillDate,
                order.m_allOrNone,
                order.m_minQty,
                order.m_volatilityType,
                order.m_trailStopPrice,
                order.m_whatIf,
                enterOrder)
                + " ) ON DUPLICATE KEY UPDATE modified_time_long = UNIX_TIMESTAMP() * 1000"
                + ";",
                "UPDATE `orders` SET"
                + "  m_status =" + SqlUtil.formatValues(orderState.m_status)
                + ", m_initMargin =" + SqlUtil.formatValues(orderState.m_initMargin)
                + ", m_maintMargin =" + SqlUtil.formatValues(orderState.m_maintMargin)
                + ", m_equityWithLoan =" + SqlUtil.formatValues(orderState.m_equityWithLoan)
                + ", m_commission =" + SqlUtil.formatValues(orderState.m_commission)
                + ", m_minCommission =" + SqlUtil.formatValues(orderState.m_minCommission)
                + ", m_maxCommission =" + SqlUtil.formatValues(orderState.m_maxCommission)
                + ", m_commissionCurrency =" + SqlUtil.formatValues(orderState.m_commissionCurrency)
                + ", m_warningText =" + SqlUtil.formatValues(orderState.m_warningText)
                + " WHERE m_permId =" + order.m_permId
                + " AND (m_status != 'Filled' OR m_commission IS NULL);",
                "UPDATE `orders` SET"
                + " submitted_time_long = UNIX_TIMESTAMP() * 1000"
                + " WHERE m_permId = " + order.m_permId
                + " AND submitted_time_long IS NULL;");
    }

    public void updateOrderStatus(int orderId, String status, int filled, int remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld) throws SQLException, ClassNotFoundException {
        executeUpdate(
                "UPDATE `orders` SET"
                + "  m_status = " + SqlUtil.formatValues(status)
                + ", filled = " + SqlUtil.formatValues(filled)
                + ", remaining = " + SqlUtil.formatValues(remaining)
                + ", avgFillPrice = " + SqlUtil.formatValues(avgFillPrice)
                + ", m_parentId = " + SqlUtil.formatValues(parentId)
                + ", lastFillPrice = " + SqlUtil.formatValues(lastFillPrice)
                + ", whyHeld = " + SqlUtil.formatValues(whyHeld)
                + ", modified_time_long = UNIX_TIMESTAMP() * 1000"
                + " WHERE m_permId = " + permId + ";");
    }

    public void updateOrderStatus(int clientId, int orderId, String status) throws SQLException, ClassNotFoundException {
        executeUpdate(
                "UPDATE `orders` SET"
                + " m_status = " + SqlUtil.formatValues(status)
                + ", modified_time_long = UNIX_TIMESTAMP() * 1000"
                + " WHERE m_clientId = " + clientId
                + " AND m_orderId = " + orderId
                + " AND m_status LIKE '%Submit%';");
    }

//    /**
//     *
//     * @param permId
//     * @param stopPrice
//     * @param trailPrice
//     * @throws SQLException
//     * @throws ClassNotFoundException
//     * @deprecated
//     */
//    public void updateOrderPrices(int permId, double stopPrice, double trailPrice) throws SQLException, ClassNotFoundException {
//        executeUpdate(
//                "UPDATE `orders` SET"
//                + " stopPrice = " + SqlUtil.formatValues(stopPrice)
//                + ", trailPrice = " + SqlUtil.formatValues(trailPrice)
//                + ", modified_time_long = UNIX_TIMESTAMP() * 1000"
//                + " WHERE m_permId = " + permId + ";");
//    }

    public int getCurrentPosition(int clientId, int conId, long since, String acctPrefix) throws SQLException, ClassNotFoundException {
        int position = Integer.MAX_VALUE;

        ResultSet resultSet = callProcedure("getCurrentPosition",
                conId, since, acctPrefix);

        while (resultSet.next()) {
            position = resultSet.getInt("position");
        }

        return position;
    }

    public double getCurrentValue(int clientId, int conId, long since, String acctPrefix) throws SQLException, ClassNotFoundException {
        double value = 0;

        ResultSet resultSet = callProcedure("getCurrentValue",
                conId, since, acctPrefix);

        while (resultSet.next()) {
            value = resultSet.getDouble("value");
        }

        return value;
    }

    public List<Execution> getExecutions(int clientId, int conId, long since) throws SQLException, ParseException, ClassNotFoundException {
        List<Execution> executions = new ArrayList<Execution>();

        ResultSet resultSet = callProcedure("getExecutions",
                conId, since, "");

        while (resultSet.next()) {
            Execution execution = new Execution();

            execution.m_execId = resultSet.getString("m_execId");
            execution.m_permId = resultSet.getInt("m_permId");
            execution.m_clientId = resultSet.getInt("m_clientId");
            execution.m_orderId = resultSet.getInt("m_orderId");
            execution.m_time = DateUtil.getTimeStamp(
                    resultSet.getLong("exe_time_long"), "yyyyMMdd  HH:mm:ss");
//            execution.m_time = DateUtil.convertDateString(
//                    resultSet.getString("m_time"),
//                    "yyyy-MM-dd HH:mm:ss",
//                    "yyyyMMdd  HH:mm:ss");
            execution.m_acctNumber = resultSet.getString("m_acctNumber");
            execution.m_exchange = resultSet.getString("m_exchange");
            execution.m_side = resultSet.getString("m_side");
            execution.m_shares = resultSet.getInt("m_shares");
            execution.m_price = resultSet.getDouble("m_price");
            execution.m_liquidation = resultSet.getInt("m_liquidation");
            execution.m_cumQty = resultSet.getInt("m_cumQty");
            execution.m_avgPrice = resultSet.getDouble("m_avgPrice");

            executions.add(execution);
        }

        return executions;
    }

    public void getLastFilledOrder(int clientId, int conId, long since, OrderRecord orderRecord) throws SQLException, IOException, ClassNotFoundException {
        ResultSet resultSet = callProcedure("getLastFilledOrder",
                clientId, conId, since);

        while (resultSet.next()) {
            reloadOrderRecord(resultSet, orderRecord);
        }
    }

    public void getSubmittedOrder(int clientId, int conId, long since, OrderRecord orderRecord) throws SQLException, IOException, ClassNotFoundException {
        ResultSet resultSet = callProcedure("getSubmittedOrder",
                clientId, conId, since);

        while (resultSet.next()) {
            reloadOrderRecord(resultSet, orderRecord);
        }
    }

    protected static void reloadOrderRecord(ResultSet resultSet, OrderRecord orderRecord) throws SQLException {
        Order order = new Order();

        order.m_clientId = resultSet.getInt("m_clientId");
        order.m_orderId = resultSet.getInt("m_orderId");
        order.m_permId = resultSet.getInt("m_permId");
        order.m_action = resultSet.getString("m_action");
        order.m_totalQuantity = resultSet.getInt("m_totalQuantity");
        order.m_auxPrice = resultSet.getDouble("m_auxPrice");
        order.m_lmtPrice = resultSet.getDouble("m_lmtPrice");
        order.m_orderType = resultSet.getString("m_orderType");

        orderRecord.setOrder(order, false);

        orderRecord.setOrderStatus(
                resultSet.getString("m_status"),
                resultSet.getInt("filled"),
                resultSet.getInt("remaining"),
                resultSet.getDouble("avgFillPrice"),
                resultSet.getDouble("lastFillPrice"),
                resultSet.getString("whyHeld"),
                false);

//        orderRecord.setStopPrice(resultSet.getDouble("stopPrice"), false);
//        orderRecord.setTrailPrice(resultSet.getDouble("trailPrice"), false);
        orderRecord.setEnterOrder(resultSet.getBoolean("enterOrder"));
        orderRecord.setSubmittedTime(resultSet.getLong("submitted_time_long"));
    }
}
