package io.smallrye.graphql.tests.client.typesafe;

import jakarta.inject.Inject;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.execution.context.SmallRyeContext;

@GraphQLApi
public class ServerPayloadApi {
    @Inject
    SmallRyeContext smallRyeContext;

    @Query
    public String getPayloadResult(String value) {
        if (!(smallRyeContext
                .getDataFetchingEnvironment()
                .getGraphQlContext()
                .get("init-payload")
                .toString())
                        .equals(value)) {
            throw new RuntimeException();
        }
        return "ok";
    }
}
