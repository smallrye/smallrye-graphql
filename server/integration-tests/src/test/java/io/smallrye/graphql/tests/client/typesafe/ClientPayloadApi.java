package io.smallrye.graphql.tests.client.typesafe;

import java.io.Closeable;

import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

@GraphQLClientApi
public interface ClientPayloadApi extends Closeable {
    @Query
    String getPayloadResult(String value);
}
