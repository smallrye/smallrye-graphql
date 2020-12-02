package io.smallrye.graphql.cdi.context;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import org.eclipse.microprofile.context.ThreadContext;

import io.smallrye.graphql.spi.ContextPropagationService;

/**
 * Context propagation service that uses MicroProfile Context Propagation.
 */
public class MicroProfileContextPropagationService implements ContextPropagationService {

    private final ThreadContext ctx;

    public MicroProfileContextPropagationService() {
        ctx = ThreadContext.builder().build();
    }

    @Override
    public <X> CompletionStage<X> withContextCapture(Supplier<X> action) {
        return ctx.withContextCapture(CompletableFuture.supplyAsync(action, ctx.currentContextExecutor()));
    }

}
