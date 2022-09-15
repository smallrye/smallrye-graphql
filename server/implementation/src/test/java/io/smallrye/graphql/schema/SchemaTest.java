package io.smallrye.graphql.schema;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.api.Directive;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.execution.SchemaPrinter;
import io.smallrye.graphql.schema.model.Schema;

class SchemaTest {
    private static final Logger LOG = Logger.getLogger(SchemaTest.class.getName());

    @Test
    void testSchemaWithDirectives() {
        Schema schema = SchemaBuilder
                .build(scan(Directive.class, IntArrayTestDirective.class, FieldDirective.class, ArgumentDirective.class,
                        TestTypeWithDirectives.class, DirectivesTestApi.class));
        assertNotNull(schema);
        GraphQLSchema graphQLSchema = Bootstrap.bootstrap(schema, true);
        assertNotNull(graphQLSchema);

        GraphQLDirective typeDirective = graphQLSchema.getDirective("intArrayTestDirective");
        assertEquals("intArrayTestDirective", typeDirective.getName());
        assertEquals("test-description", typeDirective.getDescription());
        assertEquals(1, typeDirective.getArguments().size());
        assertEquals("[Int]", typeDirective.getArgument("value").getType().toString());

        GraphQLDirective fieldDirective = graphQLSchema.getDirective("fieldDirective");
        assertEquals("fieldDirective", fieldDirective.getName());
        assertNull(fieldDirective.getDescription());
        assertEquals(0, fieldDirective.getArguments().size());

        GraphQLObjectType testTypeWithDirectives = graphQLSchema.getObjectType("TestTypeWithDirectives");
        GraphQLDirective intArrayTestDirective = testTypeWithDirectives.getDirective("intArrayTestDirective");
        assertEquals("intArrayTestDirective", intArrayTestDirective.getName());
        GraphQLArgument argument = intArrayTestDirective.getArgument("value");
        assertEquals("value", argument.getName());
        assertArrayEquals(new Object[] { 1, 2, 3 }, (Object[]) argument.getArgumentValue().getValue());

        GraphQLFieldDefinition valueField = testTypeWithDirectives.getFieldDefinition("value");
        GraphQLDirective fieldDirectiveInstance = valueField.getDirective("fieldDirective");
        assertNotNull(fieldDirectiveInstance);

        GraphQLFieldDefinition queryWithDirectives = graphQLSchema.getQueryType().getField("queryWithDirectives");
        assertNotNull(queryWithDirectives.getArgument("arg").getDirective("argumentDirective"));

        String schemaString = new SchemaPrinter().print(graphQLSchema);
        LOG.info(schemaString);
        assertTrue(schemaString.contains("\"test-description\"\n" +
                "directive @intArrayTestDirective(value: [Int]) on OBJECT | INTERFACE\n"));
        assertTrue(schemaString.endsWith("" +
                "\"Query root\"\n" +
                "type Query {\n" +
                "  queryWithDirectives(arg: [String] @argumentDirective): TestTypeWithDirectives\n" +
                "}\n" +
                "\n" +
                "type TestTypeWithDirectives @intArrayTestDirective(value : [1, 2, 3]) {\n" +
                "  value: String @fieldDirective\n" +
                "}\n"));
    }

    private IndexView scan(Class<?>... classes) {
        Indexer indexer = new Indexer();
        Stream.of(classes).forEach(cls -> index(indexer, cls));
        return indexer.complete();
    }

    private void index(Indexer indexer, Class<?> cls) {
        try {
            indexer.index(getResourceStream(cls));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream getResourceStream(Class<?> type) {
        String name = type.getName().replace(".", "/") + ".class";
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    }
}
