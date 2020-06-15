package io.smallrye.graphql.spi;

import static io.smallrye.graphql.SmallRyeGraphQLServerLogging.log;
import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import java.util.ServiceLoader;

import io.opentracing.Tracer;

public interface OpenTracingService {

    static OpenTracingService load() {
        OpenTracingService openTracingService;
        try {
            ServiceLoader<OpenTracingService> sl = ServiceLoader.load(OpenTracingService.class);
            openTracingService = sl.iterator().next();
        } catch (Exception ex) {
            openTracingService = new OpenTracingService.DefaultOpenTracingService();
        }
        log.usingTracingService(openTracingService.getName());
        return openTracingService;
    }

    String getName();

    Tracer getTracer();

    /**
     * Default Metrics service that throws an UnsupportedOperationException.
     */
    class DefaultOpenTracingService implements OpenTracingService {

        @Override
        public String getName() {
            return "Unsupported OpenTracing Service";
        }

        @Override
        public Tracer getTracer() {
            throw msg.openTracingNotSupportedWithoutCDI();
        }

    }
}
