package io.smallrye.graphql.execution;

import org.jboss.jandex.IndexView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.bootstrap.BootstrapedResult;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.schema.model.TypeAutoNameStrategy;

/**
 * Test the schema generation
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SchemaGenerationTest {

    @Test
    public void testDefaultSchema() {
        String schemaString = getSchemaString(TypeAutoNameStrategy.Default);
        Assertions.assertTrue(schemaString.contains("enum Number"));
    }

    @Test
    public void testMergeInnerSchema() {
        String schemaString = getSchemaString(TypeAutoNameStrategy.MergeInnerClass);
        Assertions.assertTrue(schemaString.contains("enum TestObjectNumber"));
    }

    @Test
    public void testFullNameSchema() {
        String schemaString = getSchemaString(TypeAutoNameStrategy.Full);
        Assertions.assertTrue(schemaString.contains("enum io_smallrye_graphql_test_TestObjectNumber"));
    }

    private String getSchemaString(TypeAutoNameStrategy autoNameStrategy) {
        IndexView index = Indexer.getTCKIndex();
        Schema schema = SchemaBuilder.build(index, autoNameStrategy);
        BootstrapedResult bootstraped = Bootstrap.bootstrap(schema);
        GraphQLSchema graphQLSchema = bootstraped.getGraphQLSchema();
        SchemaPrinter schemaPrinter = new SchemaPrinter(new Config() {
        });
        return schemaPrinter.print(graphQLSchema);
    }

}
