package io.smallrye.graphql.execution;

import static io.smallrye.graphql.JsonProviderHolder.JSON_PROVIDER;

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

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

import graphql.ExecutionResult;
import graphql.GraphQLError;
import io.smallrye.graphql.execution.error.ExecutionErrorsService;
import io.smallrye.graphql.spi.config.Config;

/**
 * Response from an execution
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ExecutionResponse {

    private static final JsonBuilderFactory jsonObjectFactory = JSON_PROVIDER.createBuilderFactory(null);
    private static final JsonReaderFactory jsonReaderFactory = JSON_PROVIDER.createReaderFactory(null);
    private static final Jsonb jsonB = JsonbBuilder.create(new JsonbConfig()
            .withNullValues(Boolean.TRUE)
            .withFormatting(Boolean.TRUE));
    private static final ExecutionErrorsService errorsService = new ExecutionErrorsService();

    private final ExecutionResult executionResult;

    private Map<String, Object> addedExtensions;

    private final Stack<Object> pathBuffer = (Config.get().isExcludeNullFieldsInResponses()) ? new Stack<>() : null;

    private final Set<List<Object>> errorPaths;

    public ExecutionResponse(ExecutionResult executionResult) {
        this(executionResult, null);
    }

    public ExecutionResponse(ExecutionResult executionResult, Map<String, Object> addedExtensions) {
        this.executionResult = executionResult;
        this.addedExtensions = addedExtensions;
        this.errorPaths = (executionResult != null)
                ? executionResult.getErrors().stream().map(GraphQLError::getPath).collect(Collectors.toSet())
                : Set.of();
    }

    public String toString() {
        return "ExecutionResponse->" + executionResult;
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

        return returnObjectBuilder.build();
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
        }
        return returnObjectBuilder;
    }

    private JsonObjectBuilder addDataToResponse(JsonObjectBuilder returnObjectBuilder, ExecutionResult executionResult) {
        if (!executionResult.isDataPresent()) {
            return returnObjectBuilder;
        }
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
        if (extensions != null) { // ERRORS
            returnObjectBuilder = addExtensionToBuilder(extensions, returnObjectBuilder);
        } else if (addedExtensions != null && !addedExtensions.isEmpty()) { // ADDED EXTENSIONS
            returnObjectBuilder = addExtensionToBuilder(new HashMap(addedExtensions), returnObjectBuilder);
        }
        return returnObjectBuilder;
    }

    private JsonObjectBuilder addExtensionToBuilder(Map<Object, Object> extensions, JsonObjectBuilder returnObjectBuilder) {
        JsonObject extensionsObject = buildExtensions(extensions);
        return returnObjectBuilder.add(EXTENSIONS, extensionsObject);
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
        } else if (pojo instanceof JsonValue) {
            return (JsonValue) pojo;
        } else if (pojo instanceof Map) {
            JsonObjectBuilder jsonObjectBuilder = jsonObjectFactory.createObjectBuilder();
            Map<String, Object> map = (Map<String, Object>) pojo;
            map.forEach((key, value) -> {
                pushToPathBuffer(key);
                JsonValue convertedValue = toJsonValue(value);
                if ((!Config.get().isExcludeNullFieldsInResponses()) || (convertedValue != JsonValue.NULL
                        || errorPaths.contains(pathBuffer))) {
                    jsonObjectBuilder.add(key, convertedValue);
                }
                popFromThePathBuffer();
            });
            jsonValue = jsonObjectBuilder.build();
        } else if (pojo instanceof Collection) {
            Collection<Object> map = ((Collection<Object>) pojo);
            JsonArrayBuilder builder = jsonObjectFactory.createArrayBuilder();
            int index = 0;
            for (final Object o : map) {
                pushToPathBuffer(index);
                builder.add(toJsonValue(o));
                popFromThePathBuffer();
                index++;
            }
            jsonValue = builder.build();
        } else if (pojo instanceof Boolean) {
            if (((Boolean) pojo)) {
                jsonValue = JsonValue.TRUE;
            } else {
                jsonValue = JsonValue.FALSE;
            }
        } else if (pojo instanceof String) {
            jsonValue = JSON_PROVIDER.createValue(((String) pojo));
        } else if (pojo instanceof Double) {
            jsonValue = JSON_PROVIDER.createValue(((Number) pojo).doubleValue());
        } else if (pojo instanceof Float) {
            //upcast to double would lead to precision loss
            jsonValue = JSON_PROVIDER.createValue(new BigDecimal(String.valueOf(((Number) pojo).floatValue())));
        } else if (pojo instanceof Long) {
            jsonValue = JSON_PROVIDER.createValue(((Long) pojo));
        } else if (pojo instanceof Integer || pojo instanceof Short || pojo instanceof Byte) {
            jsonValue = JSON_PROVIDER.createValue(((Number) pojo).intValue());
        } else if (pojo instanceof BigDecimal) {
            jsonValue = JSON_PROVIDER.createValue(((BigDecimal) pojo));
        } else if (pojo instanceof BigInteger) {
            jsonValue = JSON_PROVIDER.createValue(((BigInteger) pojo));
        } else if (pojo instanceof Enum<?>) {
            jsonValue = JSON_PROVIDER.createValue(((Enum<?>) pojo).name());
        } else {
            String json = jsonB.toJson(pojo);
            try (StringReader sr = new StringReader(json); JsonReader reader = jsonReaderFactory.createReader(sr)) {
                jsonValue = reader.readValue();
            }
        }

        return jsonValue;
    }

    private void pushToPathBuffer(Object object) {
        if (pathBuffer != null) {
            pathBuffer.push(object);
        }
    }

    private void popFromThePathBuffer() {
        if (pathBuffer != null) {
            pathBuffer.pop();
        }
    }

    private static final String DATA = "data";
    private static final String ERRORS = "errors";
    private static final String EXTENSIONS = "extensions";
}
