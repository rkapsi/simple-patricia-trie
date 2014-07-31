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
import java.util.Map.Entry;

/**
 * A {@link Cursor} is a callback interface for the {@link Trie}
 * interface.
 */
public interface Cursor<K, V> {
  
  /**
   * Called for each {@link Entry} in the {@link Trie}. Return {@code true}
   * to continue with the traverse operation or {@code false} to exit.
   */
  public boolean select(Map.Entry<? extends K, ? extends V> entry);
}