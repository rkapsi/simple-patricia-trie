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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class PatriciaIntTrie extends AbstractIntTrie implements Serializable {
    
    private static final long serialVersionUID = 7464215084236615537L;
    
    private final RootNode root = new RootNode();
    
    private int size = 0;
    
    private transient volatile Set<IntMap.Entry> entrySet = null;
    
    private transient volatile int[] keySet = null;
    
    private transient volatile int[] values = null;
    
    public PatriciaIntTrie() {
    }
    
    public PatriciaIntTrie(IntMap m) {
        putAll(m);
    }
    
    @Override
    public IntMap.Entry select(int key) {
        IntMap.Entry node = selectR(root.left, key, -1);
        if (node != root || !root.empty) {
            return node;
        }
        return null;
    }
    
    private Node selectR(Node h, int key, int bitIndex) {
        if (h.bitIndex <= bitIndex) { 
            return h; 
        }
        
        if (!isSet(key, h.bitIndex)) {
            return selectR(h.left, key, h.bitIndex);
        } else {
            return selectR(h.right, key, h.bitIndex);
        }
    }
    
    @Override
    public int put(int key, int value) {
        // This is a shortcut! The root is the only place to store null! 
        if (key == 0) {
            return putForNullKey(key, value);
        }
        
        IntMap.Entry node = select(key);
        int existing = -1;
        if (node != null) {
            existing = node.getKey();
            if (equals(key, existing)) {
                return node.setValue(value);
            }
        }
        
        int bitIndex = bitIndex(key, existing);
        root.left = putR(root.left, key, value, bitIndex, root);
        incrementSize();
        
        return -1;
    }
    
    private int putForNullKey(int key, int value) {
        if (root.empty) {
            incrementSize();
        }
        
        return root.setKeyValue(key, value);
    }
    
    private Node putR(Node h, int key, int value, int bitIndex, Node p) {
        if ((h.bitIndex >= bitIndex) || (h.bitIndex <= p.bitIndex)) {
            Node t = new Node(key, value, bitIndex);
            
            boolean isSet = isSet(key, t.bitIndex);
            t.left = isSet ? h : t;
            t.right = isSet ? t : h;
            return t;
        }
        
        if (!isSet(key, h.bitIndex)) {
            h.left = putR(h.left, key, value, bitIndex, h);
        } else {
            h.right = putR(h.right, key, value, bitIndex, h);
        }
        return h;
    }
    
    @Override
    public int remove(int key) {
        final IntMap.Entry entry = select(key);
        if (entry != null && equals(key, entry.getKey())) {
            
            // We can take a shortcut for the root Node!
            if (entry == root) {
                decrementSize();
                return root.unsetKeyValue();
            }
            
            final List<IntMap.Entry> entries 
                = new ArrayList<IntMap.Entry>(size()-1);
            
            traverse(new Cursor() {
                @Override
                public boolean select(IntMap.Entry e) {
                    if (entry != e) {
                        entries.add(e);
                    }
                    return true;
                }
            });
            
            clear();
            for (int i = entries.size()-1; i >= 0; --i) {
                IntMap.Entry e = entries.get(i);
                put(e.getKey(), e.getValue());
            }
            
            return entry.getValue();
        }
        
        return -1;
    }
    
    @Override
    public void select(int key, Cursor cursor) {
        selectR(root.left, key, cursor, -1);
    }
    
    private boolean selectR(Node h, int key, Cursor cursor, int bitIndex) {
        
        if (h.bitIndex <= bitIndex) {
            if (h != root || !root.empty) {
                return cursor.select(h); 
            }
            return true;
        }
        
        if (!isSet(key, h.bitIndex)) {
            if (selectR(h.left, key, cursor, h.bitIndex)) {
                return selectR(h.right, key, cursor, h.bitIndex);
            }
        } else {
            if (selectR(h.right, key, cursor, h.bitIndex)) {
                return selectR(h.left, key, cursor, h.bitIndex);
            }
        }
        
        return false;
    }
    
    @Override
    public void traverse(Cursor cursor) {
        traverseR(root.left, cursor, -1);
    }
    
    private boolean traverseR(Node h, Cursor cursor, int bitIndex) {
        
        if (h.bitIndex <= bitIndex) {
            if (h != root || !root.empty) {
                return cursor.select(h); 
            }
            return true;
        }
        
        if (traverseR(h.left, cursor, h.bitIndex)) {
            return traverseR(h.right, cursor, h.bitIndex);
        }
        return false;
    }
    
    @Override
    public void clear() {
        root.clear();
        
        size = 0;
        clearViews();
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public Set<IntMap.Entry> entrySet() {
        if (entrySet == null) {
            final Set<IntMap.Entry> entries 
                = new ArrayListSet<IntMap.Entry>(size());
            
            traverse(new Cursor() {
                @Override
                public boolean select(IntMap.Entry entry) {
                    entries.add(entry);
                    return true;
                }
            });
            
            entrySet = Collections.unmodifiableSet(entries);
        }
        return entrySet;
    }
    
    @Override
    public int[] keySet() {
        if (keySet == null) {
            
            final int[] index = { 0 };
            final int[] entries = new int[size()];
            
            traverse(new Cursor() {
                @Override
                public boolean select(IntMap.Entry entry) {
                    entries[index[0]++] = entry.getKey();
                    return true;
                }
            });
            
            keySet = entries;
        }
        return keySet;
    }
    
    @Override
    public int[] values() {
        if (values == null) {
            
            final int[] index = { 0 };
            final int[] entries = new int[size()];
            
            traverse(new Cursor() {
                @Override
                public boolean select(IntMap.Entry entry) {
                    entries[index[0]++] = entry.getValue();
                    return true;
                }
            });
            
            values = entries;
        }
        return values;
    }
    
    
    @Override
    public IntMap.Entry firstEntry() {
        IntMap.Entry entry = followLeft(root.left, -1, root);
        if (entry != root || !root.empty) {
            return entry;
        }
        return null;
    }

    @Override
    public IntMap.Entry lastEntry() {
        IntMap.Entry entry = followRight(root.left, -1);
        if (entry != root || !root.empty) {
            return entry;
        }
        return null;
    }
    
    private IntMap.Entry followLeft(Node h, int bitIndex, Node p) {
        if (h.bitIndex <= bitIndex) {
            if (h != root || !root.empty) {
                return h;
            }
            return p;
        }
        
        return followLeft(h.left, h.bitIndex, h);
    }
    
    private IntMap.Entry followRight(Node h, int bitIndex) {
        if (h.bitIndex <= bitIndex) {
            return h;
        }
        
        return followRight(h.right, h.bitIndex);
    }
    
    /**
     * Increments the {@link #size} counter and calls {@link #clearViews()}.
     */
    private void incrementSize() {
        ++size;
        clearViews();
    }
    
    /**
     * Decrements the {@link #size} counter and calls {@link #clearViews()}.
     */
    private void decrementSize() {
        --size;
        clearViews();
    }
    
    /**
     * Clears the various views as returned by {@link #entrySet()}, 
     * {@link #keySet()} and {@link #values()}.
     */
    private void clearViews() {
        entrySet = null;
        keySet = null;
        values = null;
    }
    
    private static final int MSB = 1 << Integer.SIZE-1;
    
    private static boolean isSet(int key, int bitIndex) {
        int mask = MSB >>> bitIndex;
        return (key & mask) != 0;
    }
    
    private static int bitIndex(int key, int otherKey) {
        int xorValue = key ^ otherKey;
        for (int i = 0; i < Integer.SIZE; i++) {
            int mask = MSB >>> i;
            if ((xorValue & mask) != 0) {
                return i;
            }
        }
        
        throw new IllegalStateException();
    }
    
    /**
     * The root node of the {@link IntTrie}.
     */
    private static class RootNode extends Node {
        
        private static final long serialVersionUID = -8857149853096688620L;
        
        private boolean empty = true;
        
        public RootNode() {
            super(-1, -1, -1);
            this.left = this;
        }
        
        /**
         * Sets the key and value of the root node.
         */
        public int setKeyValue(int key, int value) {
            this.key = key;
            this.empty = false;
            return setValue(value);
        }
        
        /**
         * Unsets the key and value of the root node.
         */
        public int unsetKeyValue() {
            this.key = -1;
            this.empty = true;
            return setValue(-1);
        }
        
        /**
         * Clears the root node and effectively also the {@link IntTrie}.
         */
        public void clear() {
            unsetKeyValue();
            this.left = this;
        }
    }
    
    /**
     * A node in the {@link IntTrie}.
     */
    private static class Node implements IntMap.Entry, Serializable {
        
        private static final long serialVersionUID = -2409938371345117780L;

        private final int bitIndex;

        protected int key;
        
        protected int value;
        
        protected Node left;
        
        protected Node right;
        
        private Node(int key, int value, int bitIndex) {
            this.bitIndex = bitIndex;
            this.key = key;
            this.value = value;
        }

        @Override
        public int getKey() {
            return key;
        }

        @Override
        public int getValue() {
            return value;
        }

        @Override
        public int setValue(int value) {
            int existing = this.value;
            this.value = value;
            return existing;
        }
        
        @Override
        public String toString() {
            return key + " (" + bitIndex + ") -> " + value;
        }
    }
    
    /**
     * An {@link ArrayList} that implements the {@link Set} interface.
     */
    private static class ArrayListSet<K> extends ArrayList<K> implements Set<K> {
        
        private static final long serialVersionUID = -1036159554753667259L;

        public ArrayListSet(int initialCapacity) {
            super(initialCapacity);
        }
    }
}
