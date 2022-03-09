package io.smallrye.graphql.execution;

import static io.smallrye.graphql.SmallRyeGraphQLServerLogging.log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicLong;

import javax.json.JsonObject;

import org.dataloader.BatchLoaderWithContext;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderFactory;
import org.dataloader.DataLoaderOptions;
import org.dataloader.DataLoaderRegistry;
import org.eclipse.microprofile.context.ThreadContext;

import graphql.ExecutionInput;
import graphql.ExecutionInput.Builder;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.ExecutionId;
import graphql.execution.ExecutionStrategy;
import graphql.execution.SubscriptionExecutionStrategy;
import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.bootstrap.DataFetcherFactory;
import io.smallrye.graphql.execution.context.SmallRyeBatchLoaderContextProvider;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.execution.datafetcher.helper.BatchLoaderHelper;
import io.smallrye.graphql.execution.datafetcher.helper.ContextHelper;
import io.smallrye.graphql.execution.error.ExceptionHandler;
import io.smallrye.graphql.execution.event.EventEmitter;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.spi.config.Config;
import io.smallrye.graphql.spi.config.LogPayloadOption;

/**
 * Executing the GraphQL request
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ExecutionService {

    private final String executionIdPrefix;
    private final AtomicLong executionId = new AtomicLong();

    private final GraphQLSchema graphQLSchema;

    private final BatchLoaderHelper batchLoaderHelper = new BatchLoaderHelper();
    private final DataFetcherFactory dataFetcherFactory = new DataFetcherFactory();
    private final List<Operation> batchOperations;

    private final EventEmitter eventEmitter = EventEmitter.getInstance();

    private GraphQL graphQL;

    private final boolean hasSubscription;
    private final QueryCache queryCache;
    private final LogPayloadOption payloadOption;

    private final ExecutionStrategy queryExecutionStrategy;
    private final ExecutionStrategy mutationExecutionStrategy;

    public ExecutionService(GraphQLSchema graphQLSchema, List<Operation> batchOperations,
            boolean hasSubscription) {
        this(graphQLSchema, batchOperations, hasSubscription, null, null);
    }

    public ExecutionService(GraphQLSchema graphQLSchema, List<Operation> batchOperations,
            boolean hasSubscription, ExecutionStrategy queryExecutionStrategy, ExecutionStrategy mutationExecutionStrategy) {

        this.graphQLSchema = graphQLSchema;
        this.batchOperations = batchOperations;
        // use schema's hash as prefix to differentiate between multiple apps
        this.executionIdPrefix = Integer.toString(Objects.hashCode(graphQLSchema));
        this.hasSubscription = hasSubscription;
        this.queryCache = new QueryCache();

        this.queryExecutionStrategy = queryExecutionStrategy;
        this.mutationExecutionStrategy = mutationExecutionStrategy;

        Config config = Config.get();
        this.payloadOption = config.logPayload();
    }

    /**
     * @deprecated - use one on the void method providing a writer.
     */
    @Deprecated
    public ExecutionResponse execute(JsonObject jsonInput) {
        try {
            JsonObjectResponseWriter jsonObjectResponseWriter = new JsonObjectResponseWriter(jsonInput);
            executeSync(jsonInput, jsonObjectResponseWriter);
            return jsonObjectResponseWriter.getExecutionResponse();
        } catch (Throwable t) {
            if (t.getClass().isAssignableFrom(RuntimeException.class)) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t);
            }
        }
    }

    public void executeSync(JsonObject jsonInput, ExecutionResponseWriter writer) {
        executeSync(jsonInput, new HashMap<>(), writer);
    }

    public void executeSync(JsonObject jsonInput, Map<String, Object> context, ExecutionResponseWriter writer) {
        execute(jsonInput, context, writer, false);
    }

    public void executeAsync(JsonObject jsonInput, ExecutionResponseWriter writer) {
        executeAsync(jsonInput, new HashMap<>(), writer);
    }

    public void executeAsync(JsonObject jsonInput, Map<String, Object> context, ExecutionResponseWriter writer) {
        execute(jsonInput, context, writer, true);
    }

    public void execute(JsonObject jsonInput, ExecutionResponseWriter writer, boolean async) {
        execute(jsonInput, new HashMap<>(), writer, async);
    }

    public void execute(JsonObject jsonInput, Map<String, Object> context, ExecutionResponseWriter writer, boolean async) {
        SmallRyeContext smallRyeContext = new SmallRyeContext(jsonInput);

        // ExecutionId
        ExecutionId finalExecutionId = ExecutionId.from(executionIdPrefix + executionId.getAndIncrement());

        try {
            String query = smallRyeContext.getQuery();
            Optional<Map<String, Object>> variables = smallRyeContext.getVariables();

            if (query == null || query.isEmpty()) {
                throw new RuntimeException("Query can not be null");
            }
            if (payloadOption.equals(LogPayloadOption.queryOnly)) {
                log.payloadIn(query);
            } else if (payloadOption.equals(LogPayloadOption.queryAndVariables)) {
                log.payloadIn(query);
                log.payloadIn(variables.toString());
            }

            GraphQL g = getGraphQL();
            if (g != null) {
                // Query
                Builder executionBuilder = ExecutionInput.newExecutionInput()
                        .query(query)
                        .executionId(finalExecutionId);

                // Variables
                smallRyeContext.getVariables().ifPresent(executionBuilder::variables);

                // Operation name
                smallRyeContext.getOperationName().ifPresent(executionBuilder::operationName);

                // DataLoaders
                if (batchOperations != null && !batchOperations.isEmpty()) {
                    DataLoaderRegistry dataLoaderRegistry = getDataLoaderRegistry(batchOperations);
                    executionBuilder.dataLoaderRegistry(dataLoaderRegistry);
                }

                ExecutionInput executionInput = executionBuilder.build();

                // Update context with execution data
                smallRyeContext = smallRyeContext.withDataFromExecution(executionInput, queryCache);

                // Context
                context.put(ContextHelper.CONTEXT, smallRyeContext);
                executionInput.getGraphQLContext().putAll(context);

                // Notify before
                eventEmitter.fireBeforeExecute(smallRyeContext);

                // Execute
                if (async) {
                    writeAsync(g, executionInput, context, writer);
                } else {
                    writeSync(g, executionInput, context, writer);
                }
            } else {
                log.noGraphQLMethodsFound();
            }
        } catch (Throwable t) {
            eventEmitter.fireOnExecuteError(finalExecutionId.toString(), t);
            writer.fail(t);
        }
    }

    private void writeAsync(GraphQL graphQL,
            ExecutionInput executionInput,
            Map<String, Object> context,
            ExecutionResponseWriter writer) {

        // Execute
        ThreadContext threadContext = ThreadContext.builder().build();
        CompletionStage<ExecutionResult> executionResult = threadContext
                .withContextCapture(graphQL.executeAsync(executionInput));

        executionResult.whenComplete((t, u) -> {
            executionInput.getGraphQLContext().putAll(context);

            SmallRyeContext smallryeContext = (SmallRyeContext) context.get(ContextHelper.CONTEXT);
            SmallRyeContext.setContext(smallryeContext);

            // Notify after
            eventEmitter.fireAfterExecute(smallryeContext);

            ExecutionResponse executionResponse = new ExecutionResponse(t);
            if (!payloadOption.equals(LogPayloadOption.off)) {
                log.payloadOut(executionResponse.toString());
            }
            writer.write(executionResponse);

            if (u != null) {
                writer.fail(u);
            }
        });
    }

    private void writeSync(GraphQL g,
            ExecutionInput executionInput,
            Map<String, Object> context,
            ExecutionResponseWriter writer) {
        try {
            ExecutionResult executionResult = g.execute(executionInput);
            // Notify after
            eventEmitter.fireAfterExecute((Context) context.get(ContextHelper.CONTEXT));

            ExecutionResponse executionResponse = new ExecutionResponse(executionResult);
            if (!payloadOption.equals(LogPayloadOption.off)) {
                log.payloadOut(executionResponse.toString());
            }

            writer.write(executionResponse);
        } catch (Throwable t) {
            writer.fail(t);
        }
    }

    private <K, T> DataLoaderRegistry getDataLoaderRegistry(List<Operation> operations) {
        DataLoaderRegistry dataLoaderRegistry = new DataLoaderRegistry();
        for (Operation operation : operations) {
            BatchLoaderWithContext<K, T> batchLoader = dataFetcherFactory.getSourceBatchLoader(operation);
            SmallRyeBatchLoaderContextProvider ctxProvider = new SmallRyeBatchLoaderContextProvider();
            DataLoaderOptions options = DataLoaderOptions.newOptions()
                    .setBatchLoaderContextProvider(ctxProvider);
            DataLoader<K, T> dataLoader = DataLoaderFactory.newDataLoader(batchLoader, options);
            ctxProvider.setDataLoader(dataLoader);
            dataLoaderRegistry.register(batchLoaderHelper.getName(operation), dataLoader);
        }
        return dataLoaderRegistry;
    }

    private GraphQL getGraphQL() {
        if (this.graphQL == null) {
            if (graphQLSchema != null) {
                GraphQL.Builder graphqlBuilder = GraphQL.newGraphQL(graphQLSchema);
                graphqlBuilder = graphqlBuilder.defaultDataFetcherExceptionHandler(new ExceptionHandler());
                graphqlBuilder = graphqlBuilder.instrumentation(queryCache);
                graphqlBuilder = graphqlBuilder.preparsedDocumentProvider(queryCache);

                if (queryExecutionStrategy != null) {
                    graphqlBuilder = graphqlBuilder.queryExecutionStrategy(queryExecutionStrategy);
                }

                if (mutationExecutionStrategy != null) {
                    graphqlBuilder = graphqlBuilder.queryExecutionStrategy(mutationExecutionStrategy);
                }
                if (hasSubscription) {
                    graphqlBuilder = graphqlBuilder
                            .subscriptionExecutionStrategy(new SubscriptionExecutionStrategy(new ExceptionHandler()));
                }

                // Allow custom extension
                graphqlBuilder = eventEmitter.fireBeforeGraphQLBuild(graphqlBuilder);

                this.graphQL = graphqlBuilder.build();
            } else {
                log.noGraphQLMethodsFound();
            }
        }
        return this.graphQL;

    }
}
