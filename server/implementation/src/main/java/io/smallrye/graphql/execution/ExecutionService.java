package io.smallrye.graphql.execution;

import static io.smallrye.graphql.SmallRyeGraphQLServerLogging.log;

import java.io.StringReader;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonValue;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.dataloader.DataLoaderRegistry;

import graphql.ExecutionInput;
import graphql.ExecutionInput.Builder;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLContext;
import graphql.GraphQLError;
import graphql.execution.ExecutionId;
import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.execution.error.ExceptionHandler;
import io.smallrye.graphql.execution.error.ExecutionErrorsService;
import io.smallrye.graphql.execution.event.EventEmitter;

/**
 * Executing the GraphQL request
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ExecutionService {

    private static final JsonBuilderFactory jsonObjectFactory = Json.createBuilderFactory(null);
    private static final JsonReaderFactory jsonReaderFactory = Json.createReaderFactory(null);
    private static final Jsonb jsonB = JsonbBuilder.create(new JsonbConfig()
            .withNullValues(Boolean.TRUE)
            .withFormatting(Boolean.TRUE));

    private final String executionIdPrefix;
    private final AtomicLong executionId = new AtomicLong();

    private final ExecutionErrorsService errorsService = new ExecutionErrorsService();

    private final Config config;

    private final GraphQLSchema graphQLSchema;

    private final DataLoaderRegistry dataLoaderRegistry;

    private final EventEmitter eventEmitter;

    private GraphQL graphQL;

    public ExecutionService(Config config, GraphQLSchema graphQLSchema) {
        this(config, graphQLSchema, null);
    }

    public ExecutionService(Config config, GraphQLSchema graphQLSchema, DataLoaderRegistry dataLoaderRegistry) {
        this.config = config;
        this.graphQLSchema = graphQLSchema;
        this.dataLoaderRegistry = dataLoaderRegistry;
        this.eventEmitter = EventEmitter.getInstance(config);
        // use schema's hash as prefix to differentiate between multiple apps
        this.executionIdPrefix = Integer.toString(Objects.hashCode(graphQLSchema));
    }

    public JsonObject execute(JsonObject jsonInput) {
        SmallRyeContext.register(jsonInput);

        // ExecutionId
        ExecutionId finalExecutionId = ExecutionId.from(executionIdPrefix + executionId.getAndIncrement());

        try {
            Context context = SmallRyeContext.getContext();

            String query = context.getQuery();

            if (config.logPayload()) {
                log.payloadIn(query);
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
                if (dataLoaderRegistry != null) {
                    executionBuilder.dataLoaderRegistry(dataLoaderRegistry);
                }

                ExecutionInput executionInput = executionBuilder.build();
                // Update context
                SmallRyeContext.setDataFromExecution(executionInput);
                // Notify before
                eventEmitter.fireBeforeExecute();
                // Execute
                ExecutionResult executionResult = g.execute(executionInput);
                // Notify after
                eventEmitter.fireAfterExecute();

                JsonObjectBuilder returnObjectBuilder = jsonObjectFactory.createObjectBuilder();

                // Errors
                returnObjectBuilder = addErrorsToResponse(returnObjectBuilder, executionResult);
                // Data
                returnObjectBuilder = addDataToResponse(returnObjectBuilder, executionResult);

                JsonObject jsonResponse = returnObjectBuilder.build();

                if (config.logPayload()) {
                    log.payloadOut(jsonResponse.toString());
                }

                return jsonResponse;
            } else {
                log.noGraphQLMethodsFound();
                return null;
            }
        } catch (Throwable t) {
            eventEmitter.fireOnExecuteError(finalExecutionId.toString(), t);
            throw t;
        } finally {
            SmallRyeContext.remove();
        }
    }

    private GraphQLContext toGraphQLContext(Context context) {
        GraphQLContext.Builder builder = GraphQLContext.newContext();
        builder = builder.of("context", context);
        return builder.build();
    }

    private JsonObjectBuilder addDataToResponse(JsonObjectBuilder returnObjectBuilder, ExecutionResult executionResult) {
        Object pojoData = executionResult.getData();
        return addDataToResponse(returnObjectBuilder, pojoData);
    }

    private JsonObjectBuilder addDataToResponse(JsonObjectBuilder returnObjectBuilder, Object pojoData) {
        if (pojoData != null) {
            JsonValue data = toJsonValue(pojoData);
            return returnObjectBuilder.add(DATA, data);
        } else {
            return returnObjectBuilder.addNull(DATA);
        }
    }

    private JsonObjectBuilder addErrorsToResponse(JsonObjectBuilder returnObjectBuilder, ExecutionResult executionResult) {
        List<GraphQLError> errors = executionResult.getErrors();
        if (errors != null) {
            JsonArray jsonArray = errorsService.toJsonErrors(errors);
            if (!jsonArray.isEmpty()) {
                returnObjectBuilder = returnObjectBuilder.add(ERRORS, jsonArray);
            }
            return returnObjectBuilder;
        } else {
            return returnObjectBuilder;
        }

    }

    private JsonValue toJsonValue(Object pojo) {
        String json = jsonB.toJson(pojo);
        try (StringReader sr = new StringReader(json); JsonReader reader = jsonReaderFactory.createReader(sr)) {
            return reader.readValue();
        }
    }

    private GraphQL getGraphQL() {
        if (this.graphQL == null) {
            ExceptionHandler exceptionHandler = new ExceptionHandler(config);
            if (graphQLSchema != null) {
                QueryCache queryCache = new QueryCache();

                GraphQL.Builder graphqlBuilder = GraphQL.newGraphQL(graphQLSchema);

                graphqlBuilder = graphqlBuilder.queryExecutionStrategy(new QueryExecutionStrategy(exceptionHandler));
                graphqlBuilder = graphqlBuilder.mutationExecutionStrategy(new MutationExecutionStrategy(exceptionHandler));
                graphqlBuilder = graphqlBuilder.instrumentation(queryCache);
                graphqlBuilder = graphqlBuilder.preparsedDocumentProvider(queryCache);

                // Allow custom extension
                graphqlBuilder = eventEmitter.fireBeforeGraphQLBuild(graphqlBuilder);

                this.graphQL = graphqlBuilder.build();
            } else {
                log.noGraphQLMethodsFound();
            }
        }
        return this.graphQL;

    }

    private static final String DATA = "data";
    private static final String ERRORS = "errors";
}
