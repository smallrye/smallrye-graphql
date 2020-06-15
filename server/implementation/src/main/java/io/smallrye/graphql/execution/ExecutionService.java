package io.smallrye.graphql.execution;

import static io.smallrye.graphql.SmallRyeGraphQLServerLogging.log;

import java.io.StringReader;
import java.util.ArrayList;
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

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.execution.ExecutionId;
import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.execution.error.ExceptionHandler;
import io.smallrye.graphql.execution.error.ExecutionErrorsService;

/**
 * Executing the GraphQL request
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ExecutionService {

    private static final JsonBuilderFactory jsonObjectFactory = Json.createBuilderFactory(null);
    private static final JsonReaderFactory jsonReaderFactory = Json.createReaderFactory(null);
    private static final Jsonb JSONB = JsonbBuilder.create(new JsonbConfig()
            .withNullValues(Boolean.TRUE)
            .withFormatting(Boolean.TRUE));

    private final String executionIdPrefix;
    private final AtomicLong executionId = new AtomicLong();

    private final GraphQLVariables graphQLVariables = new GraphQLVariables();

    private final ExecutionErrorsService errorsService = new ExecutionErrorsService();

    private final Config config;

    private final GraphQLSchema graphQLSchema;

    private GraphQL graphQL;

    private final List<ExecutionDecorator> executionDecorators = new ArrayList<>();

    public ExecutionService(Config config, GraphQLSchema graphQLSchema) {
        this.config = config;
        this.graphQLSchema = graphQLSchema;
        // use schema's hash as prefix to differentiate between multiple apps
        this.executionIdPrefix = Integer.toString(Objects.hashCode(graphQLSchema));

        if (config != null && config.isTracingEnabled()) {
            executionDecorators.add(new OpenTracingExecutionDecorator());
        }
    }

    public JsonObject execute(JsonObject jsonInput) {
        String query = jsonInput.getString(QUERY);

        if (config.logPayload()) {
            log.payloadIn(query);
        }

        GraphQL g = getGraphQL();
        if (g != null) {
            // Query
            ExecutionInput.Builder executionBuilder = ExecutionInput.newExecutionInput()
                    .query(query)
                    .executionId(ExecutionId.from(executionIdPrefix + executionId.getAndIncrement()));

            // Variables
            graphQLVariables.getVariables(jsonInput).ifPresent(executionBuilder::variables);

            // Operation name
            if (hasOperationName(jsonInput)) {
                executionBuilder.operationName(jsonInput.getString(OPERATION_NAME));
            }

            ExecutionInput executionInput = executionBuilder.build();

            ExecutionResult executionResult = execute(g, executionInput);

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
    }

    private ExecutionResult execute(final GraphQL g, final ExecutionInput executionInput) {

        for (ExecutionDecorator decorator : executionDecorators) {
            decorator.before(executionInput);
        }
        try {
            ExecutionResult executionResult = g.execute(executionInput);
            for (ExecutionDecorator decorator : executionDecorators) {
                decorator.after(executionInput, executionResult);
            }
            return executionResult;
        } catch (Throwable e) {
            for (ExecutionDecorator decorator : executionDecorators) {
                decorator.onError(executionInput, e);
            }
            throw e;
        }
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
        String json = JSONB.toJson(pojo);
        try (StringReader sr = new StringReader(json); JsonReader reader = jsonReaderFactory.createReader(sr)) {
            return reader.readValue();
        }
    }

    private GraphQL getGraphQL() {
        if (this.graphQL == null) {
            ExceptionHandler exceptionHandler = new ExceptionHandler(config);
            if (graphQLSchema != null) {
                QueryCache queryCache = new QueryCache();
                this.graphQL = GraphQL
                        .newGraphQL(graphQLSchema)
                        .queryExecutionStrategy(new QueryExecutionStrategy(exceptionHandler))
                        .mutationExecutionStrategy(new MutationExecutionStrategy(exceptionHandler))
                        .instrumentation(queryCache)
                        .preparsedDocumentProvider(queryCache)
                        .build();
            } else {
                log.noGraphQLMethodsFound();
            }
        }
        return this.graphQL;

    }

    private boolean hasOperationName(JsonObject jsonInput) {
        return jsonInput.containsKey(OPERATION_NAME)
                && jsonInput.get(OPERATION_NAME) != null
                && !jsonInput.get(OPERATION_NAME).getValueType().equals(JsonValue.ValueType.NULL);
    }

    private static final String QUERY = "query";
    private static final String OPERATION_NAME = "operationName";
    private static final String DATA = "data";
    private static final String ERRORS = "errors";
}
