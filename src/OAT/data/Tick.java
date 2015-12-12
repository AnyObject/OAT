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
 *
 * @author Antonio Yip
 */
public class Tick implements Comparable<Tick>, Price, Cloneable {

    private double price = -1;
    private long size = 0;
    private long time = 0; //milliseconds from 1st Jan 1970 GMT
    private long dayVolume = 0;
    private double dayWap = -1; //weighted average price
    private boolean b, forcedNew;

    public Tick() {
    }

    public Tick(double price, long time) {
        this.price = price;
        this.time = time;
    }

    /**
     * Create a new tick with provided data.
     *
     * @param price tick price
     * @param size tick size
     * @param time milliseconds
     * @param volume day volume
     * @param wap day weighted average price
     * @param b undefined IB field
     * @param forcedNew force to open a new bar
     */
    public Tick(double price, long size, long time, long volume, double wap, boolean b, boolean forcedNew) {
        this.price = price;
        this.size = size;
        this.time = time;
        this.dayVolume = volume;
        this.dayWap = wap;
        this.b = b;
        this.forcedNew = forcedNew;
    }

    /**
     * Create a new tick from another tick.
     *
     * @param tick cannot be null
     */
    public Tick(final Tick tick) {
        this.price = tick.price;
        this.size = tick.size;
        this.time = tick.time;
        this.dayVolume = tick.dayVolume;
        this.dayWap = tick.dayWap;
        this.b = tick.b;
        this.forcedNew = tick.forcedNew;
    }

    @Override
    public int compareTo(Tick o) {
        if (o == null) {
            return (int) time;
        }

        return (int) (time - o.time);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof Tick)) {
            return false;
        }

        Tick theOther = (Tick) obj;

        if (this.time == theOther.time
                && this.price == theOther.price
                && this.size == theOther.size
//                && this.dayVolume == theOther.dayVolume
                ) {
            return true;
        }

        return false;
    }
    

    public boolean isB() {
        return b;
    }

    public boolean isForcedNew() {
        return forcedNew;
    }

    public long getDayVolume() {
        return dayVolume;
    }

    public double getDayWap() {
        return dayWap;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public long getTime() {
        return time;
    }

    public void setB(boolean b) {
        this.b = b;
    }

    public void setForcedNew(boolean newBar) {
        this.forcedNew = newBar;
    }

    public void setDayVolume(long volume) {
        this.dayVolume = volume;
    }

    public void setDayWap(double wap) {
        this.dayWap = wap;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
