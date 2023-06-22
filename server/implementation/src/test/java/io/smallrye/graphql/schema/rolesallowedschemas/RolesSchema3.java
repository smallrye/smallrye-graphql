package io.smallrye.graphql.schema.rolesallowedschemas;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;

@GraphQLApi
public class RolesSchema3 {
    @Query
    public String doNothing() {
        return "nothing was done";
    }

    @Mutation
    public String createNothing() {
        return "created nothing";
    }

    @Subscription
    public Multi<String> nothingCreated() {
        return BroadcastProcessor.create();
    }

    public String getNothingPassword(@Source Customer customer) {
        return "nothing";
    }
}
