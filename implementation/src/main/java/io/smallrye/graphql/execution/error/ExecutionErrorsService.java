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

package io.smallrye.graphql.execution.error;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.Dependent;
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

import graphql.ErrorType;
import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.validation.ValidationError;

/**
 * Help to create the exceptions
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Dependent
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
            LOG.warn("Could not close Jsonb");
            return null;
        }
    }

    private Optional<JsonObject> getOptionalExtensions(GraphQLError error) {
        if (error.getErrorType().equals(ErrorType.ValidationError)) {
            return getValidationExtensions(error);
        } else if (error.getErrorType().equals(ErrorType.DataFetchingException)) {
            return getDataFetchingExtensions(error);
        }
        return Optional.empty();
    }

    private Optional<JsonObject> getValidationExtensions(GraphQLError graphQLError) {
        ValidationError error = (ValidationError) graphQLError;
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();

        addKeyValue(objectBuilder, DESCRIPTION, error.getDescription());
        addKeyValue(objectBuilder, VALIDATION_ERROR_TYPE, error.getValidationErrorType().toString());
        objectBuilder.add(QUERYPATH, toJsonArray(error.getQueryPath()));
        addKeyValue(objectBuilder, CLASSIFICATION, error.getErrorType().toString());

        return Optional.of(objectBuilder.build());
    }

    private Optional<JsonObject> getDataFetchingExtensions(GraphQLError graphQLError) {
        if (graphQLError instanceof ExceptionWhileDataFetching) {
            ExceptionWhileDataFetching error = (ExceptionWhileDataFetching) graphQLError;
            Throwable exception = error.getException();

            JsonObjectBuilder objectBuilder = Json.createObjectBuilder();

            addKeyValue(objectBuilder, EXCEPTION, exception.getClass().getName());
            addKeyValue(objectBuilder, CLASSIFICATION, error.getErrorType().toString());

            return Optional.of(objectBuilder.build());
        } else {
            return Optional.empty();
        }
    }

    private JsonArray toJsonArray(List list) {
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
