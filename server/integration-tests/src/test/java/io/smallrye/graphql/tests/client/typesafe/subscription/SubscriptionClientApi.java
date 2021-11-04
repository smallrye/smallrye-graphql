package io.smallrye.graphql.tests.client.typesafe.subscription;

import java.io.Closeable;

import io.smallrye.graphql.api.Subscription;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.mutiny.Multi;

@GraphQLClientApi
public interface SubscriptionClientApi extends Closeable {

    @Subscription
    Multi<Integer> countToFive();

    @Subscription
    Multi<Integer> failingImmediately();

}
