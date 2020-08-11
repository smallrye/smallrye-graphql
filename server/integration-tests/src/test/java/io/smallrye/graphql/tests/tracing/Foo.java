package io.smallrye.graphql.tests.tracing;

import java.util.concurrent.atomic.AtomicInteger;

import io.smallrye.graphql.tests.metrics.*;

public class Foo {

    private AtomicInteger version = new AtomicInteger();

    public Integer getVersion() {
        return version.get();
    }

    void update() {
        version.incrementAndGet();
    }

}
