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

package org.ardverk.collection.spt.ints;

import java.io.Serializable;

/**
 * An abstract implementation of {@link IntTrie}.
 */
abstract class AbstractIntTrie implements IntTrie, Serializable {
    
    private static final long serialVersionUID = 7235114905641948930L;

    /**
     * Returns an {@link Entry} for the given key or {@code null} if no 
     * such entry exists.
     */
    Entry entry(int key) {
        Entry entry = select(key);
        if (entry != null && equals(key, entry.getKey())) {
            return entry;
        }
        return null;
    }
    
    @Override
    public int get(int key) {
        Entry entry = entry(key);
        return entry != null ? entry.getValue() : -1;
    }
    
    @Override
    public void putAll(IntMap m) {
        for (Entry entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }
    
    @Override
    public boolean containsKey(int key) {
        return entry(key) != null;
    }
    
    @Override
    public boolean containsValue(final int value) {
        final boolean[] contains = { false };
        
        traverse(new Cursor() {
            @Override
            public boolean select(Entry entry) {
                if (AbstractIntTrie.equals(value, entry.getValue())) {
                    contains[0] = true;
                    return false;
                }
                return true;
            }
        });
        
        return contains[0];
    }
    
    @Override
    public int selectKey(int key) {
        Entry entry = select(key);
        return entry != null ? entry.getKey() : null;
    }
    
    @Override
    public int selectValue(int key) {
        Entry entry = select(key);
        return entry != null ? entry.getValue() : null;
    }
    
    @Override
    public int firstKey() {
        Entry entry = firstEntry();
        return entry != null ? entry.getKey() : null;
    }
    
    @Override
    public int lastKey() {
        Entry entry = lastEntry();
        return entry != null ? entry.getKey() : null;
    }
    
    @Override
    public Entry pollFirstEntry() {
        Entry entry = firstEntry();
        if (entry != null) {
            remove(entry.getKey());
        }
        return entry;
    }

    @Override
    public Entry pollLastEntry() {
        Entry entry = lastEntry();
        if (entry != null) {
            remove(entry.getKey());
        }
        return entry;
    }
    
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(getClass().getSimpleName()).append("[").append(size()).append("]={\n");
        
        traverse(new Cursor() {
            @Override
            public boolean select(Entry entry) {
                buffer.append("    ").append(entry).append("\n");
                return true;
            }
        });
        
        buffer.append("}\n");
        return buffer.toString();
    }
    
    /**
     * Returns {@code true} if the two values are equal.
     */
    static boolean equals(int o1, int o2) {
        return o1 == o2;
    }
}
