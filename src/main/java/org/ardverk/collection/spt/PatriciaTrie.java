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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple/lightweight implementation of a PATRICIA {@link Trie}.
 * Some operations such as {@link #remove(Object)}, {@link #entrySet()},
 * {@link #keySet()} and {@link #values()} run in O(n) time.
 */
public class PatriciaTrie<K, V> extends AbstractTrie<K, V> implements Serializable {
    
    private static final long serialVersionUID = 7464215084236615537L;

    private static KeyAnalyzer<Object> DEFAULT = new KeyAnalyzer<Object>() {
        @Override
        public boolean isSet(Object key, int bitIndex) {
            return ((PatriciaKey<?>)key).isBitSet(bitIndex);
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public int bitIndex(Object key, Object otherKey) {
            return ((PatriciaKey<Object>)key).bitIndex(otherKey);
        }
    };
    
    private final RootNode<K, V> root = new RootNode<K, V>();
    
    private final KeyAnalyzer<? super K> keyAnalyzer;
    
    private int size = 0;
    
    private transient volatile Set<Entry<K, V>> entrySet = null;
    
    private transient volatile Set<K> keySet = null;
    
    private transient volatile Collection<V> values = null;
    
    public PatriciaTrie() {
        this(DEFAULT);
    }
    
    public PatriciaTrie(KeyAnalyzer<? super K> keyAnalyzer) {
        this.keyAnalyzer = keyAnalyzer;
    }
    
    public PatriciaTrie(Map<? extends K, ? extends V> m) {
        this(keyAnalyzer(m), m);
    }
    
    public PatriciaTrie(KeyAnalyzer<? super K> keyAnalyzer, 
            Map<? extends K, ? extends V> m) {
        this.keyAnalyzer = keyAnalyzer;
        
        putAll(m);
    }
    
    /**
     * Returns the {@link KeyAnalyzer}.
     */
    public KeyAnalyzer<? super K> getKeyAnalyzer() {
        return keyAnalyzer;
    }
    
    @Override
    public Entry<K, V> select(K key) {
        Entry<K, V> node = selectR(root.left, key, -1);
        if (node != root || !root.empty) {
            return node;
        }
        return null;
    }
    
    private Node<K, V> selectR(Node<K, V> h, K key, int bitIndex) {
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
    public V put(K key, V value) {
        // This is a shortcut! The root is the only place to store null! 
        if (key == null) {
            return putForNullKey(key, value);
        }
        
        Entry<K, V> node = select(key);
        K existing = null;
        if (node != null) {
            existing = node.getKey();
            if (equals(key, existing)) {
                return node.setValue(value);
            }
        }
        
        int bitIndex = bitIndex(key, existing);
        if (bitIndex == KeyAnalyzer.NULL_KEY) {
            return putForNullKey(key, value);
        }
        
        root.left = putR(root.left, key, value, bitIndex, root);
        incrementSize();
        
        return null;
    }
    
    private V putForNullKey(K key, V value) {
        if (root.empty) {
            incrementSize();
        }
        
        return root.setKeyValue(key, value);
    }
    
    private Node<K, V> putR(Node<K, V> h, K key, V value, int bitIndex, Node<K, V> p) {
        if ((h.bitIndex >= bitIndex) || (h.bitIndex <= p.bitIndex)) {
            Node<K, V> t = new Node<K, V>(key, value, bitIndex);
            
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
    public V remove(Object key) {
        @SuppressWarnings("unchecked")
        final Entry<K, V> entry = select((K)key);
        if (entry != null && equals(key, entry.getKey())) {
            
            // We can take a shortcut for the root Node!
            if (entry == root) {
                decrementSize();
                return root.unsetKeyValue();
            }
            
            @SuppressWarnings("unchecked")
            final Map.Entry<? extends K, ? extends V>[] entries 
                = new Map.Entry[size()-1];
            
            traverse(new Cursor<K, V>() {
                
                private int index = 0;
                
                @Override
                public boolean select(Entry<? extends K, ? extends V> e) {
                    if (entry != e) {
                        
                        // We must make a copy of the root node
                        // here or we'll lose the key-value with
                        // the clear() operation!
                        if (e == root) {
                            K key = root.getKey();
                            V value = root.getValue();
                            e = new Node<K, V>(key, value, -1);
                        }
                        
                        entries[index++] = e;
                    }
                    return true;
                }
            });
            
            clear();
            
            Map.Entry<? extends K, ? extends V> entryToAdd = null;
            for (int i = 0; i < entries.length; i++) {
                entryToAdd = entries[i];
                put(entryToAdd.getKey(), entryToAdd.getValue());
            }
            
            return entry.getValue();
        }
        
        return null;
    }
    
    @Override
    public void select(K key, Cursor<? super K, ? super V> cursor) {
        selectR(root.left, key, cursor, -1);
    }
    
    private boolean selectR(Node<K, V> h, K key, 
            Cursor<? super K, ? super V> cursor, int bitIndex) {
        
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
    public void traverse(Cursor<? super K, ? super V> cursor) {
        traverseR(root.left, cursor, -1);
    }
    
    private boolean traverseR(Node<K, V> h, 
            Cursor<? super K, ? super V> cursor, int bitIndex) {
        
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
    public Set<Entry<K, V>> entrySet() {
        if (entrySet == null) {
            final Set<Entry<K, V>> entries 
                = new ArrayListSet<Entry<K, V>>(size());
            
            traverse(new Cursor<K, V>() {
                @SuppressWarnings("unchecked")
                @Override
                public boolean select(Entry<? extends K, ? extends V> entry) {
                    entries.add((Entry<K, V>)entry);
                    return true;
                }
            });
            
            entrySet = Collections.unmodifiableSet(entries);
        }
        return entrySet;
    }
    
    @Override
    public Set<K> keySet() {
        if (keySet == null) {
            final Set<K> entries = new ArrayListSet<K>(size());
            
            traverse(new Cursor<K, V>() {
                @Override
                public boolean select(Entry<? extends K, ? extends V> entry) {
                    entries.add(entry.getKey());
                    return true;
                }
            });
            
            keySet = Collections.unmodifiableSet(entries);
        }
        return keySet;
    }
    
    @Override
    public Collection<V> values() {
        if (values == null) {
            final List<V> entries = new ArrayList<V>(size());
            
            traverse(new Cursor<K, V>() {
                @Override
                public boolean select(Entry<? extends K, ? extends V> entry) {
                    entries.add(entry.getValue());
                    return true;
                }
            });
            
            values = Collections.unmodifiableCollection(entries);
        }
        return values;
    }
    
    @Override
    public Entry<K, V> firstEntry() {
        Entry<K, V> entry = followLeft(root.left, -1, root);
        if (entry != root || !root.empty) {
            return entry;
        }
        return null;
    }

    @Override
    public Entry<K, V> lastEntry() {
        Entry<K, V> entry = followRight(root.left, -1);
        if (entry != root || !root.empty) {
            return entry;
        }
        return null;
    }
    
    private Entry<K, V> followLeft(Node<K, V> h, int bitIndex, Node<K, V> p) {
        if (h.bitIndex <= bitIndex) {
            if (h != root || !root.empty) {
                return h;
            }
            return p;
        }
        
        return followLeft(h.left, h.bitIndex, h);
    }
    
    private Entry<K, V> followRight(Node<K, V> h, int bitIndex) {
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
    
    /**
     * @see KeyAnalyzer#isSet(Object, int)
     */
    private boolean isSet(K key, int bitIndex) {
        return keyAnalyzer.isSet(key, bitIndex);
    }
    
    /**
     * @see KeyAnalyzer#bitIndex(Object, Object)
     */
    private int bitIndex(K key, K otherKey) {
        return keyAnalyzer.bitIndex(key, otherKey);
    }
    
    /**
     * Returns a {@link KeyAnalyzer} for the given {@link Map}.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static KeyAnalyzer<Object> keyAnalyzer(Map<?, ?> m) {
        if (m instanceof PatriciaTrie<?, ?>) {
            return ((PatriciaTrie)m).getKeyAnalyzer();
        }
        
        return DEFAULT;
    }
    
    /**
     * The root node of the {@link Trie}.
     */
    private static class RootNode<K, V> extends Node<K, V> {
        
        private static final long serialVersionUID = -8857149853096688620L;
        
        private boolean empty = true;
        
        public RootNode() {
            super(null, null, -1);
            this.left = this;
        }
        
        /**
         * Sets the key and value of the root node.
         */
        public V setKeyValue(K key, V value) {
            this.key = key;
            this.empty = false;
            return setValue(value);
        }
        
        /**
         * Unsets the key and value of the root node.
         */
        public V unsetKeyValue() {
            this.key = null;
            this.empty = true;
            return setValue(null);
        }
        
        /**
         * Clears the root node and effectively also the {@link Trie}.
         */
        public void clear() {
            unsetKeyValue();
            this.left = this;
        }
    }
    
    /**
     * A node in the {@link Trie}.
     */
    private static class Node<K, V> implements Entry<K, V>, Serializable {
        
        private static final long serialVersionUID = -2409938371345117780L;

        private final int bitIndex;

        protected K key;
        
        protected V value;
        
        protected Node<K, V> left;
        
        protected Node<K, V> right;
        
        private Node(K key, V value, int bitIndex) {
            this.bitIndex = bitIndex;
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V existing = this.value;
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
