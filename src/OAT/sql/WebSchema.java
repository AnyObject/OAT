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

import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import java.io.IOException;
import java.sql.SQLException;
import OAT.util.MathUtil;

/**
 *
 * @author Antonio Yip
 */
public class WebSchema extends SqlConnect {

    @Override
    public void createTables() throws SQLException, IOException, ClassNotFoundException {
        runScriptFromResources("createWebTables.sql");
    }

    @Override
    public void createProcedures() throws SQLException, IOException, ClassNotFoundException {
        runScriptFromResources("createWebProcedures.sql");
    }

    public void insertWebSymbol(int id, String localSymbol) throws SQLException, ClassNotFoundException {
        executeUpdate(
                "REPLACE LOW_PRIORITY INTO `web`.`symbols`"
                + " (`ID`, `Symbol`)"
                + " VALUES("
                + SqlUtil.formatValues(id, localSymbol)
                + ");");
    }

    private void insertWebExecution(int id, Object... values) throws SQLException, ClassNotFoundException {
        executeUpdate(
                "INSERT LOW_PRIORITY IGNORE INTO `web`.`executions`"
                + " (`ID`, `Action`, `Size`, `Price`, `execTime`, `m_execId`, `m_orderId`, `m_acctNumber`)"
                + " VALUES("
                + id + ", "
                + SqlUtil.formatValues(values)
                + ");");
    }

    public void insertWebExecution(int id, Execution execution) throws SQLException, ClassNotFoundException {
        insertWebExecution(id,
                execution.m_side,
                execution.m_shares,
                execution.m_price,
                execution.m_time,
                //                orderRecord == null ? 0 : orderRecord.getCommission(),
                execution.m_execId.contains("U+") || execution.m_execId.contains("F-")
                ? execution.m_execId.substring(0, execution.m_execId.length() - 3)
                : execution.m_execId,
                execution.m_orderId,
                execution.m_acctNumber);
    }

    public void updateWebCommission(int id, Order order, OrderState orderState) throws SQLException, ClassNotFoundException {
        if (order == null || orderState == null) {
            return;
        }

        if (MathUtil.isValid(orderState.m_commission)) {
            executeUpdate(
                    "UPDATE LOW_PRIORITY IGNORE `web`.`executions` "
                    + " SET commission = " + orderState.m_commission
                    + " WHERE ID = " + id
                    + " AND m_orderId = " + order.m_orderId
                    + " ;");
        }
    }

    public void dropWebExecutions(int id, long beforeTime) throws SQLException, ClassNotFoundException {
        callProcedure("clearWebExecutions", id, beforeTime);
    }

    public void clearWebQuote(int id) throws SQLException, ClassNotFoundException {
        callProcedure("clearWebQuote", id);
    }

    public void updateWebTable(String table, int id, String field, Object value) throws SQLException, ClassNotFoundException {
        executeBatch(
                "INSERT LOW_PRIORITY IGNORE INTO `web`.`" + table + "`"
                + " SET `ID` = " + id + ";",
                "UPDATE IGNORE `web`.`" + table + "`"
                + " SET `" + field + "` = "
                + SqlUtil.formatValues(value)
                + " WHERE ID = " + id
                + ";");
    }
}
