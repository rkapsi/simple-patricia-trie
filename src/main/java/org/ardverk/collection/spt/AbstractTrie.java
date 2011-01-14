/*
 * Copyright 2011 Roger Kapsi
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.ardverk.collection.spt;

import java.io.Serializable;
import java.util.Map;

/**
 * An abstract implementation of {@link Trie}.
 */
abstract class AbstractTrie<K, V> implements Trie<K, V>, Serializable {
    
    private static final long serialVersionUID = 7235114905641948930L;

    @Override
    public V get(Object key) {
        @SuppressWarnings("unchecked")
        Map.Entry<K, V> node = select((K)key);
        if (node != null && equals(node.getKey(), key)) {
            return node.getValue();            
        }
        
        return null;
    }
    
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }
    
    @Override
    public boolean containsKey(Object key) {
        @SuppressWarnings("unchecked")
        Map.Entry<K, V> entry = select((K)key);
        return entry != null && equals(key, entry.getKey());
    }
    
    @Override
    public boolean containsValue(final Object value) {
        final boolean[] contains = { false };
        
        traverse(new Cursor<K, V>() {
            @Override
            public boolean select(Entry<? extends K, ? extends V> entry) {
                if (AbstractTrie.equals(value, entry.getValue())) {
                    contains[0] = true;
                    return false;
                }
                return true;
            }
        });
        
        return contains[0];
    }
    
    @Override
    public K selectKey(K key) {
        Map.Entry<K, V> entry = select(key);
        return entry != null ? entry.getKey() : null;
    }
    
    @Override
    public V selectValue(K key) {
        Map.Entry<K, V> entry = select(key);
        return entry != null ? entry.getValue() : null;
    }
    
    @Override
    public K firstKey() {
        Map.Entry<K, V> entry = firstEntry();
        return entry != null ? entry.getKey() : null;
    }
    
    @Override
    public K lastKey() {
        Map.Entry<K, V> entry = lastEntry();
        return entry != null ? entry.getKey() : null;
    }
    
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(getClass().getName()).append("[").append(size()).append("]={\n");
        
        traverse(new Cursor<K, V>() {
            @Override
            public boolean select(Map.Entry<? extends K, ? extends V> entry) {
                buffer.append("    ").append(entry).append("\n");
                return true;
            }
        });
        
        buffer.append("}\n");
        return buffer.toString();
    }
    
    /**
     * Returns {@code true} if the two objects are equal.
     */
    static boolean equals(Object o1, Object o2) {
        if (o1 == null) {
            return (o2 == null);
        }
        
        return o1.equals(o2);
    }
}