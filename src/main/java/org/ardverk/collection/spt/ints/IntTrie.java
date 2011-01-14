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


/**
 * A simple {@link IntTrie} interface.
 */
public interface IntTrie extends IntMap {
    
    /**
     * Selects and returns the closest {@link Entry} to the given key.
     */
    public IntMap.Entry select(int key);
    
    /**
     * Selects and returns the closest key to the given key.
     */
    public int selectKey(int key);
    
    /**
     * Selects and returns the closest value to the given key.
     */
    public int selectValue(int key);
    
    /**
     * @see NavigableMap#firstEntry()
     */
    public IntMap.Entry firstEntry();
    
    /**
     * @see SortedMap#firstKey()
     */
    public int firstKey();

    /**
     * @see NavigableMap#lastEntry()
     */
    public IntMap.Entry lastEntry();
    
    /**
     * @see SortedMap#lastKey()
     */
    public int lastKey();
    
    /**
     * Traverses the {@link IntTrie} by closeness to the given key.
     */
    public void select(int key, Cursor cursor);
    
    /**
     * Traverses the {@link IntTrie} from {@link #firstEntry()} through 
     * {@link #lastEntry()}
     */
    public void traverse(Cursor cursor);
}
