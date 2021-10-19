package io.smallrye.graphql.execution;

import static io.smallrye.graphql.SmallRyeGraphQLServerLogging.log;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import javax.json.JsonObject;

import org.dataloader.BatchLoaderWithContext;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderOptions;
import org.dataloader.DataLoaderRegistry;

import graphql.ExecutionInput;
import graphql.ExecutionInput.Builder;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLContext;
import graphql.execution.ExecutionId;
import graphql.execution.SubscriptionExecutionStrategy;
import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.bootstrap.DataFetcherFactory;
import io.smallrye.graphql.execution.context.SmallRyeBatchLoaderContextProvider;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.execution.datafetcher.helper.BatchLoaderHelper;
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

    public ExecutionService(GraphQLSchema graphQLSchema, List<Operation> batchOperations,
            boolean hasSubscription) {

        this.graphQLSchema = graphQLSchema;
        this.batchOperations = batchOperations;
        // use schema's hash as prefix to differentiate between multiple apps
        this.executionIdPrefix = Integer.toString(Objects.hashCode(graphQLSchema));
        this.hasSubscription = hasSubscription;
        this.queryCache = new QueryCache();

        Config config = Config.get();
        this.payloadOption = config.logPayload();
    }

    public ExecutionResponse execute(JsonObject jsonInput) {
        SmallRyeContext context = new SmallRyeContext(jsonInput);

        // ExecutionId
        ExecutionId finalExecutionId = ExecutionId.from(executionIdPrefix + executionId.getAndIncrement());

        try {
            String query = context.getQuery();
            Optional<Map<String, Object>> variables = context.getVariables();

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
                context.getVariables().ifPresent(executionBuilder::variables);

                // Operation name
                context.getOperationName().ifPresent(executionBuilder::operationName);

                // Context
                executionBuilder.context(toGraphQLContext(context));

                // DataLoaders
                if (batchOperations != null && !batchOperations.isEmpty()) {
                    DataLoaderRegistry dataLoaderRegistry = getDataLoaderRegistry(batchOperations);
                    executionBuilder.dataLoaderRegistry(dataLoaderRegistry);
                }

                ExecutionInput executionInput = executionBuilder.build();

                // Update context with execution data
                context = context.withDataFromExecution(executionInput, queryCache);
                ((GraphQLContext) executionInput.getContext()).put("context", context);

                // Notify before
                eventEmitter.fireBeforeExecute(context);
                // Execute
                ExecutionResult executionResult = g.execute(executionInput);
                // Notify after
                eventEmitter.fireAfterExecute(context);

                ExecutionResponse executionResponse = new ExecutionResponse(executionResult);
                if (!payloadOption.equals(LogPayloadOption.off)) {
                    log.payloadOut(executionResponse.toString());
                }

                return executionResponse;
            } else {
                log.noGraphQLMethodsFound();
                return null;
            }
        } catch (Throwable t) {
            eventEmitter.fireOnExecuteError(finalExecutionId.toString(), t);
            throw t; // TODO: can I remove that?
        }
    }

    private <K, T> DataLoaderRegistry getDataLoaderRegistry(List<Operation> operations) {
        DataLoaderRegistry dataLoaderRegistry = new DataLoaderRegistry();
        for (Operation operation : operations) {
            BatchLoaderWithContext<K, T> batchLoader = dataFetcherFactory.getSourceBatchLoader(operation);
            SmallRyeBatchLoaderContextProvider ctxProvider = new SmallRyeBatchLoaderContextProvider();
            DataLoaderOptions options = DataLoaderOptions.newOptions()
                    .setBatchLoaderContextProvider(ctxProvider);
            DataLoader<K, T> dataLoader = DataLoader.newDataLoader(batchLoader, options);
            ctxProvider.setDataLoader(dataLoader);
            dataLoaderRegistry.register(batchLoaderHelper.getName(operation), dataLoader);
        }
        return dataLoaderRegistry;
    }

    private GraphQLContext toGraphQLContext(Context context) {
        GraphQLContext.Builder builder = GraphQLContext.newContext();
        builder = builder.of("context", context);
        return builder.build();
    }

    private GraphQL getGraphQL() {
        if (this.graphQL == null) {
            if (graphQLSchema != null) {
                GraphQL.Builder graphqlBuilder = GraphQL.newGraphQL(graphQLSchema);
                graphqlBuilder = graphqlBuilder.defaultDataFetcherExceptionHandler(new ExceptionHandler());
                graphqlBuilder = graphqlBuilder.instrumentation(queryCache);
                graphqlBuilder = graphqlBuilder.preparsedDocumentProvider(queryCache);

                if (hasSubscription) {
                    graphqlBuilder = graphqlBuilder.subscriptionExecutionStrategy(new SubscriptionExecutionStrategy());
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
