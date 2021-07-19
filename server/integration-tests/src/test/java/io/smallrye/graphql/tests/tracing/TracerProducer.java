package io.smallrye.graphql.tests.tracing;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.inject.Singleton;

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
