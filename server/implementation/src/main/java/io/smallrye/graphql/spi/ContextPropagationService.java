package io.smallrye.graphql.spi;

import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import io.smallrye.graphql.SmallRyeGraphQLServerLogging;

public interface ContextPropagationService {

    ServiceLoader<ContextPropagationService> services = ServiceLoader.load(ContextPropagationService.class);

    ContextPropagationService service = load();

    <X> CompletionStage<X> withContextCapture(Supplier<X> action);

    static ContextPropagationService load() {
        ContextPropagationService service;
        try {
            service = services.iterator().next();
        } catch (Throwable t) {
            service = new NoopContextPropagationService();
        }
        SmallRyeGraphQLServerLogging.log.usingContextPropagationService(service.getClass().getName());
        return service;
    }

    static ContextPropagationService get() {
        return service;
    }

    static class NoopContextPropagationService implements ContextPropagationService {
        @Override
        public <X> CompletionStage<X> withContextCapture(Supplier<X> action) {
            return CompletableFuture.supplyAsync(action);
        }
    }

}
