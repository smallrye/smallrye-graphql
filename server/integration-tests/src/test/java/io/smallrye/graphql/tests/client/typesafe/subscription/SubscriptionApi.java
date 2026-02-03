package io.smallrye.graphql.tests.client.typesafe.subscription;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;
import org.eclipse.microprofile.graphql.Subscription;

import io.smallrye.mutiny.Multi;

@GraphQLApi
public class SubscriptionApi {

    // this needs to be here because a GraphQLApi with only subscriptions does not work
    @Query
    public String nothing() {
        return null;
    }

    @Subscription
    public Multi<Dummy> countToFive(boolean shouldFail) {
        if (shouldFail) {
            return Multi.createFrom().failure(new RuntimeException("FAILED OPERATION ROOT"));
        } else {
            return Multi.createFrom().range(0, 5).map(Dummy::new);
        }
    }

    public Integer failingSourceField(@Source Dummy dummy) {
        throw new RuntimeException("FAILED SOURCE FIELD");
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
