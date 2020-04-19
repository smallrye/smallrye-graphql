package io.smallrye.graphql.execution;

import java.io.StringReader;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.jboss.logging.Logger;

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
    private static final Logger LOG = Logger.getLogger(ExecutionService.class.getName());

    private final ExecutionErrorsService errorsService = new ExecutionErrorsService();

    private GraphQL graphQL;

    private Config config;

    private GraphQLSchema graphQLSchema;

    public ExecutionService(Config config, GraphQLSchema graphQLSchema) {
        this.config = config;
        this.graphQLSchema = graphQLSchema;
        this.graphQL = getGraphQL();
    }

    public JsonObject execute(JsonObject jsonInput) {
        String query = jsonInput.getString(QUERY);

        if (this.graphQL != null) {
            // Query
            ExecutionInput.Builder executionBuilder = ExecutionInput.newExecutionInput()
                    .query(query)
                    .executionId(ExecutionId.generate());

            // Variables
            JsonValue jvariables = jsonInput.get(VARIABLES);
            GraphQLVariables.getVariables(jvariables).ifPresent(executionBuilder::variables);

            // Operation name
            if (jsonInput.containsKey(OPERATION_NAME)) {
                String operationName = jsonInput.getString(OPERATION_NAME);
                executionBuilder.operationName(operationName);
            }

            ExecutionInput executionInput = executionBuilder.build();

            ExecutionResult executionResult = this.graphQL.execute(executionInput);

            JsonObjectBuilder returnObjectBuilder = Json.createObjectBuilder();

            // Errors
            returnObjectBuilder = addErrorsToResponse(returnObjectBuilder, executionResult);
            // Data
            returnObjectBuilder = addDataToResponse(returnObjectBuilder, executionResult);

            return returnObjectBuilder.build();
        } else {
            LOG.warn("Are you sure you have annotated your methods with @Query or @Mutation ?");
            LOG.warn("\t" + query);
            return null;
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
        JsonbConfig jsonbConfig = new JsonbConfig()
                .withNullValues(Boolean.TRUE)
                .withFormatting(Boolean.TRUE);

        try (Jsonb jsonb = JsonbBuilder.create(jsonbConfig)) {
            String json = jsonb.toJson(pojo);
            try (StringReader sr = new StringReader(json);
                    JsonReader reader = Json.createReader(sr)) {
                return reader.readValue();
            }
        } catch (Exception e) {
            LOG.warn("Could not close Jsonb object");
            return null;
        }
    }

    private GraphQL getGraphQL() {
        ExceptionHandler exceptionHandler = new ExceptionHandler(config);
        if (graphQLSchema != null) {
            return GraphQL
                    .newGraphQL(graphQLSchema)
                    .queryExecutionStrategy(new QueryExecutionStrategy(exceptionHandler))
                    .mutationExecutionStrategy(new MutationExecutionStrategy(exceptionHandler))
                    .build();
        } else {
            LOG.warn("No GraphQL methods found. Try annotating your methods with @Query or @Mutation");
        }
        return null;
    }

    private static final String QUERY = "query";
    private static final String VARIABLES = "variables";
    private static final String OPERATION_NAME = "operationName";
    private static final String DATA = "data";
    private static final String ERRORS = "errors";

}
