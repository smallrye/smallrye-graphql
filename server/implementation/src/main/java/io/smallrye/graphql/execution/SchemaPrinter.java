package io.smallrye.graphql.execution;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.Config;

/**
 * Printing the schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SchemaPrinter {

    private final graphql.schema.idl.SchemaPrinter schemaPrinter;

    public SchemaPrinter(final Config config) {
        this.schemaPrinter = createSchemaPrinter(config);
    }

    public String print(GraphQLSchema schema) {
        return schemaPrinter.print(schema);
    }

    private graphql.schema.idl.SchemaPrinter createSchemaPrinter(Config config) {
        graphql.schema.idl.SchemaPrinter.Options options = graphql.schema.idl.SchemaPrinter.Options.defaultOptions();
        options = options.descriptionsAsHashComments(false);
        options = options.includeDirectives(config.isIncludeDirectivesInSchema());
        options = options.includeIntrospectionTypes(config.isIncludeIntrospectionTypesInSchema());
        options = options.includeScalarTypes(config.isIncludeScalarsInSchema());
        options = options.includeSchemaDefinition(config.isIncludeSchemaDefinitionInSchema());
        options = options.useAstDefinitions(false);
        return new graphql.schema.idl.SchemaPrinter(options);
    }
}
