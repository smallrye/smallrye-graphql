package io.smallrye.graphql.spi;

import static io.smallrye.graphql.SmallRyeGraphQLServerLogging.log;

import java.util.ServiceLoader;

import io.smallrye.graphql.execution.context.SmallRyeContext;

/**
 * Context service that allows multiple ways to provide the context.
 * By default, thread local will be used
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface SmallRyeContextService {

    ServiceLoader<SmallRyeContextService> contextServices = ServiceLoader.load(SmallRyeContextService.class);

    SmallRyeContextService contextService = _load();

    static SmallRyeContextService _load() {
        SmallRyeContextService cs;
        try {
            cs = contextServices.iterator().next();
        } catch (Exception ex) {
            cs = new DefaultContextService();
        }
        log.usingLookupService(cs.getName());
        return cs;
    }

    static SmallRyeContextService get() {
        return contextService;
    }

    String getName();

    SmallRyeContext getSmallRyeContext();

    default void setSmallRyeContext(SmallRyeContext smallRyeContext) {
    }

    default void clearSmallRyeContext() {
    }

    /**
     * Default Context service that gets used when none is provided with SPI.
     */
    class DefaultContextService implements SmallRyeContextService {
        private static final InheritableThreadLocal<SmallRyeContext> current = new InheritableThreadLocal<>();

        @Override
        public String getName() {
            return "Thread Local (default)";
        }

        @Override
        public SmallRyeContext getSmallRyeContext() {
            return current.get();
        }

        @Override
        public void setSmallRyeContext(SmallRyeContext smallRyeContext) {
            current.set(smallRyeContext);
        }

        @Override
        public void clearSmallRyeContext() {
            current.remove();
        }
    }
}
