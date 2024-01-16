package io.smallrye.graphql.execution.error;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    private static final JsonBuilderFactory jsonBuilderFactory = Json.createBuilderFactory(null);
    private static final JsonReaderFactory jsonReaderFactory = Json.createReaderFactory(null);
    private static final Jsonb JSONB = JsonbBuilder.create(new JsonbConfig()
            .withNullValues(Boolean.TRUE)
            .withFormatting(Boolean.TRUE));

    private final ErrorExtensionProviders errorExtensionProviders = new ErrorExtensionProviders();

    private final Config config = Config.get();

    public JsonArray toJsonErrors(List<GraphQLError> errors) {
        JsonArrayBuilder arrayBuilder = jsonBuilderFactory.createArrayBuilder();
        for (GraphQLError e : errors) {
            arrayBuilder.add(toJsonError(e));
        }
        return arrayBuilder.build();
    }

    private JsonObject toJsonError(GraphQLError error) {
        String json = JSONB.toJson(error.toSpecification());
        try (StringReader sr = new StringReader(json); JsonReader reader = jsonReaderFactory.createReader(sr)) {

            JsonObject jsonErrors = reader.readObject();

            JsonObjectBuilder resultBuilder = jsonBuilderFactory.createObjectBuilder(jsonErrors);

            getOptionalExtensions(error).ifPresent(jsonObject -> resultBuilder.add(EXTENSIONS, jsonObject));
            return resultBuilder.build();
        }
    }

    private Optional<JsonObject> getOptionalExtensions(GraphQLError error) {
        if (error instanceof ValidationError) {
            return getValidationExtensions((ValidationError) error);
        } else if (error instanceof ExceptionWhileDataFetching) {
            return getDataFetchingExtensions((ExceptionWhileDataFetching) error);
        }
        return Optional.empty();
    }

    private Optional<JsonObject> getValidationExtensions(ValidationError error) {
        if (config.getErrorExtensionFields().isPresent()) {
            JsonObjectBuilder objectBuilder = jsonBuilderFactory.createObjectBuilder();
            addKeyValue(objectBuilder, Config.ERROR_EXTENSION_DESCRIPTION, error.getDescription());
            addKeyValue(objectBuilder, Config.ERROR_EXTENSION_VALIDATION_ERROR_TYPE, error.getValidationErrorType().toString());
            objectBuilder.add(Config.ERROR_EXTENSION_QUERY_PATH, toJsonArray(error.getQueryPath()));
            addKeyValue(objectBuilder, Config.ERROR_EXTENSION_CLASSIFICATION, error.getErrorType().toString());
            Map<String, Object> extensions = error.getExtensions();
            populateCustomExtensions(objectBuilder, extensions);
            return Optional.of(objectBuilder.build());
        }
        return Optional.empty();
    }

    private Optional<JsonObject> getDataFetchingExtensions(ExceptionWhileDataFetching error) {
        if (config.getErrorExtensionFields().isPresent()) {
            Throwable exception = error.getException();

            JsonObjectBuilder objectBuilder = jsonBuilderFactory.createObjectBuilder();
            addKeyValue(objectBuilder, Config.ERROR_EXTENSION_CLASSIFICATION, error.getErrorType().toString());
            addErrorExtensions(objectBuilder, exception);
            Map<String, Object> extensions = error.getExtensions();
            populateCustomExtensions(objectBuilder, extensions);

            return Optional.of(objectBuilder.build());
        }
        return Optional.empty();
    }

    private void addErrorExtensions(JsonObjectBuilder objectBuilder, Throwable exception) {
        errorExtensionProviders.get()
                .forEach(provider -> addKeyValue(objectBuilder, provider.getKey(), provider.mapValueFrom(exception)));
    }

    private void populateCustomExtensions(JsonObjectBuilder objectBuilder, Map<String, Object> extensions) {
        if (extensions != null) {
            for (Map.Entry<String, Object> entry : extensions.entrySet()) {
                if (config.getErrorExtensionFields().isEmpty()
                        || (config.getErrorExtensionFields().isPresent()
                                && config.getErrorExtensionFields().get().contains(entry.getKey()))) {
                    addKeyValue(objectBuilder, entry.getKey(), entry.getValue().toString());
                }
            }
        }
    }

    private JsonArray toJsonArray(List<?> list) {
        JsonArrayBuilder arrayBuilder = jsonBuilderFactory.createArrayBuilder();
        if (list != null && !list.isEmpty()) {
            for (Object o : list) {
                if (o != null)
                    arrayBuilder.add(o.toString());
            }
        }
        return arrayBuilder.build();
    }

    private void addKeyValue(JsonObjectBuilder objectBuilder, String key, String value) {
        addKeyValue(objectBuilder, key, Json.createValue(value));
    }

    private void addKeyValue(JsonObjectBuilder objectBuilder, String key, JsonValue value) {
        value = value != null ? value : JsonValue.NULL;
        if (config.getErrorExtensionFields().isPresent()) {
            List<String> fieldsThatShouldBeIncluded = config.getErrorExtensionFields().get();
            if (fieldsThatShouldBeIncluded.contains(key)) {
                objectBuilder.add(key, value);
            }
        } else {
            objectBuilder.add(key, value);
        }
    }

    private static final String EXTENSIONS = "extensions";

}
