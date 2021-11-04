package io.smallrye.graphql.tests.client.typesafe.subscription;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;

@GraphQLApi
public class SubscriptionApi {

    // this needs to be here because a GraphQLApi with only subscriptions does not work
    @Query
    public String nothing() {
        return null;
    }

    @Subscription
    public Multi<Integer> countToFive() {
        return Multi.createFrom().range(0, 5);
    }

    @Subscription
    public Multi<Integer> failingImmediately() {
        return Multi.createFrom().failure(new RuntimeException("blabla"));
    }

}
