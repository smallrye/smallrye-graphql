package io.smallrye.graphql.cdi.config;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
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
    @ConfigProperty(name = ConfigKey.EXCEPTION_BLACK_LIST, defaultValue = "")
    private Optional<List<String>> hideList;

    @Inject
    @ConfigProperty(name = ConfigKey.EXCEPTION_WHITE_LIST, defaultValue = "")
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
}
