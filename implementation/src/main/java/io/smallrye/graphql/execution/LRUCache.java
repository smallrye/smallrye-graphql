package io.smallrye.graphql.execution;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class LRUCache<K, V> {

    private final int maxSize;
    private final Map<K, Entry<V>> cache = new ConcurrentHashMap<>();
    private Entry<V> start;
    private Entry<V> end;

    LRUCache(int maxSize) {
        this.maxSize = maxSize;
    }

    V get(K key) {
        Entry<V> entry = cache.computeIfPresent(key, this::moveEntryToStart);
        return entry == null ? null : entry.value;
    }

    V put(K key, V value) {
        Entry<V> entry = cache.get(key);
        V oldValue = null;
        if (entry == null) {
            entry = cachePutNew(key, value);
        } else {
            entry = removeEntry(entry);
            oldValue = entry.value;
            entry.value = value;
        }
        entry = addToStart(entry);
        return oldValue;
    }

    V computeIfAbsent(K key, Function<K, V> valueFunction) {
        Entry<V> entry = cache.get(key);
        if (entry == null) {
            entry = cachePutNew(key, valueFunction.apply(key));
        } else {
            entry = removeEntry(entry);
        }
        entry = addToStart(entry);
        return entry.value;
    }

    private Entry<V> cachePutNew(K key, V value) {
        Entry<V> entry = new Entry<V>(key, value);
        cache.put(key, entry);
        synchronized (cache) {
            if (cache.size() > maxSize) {
                cache.remove(end.key);
                removeEntry(end);
            }
        }
        return entry;
    }

    private synchronized Entry<V> moveEntryToStart(K key, Entry<V> entry) {
        return addToStart(removeEntry(entry));
    }

    private synchronized Entry<V> removeEntry(Entry<V> entry) {
        if (entry.left != null) {
            entry.left.right = entry.right;
        }
        if (entry.right != null) {
            entry.right.left = entry.left;
        } else {
            end = entry.left;
        }
        return entry;
    }

    private synchronized Entry<V> addToStart(Entry<V> entry) {
        entry.right = start;
        entry.left = null;
        if (start != null) {
            start.left = entry;
        }
        start = entry;
        if (end == null) {
            end = start;
        }
        return entry;
    }

    private class Entry<V> {
        final K key;
        V value;
        Entry<V> left;
        Entry<V> right;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
