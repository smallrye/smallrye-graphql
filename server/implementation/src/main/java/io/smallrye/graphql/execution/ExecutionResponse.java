package io.smallrye.graphql.execution;

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

import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.spi.JsonProvider;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();
    private static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance;
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

    public ObjectNode getExecutionResultAsJsonObject() {
        ObjectNode returnObject = NODE_FACTORY.objectNode();
        // Errors
        addErrorsToResponse(returnObject, executionResult);
        // Data
        addDataToResponse(returnObject, executionResult);
        // Extensions
        addExtensionsToResponse(returnObject, executionResult);

        return returnObject;
    }

    /**
     * Bridge method: converts the Jackson ObjectNode to a JSON-P JsonObject.
     * This is needed for callers that still use JSON-P types (websocket handlers, JsonObjectResponseWriter).
     * Will be removed when those callers are migrated to Jackson.
     */
    public JsonObject getExecutionResultAsJsonPObject() {
        String jsonString = getExecutionResultAsString();
        JsonProvider jsonProvider = JsonProvider.provider();
        try (StringReader sr = new StringReader(jsonString);
                JsonReader reader = jsonProvider.createReader(sr)) {
            return reader.readObject();
        }
    }

    public String getExecutionResultAsString() {
        try {
            return OBJECT_MAPPER.writeValueAsString(getExecutionResultAsJsonObject());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void addErrorsToResponse(ObjectNode returnObject, ExecutionResult executionResult) {
        List<GraphQLError> errors = executionResult.getErrors();
        if (errors != null) {
            ArrayNode jsonArray = errorsService.toJsonErrors(errors);
            if (jsonArray.size() > 0) {
                returnObject.set(ERRORS, jsonArray);
            }
        }
    }

    private void addDataToResponse(ObjectNode returnObject, ExecutionResult executionResult) {
        if (!executionResult.isDataPresent()) {
            return;
        }
        Object pojoData = executionResult.getData();
        addDataToResponse(returnObject, pojoData);
    }

    private void addDataToResponse(ObjectNode returnObject, Object pojoData) {
        if (pojoData != null) {
            JsonNode data = toJsonNode(pojoData);
            returnObject.set(DATA, data);
        } else {
            returnObject.putNull(DATA);
        }
    }

    private void addExtensionsToResponse(ObjectNode returnObject, ExecutionResult executionResult) {
        final Map<Object, Object> extensions = executionResult.getExtensions();
        if (extensions != null) { // ERRORS
            addExtensionToBuilder(extensions, returnObject);
        } else if (addedExtensions != null && !addedExtensions.isEmpty()) { // ADDED EXTENSIONS
            addExtensionToBuilder(new HashMap(addedExtensions), returnObject);
        }
    }

    private void addExtensionToBuilder(Map<Object, Object> extensions, ObjectNode returnObject) {
        ObjectNode extensionsObject = buildExtensions(extensions);
        returnObject.set(EXTENSIONS, extensionsObject);
    }

    private ObjectNode buildExtensions(final Map<Object, Object> extensions) {
        ObjectNode extensionsNode = NODE_FACTORY.objectNode();
        for (final Map.Entry<Object, Object> entry : extensions.entrySet()) {
            if (entry.getKey() instanceof String) {
                String key = ((String) entry.getKey());
                final JsonNode value = toJsonNode(entry.getValue());
                extensionsNode.set(key, value);
            }
        }
        return extensionsNode;
    }

    /**
     * Build a JsonNode from the provided Object.
     * <p>
     * </p>
     * GraphQL returns a limited set of values ({@code Collection}, {@code Map}, {@code Number}, {@code Boolean}, {@code Enum}),
     * so the json value is built by hand.
     * Additionally, Jackson's {@code ObjectMapper} is used as a fallback if a different type is encountered.
     *
     * @param pojo a java object, limited to {@code Collection}, {@code Map}, {@code Number}, {@code Boolean} and {@code Enum}
     * @return the json node
     */
    private JsonNode toJsonNode(Object pojo) {

        if (pojo == null) {
            return NullNode.getInstance();
        } else if (pojo instanceof JsonNode) {
            JsonNode node = (JsonNode) pojo;
            if (Config.get().isExcludeNullFieldsInResponses()) {
                return excludeNullFields(node);
            } else {
                return node;
            }
        } else if (pojo instanceof Map) {
            ObjectNode objectNode = NODE_FACTORY.objectNode();
            Map<String, Object> map = (Map<String, Object>) pojo;
            map.forEach((key, value) -> {
                pushToPathBuffer(key);
                JsonNode convertedValue = toJsonNode(value);
                if ((!Config.get().isExcludeNullFieldsInResponses()) || (!convertedValue.isNull()
                        || errorPaths.contains(pathBuffer))) {
                    objectNode.set(key, convertedValue);
                }
                popFromThePathBuffer();
            });
            return objectNode;
        } else if (pojo instanceof Collection) {
            Collection<Object> collection = ((Collection<Object>) pojo);
            ArrayNode arrayNode = NODE_FACTORY.arrayNode();
            int index = 0;
            for (final Object o : collection) {
                pushToPathBuffer(index);
                arrayNode.add(toJsonNode(o));
                popFromThePathBuffer();
                index++;
            }
            return arrayNode;
        } else if (pojo instanceof Boolean) {
            return NODE_FACTORY.booleanNode((Boolean) pojo);
        } else if (pojo instanceof String) {
            return NODE_FACTORY.textNode((String) pojo);
        } else if (pojo instanceof Double) {
            return NODE_FACTORY.numberNode((Double) pojo);
        } else if (pojo instanceof Float) {
            //upcast to double would lead to precision loss
            return NODE_FACTORY.numberNode(new BigDecimal(String.valueOf(((Number) pojo).floatValue())));
        } else if (pojo instanceof Long) {
            return NODE_FACTORY.numberNode((Long) pojo);
        } else if (pojo instanceof Integer || pojo instanceof Short || pojo instanceof Byte) {
            return NODE_FACTORY.numberNode(((Number) pojo).intValue());
        } else if (pojo instanceof BigDecimal) {
            return NODE_FACTORY.numberNode((BigDecimal) pojo);
        } else if (pojo instanceof BigInteger) {
            return NODE_FACTORY.numberNode((BigInteger) pojo);
        } else if (pojo instanceof Enum<?>) {
            return NODE_FACTORY.textNode(((Enum<?>) pojo).name());
        } else if (pojo instanceof jakarta.json.JsonValue) {
            return jsonPValueToJsonNode((jakarta.json.JsonValue) pojo);
        } else {
            try {
                return OBJECT_MAPPER.valueToTree(pojo);
            } catch (IllegalArgumentException e) {
                return NODE_FACTORY.textNode(pojo.toString());
            }
        }
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

    private static JsonNode jsonPValueToJsonNode(jakarta.json.JsonValue jsonPValue) {
        switch (jsonPValue.getValueType()) {
            case OBJECT:
                ObjectNode objectNode = NODE_FACTORY.objectNode();
                jakarta.json.JsonObject jsonPObject = jsonPValue.asJsonObject();
                for (String key : jsonPObject.keySet()) {
                    objectNode.set(key, jsonPValueToJsonNode(jsonPObject.get(key)));
                }
                return objectNode;
            case ARRAY:
                ArrayNode arrayNode = NODE_FACTORY.arrayNode();
                for (jakarta.json.JsonValue item : jsonPValue.asJsonArray()) {
                    arrayNode.add(jsonPValueToJsonNode(item));
                }
                return arrayNode;
            case STRING:
                return NODE_FACTORY.textNode(((jakarta.json.JsonString) jsonPValue).getString());
            case NUMBER:
                jakarta.json.JsonNumber jsonPNumber = (jakarta.json.JsonNumber) jsonPValue;
                if (jsonPNumber.isIntegral()) {
                    try {
                        return NODE_FACTORY.numberNode(jsonPNumber.longValueExact());
                    } catch (ArithmeticException e) {
                        return NODE_FACTORY.numberNode(jsonPNumber.bigIntegerValue());
                    }
                }
                return NODE_FACTORY.numberNode(jsonPNumber.bigDecimalValue());
            case TRUE:
                return NODE_FACTORY.booleanNode(true);
            case FALSE:
                return NODE_FACTORY.booleanNode(false);
            case NULL:
                return NODE_FACTORY.nullNode();
            default:
                return NODE_FACTORY.nullNode();
        }
    }

    private static JsonNode excludeNullFields(JsonNode jsonNode) {
        if (jsonNode instanceof ObjectNode) {
            ObjectNode objectNode = (ObjectNode) jsonNode;
            ObjectNode result = NODE_FACTORY.objectNode();
            objectNode.fields().forEachRemaining(entry -> {
                if (entry.getValue() != null && !entry.getValue().isNull()) {
                    result.set(entry.getKey(), excludeNullFields(entry.getValue()));
                }
            });
            return result;
        } else if (jsonNode instanceof ArrayNode) {
            ArrayNode arrayNode = (ArrayNode) jsonNode;
            ArrayNode result = NODE_FACTORY.arrayNode();
            for (JsonNode value : arrayNode) {
                result.add(excludeNullFields(value));
            }
            return result;
        } else {
            return jsonNode;
        }
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        mapper.disable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    private static final String DATA = "data";
    private static final String ERRORS = "errors";
    private static final String EXTENSIONS = "extensions";
}
