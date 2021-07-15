package io.smallrye.graphql.bootstrap;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Configuration for GraphQL
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface Config {

    default String getDefaultErrorMessage() {
        return "System error";
    }

    default boolean isPrintDataFetcherException() {
        return false;
    }

    default Optional<List<String>> getHideErrorMessageList() {
        return Optional.empty();
    }

    default Optional<List<String>> getShowErrorMessageList() {
        return Optional.empty();
    }

    default Optional<List<String>> getUnwrapExceptions() {
        return Optional.empty();
    }

    default boolean isAllowGet() {
        return false;
    }

    default boolean isAllowPostWithQueryParameters() {
        return false;
    }

    default boolean isIncludeScalarsInSchema() {
        return false;
    }

    default boolean isIncludeDirectivesInSchema() {
        return false;
    }

    default boolean isIncludeSchemaDefinitionInSchema() {
        return false;
    }

    default boolean isIncludeIntrospectionTypesInSchema() {
        return false;
    }

    default boolean isTracingEnabled() {
        return false;
    }

    default boolean isMetricsEnabled() {
        return false;
    }

    default boolean isValidationEnabled() {
        return false;
    }

    default boolean isEventsEnabled() {
        return false;
    }

    default boolean shouldEmitEvents() {
        return isTracingEnabled() || isMetricsEnabled() || isValidationEnabled() || isEventsEnabled();
    }

    default LogPayloadOption logPayload() {
        return LogPayloadOption.off;
    }

    default String getFieldVisibility() {
        return FIELD_VISIBILITY_DEFAULT;
    }

    default Optional<List<String>> getErrorExtensionFields() {
        return Optional.empty();
    }

    default <T> T getConfigValue(String key, Class<T> type, T defaultValue) {
        return defaultValue;
    }

    public static final String FIELD_VISIBILITY_DEFAULT = "default";
    public static final String FIELD_VISIBILITY_NO_INTROSPECTION = "no-introspection";

    public static final String ERROR_EXTENSION_EXCEPTION = "exception";
    public static final String ERROR_EXTENSION_CLASSIFICATION = "classification";
    public static final String ERROR_EXTENSION_CODE = "code";
    public static final String ERROR_EXTENSION_DESCRIPTION = "description";
    public static final String ERROR_EXTENSION_VALIDATION_ERROR_TYPE = "validationErrorType";
    public static final String ERROR_EXTENSION_QUERY_PATH = "queryPath";

    public static final List<String> ERROR_EXTENSION_ALL_KNOWN = Arrays.asList(
            ERROR_EXTENSION_EXCEPTION,
            ERROR_EXTENSION_CLASSIFICATION,
            ERROR_EXTENSION_CODE,
            ERROR_EXTENSION_DESCRIPTION,
            ERROR_EXTENSION_VALIDATION_ERROR_TYPE,
            ERROR_EXTENSION_QUERY_PATH);
}
