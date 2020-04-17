package io.smallrye.graphql.execution.error;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.jboss.logging.Logger;

import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.validation.ValidationError;

/**
 * Help to create the exceptions
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ExecutionErrorsService {
    private static final Logger LOG = Logger.getLogger(ExecutionErrorsService.class.getName());

    public JsonArray toJsonErrors(List<GraphQLError> errors) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (GraphQLError e : errors) {
            arrayBuilder.add(toJsonError(e));
        }
        return arrayBuilder.build();
    }

    private JsonObject toJsonError(GraphQLError error) {
        JsonbConfig config = new JsonbConfig()
                .withNullValues(Boolean.TRUE)
                .withFormatting(Boolean.TRUE);

        try (Jsonb jsonb = JsonbBuilder.create(config)) {
            String json = jsonb.toJson(error.toSpecification());
            try (StringReader sr = new StringReader(json);
                    JsonReader reader = Json.createReader(sr)) {

                JsonObject jsonErrors = reader.readObject();

                JsonObjectBuilder resultBuilder = Json.createObjectBuilder(jsonErrors);

                Optional<JsonObject> optionalExtensions = getOptionalExtensions(error);
                if (optionalExtensions.isPresent()) {
                    resultBuilder.add(EXTENSIONS, optionalExtensions.get());
                }
                return resultBuilder.build();
            }
        } catch (Exception e) {
            LOG.warn("Could not close Jsonb", e);
            return null;
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
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();

        addKeyValue(objectBuilder, DESCRIPTION, error.getDescription());
        addKeyValue(objectBuilder, VALIDATION_ERROR_TYPE, error.getValidationErrorType().toString());
        objectBuilder.add(QUERYPATH, toJsonArray(error.getQueryPath()));
        addKeyValue(objectBuilder, CLASSIFICATION, error.getErrorType().toString());

        return Optional.of(objectBuilder.build());
    }

    private Optional<JsonObject> getDataFetchingExtensions(ExceptionWhileDataFetching error) {
        Throwable exception = error.getException();

        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();

        addKeyValue(objectBuilder, EXCEPTION, exception.getClass().getName());
        addKeyValue(objectBuilder, CLASSIFICATION, error.getErrorType().toString());

        return Optional.of(objectBuilder.build());
    }

    private JsonArray toJsonArray(List<?> list) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
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