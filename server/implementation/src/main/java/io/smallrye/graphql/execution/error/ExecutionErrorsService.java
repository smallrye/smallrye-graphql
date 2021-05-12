package io.smallrye.graphql.execution.error;

import static java.util.Locale.UK;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.validation.ValidationError;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.schema.model.ErrorInfo;

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

    private final Config config;

    public ExecutionErrorsService(Config config) {
        this.config = config;
    }

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
            addKeyValue(objectBuilder, Config.ERROR_EXTENSION_EXCEPTION, exception.getClass().getName());
            addKeyValue(objectBuilder, Config.ERROR_EXTENSION_CLASSIFICATION, error.getErrorType().toString());
            addKeyValue(objectBuilder, Config.ERROR_EXTENSION_CODE, toErrorCode(exception));
            Map<String, Object> extensions = error.getExtensions();
            populateCustomExtensions(objectBuilder, extensions);

            return Optional.of(objectBuilder.build());
        }
        return Optional.empty();
    }

    private String toErrorCode(Throwable exception) {
        String exceptionClassName = exception.getClass().getName();
        if (ErrorInfoMap.hasErrorInfo(exceptionClassName)) {
            ErrorInfo errorInfo = ErrorInfoMap.getErrorInfo(exceptionClassName);
            return errorInfo.getErrorCode();
        }
        return camelToKebab(exception.getClass().getSimpleName().replaceAll("Exception$", ""));
    }

    private static String camelToKebab(String input) {
        return String.join("-", input.split("(?=\\p{javaUpperCase})"))
                .toLowerCase(UK);
    }

    private void populateCustomExtensions(JsonObjectBuilder objectBuilder, Map<String, Object> extensions) {
        if (extensions != null) {
            for (Map.Entry<String, Object> entry : extensions.entrySet()) {
                if (!config.getErrorExtensionFields().isPresent()
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

        if (config.getErrorExtensionFields().isPresent()) {
            List<String> fieldsThatShouldBeIncluded = config.getErrorExtensionFields().get();
            if (fieldsThatShouldBeIncluded.contains(key)) {
                objectBuilder.add(key, value);
            }
        } else {
            if (value != null) {
                objectBuilder.add(key, value);
            }
        }
    }

    private static final String EXTENSIONS = "extensions";

}
