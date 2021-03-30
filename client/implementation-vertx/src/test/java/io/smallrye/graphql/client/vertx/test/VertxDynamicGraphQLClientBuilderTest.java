package io.smallrye.graphql.client.vertx.test;

import org.junit.Assert;
import org.junit.Test;

import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClientBuilder;
import io.smallrye.graphql.client.dynamic.vertx.VertxDynamicGraphQLClientBuilder;

public class VertxDynamicGraphQLClientBuilderTest {

    @Test
    public void testBuilder() {
        DynamicGraphQLClientBuilder builder = DynamicGraphQLClientBuilder.newBuilder();
        Assert.assertEquals(VertxDynamicGraphQLClientBuilder.class, builder.getClass());
    }

}
