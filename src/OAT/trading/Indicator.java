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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import OAT.data.Bar;
import OAT.data.Chartable;
import OAT.event.Listenable;

/**
 *
 * @author Antonio Yip
 */
public class Indicator extends Listenable implements Chartable<Indicator> {

    private Bar bar;
    private boolean sessionBreak;
    private List<Double> values;

    public Indicator(Bar bar, Double... values) {
        this.bar = bar;
        this.values = new ArrayList<Double>(Arrays.asList(values));
    }

    public Bar getBar() {
        return bar;
    }

    public long getTime() {
        return bar.getTime();
    }

    public double getValue(int series) {
        if (values == null || series >= values.size()) {
            return Double.NaN;
        }

        return values.get(series);
    }

    public List<Double> getValues() {
        return values;
    }

    public boolean isSessionBreak() {
        return sessionBreak;
    }

    public void setValues(Collection<Double> values) {
        this.values = new ArrayList<Double>(values);

        fireChanged();
    }

    public void setValue(int series, double value) {
        if (values == null) {
            values = new ArrayList<Double>();
        }

        while (series >= values.size()) {
            values.add(Double.NaN);
        }

        values.set(series, value);

        fireChanged();
    }

    public void setSessionBreak(boolean sessionBreak) {
        this.sessionBreak = sessionBreak;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof Indicator)) {
            return false;
        }

        Indicator theOther = (Indicator) obj;

        if (!this.bar.equals(theOther.bar)) {
            return false;
        }

        return true;
    }

    @Override
    public int compareTo(Indicator o) {
        return (int) (bar.getTime() - o.getTime());
    }

    @Override
    public double getX() {
        return getTime();
    }

    @Override
    public double getX(int series) {
        return getTime();
    }

    @Override
    public double getY(int series) {
        return getValue(series);
    }
}
