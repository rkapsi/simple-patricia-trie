package org.ardverk.collection.spt;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;

public class PatriciaTrieTest {

  @Test
  public void put() {
    PatriciaTrie<String, String> trie 
      = new PatriciaTrie<String, String>(
        CharSequenceKeyAnalyzer.INSTANCE);
    
    trie.put("1", "1");
    trie.put("2", "2");
    trie.put("3", "3");
    TestCase.assertEquals(3, trie.size());
    
    trie.put("2", "2");
    TestCase.assertEquals(3, trie.size());
    
    trie.put(null, "null");
    TestCase.assertEquals(4, trie.size());
  }
  
  @Test
  public void get() {
    PatriciaTrie<String, String> trie 
      = new PatriciaTrie<String, String>(
        CharSequenceKeyAnalyzer.INSTANCE);
    
    trie.put("1", "1");
    trie.put("2", "2");
    trie.put("3", "3");
    
    TestCase.assertEquals("2", trie.get("2"));
    
    TestCase.assertNull(trie.get("This key does not exist!"));
    TestCase.assertNull(trie.get(null));
    
    trie.put(null, "null-key");
    TestCase.assertEquals("null-key", trie.get(null));
  }
  
  @Test
  public void select() {
    PatriciaTrie<String, String> trie 
      = new PatriciaTrie<String, String>(
        CharSequenceKeyAnalyzer.INSTANCE);
    
    trie.put("1", "1");
    trie.put("2", "2");
    trie.put("3", "3");
    
    TestCase.assertEquals("3", trie.selectKey("3"));
  }
  
  @Test
  public void remove() {
    PatriciaTrie<String, String> trie 
      = new PatriciaTrie<String, String>(
        CharSequenceKeyAnalyzer.INSTANCE);
    
    trie.put("1", "1");
    trie.put("2", "2");
    trie.put("3", "3");
    trie.put(null, "null-key");
    
    TestCase.assertEquals(4, trie.size());
    String removed = trie.remove("2");
    TestCase.assertEquals("2", removed);
    TestCase.assertEquals(3, trie.size());
    
    TestCase.assertEquals("1", trie.get("1"));
    TestCase.assertNull(trie.get("2"));
    TestCase.assertEquals("3", trie.get("3"));
    TestCase.assertEquals("null-key", trie.get(null));
  }
  
  @Test
  public void replace() {
    PatriciaTrie<String, String> trie 
      = new PatriciaTrie<String, String>(
        CharSequenceKeyAnalyzer.INSTANCE);
    
    trie.put("1", "1");
    trie.put("2", "2");
    trie.put("3", "3");
    
    trie.put("2", "new-value");
    TestCase.assertEquals("new-value", trie.get("2"));
  }
  
  @Test
  public void entrySet() {
    PatriciaTrie<String, String> trie 
      = new PatriciaTrie<String, String>(
        CharSequenceKeyAnalyzer.INSTANCE);
    
    trie.put("1", "1");
    trie.put("2", "2");
    trie.put("3", "3");
    
    Set<Entry<String, String>> entrySet = trie.entrySet();
    TestCase.assertEquals(trie.size(), entrySet.size());
    
    System.out.println(entrySet);
    
    int index = 1;
    for (Iterator<Entry<String, String>> it 
        = entrySet.iterator(); it.hasNext(); ) {
      
      Entry<String, String> entry = it.next();
      String key = entry.getKey();
      
      TestCase.assertEquals(Integer.toString(index), key);
      
      it.remove();
      TestCase.assertEquals(trie.size(), entrySet.size());
      
      ++index;
    }
    
    TestCase.assertEquals(0, trie.size());
    TestCase.assertEquals(0, entrySet.size());
    
    TestCase.assertTrue(trie.isEmpty());
    TestCase.assertTrue(entrySet.isEmpty());
  }
  
  @Test
  public void keySet() {
    PatriciaTrie<String, String> trie 
      = new PatriciaTrie<String, String>(
        CharSequenceKeyAnalyzer.INSTANCE);
    
    trie.put("1", "1");
    trie.put("2", "2");
    trie.put("3", "3");
    
    Set<String> keySet = trie.keySet();
    TestCase.assertEquals(trie.size(), keySet.size());
    
    TestCase.assertTrue(keySet.contains("2"));
    
    boolean success = keySet.remove("2");
    TestCase.assertTrue(success);
    
    TestCase.assertFalse(keySet.contains("2"));
    
    TestCase.assertTrue(keySet.contains("1"));
    TestCase.assertTrue(keySet.contains("3"));
  }
  
  @Test
  public void values() {
    PatriciaTrie<String, String> trie 
      = new PatriciaTrie<String, String>(
        CharSequenceKeyAnalyzer.INSTANCE);
    
    trie.put("1", "1");
    trie.put("2", "2");
    trie.put("3", "3");
    
    Collection<String> values = trie.values();
    TestCase.assertEquals(trie.size(), values.size());
    
    TestCase.assertTrue(values.contains("2"));
    
    boolean success = values.remove("2");
    TestCase.assertTrue(success);
    
    TestCase.assertFalse(values.contains("2"));
    
    TestCase.assertTrue(values.contains("1"));
    TestCase.assertTrue(values.contains("3"));
  }
}
