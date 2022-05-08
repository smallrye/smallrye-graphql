package io.smallrye.graphql.tests.tracing;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Singleton;

import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;

@ApplicationScoped
public class TracerProducer {
    @Default
    @Produces
    @Singleton
    @Typed({ Tracer.class, MockTracer.class })
    public MockTracer tracer() {
        return new MockTracer();
    }
}
