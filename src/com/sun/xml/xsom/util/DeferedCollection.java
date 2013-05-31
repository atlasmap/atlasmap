/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
