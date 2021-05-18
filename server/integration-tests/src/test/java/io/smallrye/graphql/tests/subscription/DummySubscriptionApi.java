package io.smallrye.graphql.tests.subscription;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;

@GraphQLApi
public class DummySubscriptionApi {

    // FIXME: this temporarily needs to be here because a GraphQLApi with only subscriptions does not work atm
    @Query
    public String nothing() {
        return null;
    }

    @Subscription
    public Multi<Integer> countToFive() {
        return Multi.createFrom().items(1, 2, 3, 4, 5);
    }
}
