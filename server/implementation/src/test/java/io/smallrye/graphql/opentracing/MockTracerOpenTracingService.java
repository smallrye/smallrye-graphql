package io.smallrye.graphql.opentracing;

import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;
import io.smallrye.graphql.spi.OpenTracingService;

public class MockTracerOpenTracingService implements OpenTracingService {

    public static final MockTracer MOCK_TRACER = new MockTracer();

    @Override
    public String getName() {
        return "MockTracer";
    }

    @Override
    public Tracer getTracer() {
        return MOCK_TRACER;
    }
}
