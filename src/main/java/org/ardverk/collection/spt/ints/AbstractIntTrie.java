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

    @Override
    public int get(int key) {
        IntMap.Entry node = select(key);
        if (node != null && equals(node.getKey(), key)) {
            return node.getValue();            
        }
        
        return -1;
    }
    
    @Override
    public void putAll(IntMap m) {
        for (IntMap.Entry entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }
    
    @Override
    public boolean containsKey(int key) {
        IntMap.Entry entry = select(key);
        return entry != null && equals(key, entry.getKey());
    }
    
    @Override
    public boolean containsValue(final int value) {
        final boolean[] contains = { false };
        
        traverse(new Cursor() {
            @Override
            public boolean select(IntMap.Entry entry) {
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
        IntMap.Entry entry = select(key);
        return entry != null ? entry.getKey() : null;
    }
    
    @Override
    public int selectValue(int key) {
        IntMap.Entry entry = select(key);
        return entry != null ? entry.getValue() : null;
    }
    
    @Override
    public int firstKey() {
        IntMap.Entry entry = firstEntry();
        return entry != null ? entry.getKey() : null;
    }
    
    @Override
    public int lastKey() {
        IntMap.Entry entry = lastEntry();
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
        
        traverse(new Cursor() {
            @Override
            public boolean select(IntMap.Entry entry) {
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
