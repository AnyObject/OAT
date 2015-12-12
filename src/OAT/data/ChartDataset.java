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

import java.util.*;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYDataset;
import OAT.event.GenericListener;
import OAT.event.Listenable;
import OAT.ui.util.Timeline;
import OAT.util.OrderedList;
import OAT.util.Tabulable;

/**
 * Abstract class for basic chart.
 *
 * @param <E>
 * @author Antonio Yip
 */
public abstract class ChartDataset<E extends Chartable<E>> extends Listenable implements XYDataset, PriceDataSet<E>, Collection<E>, Tabulable, GenericListener {

    protected TimeZone timeZone;
    protected Timeline timeline;
    private boolean backtest;
    private DatasetGroup group = new DatasetGroup();
    private OrderedList<E> items = new OrderedList<E>();
    private LinkedList<E> descendingItems = new LinkedList<E>();

    /**
     * Return the chart title.
     *
     * @return
     */
    public abstract String getTitle();

    /**
     * Return the data series keys.
     *
     * @return
     */
    public abstract String[] getKeys();

    /**
     * Return the value ranges of axis.
     *
     * @return
     */
    public abstract Double[] getAxisRange();

    /**
     * Return the parent object.
     *
     * @return
     */
    public abstract Object getSource();

    @Override
    public String[] getColumnHeaders() {
        return getKeys();
    }

    @Override
    public Object[] getRow(int index) {
        Object[] row = new Object[getSeriesCount()];

        if (index < getItemCount()) {
            for (int i = 0; i < row.length; i++) {
                row[i] = getY(i, index);
            }
        }

        return row;
    }

    /**
     *
     * @param bar
     * @return
     */
    public abstract Object[] getRow(Bar bar);

    /**
     *
     * @return
     */
    public boolean isBacktest() {
        return backtest;
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     *
     * @param backtest
     */
    public void setBacktest(boolean backtest) {
        this.backtest = backtest;
    }

    @Override
    public boolean add(E e) {
        return add(e, true);
    }

    @Override
    public synchronized boolean addAll(Collection<? extends E> c) {
        boolean b = false;

        for (E e : c) {
            b |= add(e, false);
        }

        fireDatasetChanged();

        return b;
    }

    /**
     * Add an item to this chart.
     *
     * @param e
     * @param notify flag to notify dataChangeListener
     * @return
     */
    public synchronized boolean add(E e, boolean notify) {
        boolean b = items.add(e);

        if (b) {
            if (e instanceof Listenable) {
                ((Listenable) e).addChangeListener(this);
            }

            descendingItems.offerFirst(e);

            if (notify) {
                fireDatasetChanged();
            }
        }

        return b;
    }

    @Override
    public boolean contains(Object o) {
        return items.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return items.containsAll(c);
    }

    @Override
    public synchronized boolean remove(Object o) {
        descendingItems.remove(o);
        boolean b = items.remove(o);

        if (b) {
            fireDatasetChanged();
        }

        return b;
    }

    @Override
    public synchronized boolean removeAll(Collection<?> c) {
        descendingItems.removeAll(c);
        boolean b = items.removeAll(c);

        if (b) {
            fireDatasetChanged();
        }

        return b;
    }

    @Override
    public synchronized boolean retainAll(Collection<?> c) {
        descendingItems.retainAll(c);
        boolean b = items.retainAll(c);

        if (b) {
            fireDatasetChanged();
        }

        return b;
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public Object[] toArray() {
        return items.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return items.toArray(a);
    }

    @Override
    public synchronized void clear() {
        descendingItems.clear();
        items.clear();

        fireDatasetChanged();
    }

    public synchronized void removeLast() {
        if (!items.isEmpty()) {
            descendingItems.remove(descendingItems.getFirst());
            items.remove(items.getLast());
        }

        fireDatasetChanged();
    }

    /**
     *
     * @param before
     */
    public synchronized void clear(long before) {
//        int index = 0;
//
//        for (E e : descendingItems) {
//            if (Double.isNaN(e.getX()) || e.getX() < before) {
//                break;
//            }
//
//            index++;
//        }
//        
//        List<E> subList = descendingItems.subList(index, descendingItems.size());
//
//        items.removeAll(subList);
//        descendingItems.removeAll(subList);

        for (Iterator<E> it = items.descendingIterator(); it.hasNext();) {
            E e = it.next();
            if (Double.isNaN(e.getX()) || e.getX() < before) {
                descendingItems.remove(e);
                it.remove();
            }
        }

        fireDatasetChanged();
    }

    public E get(int index) {
        return items.get(index);
    }

    @Override
    public E getFirst() {
        if (items.isEmpty()) {
            return null;
        }

        return items.getFirst();
    }

    @Override
    public E getLast() {
        if (items.isEmpty()) {
            return null;
        }

        return items.getLast();
    }

    public List<E> getAll() {
        return items;
    }

    @Override
    public Iterator<E> iterator() {
        return items.iterator();
    }

    public E getPrev(E e) {
        if (e == null) {
            return null;
        }

        if (items.hasPrevious(e)) {
            return items.getPrevious(e);
        } else {
            return null;
        }
    }

    /**
     * Get descending items that synchronise to the backed items.
     *
     * @return
     */
    public OrderedList<E> getAscendingItems() {
        return items;
    }

    /**
     * Get descending items that synchronise to the backed items.
     *
     * @return
     */
    public LinkedList<E> getDescendingItems() {
        return descendingItems;
    }

    @Override
    public int getItemCount(int series) {
        return getItemCount();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public DomainOrder getDomainOrder() {
        return DomainOrder.NONE;
    }

    @Override
    public DatasetGroup getGroup() {
        return group;
    }

    @Override
    public void setGroup(DatasetGroup group) {
        if (group == null) {
            throw new IllegalArgumentException("Null 'group' argument.");
        }
        this.group = group;
    }

    @Override
    public Number getX(int series, int item) {
        return items.get(item).getX(series);
    }

    @Override
    public double getXValue(int series, int item) {
        return getX(series, item).doubleValue();
    }

    @Override
    public Number getY(int series, int item) {
        return items.get(item).getY(series);
    }

    @Override
    public double getYValue(int series, int item) {
        return getY(series, item).doubleValue();
    }

    @Override
    public int getSeriesCount() {
        if (getKeys() == null) {
            return 0;
        }

        return getKeys().length;
    }

    @Override
    public Comparable getSeriesKey(int series) {
        return getKeys()[series];
    }

    /**
     * Returns the time line.
     *
     * @return
     */
    public Timeline getTimeline() {
        return timeline;
    }

    /**
     * Set time line.
     *
     * @param timeline
     */
    public void setTimeline(Timeline timeline) {
        this.timeline = timeline;
    }

    /**
     * Returns index of the element.
     *
     * @param e
     * @return
     */
    public int indexOf(E e) {
        return items.indexOf(e);
    }

    @Override
    public int indexOf(Comparable seriesKey) {
        for (int s = 0; s < getSeriesCount(); s++) {
            if (getSeriesKey(s).equals(seriesKey)) {
                return s;
            }
        }
        return -1;
    }

    @Override
    public void eventHandler(EventObject event) {
        fireDatasetChanged();
    }

    @Override
    public void addChangeListener(DatasetChangeListener listener) {
        super.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(DatasetChangeListener listener) {
        super.removeChangeListener(listener);
    }

    /**
     * Fire ChartDataset Changed.
     *
     */
    public void fireDatasetChanged() {
        notifyListeners(new DatasetChangeEvent(this, this));
    }
}
