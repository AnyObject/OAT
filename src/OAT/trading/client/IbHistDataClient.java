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

package OAT.trading.client;

import com.google.common.collect.HashBiMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import OAT.data.Bar;
import OAT.data.BarDataset;
import OAT.trading.thread.BaseThread;
import OAT.trading.thread.DataThread;
import OAT.util.DateUtil;

/**
 *
 * @author Antonio Yip
 */
public class IbHistDataClient extends IbClient implements HistDataClient {

    private Map<Integer, DataThread> dataThreadMap = new HashMap<Integer, DataThread>();
    private HashBiMap<Integer, BarDataset> chartMap = HashBiMap.create();

    public IbHistDataClient(BaseThread baseThread) {
        super(baseThread);
    }

    @Override
    public int getClientId() {
        return 40;
    }

    /**
     * Request historical data.
     *
     * @param dataThread
     * @param chart
     * @param end
     * @param duration
     */
    @Override
    public void reqHistoricalData(final DataThread dataThread, final BarDataset chart, final long end, final long duration) {
        connect();

//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
                if (chart == null) {
                    return;
                }


                int reqId = REQ_HISTORICAL_DATA_I + dataThread.getThreadId();

                if (chartMap.containsValue(chart)) {
                    return;
                }

                while (chartMap.containsKey(reqId)) {
                    reqId++;
                }

                chartMap.put(reqId, chart);
                dataThreadMap.put(reqId, dataThread);

                dataThread.log(Level.FINE, "reqHistoricalData #{0}: {1} {2}",
                        new Object[]{
                            String.valueOf(reqId),
                            chart.getTitle(),
                            DateUtil.getHistDataDurationStr(duration)});

                eClientSocket.reqHistoricalData(
                        reqId,
                        dataThread.getContract(),
                        DateUtil.getTimeStamp(end, "yyyyMMdd HH:mm:ss"),
                        DateUtil.getHistDataDurationStr(duration),
                        DateUtil.getHistDataBarSizeStr(chart.getBarSize()),
                        "TRADES", 1, 2);
//            }
//        }).start();
    }

    @Override
    public void cancelHistoricalData(DataThread dataThread, BarDataset chart) {
        if (!eClientSocket.isConnected()) {
            return;
        }

        int reqId = chartMap.inverse().get(chart);

        eClientSocket.cancelHistoricalData(reqId);
        chartMap.remove(reqId);
    }

    //
    //com.ib.client.Ewrapper
    //
    @Override
    public void error(final int id, final int errorCode, final String errorMsg) {
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
                DataThread dataThread = dataThreadMap.get(id);
                int typeId = getReqTypeId(id);

                if (errorCode == 322) { // Duplicate Id
                    logError(Level.WARNING, id, errorCode, errorMsg);

                } else {
                    switch (typeId) {
                        case REQ_HISTORICAL_DATA_I:
                            logError(Level.WARNING, id, errorCode, errorMsg);

                            BarDataset reqChart = chartMap.get(id);

                            if (reqChart != null) {
                                dataThread.log(Level.WARNING,
                                        "Error getting historical data: {0}",
                                        reqChart.getTitle());
                            }

                            chartMap.remove(id);
                            return;

                        default:
                            unhandledError(id, errorCode, errorMsg);
                    }
                }
//            }
//        }).start();
    }

    @Override
    public void historicalData(final int reqId, final String date, final double open, final double high, final double low, final double close, final int volume, final int count, final double wap, final boolean hasGaps) {
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
                try {
                    DataThread dataThread = dataThreadMap.get(reqId);

                    if (dataThread != null) {
                        BarDataset reqChart = chartMap.get(reqId);

                        if (reqChart != null) {
                            if (date.contains("finished")) {
                                reqChart.fireDatasetChanged();
                                dataThread.log(Level.FINE,
                                        "histData #{0}: {1}",
                                        new Object[]{
                                            String.valueOf(reqId),
                                            reqChart});
                                chartMap.remove(reqId);

                            } else {
                                Bar newBar = new Bar(
                                        Long.parseLong(date) * 1000,
                                        open, high, low, close,
                                        volume, wap, count);

                                reqChart.add(newBar, false);
                            }
                        }
                    }
                } catch (Exception e) {
                    log(Level.SEVERE, null, e);
                }
//            }
//        }).start();

        super.historicalData(reqId, date, open, high, low, close, volume, count, wap, hasGaps);
    }
}
