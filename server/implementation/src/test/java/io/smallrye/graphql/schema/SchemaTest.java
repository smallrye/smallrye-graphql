package io.smallrye.graphql.schema;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.api.Directive;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.execution.SchemaPrinter;
import io.smallrye.graphql.schema.model.Schema;

class SchemaTest {
    private static final Logger LOG = Logger.getLogger(SchemaTest.class.getName());
    private static final Config PRINTER_CONFIG = new Config() {
        @Override
        public boolean isIncludeDirectivesInSchema() {
            return true;
        }
    };

    @Test
    void testSchemaWithDirective() {
        Schema schema = SchemaBuilder
                .build(scan(Directive.class, IntArrayTestDirective.class, TestTypeWithDirectives.class,
                        DirectivesTestApi.class));
        assertNotNull(schema);
        GraphQLSchema graphQLSchema = Bootstrap.bootstrap(schema);
        assertNotNull(graphQLSchema);

        GraphQLDirective directiveType = graphQLSchema.getDirective("intArrayTestDirective");
        assertEquals("intArrayTestDirective", directiveType.getName());
        assertEquals("test-description", directiveType.getDescription());
        assertEquals(1, directiveType.getArguments().size());
        assertEquals("[Int]", directiveType.getArgument("value").getType().toString());

        GraphQLObjectType testTypeWithDirectives = graphQLSchema.getObjectType("TestTypeWithDirectives");
        GraphQLDirective intArrayTestDirective = testTypeWithDirectives.getDirective("intArrayTestDirective");
        assertEquals("intArrayTestDirective", intArrayTestDirective.getName());
        GraphQLArgument argument = intArrayTestDirective.getArgument("value");
        assertEquals("value", argument.getName());
        assertArrayEquals(new Object[] { 1, 2, 3 }, (Object[]) argument.getValue());

        String schemaString = new SchemaPrinter(PRINTER_CONFIG).print(graphQLSchema);
        LOG.info(schemaString);
        assertTrue(schemaString.contains("\"test-description\"\n" +
                "directive @intArrayTestDirective(value: [Int]) on OBJECT | INTERFACE\n"));
        assertTrue(schemaString.endsWith("" +
                "\"Query root\"\n" +
                "type Query {\n" +
                "  testTypeWithDirectives(arg: [String]): TestTypeWithDirectives\n" +
                "}\n" +
                "\n" +
                "type TestTypeWithDirectives @intArrayTestDirective(value : [1, 2, 3]) {\n" +
                "  value: String\n" +
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
