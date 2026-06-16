package io.smallrye.graphql.execution.error;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.json.JsonValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.validation.ValidationError;
import io.smallrye.graphql.spi.config.Config;

/**
 * Help to create the exceptions
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ExecutionErrorsService {

    private static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ErrorExtensionProviders errorExtensionProviders = new ErrorExtensionProviders();

    private final Config config = Config.get();

    public ArrayNode toJsonErrors(List<GraphQLError> errors) {
        ArrayNode arrayNode = NODE_FACTORY.arrayNode();
        for (GraphQLError e : errors) {
            arrayNode.add(toJsonError(e));
        }
        return arrayNode;
    }

    private ObjectNode toJsonError(GraphQLError error) {
        ObjectNode jsonErrors = OBJECT_MAPPER.valueToTree(error.toSpecification());

        getOptionalExtensions(error).ifPresent(extensionsNode -> jsonErrors.set(EXTENSIONS, extensionsNode));
        return jsonErrors;
    }

    private Optional<ObjectNode> getOptionalExtensions(GraphQLError error) {
        if (error instanceof ValidationError) {
            return getValidationExtensions((ValidationError) error);
        } else if (error instanceof ExceptionWhileDataFetching) {
            return getDataFetchingExtensions((ExceptionWhileDataFetching) error);
        }
        return Optional.empty();
    }

    private Optional<ObjectNode> getValidationExtensions(ValidationError error) {
        if (config.getErrorExtensionFields().isPresent()) {
            ObjectNode objectNode = NODE_FACTORY.objectNode();
            addKeyValue(objectNode, Config.ERROR_EXTENSION_DESCRIPTION, error.getDescription());
            addKeyValue(objectNode, Config.ERROR_EXTENSION_VALIDATION_ERROR_TYPE, error.getValidationErrorType().toString());
            objectNode.set(Config.ERROR_EXTENSION_QUERY_PATH, toJsonArray(error.getQueryPath()));
            addKeyValue(objectNode, Config.ERROR_EXTENSION_CLASSIFICATION, error.getErrorType().toString());
            Map<String, Object> extensions = error.getExtensions();
            populateCustomExtensions(objectNode, extensions);
            return Optional.of(objectNode);
        }
        return Optional.empty();
    }

    private Optional<ObjectNode> getDataFetchingExtensions(ExceptionWhileDataFetching error) {
        if (config.getErrorExtensionFields().isPresent()) {
            Throwable exception = error.getException();

            ObjectNode objectNode = NODE_FACTORY.objectNode();
            addKeyValue(objectNode, Config.ERROR_EXTENSION_CLASSIFICATION, error.getErrorType().toString());
            addErrorExtensions(objectNode, exception);
            Map<String, Object> extensions = error.getExtensions();
            populateCustomExtensions(objectNode, extensions);

            return Optional.of(objectNode);
        }
        return Optional.empty();
    }

    private void addErrorExtensions(ObjectNode objectNode, Throwable exception) {
        errorExtensionProviders.get()
                .forEach(provider -> {
                    JsonValue jsonValue = provider.mapValueFrom(exception);
                    // Convert jakarta.json.JsonValue to Jackson JsonNode
                    JsonNode jacksonNode = convertJsonValueToJsonNode(jsonValue);
                    addKeyValue(objectNode, provider.getKey(), jacksonNode);
                });
    }

    /**
     * Bridge method: converts a jakarta.json.JsonValue to a Jackson JsonNode.
     * This is needed because ErrorExtensionProvider (in server/api) returns JSON-P types.
     */
    private JsonNode convertJsonValueToJsonNode(JsonValue jsonValue) {
        if (jsonValue == null) {
            return NODE_FACTORY.nullNode();
        }
        try {
            // Use the JSON-P toString() which produces valid JSON, then parse with Jackson
            return OBJECT_MAPPER.readTree(jsonValue.toString());
        } catch (Exception e) {
            // fallback: treat as string
            return NODE_FACTORY.textNode(jsonValue.toString());
        }
    }

    private void populateCustomExtensions(ObjectNode objectNode, Map<String, Object> extensions) {
        if (extensions != null) {
            for (Map.Entry<String, Object> entry : extensions.entrySet()) {
                if (config.getErrorExtensionFields().isEmpty()
                        || (config.getErrorExtensionFields().isPresent()
                                && config.getErrorExtensionFields().get().contains(entry.getKey()))) {
                    Object value = entry.getValue();
                    if (value instanceof JsonNode) {
                        addKeyValue(objectNode, entry.getKey(), (JsonNode) value);
                    } else if (value instanceof JsonValue) {
                        addKeyValue(objectNode, entry.getKey(), convertJsonValueToJsonNode((JsonValue) value));
                    } else if (value instanceof Map) {
                        addKeyValue(objectNode, entry.getKey(), OBJECT_MAPPER.valueToTree(value));
                    } else {
                        addKeyValue(objectNode, entry.getKey(), value != null ? value.toString() : null);
                    }
                }
            }
        }
    }

    private ArrayNode toJsonArray(List<?> list) {
        ArrayNode arrayNode = NODE_FACTORY.arrayNode();
        if (list != null && !list.isEmpty()) {
            for (Object o : list) {
                if (o != null)
                    arrayNode.add(o.toString());
            }
        }
        return arrayNode;
    }

    private void addKeyValue(ObjectNode objectNode, String key, String value) {
        addKeyValue(objectNode, key, value != null ? NODE_FACTORY.textNode(value) : NODE_FACTORY.nullNode());
    }

    private void addKeyValue(ObjectNode objectNode, String key, JsonNode value) {
        value = value != null ? value : NODE_FACTORY.nullNode();
        if (config.getErrorExtensionFields().isPresent()) {
            List<String> fieldsThatShouldBeIncluded = config.getErrorExtensionFields().get();
            if (fieldsThatShouldBeIncluded.contains(key)) {
                objectNode.set(key, value);
            }
        } else {
            objectNode.set(key, value);
        }
    }

    private static final String EXTENSIONS = "extensions";

}
