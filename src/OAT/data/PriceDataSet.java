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
 * @param <T>
 * @author Antonio Yip
 */
public interface PriceDataSet<T extends Comparable<T>> extends Iterable<T> {

    /**
     * Add an element to the dataset.
     *
     * @param o 
     * @return  
     */
    public boolean add(T o);

//    /**
//     * Add all elements in the list to the dataset.
//     *
//     * @param list a list of <@link Chartable>.
//     * @return  
//     */
//    public boolean addAll(Iterable<T> list);

    /**
     * Clear all elements.
     *
     */
    public void clear();

    /**
     * Test if the dataset is empty.
     *
     * @return
     */
    public boolean isEmpty();

    /**
     * Returns size of the dataset.
     * 
     * @return
     */
    public int size() ;

//    /**
//     * Return the element of index.
//     *
//     * @param index
//     * @return
//     */
//    public T get(int index);

    /**
     * Return the First element.
     *
     * @return
     */
    public T getFirst();

    /**
     * Return the last element.
     *
     * @return
     */
    public T getLast();
//
//    /**
//     * Return a list of all elements.
//     *
//     * @return
//     */
//    public List<T> getAll();

//    /**
//     * Return the number of items.
//     *
//     * @return
//     */
//    public int getItemCount();
    /**
     * Return an iterator of the items.
     *
     * @return
     * @deprecated
     */
//    public ListIterator<T> iterator();
    /**
     * Return an iterator of the items in descending order.
     *
     * @return
     * @deprecated
     */
//    public Iterator<T> descendingIterator();
}
