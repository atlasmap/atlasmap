/*
 * Copyright (C) 2017 Red Hat, Inc.
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
package io.atlasmap.expression.internal;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A Simple LRU Cache.
 * 
 * @param <K> key
 * @param <V> value
 */

public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = -342098639681884413L;
    protected int maxCacheSize = 10000;

    /**
     * Default constructor for an LRU Cache The default capacity is 10000.
     */
    public LRUCache() {
        this(0,10000, 0.75f, true);
    }

    /**
     * Constructs a LRUCache with a maximum capacity.
     * 
     * @param maximumCacheSize maximum cache size
     */
    public LRUCache(int maximumCacheSize) {
        this(0, maximumCacheSize, 0.75f, true);
    }

    /**
     * Constructs an empty {@code LRUCache} instance with the specified
     * initial capacity, maximumCacheSize,load factor and ordering mode.
     * 
     * @param initialCapacity the initial capacity.
     * @param maximumCacheSize maximum cache size
     * @param loadFactor the load factor.
     * @param accessOrder the ordering mode - {@code true} for access-order,
     *                {@code false} for insertion-order.
     * @throws IllegalArgumentException if the initial capacity is negative or
     *                 the load factor is non-positive.
     */

    public LRUCache(int initialCapacity, int maximumCacheSize, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor, accessOrder);
        this.maxCacheSize = maximumCacheSize;
    }

    /**
     * @return Returns the maxCacheSize.
     */
    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    /**
     * @param maxCacheSize The maxCacheSize to set.
     */
    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        if( size() > maxCacheSize ) {
            onCacheEviction(eldest);
            return true;
        }
        return false;
    }

    protected void onCacheEviction(Map.Entry<K,V> eldest) {
    }
}
