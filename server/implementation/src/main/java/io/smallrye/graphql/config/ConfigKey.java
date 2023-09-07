package io.smallrye.graphql.config;

/**
 * All the config options available
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface ConfigKey extends org.eclipse.microprofile.graphql.ConfigKey {

    public static final String PRINT_DATAFETCHER_EXCEPTION = "smallrye.graphql.printDataFetcherException";
    public static final String ALLOW_GET = "smallrye.graphql.allowGet";
    public static final String ALLOW_POST_WITH_QUERY_PARAMETERS = "smallrye.graphql.allowPostWithQueryParameters";
    public static final String ENABLE_METRICS = "smallrye.graphql.metrics.enabled";
    public static final String ENABLE_TRACING = "smallrye.graphql.tracing.enabled";
    public static final String ENABLE_EVENTS = "smallrye.graphql.events.enabled";
    public static final String ENABLE_FEDERATION = "smallrye.graphql.federation.enabled";
    public static final String ENABLE_FEDERATION_BATCH_RESOLVING = "smallrye.graphql.federation.batchResolving.enabled";
    public static final String SCHEMA_INCLUDE_SCALARS = "smallrye.graphql.schema.includeScalars";
    public static final String SCHEMA_INCLUDE_DEFINITION = "smallrye.graphql.schema.includeSchemaDefinition";
    public static final String SCHEMA_INCLUDE_DIRECTIVES = "smallrye.graphql.schema.includeDirectives";
    public static final String SCHEMA_INCLUDE_INTROSPECTION_TYPES = "smallrye.graphql.schema.includeIntrospectionTypes";
    public static final String LOG_PAYLOAD = "smallrye.graphql.logPayload";
    public static final String ERROR_EXTENSION_FIELDS = "smallrye.graphql.errorExtensionFields";
    public static final String FIELD_VISIBILITY = "smallrye.graphql.fieldVisibility";
    public static final String UNWRAP_EXCEPTIONS = "smallrye.graphql.unwrapExceptions";
    public static final String PARSER_CAPTURE_IGNORED_CHARS = "smallrye.graphql.parser.capture.ignoredChars";
    public static final String PARSER_CAPTURE_LINE_COMMENTS = "smallrye.graphql.parser.capture.lineComments";
    public static final String PARSER_CAPTURE_SOURCE_LOCATION = "smallrye.graphql.parser.capture.sourceLocation";
    public static final String PARSER_MAX_TOKENS = "smallrye.graphql.parser.maxTokens";
    public static final String PARSER_MAX_WHITESPACE_TOKENS = "smallrye.graphql.parser.maxWhitespaceTokens";
    public static final String INSTRUMENTATION_QUERY_COMPLEXITY = "smallrye.graphql.instrumentation.queryComplexity";
    public static final String INSTRUMENTATION_QUERY_DEPTH = "smallrye.graphql.instrumentation.queryDepth";

}