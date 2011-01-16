package org.ardverk.collection.spt;

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
}
