package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

public class LRUCacheTest {
    @Test
    public void testCache() throws Exception {
        // first cache with a size of 0 to stress the remove logic and make sure we
        // don't leak
        LRUCache<Integer, Integer> lruCache = new LRUCache<>(0);
        testWorker(lruCache, k -> {
            return Integer.valueOf(1);
        });
        validateCache(lruCache, 0);
        testWorker(lruCache, k -> {
            return Integer.valueOf(k);
        });
        validateCache(lruCache, 0);
        // do the same test with a cache of size 1
        lruCache = new LRUCache<>(1);
        testWorker(lruCache, k -> {
            return Integer.valueOf(1);
        });
        validateCache(lruCache, 1);
        assertNotNull(lruCache.get(Integer.valueOf(1)));
        testWorker(lruCache, k -> {
            return Integer.valueOf(k);
        });
        validateCache(lruCache, 1);
        assertNull(lruCache.get(Integer.valueOf(1)));
        // Now test a larger cache
        lruCache = new LRUCache<>(Runtime.getRuntime().availableProcessors() * 2);
        testWorker(lruCache, k -> {
            return Integer.valueOf(1);
        });
        validateCache(lruCache, 1);
        assertNotNull(lruCache.get(Integer.valueOf(1)));
        testWorker(lruCache, k -> {
            return Integer.valueOf(k);
        });
        validateCache(lruCache, Runtime.getRuntime().availableProcessors() * 2);
        assertNull(lruCache.get(Integer.valueOf(1)));
        // finally test the default cache size
        lruCache = new LRUCache<>(2048);
        testWorker(lruCache, k -> {
            return Integer.valueOf(1);
        });
        validateCache(lruCache, 1);
        assertNotNull(lruCache.get(Integer.valueOf(1)));
        testWorker(lruCache, k -> {
            return Integer.valueOf(k);
        });
        validateCache(lruCache, 2048);
        assertNull(lruCache.get(Integer.valueOf(1)));
    }

    private void validateCache(LRUCache<Integer, Integer> lruCache, int size) throws Exception {
        Field sizeField = LRUCache.class.getDeclaredField("size");
        sizeField.setAccessible(true);
        AtomicInteger lruCacheSize = (AtomicInteger) sizeField.get(lruCache);
        assertEquals(size, lruCacheSize.get());
        Field cacheField = LRUCache.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        @SuppressWarnings("rawtypes")
        Map cache = (Map) cacheField.get(lruCache);
        assertEquals(size, cache.size());
    }

    private void testWorker(LRUCache<Integer, Integer> lruCache, Function<Integer, Integer> function) throws Exception {
        int numThreads = Runtime.getRuntime().availableProcessors() * 2;
        final CountDownLatch latch = new CountDownLatch(numThreads);
        Thread[] ts = new Thread[numThreads];
        for (int i = 0; i < numThreads; ++i) {
            ts[i] = new Thread(() -> {
                latch.countDown();
                try {
                    latch.await();
                    for (int j = 0; j < 100000; j++) {
                        lruCache.computeIfAbsent(function.apply(j), k -> {
                            return k;
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            ts[i].start();
        }
        for (int i = 0; i < numThreads; ++i) {
            ts[i].join();
        }
    }
}
