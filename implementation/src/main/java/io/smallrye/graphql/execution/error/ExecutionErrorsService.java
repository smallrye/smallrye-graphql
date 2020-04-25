package io.smallrye.graphql.execution.error;

import java.io.StringReader;
import java.util.List;
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

            Optional<JsonObject> optionalExtensions = getOptionalExtensions(error);
            if (optionalExtensions.isPresent()) {
                resultBuilder.add(EXTENSIONS, optionalExtensions.get());
            }
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
        JsonObjectBuilder objectBuilder = jsonBuilderFactory.createObjectBuilder();

        addKeyValue(objectBuilder, DESCRIPTION, error.getDescription());
        addKeyValue(objectBuilder, VALIDATION_ERROR_TYPE, error.getValidationErrorType().toString());
        objectBuilder.add(QUERYPATH, toJsonArray(error.getQueryPath()));
        addKeyValue(objectBuilder, CLASSIFICATION, error.getErrorType().toString());

        return Optional.of(objectBuilder.build());
    }

    private Optional<JsonObject> getDataFetchingExtensions(ExceptionWhileDataFetching error) {
        Throwable exception = error.getException();

        JsonObjectBuilder objectBuilder = jsonBuilderFactory.createObjectBuilder();

        addKeyValue(objectBuilder, EXCEPTION, exception.getClass().getName());
        addKeyValue(objectBuilder, CLASSIFICATION, error.getErrorType().toString());

        return Optional.of(objectBuilder.build());
    }

    private JsonArray toJsonArray(List<?> list) {
        JsonArrayBuilder arrayBuilder = jsonBuilderFactory.createArrayBuilder();
        for (Object o : list) {
            if (o != null)
                arrayBuilder.add(o.toString());
        }
        return arrayBuilder.build();
    }

    private void addKeyValue(JsonObjectBuilder objectBuilder, String key, String value) {
        if (value != null) {
            objectBuilder.add(key, value);
        }
    }

    private static final String EXCEPTION = "exception";
    private static final String DESCRIPTION = "description";
    private static final String VALIDATION_ERROR_TYPE = "validationErrorType";
    private static final String QUERYPATH = "queryPath";
    private static final String CLASSIFICATION = "classification";
    private static final String EXTENSIONS = "extensions";

}
