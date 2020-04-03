package io.smallrye.graphql.execution;

import graphql.schema.GraphQLSchema;

/**
 * Printing the schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SchemaPrinter {

    private SchemaPrinter() {
    }

    public static String print(GraphQLSchema schema) {
        return SCHEMAPRINTER.print(schema);
    }

    private static final graphql.schema.idl.SchemaPrinter SCHEMAPRINTER;
    static {
        graphql.schema.idl.SchemaPrinter.Options options = graphql.schema.idl.SchemaPrinter.Options.defaultOptions();
        options = options.descriptionsAsHashComments(false);
        options = options.includeDirectives(false);
        options = options.includeExtendedScalarTypes(false);
        options = options.includeIntrospectionTypes(false);
        options = options.includeScalarTypes(false);
        options = options.includeSchemaDefinition(false);
        options = options.useAstDefinitions(false);
        SCHEMAPRINTER = new graphql.schema.idl.SchemaPrinter(options);
    }
}
