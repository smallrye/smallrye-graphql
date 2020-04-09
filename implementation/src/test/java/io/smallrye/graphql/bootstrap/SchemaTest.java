package io.smallrye.graphql.bootstrap;

import java.io.IOException;

import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.execution.SchemaPrinter;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.testhelper.Indexer;

/**
 * Test the graphql-java Schema creation from the schema model
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SchemaTest {
    private static final Logger LOG = Logger.getLogger(SchemaTest.class.getName());

    private Schema schema;

    @Before
    public void init() {
        IndexView index = Indexer.getTCKIndex();
        this.schema = SchemaBuilder.build(index);
        Assert.assertNotNull(schema);
    }

    @Test
    public void testSchemaModelCreation() throws IOException {
        GraphQLSchema graphQLSchema = Bootstrap.bootstrap(schema);
        LOG.error(SchemaPrinter.print(graphQLSchema));
        Assert.assertNotNull(graphQLSchema);
    }
}
