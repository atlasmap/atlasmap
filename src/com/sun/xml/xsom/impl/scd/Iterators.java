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

package com.sun.xml.xsom.impl.scd;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * Various convenient {@link Iterator} implementations.
 * @author Kohsuke Kawaguchi
 */
public class Iterators {

    static abstract class ReadOnly<T> implements Iterator<T> {
        public final void remove() {
            throw new UnsupportedOperationException();
        }
    }

    // we need to run on JDK 1.4
    private static final Iterator EMPTY = Collections.EMPTY_LIST.iterator();

    public static <T> Iterator<T> empty() {
        return EMPTY;
    }

    public static <T> Iterator<T> singleton(T value) {
        return new Singleton<T>(value);
    }

    /**
     * {@link Iterator} that returns a single (or no) value.
     */
    static final class Singleton<T> extends ReadOnly<T> {
        private T next;

        Singleton(T next) {
            this.next = next;
        }

        public boolean hasNext() {
            return next!=null;
        }

        public T next() {
            T r = next;
            next = null;
            return r;
        }
    }

    /**
     * {@link Iterator} that wraps another {@link Iterator} and changes its type.
     */
    public static abstract class Adapter<T,U> extends ReadOnly<T> {
        private final Iterator<? extends U> core;

        public Adapter(Iterator<? extends U> core) {
            this.core = core;
        }

        public boolean hasNext() {
            return core.hasNext();
        }

        public T next() {
            return filter(core.next());
        }

        protected abstract T filter(U u);
    }

    /**
     * For each U, apply U->Iterator&lt;T> function and then iterate all
     * the resulting T.
     */
    public static abstract class Map<T,U> extends ReadOnly<T> {
        private final Iterator<? extends U> core;

        private Iterator<? extends T> current;

        protected Map(Iterator<? extends U> core) {
            this.core = core;
        }

        public boolean hasNext() {
            while(current==null || !current.hasNext()) {
                if(!core.hasNext())
                    return false;   // nothing more to enumerate
                current = apply(core.next());
            }
            return true;
        }

        public T next() {
            return current.next();
        }

        protected abstract Iterator<? extends T> apply(U u);
    }

    /**
     * Filter out objects from another iterator.
     */
    public static abstract class Filter<T> extends ReadOnly<T> {
        private final Iterator<? extends T> core;
        private T next;

        protected Filter(Iterator<? extends T> core) {
            this.core = core;
        }

        /**
         * Return true to retain the value.
         */
        protected abstract boolean matches(T value);

        public boolean hasNext() {
            while(core.hasNext() && next==null) {
                next = core.next();
                if(!matches(next))
                    next = null;
            }

            return next!=null;
        }

        public T next() {
            if(next==null)      throw new NoSuchElementException();
            T r = next;
            next = null;
            return r;
        }
    }

    /**
     * Only return unique items.
     */
    static final class Unique<T> extends Filter<T> {
        private Set<T> values = new HashSet<T>();
        public Unique(Iterator<? extends T> core) {
            super(core);
        }

        protected boolean matches(T value) {
            return values.add(value);
        }
    }

    /**
     * Union of two iterators.
     */
    public static final class Union<T> extends ReadOnly<T> {
        private final Iterator<? extends T> first,second;

        public Union(Iterator<? extends T> first, Iterator<? extends T> second) {
            this.first = first;
            this.second = second;
        }

        public boolean hasNext() {
            return first.hasNext() || second.hasNext();
        }

        public T next() {
            if(first.hasNext())     return first.next();
            else                    return second.next();
        }
    }

    /**
     * Array iterator.
     */
    public static final class Array<T> extends ReadOnly<T> {
        private final T[] items;
        private int index=0;
        public Array(T[] items) {
            this.items = items;
        }

        public boolean hasNext() {
            return index<items.length;
        }

        public T next() {
            return items[index++];
        }
    }
}
