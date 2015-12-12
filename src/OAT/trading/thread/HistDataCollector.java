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

import com.ib.client.EWrapperMsgGenerator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import OAT.trading.Main;
import OAT.util.DateUtil;
import OAT.util.FileUtil;
import OAT.util.TextUtil;

/**
 *
 * @author Antonio Yip
 */
public class HistDataCollector extends BaseThread {

    final long STEP = DateUtil.DAY_TIME;
    final long WAIT = 3000;
    final long ERROR_WAIT = DateUtil.MINUTE_TIME / 2;
    final String DELIMITER = ",";
    final String SPACER = "_";
    private boolean reqHistData;
    private StringBuffer stringBuffer;
    private int dayCount = 0;
    private int steps;
    private String barSize;
    private String folder;
    private String day;
    private String symbol;

    @Override
    public String getDefaultNodeName() {
        return "Data";
    }

    @Override
    public int getThreadId() {
        return 9999;
    }

    @Override
    public String getDefaultLoggerName() {
        return "Data";
    }

    @Override
    public String getChildrenNodeName() {
        return null;
    }

    @Override
    protected void preExit() {
    }

    @Override
    public void postWakeUp() {
    }

    @Override
    public void postConnection() {
    }

    @Override
    public void preSleep() {
    }

    public void reqHistoricalData(String symbol, String type, String exchange,
            String currency, String expiry, String strike, String right,
            String barSize, String duration, String endDate, int steps,
            String folder) {

        this.symbol = symbol;
        this.steps = steps;
        this.barSize = barSize;
        this.folder = folder;

        dayCount = 0;

        if (reqHistData) {
            return;
        }

        long endDateL = DateUtil.getTime(endDate);

        while (dayCount < steps) {
            stringBuffer = new StringBuffer();

            if (!DateUtil.isWeekend(DateUtil.getCalendarDate(endDateL))) {
                String endDateS = DateUtil.getTimeStamp(
                        endDateL,
                        "yyyyMMdd") + " 23:59:59 EST";

                day = endDateS.substring(0, 8);

                reqHistData = true;

                throw new UnsupportedOperationException("code not finished.");
//                reqHistoricalData(
//                        REQ_HISTORICAL_DATA_I,
//                        createContract(symbol, type, exchange, currency),
//                        endDateS,
//                        duration,
//                        barSize,
//                        "TRADES", 1, 2);
//                
//                getDataClient().reqHistoricalData(this, primaryChart, endDateL, duration);


//                new Waiting(WAIT, Main.p_Wait_Interval, logger) {
//
//                    @Override
//                    public boolean waitWhile() {
//                        return isReqHistData();
//                    }
//
//                    @Override
//                    public void retry() {
//                    }
//
//                    @Override
//                    public String message() {
//                        return "Requesting historical data...";
//                    }
//
//                    @Override
//                    public void timeout() {
//                        logger.warning("No data for " + day);
//                    }
//                };
            }

//            if (pacingError) {
//                getDataClient().cancelHistoricalData(this, primaryChart);
//
//                new Waiting(ERROR_WAIT, Main.p_Wait_Interval, logger) {
//
//                    @Override
//                    public boolean waitWhile() {
//                        return isReqHistData();
//                    }
//
//                    @Override
//                    public void retry() {
//                    }
//
//                    @Override
//                    public String message() {
//                        return "Pause due to pacing error...";
//                    }
//
//                    @Override
//                    public void timeout() {
//                    }
//                };
//
//                pacingError = false;
//            } else {
//                endDateL -= STEP;
//            }
        }
    }

    public void historicalData(int reqId, String date, double open, double high, double low, double close, int volume, int count, double WAP, boolean hasGaps) {
        if (date.contains("finished")) {
            dayCount++;
            reqHistData = false;
//            pacingError = false;

            log(Level.INFO, "{0} days download.", dayCount);

            if (folder == null || folder.isEmpty()) {
                folder = FileUtil.getFolder(
                        Main.dataFolder, TextUtil.convertSymbol(symbol));
            }

            File file = new File(folder + File.separator
                    + TextUtil.convertSymbol(symbol) + SPACER
                    + barSize.replace(" ", "") + SPACER
                    + day
                    + ".csv");

            try {
                FileUtil.saveToFile(stringBuffer.toString(), file);
            } catch (IOException ex) {
                log(Level.SEVERE, null, ex);
            }

            if (dayCount == steps) {
                log(Level.INFO, "Finished");

                exit();
            }

            return;
        }

        stringBuffer.append("\n");
        stringBuffer.append(TextUtil.toString(
                new Object[]{date, open, high, low, close, volume, count},
                DELIMITER));

        log(Level.INFO, EWrapperMsgGenerator.historicalData(reqId, date, open, high, low, close, volume, count, WAP, hasGaps));
    }

    private void saveToFile(String string, String file) {
        FileWriter fw = null;

        try {
            fw = new FileWriter(file);
            PrintWriter out = new PrintWriter(fw);

            out.print(string);
            out.close();
        } catch (IOException ex) {
            log(Level.SEVERE, null, ex);
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                log(Level.SEVERE, null, ex);
            }
        }
    }
}
