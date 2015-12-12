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

import OAT.trading.Main;
import OAT.trading.thread.BaseThread;
import OAT.trading.thread.DataThread;
import OAT.util.ThreadLogger;

/**
 *
 * @author Antonio Yip
 */
public class BacktestDataClient extends IbDataClient {

    public BacktestDataClient(BaseThread baseThread) {
        super(baseThread);
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void connect() {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public ThreadLogger getLogger() {
        return Main.backtestThread.getLogger();
    }

    @Override
    public String getLogPrefix() {
        return "";
    }

    @Override
    public int getClientId() {
        return 920;
    }

    @Override
    public void reqMktData(DataThread dataThread) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void cancelMktData(DataThread dataThread) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void reqContractDetails(DataThread dataThread) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void reqRealTimeBars(DataThread dataThread) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void cancelRealTimeBars(DataThread dataThread) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
