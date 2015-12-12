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

package OAT.util;

import java.util.*;

/**
 * A list that items are stored orderly in a linked list.
 *
 * @param <E>
 * @author Antonio Yip
 */
public class OrderedList<E extends Comparable<E>> implements Collection<E>, List<E> {

    private LinkedList<E> list = new LinkedList<E>();
//    private LinkedList<E> descendingList = new LinkedList<E>();

    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

    public Iterator<E> descendingIterator() {
        return list.descendingIterator();
    }

    @Override
    public ListIterator<E> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return list.listIterator(index);
    }

    /**
     * Inserts the specified element into the list preserving the list's order.
     *
     * @param element element to be inserted
     * @return
     */
    @Override
    public boolean add(E element) {
        ListIterator<E> iterator = listIterator(list.size());

        while (iterator.hasPrevious()) {
            E e = iterator.previous();

            int compare = element.compareTo(e);

            if (compare > 0) {
//                descendingList.add(list.size() - 1 - list.indexOf(e), element);
                iterator.next();
                iterator.add(element);
                return true;

            } else if (compare == 0) {
//                descendingList.set(descendingList.indexOf(e), element); 
                iterator.remove();
                iterator.add(element);
                return true;
            }
        }

        //No item <= obj, add obj to the front of theList
//        descendingList.offerLast(element);
        iterator.add(element);
        return true;
    }

    @Override
    public void clear() {
//        descendingList.clear();
//        list.clear();
        list = new LinkedList<E>();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public E get(int index) {
        return list.get(index);
    }

    public E getFirst() {
        return list.getFirst();
    }

    public E getLast() {
        return list.getLast();
    }

    public E getPrevious(E element) {
        int i = indexOf(element);

        if (i < 0) {
            return null;
        }

        return list.listIterator(i).previous();
    }

    public E getNext(E element) {
        int i = indexOf(element);

        if (i < 0) {
            return null;
        }

        return list.listIterator(i + 1).next();
    }

    public void removeBefore(E element) {
        for (Iterator<E> it = this.descendingIterator(); it.hasNext();) {
            E e = it.next();

            if (e.compareTo(element) < 0) {
//                descendingList.remove(e);
                it.remove();
            }
        }
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

//    public List<E> descendingSubList(int fromIndex, int toIndex) {
//        return descendingList.subList(fromIndex, toIndex);
//    }
//
//    public List<E> getDescendingList() {
//        return descendingList;
//    }
    public boolean hasPrevious(E element) {
        int i = indexOf(element);

        if (i < 0) {
            return false;
        }

        return list.listIterator(i).hasPrevious();
    }

    public boolean hasNext(E element) {
        int i = indexOf(element);

        if (i < 0) {
            return false;
        }

        return list.listIterator(i + 1).hasNext();
    }

    public boolean remove(E element) {
        if (element == null) {
            return false;
        }

//        descendingList.remove(element);
        return list.remove(element);
    }

    @Override
    public int size() {
        return list.size();
    }

    public int indexOf(E element) {
        return list.indexOf(element);
    }

    @Override
    public E[] toArray() {
        return (E[]) list.toArray();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (E e : list) {
            sb.append(e.toString());
            sb.append(TextUtil.LINE_SEPARATOR);
        }
        return sb.toString();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
//        descendingList.remove(o);
        return list.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean b = false;

        for (E e : c) {
            b |= add(e);
        }

        return b;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
//        descendingList.removeAll(c);
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
//        descendingList.retainAll(c);
        return list.retainAll(c);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public E remove(int index) {
//        descendingList.remove(list.size() - 1 - index);
        return list.remove(index);
    }

    @Override
    public void add(int index, E element) {
//        descendingList.add(list.size() - 1 - index, element);
        list.add(index, element);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
//        descendingList.addAll(list.size() - 1 - index, c);
        return list.addAll(index, c);
    }

    @Override
    public E set(int index, E element) {
//        descendingList.set(list.size() - 1 - index, element);
        return list.set(index, element);
    }
}
