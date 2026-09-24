package io.smallrye.graphql.tests.client.typesafe;

import jakarta.inject.Inject;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.execution.context.SmallRyeContext;
import tools.jackson.databind.json.JsonMapper;

@GraphQLApi
public class ServerPayloadApi {
    @Inject
    SmallRyeContext smallRyeContext;

    @Query
    public String getPayloadResult(String value) {
        Object initPayload = smallRyeContext
                .getDataFetchingEnvironment()
                .getGraphQlContext()
                .get("init-payload");
        if (!JsonMapper.shared().writeValueAsString(initPayload).equals(value)) {
            throw new RuntimeException();
        }
        return "ok";
    }
}
