package io.smallrye.graphql.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.junit.jupiter.api.AfterEach;

import graphql.language.StringValue;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.execution.TestConfig;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.spi.config.Config;

public abstract class SchemaTestBase {
    protected final TestConfig config = (TestConfig) Config.get();

    @AfterEach
    void tearDown() {
        config.reset();
    }

    protected void assertKeyDirective(GraphQLDirective graphQLDirective, String fieldsValue, Boolean resolvableValue) {
        assertEquals("key", graphQLDirective.getName());
        assertEquals(2, graphQLDirective.getArguments().size());
        assertEquals("fields", graphQLDirective.getArguments().get(0).getName());
        assertEquals("resolvable", graphQLDirective.getArguments().get(1).getName());
        assertEquals(fieldsValue,
                ((StringValue) graphQLDirective.getArguments().get(0).toAppliedArgument().getArgumentValue().getValue())
                        .getValue());
        assertEquals(resolvableValue, graphQLDirective.getArguments().get(1).toAppliedArgument().getArgumentValue().getValue());
        assertEquals(true, graphQLDirective.getArguments().get(1).getArgumentDefaultValue().getValue());
    }

    protected GraphQLSchema createGraphQLSchema(Class<?>... api) {
        Schema schema = SchemaBuilder.build(scan(api));
        assertNotNull(schema, "Schema should not be null");
        GraphQLSchema graphQLSchema = Bootstrap.bootstrap(schema, true);
        assertNotNull(graphQLSchema, "GraphQLSchema should not be null");
        return graphQLSchema;
    }

    protected IndexView scan(Class<?>... classes) {
        Indexer indexer = new Indexer();
        Stream.of(classes).forEach(cls -> index(indexer, cls));
        return indexer.complete();
    }

    protected void index(Indexer indexer, Class<?> cls) {
        try {
            indexer.index(getResourceStream(cls));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected InputStream getResourceStream(Class<?> type) {
        String name = type.getName().replace(".", "/") + ".class";
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    }
}
