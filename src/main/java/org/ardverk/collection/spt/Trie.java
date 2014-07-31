/*
 * Copyright 2011 Roger Kapsi
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.ardverk.collection.spt;

import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;

/**
 * A simple {@link Trie} interface.
 */
public interface Trie<K, V> extends Map<K, V> {
  
  /**
   * Selects and returns the closest {@link Entry} to the given key.
   */
  public Map.Entry<K, V> select(K key);
  
  /**
   * Selects and returns the closest key to the given key.
   */
  public K selectKey(K key);
  
  /**
   * Selects and returns the closest value to the given key.
   */
  public V selectValue(K key);
  
  /**
   * @see NavigableMap#firstEntry()
   */
  public Map.Entry<K, V> firstEntry();
  
  /**
   * @see SortedMap#firstKey()
   */
  public K firstKey();

  /**
   * @see NavigableMap#lastEntry()
   */
  public Map.Entry<K, V> lastEntry();
  
  /**
   * @see SortedMap#lastKey()
   */
  public K lastKey();
  
  /**
   * @see NavigableMap#pollFirstEntry()
   */
  public Map.Entry<K, V> pollFirstEntry();
  
  /**
   * @see NavigableMap#pollLastEntry()
   */
  public Map.Entry<K, V> pollLastEntry();
  
  /**
   * Traverses the {@link Trie} by closeness to the given key.
   */
  public void select(K key, Cursor<? super K, ? super V> cursor);
  
  /**
   * Traverses the {@link Trie} from {@link #firstEntry()} through 
   * {@link #lastEntry()}
   */
  public void traverse(Cursor<? super K, ? super V> cursor);
}
