package io.smallrye.graphql.execution;

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;
import jakarta.json.JsonValue;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.spi.JsonProvider;

import graphql.ExecutionResult;
import graphql.GraphQLError;
import io.smallrye.graphql.execution.error.ExecutionErrorsService;

/**
 * Response from an execution
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ExecutionResponse {

    private static final JsonProvider jsonProvider = JsonProvider.provider();
    private static final JsonBuilderFactory jsonObjectFactory = Json.createBuilderFactory(null);
    private static final JsonReaderFactory jsonReaderFactory = Json.createReaderFactory(null);
    private static final Jsonb jsonB = JsonbBuilder.create(new JsonbConfig()
            .withNullValues(Boolean.TRUE)
            .withFormatting(Boolean.TRUE));
    private static final ExecutionErrorsService errorsService = new ExecutionErrorsService();

    private final ExecutionResult executionResult;

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

    /**
     * Build a JsonValue from the provided Object.
     * <p>
     * </p>
     * GraphQL returns a limited set of values ({@code Collection}, {@code Map}, {@code Number}, {@code Boolean}, {@code Enum}),
     * so the json value is build by hand.
     * Additionally, {@code JsonB} is used as a fallback if an different type is encountered.
     * 
     * @param pojo a java object, limited to {@code Collection}, {@code Map}, {@code Number}, {@code Boolean} and {@code Enum}
     * @return the json value
     */
    private JsonValue toJsonValue(Object pojo) {
        final JsonValue jsonValue;
        if (pojo == null) {
            return JsonValue.NULL;
        } else if (pojo instanceof Map) {
            JsonObjectBuilder jsonObjectBuilder = jsonObjectFactory.createObjectBuilder();
            Map<String, Object> map = (Map<String, Object>) pojo;
            for (final Map.Entry<String, Object> stringObjectEntry : map.entrySet()) {
                jsonObjectBuilder.add(stringObjectEntry.getKey(), toJsonValue(stringObjectEntry.getValue()));
            }
            jsonValue = jsonObjectBuilder.build();
        } else if (pojo instanceof Collection) {
            Collection<Object> map = ((Collection<Object>) pojo);
            JsonArrayBuilder builder = jsonObjectFactory.createArrayBuilder();
            for (final Object o : map) {
                builder.add(toJsonValue(o));
            }
            jsonValue = builder.build();
        } else if (pojo instanceof Boolean) {
            if (((Boolean) pojo)) {
                jsonValue = JsonValue.TRUE;
            } else {
                jsonValue = JsonValue.FALSE;
            }
        } else if (pojo instanceof String) {
            jsonValue = jsonProvider.createValue(((String) pojo));
        } else if (pojo instanceof Double) {
            jsonValue = jsonProvider.createValue(((Number) pojo).doubleValue());
        } else if (pojo instanceof Float) {
            //upcast to double would lead to precision loss
            jsonValue = jsonProvider.createValue(new BigDecimal(String.valueOf(((Number) pojo).floatValue())));
        } else if (pojo instanceof Long) {
            jsonValue = jsonProvider.createValue(((Long) pojo));
        } else if (pojo instanceof Integer || pojo instanceof Short || pojo instanceof Byte) {
            jsonValue = jsonProvider.createValue(((Number) pojo).intValue());
        } else if (pojo instanceof BigDecimal) {
            jsonValue = jsonProvider.createValue(((BigDecimal) pojo));
        } else if (pojo instanceof BigInteger) {
            jsonValue = jsonProvider.createValue(((BigInteger) pojo));
        } else if (pojo instanceof Enum<?>) {
            jsonValue = jsonProvider.createValue(((Enum<?>) pojo).name());
        } else {
            String json = jsonB.toJson(pojo);
            try (StringReader sr = new StringReader(json); JsonReader reader = jsonReaderFactory.createReader(sr)) {
                jsonValue = reader.readValue();
            }
        }

        return jsonValue;
    }

    private static final String DATA = "data";
    private static final String ERRORS = "errors";
    private static final String EXTENSIONS = "extensions";
}
