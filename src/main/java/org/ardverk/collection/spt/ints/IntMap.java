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

package org.ardverk.collection.spt.ints;

import java.util.Map;

/**
 * An interface that replicates the {@link Map} interface 
 * but for {@code int} values.
 * 
 * @see Map
 */
public interface IntMap {

  /**
   * @see Map#put(Object, Object)
   */
  public int put(int key, int value);
  
  /**
   * @see Map#get(Object)
   */
  public int get(int key);
  
  /**
   * @see Map#remove(Object)
   */
  public int remove(int key);
  
  /**
   * @see Map#containsKey(Object)
   */
  public boolean containsKey(int key);
  
  /**
   * @see Map#containsValue(Object)
   */
  public boolean containsValue(int value);
  
  /**
   * @see Map#size()
   */
  public int size();
  
  /**
   * @see Map#isEmpty()
   */
  public boolean isEmpty();
  
  /**
   * @see Map#clear()
   */
  public void clear();
  
  /**
   * @see Map#entrySet()
   */
  public IntMap.Entry[] entrySet();
  
  /**
   * @see Map#keySet()
   */
  public int[] keySet();
  
  /**
   * @see Map#values()
   */
  public int[] values();
  
  /**
   * @see Map#putAll(Map)
   */
  public void putAll(IntMap m);
  
  /**
   * @see Map.Entry
   */
  public static interface Entry {
    
    /**
     * @see Map.Entry#getKey()
     */
    public int getKey();
    
    /**
     * @see Map.Entry#getValue()
     */
    public int getValue();
    
    /**
     * @see Map.Entry#setValue(Object)
     */
    public int setValue(int value);
  }
}
