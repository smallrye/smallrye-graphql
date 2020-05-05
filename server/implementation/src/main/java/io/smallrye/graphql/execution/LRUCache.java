package io.smallrye.graphql.execution;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class LRUCache<K, V> {
    private final int maxSize;
    private final Map<K, Entry<V>> cache = new ConcurrentHashMap<>();
    private final AtomicInteger size = new AtomicInteger();
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
        final AtomicBoolean called = new AtomicBoolean();
        Entry<V> entry = cache.computeIfAbsent(key, k -> {
            called.set(true);
            Entry<V> e = new Entry<V>(k, valueFunction.apply(k));
            addToStart(e);
            return e;
        });
        if (!called.get()) {
            cache.computeIfPresent(key, this::moveEntryToStart);
        } else {
            int newSize = size.incrementAndGet();
            if (newSize > maxSize) {
                AtomicBoolean removed = new AtomicBoolean();
                do {
                    final Entry<V> entryToRemove;
                    synchronized (this) {
                        entryToRemove = end;
                    }
                    if (entryToRemove == null) {
                        break;
                    }
                    cache.computeIfPresent(entryToRemove.key, (k, v) -> {
                        if (v == entryToRemove) {
                            removed.set(true);
                            removeEntry(entryToRemove);
                            return null;
                        } else {
                            return v;
                        }
                    });
                } while (!removed.get());
                if (removed.get()) {
                    size.decrementAndGet();
                }
            }
        }
        return entry.value;
    }

    private synchronized Entry<V> moveEntryToStart(K key, Entry<V> entry) {
        // If it is already at the start there is nothing to do
        if (start != entry) {
            removeEntry(entry);
            addToStart(entry);
        }
        return entry;
    }

    private synchronized void removeEntry(Entry<V> entry) {
        // If entry is null or was already removed, do nothing and return.
        if (entry == null || entry.left == entry) {
            return;
        }
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
        entry.left = entry.right = entry;
    }

    private synchronized void addToStart(Entry<V> entry) {
        // If entry is null, do nothing and return.
        if (entry == null) {
            return;
        }
        entry.right = start;
        entry.left = null;
        if (start != null) {
            start.left = entry;
        }
        start = entry;
        if (end == null) {
            end = start;
        }
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