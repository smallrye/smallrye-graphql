package io.smallrye.graphql.test.integration.metrics;

import java.util.concurrent.atomic.AtomicInteger;

public class Foo {

    private AtomicInteger version = new AtomicInteger();

    public Integer getVersion() {
        return version.get();
    }

    void update() {
        version.incrementAndGet();
    }

}
