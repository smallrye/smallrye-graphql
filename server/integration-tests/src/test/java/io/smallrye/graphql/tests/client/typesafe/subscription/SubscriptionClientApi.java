package io.smallrye.graphql.tests.client.typesafe.subscription;

import java.io.Closeable;

import org.eclipse.microprofile.graphql.Subscription;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.mutiny.Multi;

@GraphQLClientApi
public interface SubscriptionClientApi extends Closeable {

    @Subscription
    Multi<Dummy> countToFive(boolean shouldFail);

    @Subscription(value = "countToFive")
    Multi<DummyWithSourceField> countToFiveWithFailingSourceField(boolean shouldFail);

    // the `shouldFail` argument refers to the operation root, not the source field!
    // the source field will fail always
    @Subscription(value = "countToFive")
    Multi<DummyWithErrorOrOnFailingSourceField> countToFiveWithFailingSourceFieldInErrorOr(boolean shouldFail);

    @Subscription
    Multi<Integer> failingImmediately();

    @Subscription
    Multi<Integer> throwingExceptionDirectly();

}
