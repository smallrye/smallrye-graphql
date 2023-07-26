package io.smallrye.graphql.execution;

import static io.smallrye.graphql.SmallRyeGraphQLServerLogging.log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.json.JsonObject;

import org.dataloader.BatchLoaderWithContext;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderFactory;
import org.dataloader.DataLoaderRegistry;

import graphql.*;
import graphql.ExecutionInput.Builder;
import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.execution.ExecutionId;
import graphql.execution.ExecutionStrategy;
import graphql.execution.SubscriptionExecutionStrategy;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import graphql.parser.ParserOptions;
import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.DataFetcherFactory;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.execution.context.SmallRyeContextManager;
import io.smallrye.graphql.execution.datafetcher.helper.BatchLoaderHelper;
import io.smallrye.graphql.execution.error.ExceptionHandler;
import io.smallrye.graphql.execution.error.UnparseableDocumentException;
import io.smallrye.graphql.execution.event.EventEmitter;
import io.smallrye.graphql.execution.metrics.MetricsEmitter;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.schema.model.Type;
import io.smallrye.graphql.spi.config.Config;
import io.smallrye.graphql.spi.config.LogPayloadOption;
import io.smallrye.mutiny.Uni;

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
    private final Schema schema;

    private final EventEmitter eventEmitter = EventEmitter.getInstance();
    private final MetricsEmitter metricsEmitter = MetricsEmitter.getInstance();

    private GraphQL graphQL;

    private final QueryCache queryCache;
    private final LogPayloadOption payloadOption;

    private final ExecutionStrategy queryExecutionStrategy;
    private final ExecutionStrategy mutationExecutionStrategy;

    public ExecutionService(GraphQLSchema graphQLSchema, Schema schema) {
        this(graphQLSchema, schema, null, null);
    }

    public ExecutionService(GraphQLSchema graphQLSchema, Schema schema, ExecutionStrategy queryExecutionStrategy,
            ExecutionStrategy mutationExecutionStrategy) {

        this.graphQLSchema = graphQLSchema;
        this.schema = schema;
        // use schema's hash as prefix to differentiate between multiple apps
        this.executionIdPrefix = Integer.toString(Objects.hashCode(graphQLSchema));
        this.queryCache = new QueryCache();

        this.queryExecutionStrategy = queryExecutionStrategy;
        this.mutationExecutionStrategy = mutationExecutionStrategy;

        Config config = Config.get();
        this.payloadOption = config.logPayload();
    }

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
        SmallRyeContext smallRyeContext = SmallRyeContextManager.fromInitialRequest(jsonInput);

        // ExecutionId
        ExecutionId finalExecutionId = ExecutionId.from(executionIdPrefix + executionId.getAndIncrement());

        try {
            String query = smallRyeContext.getQuery();
            Optional<Map<String, Object>> variables = smallRyeContext.getVariables();

            if (query == null || query.isEmpty()) {
                sendError("Missing 'query' field in the request", writer);
                return;
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
                List<Operation> batchOperations = schema.getBatchOperations();
                if (batchOperations != null && !batchOperations.isEmpty()) {
                    DataLoaderRegistry dataLoaderRegistry = getDataLoaderRegistry(batchOperations);
                    executionBuilder.dataLoaderRegistry(dataLoaderRegistry);
                }

                ExecutionInput executionInput = executionBuilder.build();
                // Context
                try {
                    smallRyeContext = SmallRyeContextManager.populateFromExecutionInput(executionInput, queryCache);
                } catch (UnparseableDocumentException ex) {
                    sendError("Unparseable input document", writer);
                    return;
                }
                context.put(SmallRyeContextManager.CONTEXT, smallRyeContext);
                executionInput.getGraphQLContext().putAll(context);

                // Notify before
                eventEmitter.fireBeforeExecute(smallRyeContext);

                // Execute
                if (async) {
                    writeAsync(g, executionInput, smallRyeContext, writer);
                } else {
                    writeSync(g, executionInput, smallRyeContext, writer);
                }
            } else {
                log.noGraphQLMethodsFound();
            }
        } catch (Throwable t) {
            eventEmitter.fireOnExecuteError(smallRyeContext, t);
            writer.fail(t);
        }
    }

    private static void sendError(String errorMessage, ExecutionResponseWriter writer) {
        GraphQLError error = GraphqlErrorBuilder
                .newError()
                .message(errorMessage)
                .build();
        ExecutionResult executionResult = ExecutionResultImpl
                .newExecutionResult()
                .addError(error)
                .build();
        ExecutionResponse executionResponse = new ExecutionResponse(executionResult);
        writer.write(executionResponse);
    }

    private void writeAsync(GraphQL graphQL,
            ExecutionInput executionInput,
            SmallRyeContext smallRyeContext,
            ExecutionResponseWriter writer) {

        Uni.createFrom().completionStage(() -> graphQL.executeAsync(executionInput))

                .subscribe().with(executionResult -> {

                    SmallRyeContextManager.restore(smallRyeContext);

                    notifyAndWrite(smallRyeContext, executionResult, writer);

                }, failure -> {
                    if (failure != null) {
                        writer.fail(failure);
                    }
                });
    }

    private void writeSync(GraphQL g,
            ExecutionInput executionInput,
            SmallRyeContext smallRyeContext,
            ExecutionResponseWriter writer) {
        try {
            ExecutionResult executionResult = g.execute(executionInput);
            notifyAndWrite(smallRyeContext, executionResult, writer);
        } catch (Throwable t) {
            writer.fail(t);
        }
    }

    private void notifyAndWrite(SmallRyeContext smallRyeContext,
            ExecutionResult executionResult,
            ExecutionResponseWriter writer) {
        smallRyeContext.setExecutionResult(executionResult);
        // Notify after
        eventEmitter.fireAfterExecute(smallRyeContext);

        ExecutionResponse executionResponse = new ExecutionResponse(executionResult,
                smallRyeContext.getAddedExtensions());
        if (!payloadOption.equals(LogPayloadOption.off)) {
            log.payloadOut(executionResponse.toString());
        }

        writer.write(executionResponse);
    }

    private <K, T> DataLoaderRegistry getDataLoaderRegistry(List<Operation> operations) {
        DataLoaderRegistry dataLoaderRegistry = new DataLoaderRegistry();
        for (Operation operation : operations) {
            Map<String, Type> types = schema.getTypes();
            BatchLoaderWithContext<K, T> batchLoader = dataFetcherFactory.getSourceBatchLoader(operation,
                    types.get(operation.getName()));
            DataLoader<K, T> dataLoader = DataLoaderFactory.newDataLoader(batchLoader);
            dataLoaderRegistry.register(batchLoaderHelper.getName(operation), dataLoader);
        }
        return dataLoaderRegistry;
    }

    private GraphQL getGraphQL() {
        if (this.graphQL == null) {
            if (graphQLSchema != null) {
                Config config = Config.get();
                setParserOptions(config);

                GraphQL.Builder graphqlBuilder = GraphQL.newGraphQL(graphQLSchema);
                graphqlBuilder = graphqlBuilder.defaultDataFetcherExceptionHandler(new ExceptionHandler());

                List<Instrumentation> chainedList = new ArrayList<>();

                if (config.getQueryComplexityInstrumentation().isPresent()) {
                    chainedList.add(new MaxQueryComplexityInstrumentation(config.getQueryComplexityInstrumentation().get()));
                }
                if (config.getQueryDepthInstrumentation().isPresent()) {
                    chainedList.add(new MaxQueryDepthInstrumentation(config.getQueryDepthInstrumentation().get()));
                }
                chainedList.add(queryCache);
                // TODO: Allow users to add custome instumentations
                graphqlBuilder = graphqlBuilder.instrumentation(new ChainedInstrumentation(chainedList));

                graphqlBuilder = graphqlBuilder.preparsedDocumentProvider(queryCache);

                if (queryExecutionStrategy != null) {
                    graphqlBuilder = graphqlBuilder.queryExecutionStrategy(queryExecutionStrategy);
                }

                if (mutationExecutionStrategy != null) {
                    graphqlBuilder = graphqlBuilder.mutationExecutionStrategy(mutationExecutionStrategy);
                }
                if (schema.hasSubscriptions()) {
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

    private void setParserOptions(Config config) {
        if (config.hasParserOptions()) {
            ParserOptions.Builder parserOptionsBuilder = ParserOptions.newParserOptions();
            if (config.isParserCaptureIgnoredChars().isPresent()) {
                parserOptionsBuilder = parserOptionsBuilder
                        .captureIgnoredChars(config.isParserCaptureIgnoredChars().get());
            }
            if (config.isParserCaptureLineComments().isPresent()) {
                parserOptionsBuilder = parserOptionsBuilder
                        .captureLineComments(config.isParserCaptureLineComments().get());
            }
            if (config.isParserCaptureSourceLocation().isPresent()) {
                parserOptionsBuilder = parserOptionsBuilder
                        .captureSourceLocation(config.isParserCaptureSourceLocation().get());
            }
            if (config.getParserMaxTokens().isPresent()) {
                parserOptionsBuilder = parserOptionsBuilder.maxTokens(config.getParserMaxTokens().get());
            }
            if (config.getParserMaxWhitespaceTokens().isPresent()) {
                parserOptionsBuilder = parserOptionsBuilder
                        .maxWhitespaceTokens(config.getParserMaxWhitespaceTokens().get());
            }
            ParserOptions.setDefaultParserOptions(parserOptionsBuilder.build());
        }
    }
}
