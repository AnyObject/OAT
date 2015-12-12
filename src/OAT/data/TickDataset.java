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

import java.util.Collection;
import java.util.Iterator;
import OAT.util.OrderedList;

/**
 *
 * @author Antonio Yip
 */
public class TickDataset implements PriceDataSet<Tick>, Comparable<TickDataset> {

    private int newBarCount;
    private OrderedList<Tick> ticks = new OrderedList<Tick>();

    public TickDataset() {
    }

    public TickDataset(Tick tick) {
        this.ticks.add(tick);
    }

    public TickDataset(Collection<Tick> ticks) {
        this.ticks.addAll(ticks);
    }

    @Override
    public boolean add(Tick tick) {
        if (tick.isForcedNew()) {
            newBarCount++;
        }

        return ticks.add(tick);
    }

    @Override
    public synchronized void clear() {
        ticks.clear();
    }

    @Override
    public Tick getFirst() {
        if (isEmpty()) {
            return null;
        }

        return ticks.getFirst();
    }

    @Override
    public Tick getLast() {
        if (isEmpty()) {
            return null;
        }

        return ticks.getLast();
    }

    @Override
    public boolean isEmpty() {
        return ticks.isEmpty();
    }

    public long getDayVolume() {
        if (getLast() == null) {
            return 0;
        }

        return getLast().getDayVolume();
    }

    public int getNewBarCount() {
        if (!ticks.isEmpty() && newBarCount > 0) {
            return newBarCount;
        }

        // only if newBarCount is invalid
        newBarCount = 0;
        for (Tick tick : ticks) {
            if (tick.isForcedNew()) {
                newBarCount++;
            }
        }
        return newBarCount;
    }

    @Override
    public int compareTo(TickDataset o) {
        if (isEmpty()) {
            return 0;
        }

        return getFirst().compareTo(o.getFirst());
    }

    @Override
    public Iterator<Tick> iterator() {
        return ticks.iterator();
    }

    @Override
    public int size() {
        return ticks.size();
    }
}
