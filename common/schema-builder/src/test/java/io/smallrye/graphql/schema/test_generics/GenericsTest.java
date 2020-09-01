package io.smallrye.graphql.schema.test_generics;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.index.SchemaBuilderTest;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;

public class GenericsTest {

    private static final Logger LOG = Logger.getLogger(GenericsTest.class.getName());

    @Test
    public void testSchemaString() throws Exception {

        Indexer indexer = new Indexer();
        SchemaBuilderTest.indexDirectory(indexer, "io/smallrye/graphql/schema/test_generics");
        Index index = indexer.complete();

        Schema schema = SchemaBuilder.build(index);
        assertNotNull(schema);

        String schemaString = SchemaBuilderTest.toString(schema);
        LOG.info(schemaString);

        assertTrue(schemaString.contains("io.smallrye.graphql.schema.test_generics.InterfaceWithOneGenericsParam"));
        assertTrue(schemaString.contains("InterfaceWithOneGenericsParam_Int"));
        assertTrue(schemaString.contains("InterfaceWithOneGenericsParam_String"));

        assertTrue(schemaString.contains("io.smallrye.graphql.schema.test_generics.ClassWithoutGenerics"));
        assertTrue(schemaString.contains("ClassWithoutGenerics"));

        assertTrue(schemaString.contains("io.smallrye.graphql.schema.test_generics.ClassWithOneGenericsParamToString2"));
        assertTrue(schemaString.contains("ClassWithOneGenericsParamToString2_String"));

        assertTrue(schemaString.contains("io.smallrye.graphql.schema.test_generics.ClassWithOneGenericsParam"));
        assertTrue(schemaString.contains("ClassWithOneGenericsParam_String"));
        assertTrue(schemaString.contains("ClassWithOneGenericsParam_Int"));
        assertTrue(schemaString.contains("ClassWithOneGenericsParam_StringInput"));

        assertTrue(schemaString.contains("io.smallrye.graphql.schema.test_generics.ClassWithTwoGenericsParams"));
        assertTrue(schemaString.contains("ClassWithTwoGenericsParams_String_ClassWithOneGenericsParam_Integer"));

    }

}
