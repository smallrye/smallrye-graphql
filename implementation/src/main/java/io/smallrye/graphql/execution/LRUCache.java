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

    V computeIfAbsent(K key, Function<K, V> valueFunction) {
        Entry<V> entry = cache.computeIfAbsent(key, k -> {
            return cachePutNew(k, valueFunction.apply(k));
        });
        entry = moveEntryToStart(key, entry);
        return entry.value;
    }

    private Entry<V> cachePutNew(K key, V value) {
        Entry<V> entry = new Entry<V>(key, value);
        synchronized (cache) {
            if (cache.size() > maxSize - 1) {
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
        } else {
            start = entry.right;
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
