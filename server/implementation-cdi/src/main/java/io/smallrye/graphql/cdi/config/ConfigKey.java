package io.smallrye.graphql.cdi.config;

/**
 * All the config options available
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface ConfigKey extends org.eclipse.microprofile.graphql.ConfigKey {

    public static final String PRINT_DATAFETCHER_EXCEPTION = "smallrye.graphql.printDataFetcherException";
    public static final String ALLOW_GET = "smallrye.graphql.allowGet";
    public static final String ENABLE_METRICS = "smallrye.graphql.metrics.enabled";
    public static final String ENABLE_TRACING = "smallrye.graphql.tracing.enabled";
    public static final String ENABLE_VALIDATION = "smallrye.graphql.validation.enabled";
    public static final String SCHEMA_INCLUDE_SCALARS = "smallrye.graphql.schema.includeScalars";
    public static final String SCHEMA_INCLUDE_DEFINITION = "smallrye.graphql.schema.includeSchemaDefinition";
    public static final String SCHEMA_INCLUDE_DIRECTIVES = "smallrye.graphql.schema.includeDirectives";
    public static final String SCHEMA_INCLUDE_INTROSPECTION_TYPES = "smallrye.graphql.schema.includeIntrospectionTypes";
    public static final String LOG_PAYLOAD = "smallrye.graphql.logPayload";
    public static final String FIELD_VISIBILITY = "smallrye.graphql.fieldVisibility";
}
