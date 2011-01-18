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

/**
 * A {@link KeyAnalyzer} provides a simple interface to access keys bitwise.
 */
public interface KeyAnalyzer<K> {

    /**
     * Returned by {@link #bitIndex(Object, Object)} if the first
     * argument is either {@code null} or if all bits were 0s.
     */
    public static final int NULL_KEY = -1;
    
    /**
     * Returned by {@link #bitIndex(Object, Object)} if both arguments
     * were equal.
     */
    public static final int EQUAL_KEY = -2;
    
    /**
     * Returns {@code true} if the key's bit at the given bit index is set.
     */
    public boolean isSet(K key, int bitIndex);
    
    /**
     * Returns the first bit that's different in the given keys.
     */
    public int bitIndex(K key, K otherKey);
}
