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
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A simple/lightweight implementation of a PATRICIA {@link Trie}.
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
        
    private final KeyAnalyzer<? super K> keyAnalyzer;
    
    private volatile RootNode<K, V> root = new RootNode<K, V>();
    
    private volatile int size = 0;
    
    private transient volatile Entry<? extends K, ? extends V>[] entries = null;
    
    private transient volatile EntrySet entrySet = null;
    
    private transient volatile KeySet keySet = null;
    
    private transient volatile Values values = null;
    
    private transient volatile int modCount = 0;
    
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
        Entry<K, V> entry = selectR(root.left, key, -1);
        if (entry != root || !root.empty) {
            return entry;
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
        
        Entry<K, V> entry = select(key);
        K existing = null;
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
        
        return null;
    }
    
    /**
     * Stores the given key-value at the {@link RootNode}.
     */
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
        Entry<K, V> entry = entry((K)key);
        if (entry != null) {
            return removeEntry(entry);
        }
        
        return null;
    }
    
    /**
     * Removes the given {@link Entry} from the Trie.
     */
    private V removeEntry(final Entry<? extends K, ? extends V> entry) {
        // We're traversing the old Trie and adding elements to the new Trie!
        RootNode<K, V> old = clear0();
        traverse(old, new Cursor<K, V>() {
            @Override
            public boolean select(Entry<? extends K, ? extends V> e) {
                if (!entry.equals(e)) {
                    put(e.getKey(), e.getValue());
                }
                return true;
            }
        });
        
        return entry.getValue();
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
        traverse(root, cursor);
    }
    
    private static <K, V> void traverse(RootNode<K, V> root, 
            Cursor<? super K, ? super V> cursor) {
        traverseR(root, root.left, cursor, -1);
    }
    
    private static <K, V> boolean traverseR(RootNode<K, V> root, Node<K, V> h, 
            Cursor<? super K, ? super V> cursor, int bitIndex) {
        
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
    public Set<Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet();
        }
        return entrySet;
    }
    
    @Override
    public Set<K> keySet() {
        if (keySet == null) {
            keySet = new KeySet();
        }
        return keySet;
    }
    
    @Override
    public Collection<V> values() {
        if (values == null) {
            values = new Values();
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
     * Increments the {@link #size} counter and calls {@link #clearEntriesArray()}.
     */
    private void incrementSize() {
        ++size;
        clearEntriesArray();
    }
    
    /**
     * Clears the {@link PatriciaTrie} and returns the old {@link RootNode}.
     * The {@link RootNode} may be used to {@link #traverse(RootNode, Cursor)}
     * the old {@link PatriciaTrie}.
     * 
     * @see #remove(Object)
     */
    private RootNode<K, V> clear0() {
        RootNode<K, V> previous = root;
        
        root = new RootNode<K, V>();
        size = 0;
        clearEntriesArray();
        
        return previous;
    }
    
    /**
     * Clears the {@link #entries} array.
     */
    private void clearEntriesArray() {
        entries = null;
        ++modCount;
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
     * Turns the {@link PatriciaTrie} into an {@link Entry[]}. The array
     * is being cached for as long as the {@link PatriciaTrie} isn't being
     * modified.
     * 
     * @see ViewIterator
     */
    private Entry<? extends K, ? extends V>[] toArray() {
        if (entries == null) {
            @SuppressWarnings("unchecked")
            final Entry<? extends K, ? extends V>[] dst 
                = new Entry[size()];
            
            traverse(new Cursor<K, V>() {
                
                private int index = 0;
                
                @Override
                public boolean select(Entry<? extends K, ? extends V> entry) {
                    dst[index++] = entry;
                    return true;
                }
            });
            
            entries = dst;
        }
        return entries;
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
     * An {@link Iterator} for {@link Entry}s.
     * 
     * @see PatriciaTrie#toArray()
     */
    private abstract class ViewIterator<E> implements Iterator<E> {
                
        private final Entry<? extends K, ? extends V>[] entries = toArray();
        
        private int expectedModCount = PatriciaTrie.this.modCount;
        
        private int index = 0;
        
        private Entry<? extends K, ? extends V> current = null;
        
        @Override
        public boolean hasNext() {
            return index < entries.length;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            
            if (expectedModCount != PatriciaTrie.this.modCount) {
                throw new ConcurrentModificationException();
            }
            
            current = entries[index++];
            return next(current);
        }

        /**
         * Called for each {@link Entry}.
         * 
         * @see #next()
         */
        protected abstract E next(Entry<? extends K, ? extends V> entry);
        
        @Override
        public void remove() {
            if (current == null) {
                throw new IllegalStateException();
            }
            
            removeEntry(current);
            expectedModCount = PatriciaTrie.this.modCount;
            current = null;
        }
    }
    
    /**
     * An abstract base class for the various views.
     */
    private abstract class AbstractView<E> extends AbstractCollection<E> {

        @Override
        public void clear() {
            PatriciaTrie.this.clear();
        }
        
        @Override
        public int size() {
            return PatriciaTrie.this.size();
        }
    }
    
    /**
     * @see PatriciaTrie#entrySet()
     */
    private class EntrySet extends AbstractView<Entry<K, V>> implements Set<Entry<K, V>> {
        
        private Entry<K, V> entry(Entry<K, V> entry) {
            Entry<K, V> other = PatriciaTrie.this.entry(entry.getKey());
            
            if (other != null && other.equals(entry)) {
                return other;
            }
            
            return null;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public boolean contains(Object o) {
            if (o instanceof Entry<?, ?>) {
                return entry((Entry<K, V>)o) != null;
            }
            return false;
        }

        @Override
        public boolean remove(Object o) {
            if (o instanceof Entry<?, ?>) {
                @SuppressWarnings("unchecked")
                Entry<K, V> entry = entry((Entry<K, V>)o);
                if (entry != null) {
                    int size = size();
                    PatriciaTrie.this.removeEntry(entry);
                    return size != size();
                }
            }
            return false;
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new ViewIterator<Entry<K, V>>() {
                @SuppressWarnings("unchecked")
                @Override
                protected Entry<K, V> next(Entry<? extends K, ? extends V> entry) {
                    return (Entry<K, V>)entry;
                }
            };
        }
    }
    
    /**
     * @see PatriciaTrie#keySet()
     */
    private class KeySet extends AbstractView<K> implements Set<K> {
        
        @Override
        public boolean remove(Object key) {
            int size = size();
            PatriciaTrie.this.remove(key);
            return size != size();
        }
        
        @Override
        public boolean contains(Object o) {
            return PatriciaTrie.this.containsKey(o);
        }
        
        @Override
        public Iterator<K> iterator() {
            return new ViewIterator<K>() {
                @Override
                protected K next(Entry<? extends K, ? extends V> entry) {
                    return entry.getKey();
                }
            };
        }
    }
    
    /**
     * @see PatriciaTrie#values()
     */
    private class Values extends AbstractView<V> {
        
        @Override
        public boolean remove(Object value) {
            for (Entry<K, V> entry : entrySet()) {
                if (AbstractTrie.equals(value, entry.getValue())) {
                    int size = size();
                    PatriciaTrie.this.removeEntry(entry);
                    return size != size();
                }
            }
            
            return false;
        }
        
        @Override
        public Iterator<V> iterator() {
            return new ViewIterator<V>() {
                @Override
                protected V next(Entry<? extends K, ? extends V> entry) {
                    return entry.getValue();
                }
            };
        }
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
        public int hashCode() {
            return 31 * (key != null ? key.hashCode() : 0)
                    + (value != null ? value.hashCode() : 0);
        }
        
        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof Entry<?, ?>)) {
                return false;
            }
            
            Entry<?, ?> other = (Entry<?, ?>)o;
            return AbstractTrie.equals(key, other.getKey())
                && AbstractTrie.equals(value, other.getValue());
        }
        
        @Override
        public String toString() {
            return key + " (" + bitIndex + ") -> " + value;
        }
    }
}
