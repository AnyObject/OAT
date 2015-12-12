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

package OAT.data;

/**
 * Interface for Open-High-Low-Close bar.
 *
 * @author Antonio Yip
 */
public interface OHLC extends Price {

    /**
     * Get close price.
     *
     * @return can be -1 or Double.NaN if not available
     */
    public abstract double getClose();

    /**
     * Get last updated time.
     *
     * @return UNIX time in milliseconds
     */
    public abstract long getCloseTime();

    /**
     * Get high price.
     *
     * @return can be -1 or Double.NaN if not available
     */
    public abstract double getHigh();

    /**
     * Get time that recorded the high price.
     *
     * @return UNIX time in milliseconds
     */
    public abstract long getHighTime();

    /**
     * Get low price.
     *
     * @return can be -1 or Double.NaN if not available
     */
    public abstract double getLow();

    /**
     * Get time that recorded the low price.
     *
     * @return UNIX time in milliseconds
     */
    public abstract long getLowTime();

    /**
     * Get open price.
     *
     * @return can be -1 or Double.NaN if not available
     */
    public abstract double getOpen();

    /**
     * Get time that recorded the open price.
     *
     * @return UNIX time in milliseconds
     */
    public abstract long getOpenTime();

    /**
     * Get number of ticks of the bar.
     *
     * @return number of ticks
     */
    public abstract int getTickCount();

    /**
     * Get volume of the bar.
     *
     * @return volume > 0
     */
    public abstract long getVolume();

    /**
     * Get weighted average price of the bar.
     *
     * @return can be -1 or Double.NaN if not available
     */
    public abstract double getWap();

    @Override
    public abstract String toString();
}
