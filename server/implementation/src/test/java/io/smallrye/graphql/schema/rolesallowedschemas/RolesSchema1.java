package io.smallrye.graphql.schema.rolesallowedschemas;

import jakarta.annotation.security.RolesAllowed;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;

@GraphQLApi
public class RolesSchema1 {
    @RolesAllowed(value = "admin")
    @Query
    public String hello() {
        return "hello";
    }

    @Query
    public String anotherHello(String param) {
        return param;
    }

    @RolesAllowed(value = "admin")
    @Mutation
    public String createHello(String hello) {
        return "hello";
    }

    @RolesAllowed(value = "basic")
    @Subscription
    public Multi<String> helloCreated() {
        return BroadcastProcessor.create();
    }

}
