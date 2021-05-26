package io.smallrye.graphql.client.vertx.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClientBuilder;
import io.smallrye.graphql.client.dynamic.vertx.VertxDynamicGraphQLClientBuilder;

public class VertxDynamicGraphQLClientBuilderTest {

    @Test
    public void testBuilder() {
        DynamicGraphQLClientBuilder builder = DynamicGraphQLClientBuilder.newBuilder();
        assertEquals(VertxDynamicGraphQLClientBuilder.class, builder.getClass());
    }
}
