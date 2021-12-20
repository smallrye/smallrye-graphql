package io.smallrye.graphql.client;

import java.util.List;
import java.util.Map;

public interface GraphQLError {

    /**
     * Textual description of the error, supplied by the service.
     */
    String getMessage();

    /**
     * List of locations in the request that correspond to this error. Each location is a map that
     * should contain the `line` and `column` items.
     */
    List<Map<String, Integer>> getLocations();

    /**
     * The path to the response field where the error arose. This is represented as an array, where each item is either
     * a string (when it represents a field), or an integer (when it represents an index).
     */
    Object[] getPath();

    /**
     * Contents of the `extensions` field. This is used by GraphQL services to pass extra information about the error.
     */
    Map<String, Object> getExtensions();

    /**
     * Any other fields beyond message, locations, path and extensions. These are discouraged by the spec,
     * but if a GraphQL service adds them, they will appear in this map.
     */
    Map<String, Object> getOtherFields();

    /**
     * Contents of one of the `extensions` fields, converted to a String.
     * This is used by GraphQL services to pass extra information about the error.
     */
    default String getStringExtension(String name) {
        if (getExtensions() == null) {
            return null;
        }
        Object value = getExtensions().get(name);
        return (value == null) ? null : value.toString();
    }

    /**
     * The name of the exception thrown in the service.
     * This may not be stable, so it's generally better to use the {@link #getCode()}
     */
    default String getException() {
        return getStringExtension("exception");
    }

    /** The general type of failure */
    default String getClassification() {
        return getStringExtension("classification");
    }

    /** A stable error code */
    default String getCode() {
        return getStringExtension("code");
    }

    /** A human-readable description of the failure */
    default String getDescription() {
        return getStringExtension("description");
    }

    /** The type of mistake leading to the rejection of the GraphQL query */
    default String getValidationErrorType() {
        return getStringExtension("validationErrorType");
    }

    /** The path within the GraphQL query that was rejected */
    default String getQueryPath() {
        return getStringExtension("queryPath");
    }
}
