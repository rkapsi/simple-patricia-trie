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

import java.util.Set;

public interface IntMap {

    public int put(int key, int value);
    
    public int get(int key);
    
    public int remove(int key);
    
    public boolean containsKey(int key);
    
    public boolean containsValue(int value);
    
    public int size();
    
    public boolean isEmpty();
    
    public void clear();
    
    public Set<Entry> entrySet();
    
    public int[] keySet();
    
    public int[] values();
    
    public void putAll(IntMap m);
    
    public static interface Entry {
        
        public int getKey();
        
        public int getValue();
        
        public int setValue(int value);
    }
}
