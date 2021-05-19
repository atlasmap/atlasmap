/*
 * Copyright (C) 2017 Oracle
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.xml.xsom.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * {@link Collection} that returns the view of objects which are actually fetched
 * lazily from an {@link Iterator}.
 *
 * @author Kohsuke Kawaguchi
 */
public class DeferedCollection<T> implements Collection<T> {
    /**
     * The iterator that lazily evaluates SCD query.
     */
    private final Iterator<T> result;

    /**
     * Stores values that are already fetched from {@link #result}.
     */
    private final List<T> archive = new ArrayList<T>();

    public DeferedCollection(Iterator<T> result) {
        this.result = result;
    }

    public boolean isEmpty() {
        if(archive.isEmpty())
            fetch();
        return archive.isEmpty();
    }

    public int size() {
        fetchAll();
        return archive.size();
    }

    public boolean contains(Object o) {
        if(archive.contains(o))
            return true;
        while(result.hasNext()) {
            T value = result.next();
            archive.add(value);
            if(value.equals(o))
                return true;
        }
        return false;
    }

    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if(!contains(o))
                return false;
        }
        return true;
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int idx=0;
            public boolean hasNext() {
                if(idx<archive.size())
                    return true;
                return result.hasNext();
            }

            public T next() {
                if(idx==archive.size())
                    fetch();
                if(idx==archive.size())
                    throw new NoSuchElementException();
                return archive.get(idx++);
            }

            public void remove() {
                // TODO
            }
        };
    }

    public Object[] toArray() {
        fetchAll();
        return archive.toArray();
    }

    public <T>T[] toArray(T[] a) {
        fetchAll();
        return archive.toArray(a);
    }



    private void fetchAll() {
        while(result.hasNext())
            archive.add(result.next());
    }

    /**
     * Fetches another item from {@link
     */
    private void fetch() {
        if(result.hasNext())
            archive.add(result.next());
    }

// mutation methods are unsupported
    public boolean add(T o) {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }
}
