package io.smallrye.graphql.tests.client.dynamic.subscription;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Subscription;

import io.smallrye.mutiny.Multi;

@GraphQLApi
public class DynamicClientSubscriptionApi {

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

    @Subscription
    public Multi<Integer> throwingExceptionDirectly() {
        throw new RuntimeException("blabla");
    }

}
