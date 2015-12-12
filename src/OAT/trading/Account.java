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

package OAT.trading;

/**
 *
 * @author Antonio Yip
 */
public enum Account {

    BACKTEST("data", "model", "backtest"),
    CASH("data", "model", "cash"),
    PAPER("data", "model", "paper"),
    DATA("data", "model", ""),
    DEMO("demo", "demo", "demo");
    public final String backtestSchema = "backtest";
    public final String dataSchema;
    public final String modelSchema;
    public final String tradingSchema;
    public final String webSchema = "web";

    Account(String data, String model, String trading) {
        this.dataSchema = data;
        this.modelSchema = model;
        this.tradingSchema = trading;
    }
}
