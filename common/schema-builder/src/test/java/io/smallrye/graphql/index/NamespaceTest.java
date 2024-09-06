package io.smallrye.graphql.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.index.app.namespace.ApiWithName;
import io.smallrye.graphql.index.app.namespace.ApiWithNameAndNamespace;
import io.smallrye.graphql.index.app.namespace.ApiWithNamespace;
import io.smallrye.graphql.schema.IndexCreator;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;

/**
 * Test namespaces
 */
public class NamespaceTest {
    @Test
    public void apiWithNameTest() {
        Index index = IndexCreator.indexWithPackage(ApiWithName.class);
        Schema schema = SchemaBuilder.build(index);

        assertNotNull(schema);
        assertEquals(schema.getAllNamespacedQueryOperations().size(), 1);
    }

    @Test
    public void apiWithNameTestWithEnabledUseNamespaces() {
        Index index = IndexCreator.indexWithPackage(ApiWithNameAndNamespace.class);

        assertThrows(RuntimeException.class, () -> SchemaBuilder.build(index));
    }

    @Test
    public void apiWithNamespaceTest() {
        Index index = IndexCreator.indexWithPackage(ApiWithNamespace.class);
        Schema schema = SchemaBuilder.build(index);

        assertNotNull(schema);
        assertEquals(schema.getAllNamespacedQueryOperations().size(), 1);
    }
}
