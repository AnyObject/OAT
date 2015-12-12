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

package OAT.event;

import java.util.logging.Level;

/**
 *
 * @author Antonio Yip
 */
public enum State {

    //connection states
    SERVER_DELAYED(-1, "Server delayed.", Level.WARNING),
    EXITED(0, null),
    //    CONNECTING(1, "Connecting to IB..."),
    INITIALIZED(2, "Initialized."),
    LOADED(3, "Client loaded successfully."),
    //
    //backtest states
    BACKTESTING(90, null, Level.FINER),
    BACKTEST_FINISHED(91, null, Level.FINER),
    //
    //base thread states
    //    INITIALIZING(11, "Initializing..."),
    //    RESTARTING(12, "Restarting..."),
    WAKING_UP(13, "Waking up..."),
    //    PENDING(14, "Pending..."),
    SLEEPING(15, "Going to sleep..."),
    EXITING(16, "Exiting..."),
    //
    //data thread states
    DATA_IS_NOT_SUBSCRIBED(-22, "Market data is not subscribed.", Level.WARNING),
    DATA_BROKEN(-21, "Market data was broken.", Level.WARNING),
    CD_ERROR(-20, "Error getting contract details.", Level.WARNING),
    GETTING_MARKET_DATA(21, "Getting market data."),
    FORCING_NEW_BAR(22, "Forcing new bar.", Level.FINE),
    MARKET_JUST_OPENED(23, "Market just opened."),
    //    OPEN_GAP(24, "Market opened with gap."),
    REQUESTING_CONTRACT_DETAILS(25, "Requesting contract details...", Level.FINE),
    REQUESTING_TRADING_CONTRACT_DETAILS(26, "Requesting trading contract details...", Level.FINE),
    GETTING_REAL_TIME_BAR(27, "Getting real-time bar."),
    //
    //Trading thread states
    TRADING_SUSPENDED(30, "Trading is suspended. Trading is not allowed until next trading session.", Level.WARNING),
    //    PENDING_REVERSE_POSITION(31, "Pending to reverse position...", Level.FINE),
    //    MAX_CONSEC_LOSS(31, "Reached maximum consecutive loss."),
    //    MAX_DAILY_LOSS(32, "Reached daily loss."),
    CLOSING_POSITION(33, "Closing position..."),
    REQUESTING_EXECUTIONS(34, "Requesting executions...", Level.FINE),
    //    REQUESTING_OPEN_ORDERS(35, "Requesting open orders...", Level.FINE),
    PENDING_REQUEST_EXECUTIONS(36, "Pending to request executions...", Level.FINE),
    PENDING_CHECK_POSITION(37, "Pending to check position...", Level.FINE),
    //
    //account thread states
    SUBSCRIBED_ACCOUNT_UPDATE(50, null, Level.FINE);
//    DOWNLOADING_ACCOUNT_UPDATE(51, "Downloading account.");
    //
    public final int code;
    public final String message;
    public Level level;

    private State(int code, String message, Level level) {
        this.code = code;
        this.message = message;
        this.level = level;
    }

    State(int code, String message) {
        this(code, message, Level.INFO);
    }
}
