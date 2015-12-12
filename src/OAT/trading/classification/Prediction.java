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

package OAT.trading.classification;

/**
 *
 * @author Antonio Yip
 */
public enum Prediction {

    WIN(1),
    LOSS(-1),
    INSIGNIFICANT(0),
    NOT_APPLICABLE(0);
    public final int sign;

    Prediction(int sign) {
        this.sign = sign;
    }

    public static Prediction valueOf(int position) {
        return valueOf(Double.valueOf(position));
    }

    public static Prediction valueOf(double number) {
        return valueOf(number, 0);
    }

    public static Prediction valueOf(double number, double significance) {
        if (number > Math.max(0, significance)) {
            return WIN;
        } else if (number < -Math.max(0, significance)) {
            return LOSS;
        } else {
            return INSIGNIFICANT;
        }
    }

    public Prediction add(Prediction prediction) {
        return valueOf(sign + prediction.sign);
    }
}
