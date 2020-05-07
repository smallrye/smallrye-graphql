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
    private Optional<List<String>> blackList;

    @Inject
    @ConfigProperty(name = ConfigKey.EXCEPTION_WHITE_LIST, defaultValue = "")
    private Optional<List<String>> whiteList;

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

    public String getDefaultErrorMessage() {
        return defaultErrorMessage;
    }

    public boolean isPrintDataFetcherException() {
        return printDataFetcherException;
    }

    public Optional<List<String>> getBlackList() {
        return blackList;
    }

    public Optional<List<String>> getWhiteList() {
        return whiteList;
    }

    public boolean isAllowGet() {
        return allowGet;
    }

    public boolean isMetricsEnabled() {
        return metricsEnabled;
    }

    @Override
    public boolean isTracingEnabled() {
        return tracingEnabled;
    }

    public boolean isIncludeDirectivesInSchema() {
        return includeDirectivesInSchema;
    }

    public boolean isIncludeSchemaDefinitionInSchema() {
        return includeSchemaDefinitionInSchema;
    }

    public boolean isIncludeIntrospectionTypesInSchema() {
        return includeIntrospectionTypesInSchema;
    }

    public boolean isIncludeScalarsInSchema() {
        return includeScalarsInSchema;
    }

    public void setBlackList(Optional<List<String>> blackList) {
        this.blackList = blackList;
    }

    public void setWhiteList(Optional<List<String>> whiteList) {
        this.whiteList = whiteList;
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

}
