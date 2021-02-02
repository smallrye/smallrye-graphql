package io.smallrye.graphql;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;

@ApplicationScoped
public class TracerProducer {
    @Default
    @Produces
    @Singleton
    public Tracer tracer() {
        return new MockTracer();
    }
}