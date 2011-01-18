# Simple PATRICIA Trie

*Practical Algorithm to Retrieve Information Coded in Alphanumeric*

If you need a PATRICIA Trie that implements the `SortedMap` interface or removes elements faster than _O(n)_ time then please see my other [patricia-trie](http://github.com/rkapsi/patricia-trie) project.

## org.ardverk.collection.sedgewick

This is an (almost 1:1) implementation of the PATRICIA Trie as described in Robert Sedgewick's Algorithms in Java (3rd Edition, Parts 1-4, pages 650-657). It's a bit rough around the edges and probably not very practical but it's a good starting point if you want to implement your own PATRICIA Trie.

## org.ardverk.collection.spt

This is a general purpose implementation of the PATRICIA Trie. It implements the `Map` interface and a few methods were borrowed from `SortedMap` and `NavigableMap` such as `firstEntry()` or `lastKey()` but no attempts were (and will be) made to implement all `SortedMap` and `NavigableMap` features.

Some operations ain't heavily optimized to keep the implementation simple, lightweight and easy to extend. The various `remove()` operations run in _O(n)_ time (i.e. the entire Trie gets rebuild) and everything that utilizes an `Iterator` will pre-iterate the Trie.

## org.ardverk.collection.spt.ints

This is an example how to change the PATRICIA Trie from a data structure that stores `Object` -> `Object` pairs to a data structure that stores `int` -> `int` pairs. It eliminates a lot of the overhead that's necessary if you're working with `Object`s.

* * *

Use these three PATRICIA Tries as a starting point for your own customized PATRICIA Trie implementations.