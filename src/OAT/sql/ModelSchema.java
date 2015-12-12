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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import OAT.trading.Trade;
import OAT.util.TextUtil;

/**
 *
 * @author Antonio Yip
 */
public class ModelSchema extends SqlConnect {

    @Override
    public void createTables() throws SQLException, IOException, ClassNotFoundException {
        runScriptFromResources("createModelTables.sql");
    }

    @Override
    public void createProcedures() throws SQLException, IOException, ClassNotFoundException {
        runScriptFromResources("createModelProcedures.sql");
    }

    public void insertTrade(Trade trade) throws SQLException, ClassNotFoundException {
        if (trade == null) {
            return;
        }

        callProcedure("insertTrade",
                trade.getContractDetails().m_summary.m_conId,
                trade.getSide().sign,
                trade.getEnterTime(),
                trade.getEnterPrice(),
                trade.getExitTime(),
                trade.getExitPrice());
    }

    public void populateTickPatterns(String symbol, int size) throws SQLException, IOException, ClassNotFoundException {
//        callProcedure("createTrainingSetTable", size * 2);

        ResultSet conIds = callProcedure("getConIds", symbol);

        while (conIds.next()) {
            int conId = conIds.getInt("m_conId");
//            System.out.println(conId);

            ResultSet trades = callProcedure("getTrades", conId);

            while (trades.next()) {
                long enterTime = trades.getLong("enter_time_long");
                int tradeId = trades.getInt("trade_Id");

//                System.out.println(" " + enterTime);

//            double enterPrice = trades.getDouble("enter_price");

                ResultSet tickPattern = callProcedure("getTickPattern", conId, enterTime, size);

                List<Double> values = new ArrayList<Double>();

                while (tickPattern.next()) {
                    values.add(tickPattern.getDouble(1));
//                    values.add(tickPattern.getDouble(2));
                }

//                System.out.println(TextUtil.toString(values,"...\n"));

//                callProcedure("insertTickPattern", values.toArray());
                callProcedure("insertTickPattern",
                        tradeId,
                        values.size(),
                        TextUtil.toString(values, ","));
            }
        }
    }

    public void dropTrades(String symbol) throws SQLException, ClassNotFoundException {
        callProcedure("dropTrades", symbol);
    }
}
