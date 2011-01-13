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
 * A {@link KeyAnalyzer} for {@link CharSequence}es.
 */
public class CharSequenceKeyAnalyzer implements KeyAnalyzer<CharSequence> {

    public static final CharSequenceKeyAnalyzer INSTANCE 
        = new CharSequenceKeyAnalyzer();
    
    /**
     * The most significant bit of a {@code char}.
     */
    private static final int MSB = 1 << Character.SIZE-1;
    
    public int lengthInBits(CharSequence key) {
        return key != null ? key.length() * Character.SIZE : 0;
    }
    
    @Override
    public boolean isSet(CharSequence key, int bitIndex) {
        if (key == null) {
            return false;
        }
        
        int index = (int)(bitIndex / Character.SIZE);        
        int bit = (int)(bitIndex % Character.SIZE);

        if (index >= key.length()) {
            return false;
        }
        
        int mask = (MSB >>> bit);
        return (key.charAt(index) & mask) != 0;
    }
    
    @Override
    public int bitIndex(CharSequence key, CharSequence otherKey) {
        
        boolean allNull = true;
        int lengthInBits = lengthInBits(key);
        
        if (0 < lengthInBits) {
            int length = Math.max(lengthInBits, lengthInBits(otherKey));
            for(int i = 0; i < length; i++) {
                boolean isSet = isSet(key, i);
                if (isSet) {
                    allNull = false;
                }
                
                if (isSet != isSet(otherKey, i)) {
                    return i;
                }
            }
        }
        
        // This is a special case. This key was maybe not null
        // but it's possible to have keys whose bits are all 0s.
        if (allNull) {
            return NULL_KEY;
        }
        
        throw new IllegalStateException();
    }
}
