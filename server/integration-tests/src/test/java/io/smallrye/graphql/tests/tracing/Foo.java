package io.smallrye.graphql.tests.tracing;

import java.util.concurrent.atomic.AtomicInteger;

public class Foo {

    private final AtomicInteger version = new AtomicInteger();

    private String tracerString;

    public Integer getVersion() {
        return version.get();
    }

    public String getTracerString() {
        return tracerString;
    }

    public void setTracerString(String tracerString) {
        this.tracerString = tracerString;
    }

    void update() {
        version.incrementAndGet();
    }

}
