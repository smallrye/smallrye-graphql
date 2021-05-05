package io.smallrye.graphql.execution;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

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

import graphql.ExecutionResult;
import graphql.GraphQLError;
import io.smallrye.graphql.execution.error.ExecutionErrorsService;

/**
 * Response from an execution
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ExecutionResponse {
    private static final JsonBuilderFactory jsonObjectFactory = Json.createBuilderFactory(null);
    private static final JsonReaderFactory jsonReaderFactory = Json.createReaderFactory(null);
    private static final Jsonb jsonB = JsonbBuilder.create(new JsonbConfig()
            .withNullValues(Boolean.TRUE)
            .withFormatting(Boolean.TRUE));

    private final ExecutionErrorsService errorsService = new ExecutionErrorsService();

    private ExecutionResult executionResult;

    public ExecutionResponse(ExecutionResult executionResult) {
        this.executionResult = executionResult;
    }

    public ExecutionResult getExecutionResult() {
        return this.executionResult;
    }

    public JsonObject getExecutionResultAsJsonObject() {
        JsonObjectBuilder returnObjectBuilder = jsonObjectFactory.createObjectBuilder();
        // Errors
        returnObjectBuilder = addErrorsToResponse(returnObjectBuilder, executionResult);
        // Data
        returnObjectBuilder = addDataToResponse(returnObjectBuilder, executionResult);
        // Extensions
        returnObjectBuilder = addExtensionsToResponse(returnObjectBuilder, executionResult);

        JsonObject jsonResponse = returnObjectBuilder.build();

        return jsonResponse;
    }

    public String getExecutionResultAsString() {
        return getExecutionResultAsJsonObject().toString();
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

    private JsonObjectBuilder addExtensionsToResponse(JsonObjectBuilder returnObjectBuilder, ExecutionResult executionResult) {
        final Map<Object, Object> extensions = executionResult.getExtensions();
        if (extensions != null) {
            JsonObject extensionsObject = buildExtensions(extensions);
            returnObjectBuilder = returnObjectBuilder.add(EXTENSIONS, extensionsObject);
        }
        return returnObjectBuilder;
    }

    private JsonObject buildExtensions(final Map<Object, Object> extensions) {
        JsonObjectBuilder extensionsBuilder = jsonObjectFactory.createObjectBuilder();
        for (final Map.Entry<Object, Object> entry : extensions.entrySet()) {
            if (entry.getKey() instanceof String) {
                String key = ((String) entry.getKey());
                final JsonValue value = toJsonValue(entry.getValue());
                extensionsBuilder.add(key, value);
            }
        }
        return extensionsBuilder.build();
    }

    private JsonValue toJsonValue(Object pojo) {
        String json = jsonB.toJson(pojo);
        try (StringReader sr = new StringReader(json); JsonReader reader = jsonReaderFactory.createReader(sr)) {
            return reader.readValue();
        }
    }

    private static final String DATA = "data";
    private static final String ERRORS = "errors";
    private static final String EXTENSIONS = "extensions";
}
