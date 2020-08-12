package io.smallrye.graphql.bootstrap;

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

    default boolean isAllowGet() {
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

    default boolean shouldEmmitEvents() {
        return isTracingEnabled() || isMetricsEnabled() || isValidationEnabled() || isEventsEnabled();
    }

    default boolean logPayload() {
        return false;
    }

    default String getFieldVisibility() {
        return FIELD_VISIBILITY_DEFAULT;
    }

    default <T> T getConfigValue(String key, Class<T> type, T defaultValue) {
        return defaultValue;
    }

    public static final String FIELD_VISIBILITY_DEFAULT = "default";
    public static final String FIELD_VISIBILITY_NO_INTROSPECTION = "no-introspection";
}
