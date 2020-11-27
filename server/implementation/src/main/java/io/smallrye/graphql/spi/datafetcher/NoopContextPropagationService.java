package io.smallrye.graphql.spi.datafetcher;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import io.smallrye.graphql.spi.ContextPropagationService;

/**
 * No-op context propagation (does not actually perform any context propagation).
 */
public class NoopContextPropagationService implements ContextPropagationService {

    @Override
    public <X> CompletionStage<X> withContextCapture(Supplier<X> action) {
        return CompletableFuture.supplyAsync(action);
    }

}
