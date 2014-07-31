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

import java.io.Serializable;

/**
 * A {@link KeyAnalyzer} for {@link Integer} and {@code int} keys.
 */
public class IntegerKeyAnalyzer implements KeyAnalyzer<Integer>, Serializable {

  private static final long serialVersionUID = 3198463951399568393L;
  
  public static final IntegerKeyAnalyzer INSTANCE = new IntegerKeyAnalyzer();
  
  private static final int MSB = 1 << Integer.SIZE-1;
  
  @Override
  public boolean isSet(Integer key, int bitIndex) {
    return isSet(intValue(key), bitIndex);
  }
  
  @Override
  public int bitIndex(Integer key, Integer otherKey) {
    return bitIndex(intValue(key), intValue(otherKey));
  }
  
  /**
   * Returns {@code true} if the given bit is set.
   */
  public boolean isSet(int key, int bitIndex) {
    return (key & (MSB >>> bitIndex)) != 0;
  }
  
  /**
   * Returns the first different bit in the two keys or {@link KeyAnalyzer#EQUAL_KEY}
   * if they're equal and {@link KeyAnalyzer#NULL_KEY} if the first argument is 0.
   */
  public int bitIndex(int key, int otherKey) {
    if (key != 0) {
      
      int xor = key ^ otherKey;
      for (int i = 0; i < Integer.SIZE; i++) {
        if ((xor & (MSB >>> i)) != 0) {
          return i;
        }
      }
      
      return EQUAL_KEY;
    }
    
    return NULL_KEY;
  }
  
  /**
   * Problem: PATRICIA operates on unsigned values and Java's primitive
   * types are are in "two's complement" format. In two's complement format 
   * the most significant bit (MSB) is used to represent the sign.
   * 
   * The basic PATRICIA operations such as get(), select() or put() will
   * work unaffected by the signed/unsigned issue but things like firstKey()
   * will break.
   * 
   * One thing we can do is to transform all keys prior to adding to the
   * Trie and after retrieving it from it. You can hack this functionality
   * straight into the Trie but that will require some changes. 
   */
  public static int transform(int value) {
    return value + Integer.MIN_VALUE; /* 0x80000000 */
  }

  /**
   * An utility method that will return 0 for {@code null}.
   */
  private static int intValue(Integer value) {
    return value != null ? value.intValue() : 0;
  }
}
