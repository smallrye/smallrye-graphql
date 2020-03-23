/*
 * Copyright 2020 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.smallrye.graphql.execution;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
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
import io.smallrye.graphql.bootstrap.SmallRyeGraphQLBootstrap;
import io.smallrye.graphql.execution.error.ExceptionHandler;
import io.smallrye.graphql.execution.error.ExecutionErrorsService;

/**
 * Executing the GraphQL request
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class ExecutionService {
    private static final Logger LOG = Logger.getLogger(ExecutionService.class.getName());

    private final ExecutionErrorsService errorsService = new ExecutionErrorsService();

    private GraphQL graphQL;

    @Inject
    private GraphQLConfig config;

    @PostConstruct
    public void init() {
        ExceptionHandler exceptionHandler = new ExceptionHandler(config);
        this.graphQL = getGraphQL(SmallRyeGraphQLBootstrap.GRAPHQL_SCHEMA, exceptionHandler);
    }

    public JsonObject execute(JsonObject jsonInput) {
        String query = jsonInput.getString(QUERY);

        if (this.graphQL != null) {
            // Query
            ExecutionInput.Builder executionBuilder = ExecutionInput.newExecutionInput()
                    .query(query)
                    .executionId(ExecutionId.generate());

            // Variables
            if (jsonInput.containsKey(VARIABLES)) {
                JsonValue jvariables = jsonInput.get(VARIABLES);
                if (!jvariables.equals(JsonValue.NULL)
                        && !jvariables.equals(JsonValue.EMPTY_JSON_OBJECT)
                        && !jvariables.equals(JsonValue.EMPTY_JSON_ARRAY)) {
                    Map<String, Object> variables = toMap(jsonInput.getJsonObject(VARIABLES));
                    executionBuilder.variables(variables);
                }
            }

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

    private Map<String, Object> toMap(JsonObject jo) {
        Map<String, Object> ro = new HashMap<>();
        if (jo != null) {
            Set<Map.Entry<String, JsonValue>> entrySet = jo.entrySet();
            for (Map.Entry<String, JsonValue> es : entrySet) {
                ro.put(es.getKey(), toObject(es.getValue()));
            }
        }
        return ro;
    }

    private Object toObject(JsonValue jsonValue) {
        Object ret = null;
        JsonValue.ValueType typ = jsonValue.getValueType();
        if (null != typ)
            switch (typ) {
                case NUMBER:
                    ret = ((JsonNumber) jsonValue).bigDecimalValue();
                    break;
                case STRING:
                    ret = ((JsonString) jsonValue).getString();
                    break;
                case FALSE:
                    ret = Boolean.FALSE;
                    break;
                case TRUE:
                    ret = Boolean.TRUE;
                    break;
                case ARRAY:
                    JsonArray arr = (JsonArray) jsonValue;
                    List<Object> vals = new ArrayList<>();
                    int sz = arr.size();
                    for (int i = 0; i < sz; i++) {
                        JsonValue v = arr.get(i);
                        vals.add(toObject(v));
                    }
                    ret = vals;
                    break;
                case OBJECT:
                    ret = jsonValue.toString();
                    break;
                default:
                    break;
            }
        return ret;
    }

    private GraphQL getGraphQL(GraphQLSchema graphQLSchema, ExceptionHandler exceptionHandler) {
        if (graphQLSchema != null) {
            return GraphQL
                    .newGraphQL(SmallRyeGraphQLBootstrap.GRAPHQL_SCHEMA)
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
