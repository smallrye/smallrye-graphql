/*
 * Copyright 2019 Red Hat, Inc.
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

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.graphql.GraphQLException;
import org.jboss.logging.Logger;

import graphql.ErrorType;
import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import graphql.validation.ValidationError;
import io.smallrye.graphql.execution.datafetchers.DataFetcherException;

/**
 * Help to create the exceptions
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Dependent
public class ExecutionErrorsService {
    private static final Logger LOG = Logger.getLogger(ExecutionErrorsService.class.getName());

    @Inject
    @ConfigProperty(name = "mp.graphql.defaultErrorMessage")
    private String defaultErrorMessage;

    public ErrorData getErrorData(List<GraphQLError> errors) {
        ErrorData errorData = new ErrorData();
        for (GraphQLError e : errors) {
            populate(errorData, e);
        }
        return errorData;
    }

    //    TODO: Is this not the correct way to do this ?    
    //    private JsonObject toJsonObjectError(GraphQLError error) {
    //        return toJsonObject(error.toSpecification());
    //    }

    private void populate(ErrorData errorData, GraphQLError error) {
        if (error.getErrorType().equals(ErrorType.ValidationError)) {
            handleValidationError(errorData, error);
        } else if (error.getErrorType().equals(ErrorType.DataFetchingException)) {
            handleDataFetchingError(errorData, error);
        } else {
            handleOtherError(errorData, error);
        }
    }

    private void handleDataFetchingError(ErrorData errorData, GraphQLError graphQLError) {
        if (graphQLError.getClass().isAssignableFrom(ExceptionWhileDataFetching.class)) {
            handleExceptionWhileDataFetching(errorData, graphQLError);
        } else {
            handleOtherError(errorData, graphQLError);
        }
    }

    private void handleOtherError(ErrorData errorData, GraphQLError error) {
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        // Message
        addKeyValue(objectBuilder, MESSAGE, error.getMessage());
        // Location
        if (error.getLocations() != null) {
            objectBuilder.add(LOCATIONS, toJsonArrayLocations(error.getLocations()));
        } else {
            objectBuilder.addNull(LOCATIONS);
        }
        // Error Type
        addKeyValue(objectBuilder, ERROR_TYPE, error.getErrorType().toString());
        // Path
        if (error.getPath() != null) {
            objectBuilder.add(PATH, toJsonArrayPath(error.getPath()));
        } else {
            objectBuilder.addNull(PATH);
        }
        // Extensions (TODO: What is this ?)
        objectBuilder.addNull(EXTENSIONS);

        errorData.add(objectBuilder.build());
    }

    private void handleExceptionWhileDataFetching(ErrorData errorData, GraphQLError graphQLError) {
        ExceptionWhileDataFetching error = (ExceptionWhileDataFetching) graphQLError;
        Throwable exception = error.getException();
        handleExceptionWhileDataFetching(errorData, error, exception);
    }

    private void handleExceptionWhileDataFetching(ErrorData errorData, ExceptionWhileDataFetching error, Throwable exception) {

        if (exception instanceof DataFetcherException) {
            DataFetcherException dataFetcherException = (DataFetcherException) error.getException();
            Throwable cause = dataFetcherException.getCause();
            if (cause != null && (cause instanceof GraphQLException)) {
                handleExceptionWhileDataFetching(errorData, error, cause);
            } else {

                LOG.warn(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> handleExceptionWhileDataFetching 1 "
                        + error.getException().getClass().getName());
                handleUnknownError(errorData, error);
            }
        } else if (exception instanceof GraphQLException) {
            LOG.warn(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> handleExceptionWhileDataFetching 2 "
                    + error.getException().getClass().getName());
            handleGraphQLException(errorData, error, (GraphQLException) exception);
        } else {
            LOG.warn(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> handleExceptionWhileDataFetching 3 "
                    + error.getException().getClass().getName());
            handleUnknownError(errorData, error);
        }
    }

    private void handleGraphQLException(ErrorData errorData, ExceptionWhileDataFetching error,
            GraphQLException graphQLException) {

        Object partialResults = graphQLException.getPartialResults();

        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        // Message
        addKeyValue(objectBuilder, MESSAGE, graphQLException.getMessage());
        // Location
        if (error.getLocations() != null) {
            objectBuilder.add(LOCATIONS, toJsonArrayLocations(error.getLocations()));
        } else {
            objectBuilder.addNull(LOCATIONS);
        }
        // Error Type
        addKeyValue(objectBuilder, ERROR_TYPE, error.getErrorType().toString());
        // Path
        //if (error.getPath() != null) {
        //    objectBuilder.add(PATH, toJsonArrayPath(error.getPath()));
        //} else {
        objectBuilder.addNull(PATH);
        //}
        // Extensions (TODO: What is this ?)
        //objectBuilder.addNull(EXTENSIONS);

        errorData.add(objectBuilder.build(), partialResults);
    }

    private void handleUnknownError(ErrorData errorData, ExceptionWhileDataFetching error) {
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        // Message
        addKeyValue(objectBuilder, MESSAGE, defaultErrorMessage);
        // Location
        if (error.getLocations() != null) {
            objectBuilder.add(LOCATIONS, toJsonArrayLocations(error.getLocations()));
        } else {
            objectBuilder.addNull(LOCATIONS);
        }
        //LOG.warn(">>>>>>>>>>>> " + error.getException().getClass().getName());
        //LOG.warn(">>>>>>>>>>>> " + error.getException().getMessage());
        //LOG.warn(">>>>>>>>>>>> " + error.getClass().getName());

        // Error Type
        //addKeyValue(objectBuilder, ERROR_TYPE, error.getErrorType().toString());

        // Path
        //if (error.getPath() != null) {
        //    objectBuilder.add(PATH, toJsonArrayPath(error.getPath()));
        //} else {
        objectBuilder.addNull(PATH);
        //}

        // Extensions (TODO: What is this ?)
        //objectBuilder.addNull(EXTENSIONS);

        errorData.add(objectBuilder.build());
    }

    private void handleValidationError(ErrorData errorData, GraphQLError graphQLError) {
        ValidationError error = (ValidationError) graphQLError;
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        // Message
        addKeyValue(objectBuilder, MESSAGE, error.getMessage());
        // Location
        if (error.getLocations() != null) {
            objectBuilder.add(LOCATIONS, toJsonArrayLocations(error.getLocations()));
        } else {
            objectBuilder.addNull(LOCATIONS);
        }

        addKeyValue(objectBuilder, DESCRIPTION, error.getDescription());
        addKeyValue(objectBuilder, VALIDATION_ERROR_TYPE, error.getValidationErrorType().toString());
        objectBuilder.add(QUERYPATH, toJsonArrayPath(error.getQueryPath()));

        // Error Type
        addKeyValue(objectBuilder, ERROR_TYPE, error.getErrorType().toString());

        // Path
        if (error.getPath() != null) {
            objectBuilder.add(PATH, toJsonArrayPath(error.getPath()));
        } else {
            objectBuilder.addNull(PATH);
        }

        // Extensions (TODO: What is this ?)
        objectBuilder.addNull(EXTENSIONS);

        errorData.add(objectBuilder.build());
    }

    private JsonArray toJsonArrayLocations(List<SourceLocation> locations) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (SourceLocation s : locations) {
            arrayBuilder.add(toJsonObjectLocation(s));
        }
        return arrayBuilder.build();
    }

    private JsonValue toJsonObjectLocation(SourceLocation s) {
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        objectBuilder.add(LINE, s.getLine());
        objectBuilder.add(COLUMN, s.getColumn());
        addKeyValue(objectBuilder, SOURCENAME, s.getSourceName());
        return objectBuilder.build();
    }

    private JsonArray toJsonArrayPath(List path) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Object o : path) {
            if (o != null)
                arrayBuilder.add(o.toString());
        }
        return arrayBuilder.build();
    }

    private void addKeyValue(JsonObjectBuilder objectBuilder, String key, String value) {
        if (value != null) {
            objectBuilder.add(key, value);
        } else {
            objectBuilder.addNull(key);
        }
    }

    private static final String MESSAGE = "message";
    private static final String LOCATIONS = "locations";
    private static final String LINE = "line";
    private static final String COLUMN = "column";
    private static final String SOURCENAME = "sourceName";
    private static final String DESCRIPTION = "description";
    private static final String VALIDATION_ERROR_TYPE = "validationErrorType";
    private static final String QUERYPATH = "queryPath";
    private static final String ERROR_TYPE = "errorType";
    private static final String PATH = "path";
    private static final String EXTENSIONS = "extensions";

}
