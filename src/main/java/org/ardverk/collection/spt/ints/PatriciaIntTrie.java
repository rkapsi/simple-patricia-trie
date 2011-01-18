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

import org.ardverk.collection.spt.IntegerKeyAnalyzer;
import org.ardverk.collection.spt.KeyAnalyzer;

public class PatriciaIntTrie extends AbstractIntTrie implements Serializable {
    
    private static final long serialVersionUID = 7464215084236615537L;
    
    private volatile RootNode root = new RootNode();
    
    private volatile int size = 0;
    
    private transient volatile Entry[] entrySet = null;
    
    private transient volatile int[] keySet = null;
    
    private transient volatile int[] values = null;
    
    public PatriciaIntTrie() {
    }
    
    public PatriciaIntTrie(IntMap m) {
        putAll(m);
    }
    
    @Override
    public Entry select(int key) {
        Entry entry = selectR(root.left, key, -1);
        if (entry != root || !root.empty) {
            return entry;
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
        
        Entry entry = select(key);
        int existing = 0;
        if (entry != null) {
            existing = entry.getKey();
            if (equals(key, existing)) {
                return entry.setValue(value);
            }
        }
        
        int bitIndex = bitIndex(key, existing);
        if (bitIndex == KeyAnalyzer.NULL_KEY) {
            return putForNullKey(key, value);
        }
        
        assert (bitIndex >= 0);
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
        final Entry entry = entry(key);
        if (entry != null) {
            
            // We can take a shortcut for the root Node!
            if (entry == root) {
                decrementSize();
                return root.unsetKeyValue();
            }
            
            // We're traversing the old Trie and 
            // adding elements to the new Trie!
            RootNode old = clear0();
            traverse(old, new Cursor() {
                @Override
                public boolean select(Entry e) {
                    if (entry != e) {
                        put(e.getKey(), e.getValue());
                    }
                    return true;
                }
            });
            
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
        traverse(root, cursor);
    }
    
    private static void traverse(RootNode root, Cursor cursor) {
        traverseR(root, root.left, cursor, -1);
    }
    
    private static boolean traverseR(RootNode root, Node h, Cursor cursor, int bitIndex) {
        
        if (h.bitIndex <= bitIndex) {
            if (h != root || !root.empty) {
                return cursor.select(h); 
            }
            return true;
        }
        
        if (traverseR(root, h.left, cursor, h.bitIndex)) {
            return traverseR(root, h.right, cursor, h.bitIndex);
        }
        return false;
    }
    
    @Override
    public void clear() {
        clear0();
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public Entry[] entrySet() {
        if (entrySet == null) {
            final Entry[] entries = new Entry[size()];
            
            traverse(new Cursor() {
                
                private int index = 0;
                
                @Override
                public boolean select(Entry entry) {
                    entries[index++] = entry;
                    return true;
                }
            });
            
            entrySet = entries;
        }
        return entrySet;
    }
    
    @Override
    public int[] keySet() {
        if (keySet == null) {
            final int[] entries = new int[size()];
            
            traverse(new Cursor() {
                
                private int index = 0;
                
                @Override
                public boolean select(Entry entry) {
                    entries[index++] = entry.getKey();
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
            
            final int[] entries = new int[size()];
            
            traverse(new Cursor() {
                
                private int index = 0;
                
                @Override
                public boolean select(Entry entry) {
                    entries[index++] = entry.getValue();
                    return true;
                }
            });
            
            values = entries;
        }
        return values;
    }
    
    
    @Override
    public Entry firstEntry() {
        Entry entry = followLeft(root.left, -1, root);
        if (entry != root || !root.empty) {
            return entry;
        }
        return null;
    }

    @Override
    public Entry lastEntry() {
        Entry entry = followRight(root.left, -1);
        if (entry != root || !root.empty) {
            return entry;
        }
        return null;
    }
    
    private Entry followLeft(Node h, int bitIndex, Node p) {
        if (h.bitIndex <= bitIndex) {
            if (h != root || !root.empty) {
                return h;
            }
            return p;
        }
        
        return followLeft(h.left, h.bitIndex, h);
    }
    
    private Entry followRight(Node h, int bitIndex) {
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
     * Clears the {@link PatriciaIntTrie} and returns the old {@link RootNode}.
     * The {@link RootNode} may be used to {@link #traverse(RootNode, Cursor)}
     * the old {@link PatriciaIntTrie}.
     * 
     * @see #remove(Object)
     */
    private RootNode clear0() {
        RootNode previous = root;
        
        root = new RootNode();
        size = 0;
        clearViews();
        
        return previous;
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
    
    /**
     * @see IntegerKeyAnalyzer#isSet(int, int)
     */
    private static boolean isSet(int key, int bitIndex) {
        return IntegerKeyAnalyzer.INSTANCE.isSet(key, bitIndex);
    }
    
    /**
     * @see IntegerKeyAnalyzer#bitIndex(int, int)
     */
    private static int bitIndex(int key, int otherKey) {
        return IntegerKeyAnalyzer.INSTANCE.bitIndex(key, otherKey);
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
    }
    
    /**
     * A node in the {@link IntTrie}.
     */
    private static class Node implements Entry, Serializable {
        
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
}
