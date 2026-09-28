package io.smallrye.graphql.client.impl.core.utils;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.core.Variable;
import io.smallrye.graphql.client.core.factory.VariableFactory;
import io.smallrye.graphql.client.core.utils.ServiceUtils;

/**
 * See https://github.com/smallrye/smallrye-graphql/issues/2696
 */
class ServiceUtilsTest {

    private static final int THREADS = 32;

    // Uses a factory that testConcurrentUse does not, so that test always starts with a cold cache
    @Test
    public void testNewInstancePerCall() {
        Variable first = ServiceUtils.getNewInstanceFromFactory(VariableFactory.class);
        Variable second = ServiceUtils.getNewInstanceFromFactory(VariableFactory.class);
        assertNotSame(first, second);
    }

    @Test
    public void testConcurrentUse() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        try {
            CountDownLatch start = new CountDownLatch(1);
            List<Future<String>> results = new ArrayList<>();
            for (int i = 0; i < THREADS; i++) {
                String name = "field" + i;
                results.add(executor.submit(() -> {
                    start.await();
                    Document document = document(operation(field(name, args(arg("key", "value")))));
                    return document.build();
                }));
            }
            start.countDown();
            for (int i = 0; i < THREADS; i++) {
                assertEquals("query {field" + i + "(key:\"value\")}", results.get(i).get(30, TimeUnit.SECONDS));
            }
        } finally {
            executor.shutdownNow();
        }
    }
}
