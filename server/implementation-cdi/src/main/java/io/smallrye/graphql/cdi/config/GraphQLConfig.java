package io.smallrye.graphql.cdi.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.smallrye.graphql.bootstrap.Config;

/**
 * Configuration for GraphQL
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class GraphQLConfig implements Config {

    @Inject
    org.eclipse.microprofile.config.Config microProfileConfig;

    @Deprecated
    @Inject
    @ConfigProperty(name = ConfigKey.EXCEPTION_BLACK_LIST, defaultValue = "") // EXCEPTION_HIDE_ERROR_MESSAGE_LIST
    private Optional<List<String>> blackList;

    @Inject
    @ConfigProperty(name = "mp.graphql.hideErrorMessage", defaultValue = "")
    private Optional<List<String>> hideList;

    @Deprecated
    @Inject
    @ConfigProperty(name = ConfigKey.EXCEPTION_WHITE_LIST, defaultValue = "")
    private Optional<List<String>> whiteList;

    @Inject
    @ConfigProperty(name = "mp.graphql.showErrorMessage", defaultValue = "") // EXCEPTION_SHOW_ERROR_MESSAGE_LIST
    private Optional<List<String>> showList;

    @Inject
    @ConfigProperty(name = ConfigKey.DEFAULT_ERROR_MESSAGE, defaultValue = "Server Error")
    private String defaultErrorMessage;

    @Inject
    @ConfigProperty(name = ConfigKey.PRINT_DATAFETCHER_EXCEPTION, defaultValue = "false")
    private boolean printDataFetcherException;

    @Inject
    @ConfigProperty(name = ConfigKey.ALLOW_GET, defaultValue = "false")
    private boolean allowGet;

    @Inject
    @ConfigProperty(name = ConfigKey.ENABLE_METRICS, defaultValue = "false")
    private boolean metricsEnabled;

    @Inject
    @ConfigProperty(name = ConfigKey.ENABLE_TRACING, defaultValue = "false")
    private boolean tracingEnabled;

    @Inject
    @ConfigProperty(name = ConfigKey.ENABLE_VALIDATION, defaultValue = "false")
    private boolean validationEnabled;

    @Inject
    @ConfigProperty(name = ConfigKey.ENABLE_EVENTS, defaultValue = "false")
    private boolean eventsEnabled;

    @Inject
    @ConfigProperty(name = ConfigKey.SCHEMA_INCLUDE_SCALARS, defaultValue = "true")
    private boolean includeScalarsInSchema;

    @Inject
    @ConfigProperty(name = ConfigKey.SCHEMA_INCLUDE_DIRECTIVES, defaultValue = "false")
    private boolean includeDirectivesInSchema;

    @Inject
    @ConfigProperty(name = ConfigKey.SCHEMA_INCLUDE_DEFINITION, defaultValue = "false")
    private boolean includeSchemaDefinitionInSchema;

    @Inject
    @ConfigProperty(name = ConfigKey.SCHEMA_INCLUDE_INTROSPECTION_TYPES, defaultValue = "false")
    private boolean includeIntrospectionTypesInSchema;

    @Inject
    @ConfigProperty(name = ConfigKey.LOG_PAYLOAD, defaultValue = "false")
    private boolean logPayload;

    @Inject
    @ConfigProperty(name = ConfigKey.FIELD_VISIBILITY, defaultValue = Config.FIELD_VISIBILITY_DEFAULT)
    private String fieldVisibility;

    @Inject
    @ConfigProperty(name = ConfigKey.UNWRAP_EXCEPTIONS, defaultValue = "")
    private Optional<List<String>> unwrapExceptions;

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        hideList = mergeList(hideList, blackList);
        showList = mergeList(showList, whiteList);
    }

    @Override
    public String getDefaultErrorMessage() {
        return defaultErrorMessage;
    }

    @Override
    public boolean isPrintDataFetcherException() {
        return printDataFetcherException;
    }

    @Override
    public Optional<List<String>> getHideErrorMessageList() {
        return hideList;
    }

    @Override
    public Optional<List<String>> getShowErrorMessageList() {
        return showList;
    }

    @Override
    public boolean isAllowGet() {
        return allowGet;
    }

    @Override
    public boolean isMetricsEnabled() {
        return metricsEnabled;
    }

    @Override
    public boolean isTracingEnabled() {
        return tracingEnabled;
    }

    @Override
    public boolean isValidationEnabled() {
        return validationEnabled;
    }

    @Override
    public boolean isEventsEnabled() {
        return eventsEnabled;
    }

    @Override
    public boolean isIncludeDirectivesInSchema() {
        return includeDirectivesInSchema;
    }

    @Override
    public boolean isIncludeSchemaDefinitionInSchema() {
        return includeSchemaDefinitionInSchema;
    }

    @Override
    public boolean isIncludeIntrospectionTypesInSchema() {
        return includeIntrospectionTypesInSchema;
    }

    @Override
    public boolean isIncludeScalarsInSchema() {
        return includeScalarsInSchema;
    }

    @Override
    public boolean logPayload() {
        return logPayload;
    }

    @Override
    public String getFieldVisibility() {
        return fieldVisibility;
    }

    @Override
    public <T> T getConfigValue(String key, Class<T> type, T defaultValue) {
        return microProfileConfig.getOptionalValue(key, type).orElse(defaultValue);
    }

    @Override
    public Optional<List<String>> getUnwrapExceptions() {
        return unwrapExceptions;
    }

    public void setHideErrorMessageList(Optional<List<String>> hideList) {
        this.hideList = hideList;
    }

    public void setShowErrorMessageList(Optional<List<String>> showList) {
        this.showList = showList;
    }

    public void setDefaultErrorMessage(String defaultErrorMessage) {
        this.defaultErrorMessage = defaultErrorMessage;
    }

    public void setPrintDataFetcherException(boolean printDataFetcherException) {
        this.printDataFetcherException = printDataFetcherException;
    }

    public void setAllowGet(boolean allowGet) {
        this.allowGet = allowGet;
    }

    public void setMetricsEnabled(boolean metricsEnabled) {
        this.metricsEnabled = metricsEnabled;
    }

    public void setTracingEnabled(final boolean tracingEnabled) {
        this.tracingEnabled = tracingEnabled;
    }

    public void setIncludeScalarsInSchema(boolean includeScalarsInSchema) {
        this.includeScalarsInSchema = includeScalarsInSchema;
    }

    public void setIncludeDirectivesInSchema(boolean includeDirectivesInSchema) {
        this.includeDirectivesInSchema = includeDirectivesInSchema;
    }

    public void setIncludeSchemaDefinitionInSchema(boolean includeSchemaDefinitionInSchema) {
        this.includeSchemaDefinitionInSchema = includeSchemaDefinitionInSchema;
    }

    public void setIncludeIntrospectionTypesInSchema(boolean includeIntrospectionTypesInSchema) {
        this.includeIntrospectionTypesInSchema = includeIntrospectionTypesInSchema;
    }

    public void setLogPayload(boolean logPayload) {
        this.logPayload = logPayload;
    }

    public void setFieldVisibility(String fieldVisibility) {
        this.fieldVisibility = fieldVisibility;
    }

    public void setUnwrapExceptions(Optional<List<String>> unwrapExceptions) {
        this.unwrapExceptions = unwrapExceptions;
    }

    private Optional<List<String>> mergeList(Optional<List<String>> currentList, Optional<List<String>> deprecatedList) {

        List<String> combined = new ArrayList<>();
        if (deprecatedList.isPresent()) {
            combined.addAll(deprecatedList.get());
        }
        if (currentList.isPresent()) {
            combined.addAll(currentList.get());
        }

        if (!combined.isEmpty()) {
            return Optional.of(combined);
        } else {
            return Optional.empty();
        }
    }
}
