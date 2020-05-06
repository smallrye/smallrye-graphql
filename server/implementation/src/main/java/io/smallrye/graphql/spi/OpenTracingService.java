package io.smallrye.graphql.spi;

import java.util.ServiceLoader;

import org.jboss.logging.Logger;

import io.opentracing.Tracer;

public interface OpenTracingService {
    static final Logger LOG = Logger.getLogger(OpenTracingService.class.getName());

    public static OpenTracingService load() {
        OpenTracingService openTracingService;
        try {
            ServiceLoader<OpenTracingService> sl = ServiceLoader.load(OpenTracingService.class);
            openTracingService = sl.iterator().next();
        } catch (Exception ex) {
            openTracingService = new OpenTracingService.DefaultOpenTracingService();
        }
        LOG.debug("Using " + openTracingService.getName() + " lookup service");
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
            return "Unsupported Metrics Service";
        }

        @Override
        public Tracer getTracer() {
            throw new UnsupportedOperationException("OpenTracing is not supported without CDI");
        }

    }
}
