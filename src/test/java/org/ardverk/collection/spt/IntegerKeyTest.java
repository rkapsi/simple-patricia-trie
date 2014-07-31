package org.ardverk.collection.spt;

import junit.framework.TestCase;

import org.ardverk.collection.spt.ints.PatriciaIntTrie;
import org.junit.Test;

public class IntegerKeyTest {

  @Test
  public void putUnsigned() {
    PatriciaIntTrie m = new PatriciaIntTrie();
    m.put(-1, -1);
    m.put(0, 0);
    m.put(1, 1);
    
    TestCase.assertEquals(0, m.firstKey());
    TestCase.assertEquals(-1, m.lastKey());
  }
  
  @Test
  public void putSigned() {
    
    PatriciaIntTrie m = new PatriciaIntTrie();
    m.put(IntegerKeyAnalyzer.transform(-1), -1);
    m.put(IntegerKeyAnalyzer.transform(0), 0);
    m.put(IntegerKeyAnalyzer.transform(1), 1);
    
    TestCase.assertEquals(-1, IntegerKeyAnalyzer.transform(m.firstKey()));
    TestCase.assertEquals(1, IntegerKeyAnalyzer.transform(m.lastKey()));
    
    int key = IntegerKeyAnalyzer.transform(0);
    TestCase.assertEquals(0, m.get(key));
  }
}
