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
public enum Side {

    LONG(1, "BUY", "BOUGHT", "BOT", "LONG"),
    SHORT(-1, "SELL", "SOLD", "SLD", "SHORT"),
    NEUTRAL(0, "", "", "", "");
    public final int sign;
    public final String actionName;
    public final String executionName;
    public final String executionSimpleName;
    public final String positionName;

    Side(int sign, String action, String executionName, String executionSimpleName, String positionName) {
        this.sign = sign;
        this.actionName = action;
        this.executionName = executionName;
        this.executionSimpleName = executionSimpleName;
        this.positionName = positionName;
    }

    public static Side valueOfAction(String action) {
        for (Side side : Side.values()) {
            if (side.actionName.equalsIgnoreCase(action)
                    || side.executionName.equalsIgnoreCase(action)
                    || side.executionSimpleName.equalsIgnoreCase(action)
                    || side.positionName.equalsIgnoreCase(action)
                    || String.valueOf(side.sign).equals(action)) {
                return side;
            }
        }

        return NEUTRAL;
    }

    public static Side valueOf(int position) {
        return valueOf(Double.valueOf(position));
    }

    public static Side valueOf(double number) {
        return valueOf(number, 0);
    }

    public static Side valueOf(double number, double significance) {
        if (number > Math.max(0, significance)) {
            return LONG;
        } else if (number < -Math.max(0, significance)) {
            return SHORT;
        } else {
            return NEUTRAL;
        }
    }

    public Side reverse() {
        if (this == LONG) {
            return SHORT;
        } else if (this == SHORT) {
            return LONG;
        } else {
            return NEUTRAL;
        }
    }

    public Side add(Side side) {
        return valueOf(sign + side.sign);
    }
}
