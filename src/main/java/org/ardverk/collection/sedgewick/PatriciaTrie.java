package org.ardverk.collection.sedgewick;

/**
 * This is an implementation of the PATRICIA Trie as described in Robert 
 * Sedgewick's Algorithms in Java (3rd Edition, Parts 1-4, pages 650-657).
 * 
 * Please note, this is a bare minimum implementation and it's not suitable 
 * for use in a production environment. It doesn't handle {@code null} keys
 * for example (this includes empty Strings, "\0"s ...).
 */
public class PatriciaTrie {

    private final Node head = new Node(-1);
    
    // INIT
    {
        head.l = head;
    }
    
    public Object search(String key) {
        Node t = searchR(head.l, key, -1);
        if (t == null || !equals(t.key, key)) {
            return null;
        }
        
        return t.value;
    }
    
    private Node searchR(Node h, String key, int i) {
        if (h.bit <= i) { 
            return h; 
        }
        
        if (!isSet(key, h.bit)) {
            return searchR(h.l, key, h.bit);
        } else {
            return searchR(h.r, key, h.bit);
        }
    }
    
    public void insert(String key, Object value) {
        Node node = searchR(head.l, key, -1);
        
        String existing = null;
        if (node != null) {
            existing = node.key;
            if (equals(key, existing)) {
                node.value = value;
                return;
            }
        }
        
        int bit = 0;
        while (isSet(key, bit) == isSet(existing, bit)) {
            ++bit;
        }
        
        head.l = insertR(head.l, key, value, bit, head);
    }
    
    private Node insertR(Node h, String key, Object value, int i, Node p) {
        if ((h.bit >= i) || (h.bit <= p.bit)) {
            Node t = new Node(key, value, i);
            
            boolean isSet = isSet(key, t.bit);
            t.l = isSet ? h : t;
            t.r = isSet ? t : h;
            return t;
        }
        
        if (!isSet(key, h.bit)) {
            h.l = insertR(h.l, key, value, i, h);
        } else {
            h.r = insertR(h.r, key, value, i, h);
        }
        return h;
    }
    
    @Override
    public String toString() {
        return toStringR(head.l, -1);
    }
    
    private String toStringR(Node h, int i) {
        if (h == head) {
            return "";
        }
        
        if (h.bit <= i) {
            return h.value + "\n";
        }
        
        return toStringR(h.l, h.bit) + toStringR(h.r, h.bit);
    }
    
    /**
     * The most significant bit of a {@code char}.
     */
    private static final int MSB = 1 << Character.SIZE-1;
    
    /**
     * Returns {@code true} if the key's bit at the given bit index is set.
     */
    private static boolean isSet(String key, int bitIndex) {
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
    
    /**
     * Returns {@code true} if the two objects are equal.
     */
    private static boolean equals(Object o1, Object o2) {
        if (o1 == null) {
            return (o2 == null);
        }
        
        return o1.equals(o2);
    }
    
    private static class Node {
        
        private final int bit;

        private String key;
        
        private Object value;
        
        private Node l;
        
        private Node r;
        
        private Node(int bit) {
            this(null, null, bit);
        }
        
        private Node(String key, Object value, int bit) {
            this.bit = bit;
            this.key = key;
            this.value = value;
        }
    }
    
    public static void main(String[] args) {
        PatriciaTrie trie = new PatriciaTrie();
        trie.insert("Hello", "World");
        trie.insert("Foo", "Bar");
        
        System.out.println(trie.search("Hello"));
        System.out.println(trie.search("Foo"));
        System.out.println(trie.search("This Key Doesn't Exist"));
    }
}
