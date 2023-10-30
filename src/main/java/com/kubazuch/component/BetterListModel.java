package com.kubazuch.component;

import javax.swing.*;
import java.util.List;
import java.util.Iterator;
import java.util.ListIterator;

public class BetterListModel<E> extends AbstractListModel<E> implements Iterable<E> {
    private final List<E> collection;

    public BetterListModel(List<E> collection) {
        this.collection = collection;
    }

    @Override
    public Iterator<E> iterator() {
        return collection.iterator();
    }

    @Override
    public int getSize() {
        return collection.size();
    }

    @Override
    public E getElementAt(int index) {
        return collection.get(index);
    }

    public void add(E element) {
        int index = collection.size();
        collection.add(element);
        fireIntervalAdded(this, index, index);
    }

    public ListIterator<E> listIterator() {
        return collection.listIterator();
    }

    public ListIterator<E> listIterator(int index) {
        return collection.listIterator(index);
    }

    public void add(int index, E value) {
        collection.add(index, value);
        fireIntervalAdded(this, index, index);
    }

    public void remove(int index) {
        collection.remove(index);
        fireIntervalRemoved(this, index, index);
    }

    public boolean remove(E value) {
        int index = indexOf(value);
        boolean rv = collection.remove(value);
        if (index >= 0) {
            fireIntervalRemoved(this, index, index);
        }
        return rv;
    }

    private int indexOf(E value) {
        return collection.indexOf(value);
    }

    public void addFirst(E value) {
        collection.add(0, value);
        fireIntervalAdded(this, 0, 0);
    }

    public void clear() {
        int index = collection.size();
        collection.clear();
        fireIntervalRemoved(this, 0, index);
    }
}
