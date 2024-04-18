package io.smallrye.graphql.spi;

import io.smallrye.graphql.api.Context;

public interface MetricsService {
    void start(Long measurementId, Context context);

    void end(Long measurementId);

    /**
     * Tracks the number of active connections to GraphQL Subscriptions
     * Indicates to backing Metrics Service that a new connection has been established.
     *
     * @param context GraphQL Subscription Context
     */
    void subscriptionStart(Context context);

    /**
     * Tracks the number of active connections to GraphQL Subscriptions
     * Indicates to backing Metrics Service that a connection has been terminated
     *
     * @param context GraphQL Subscription Context
     */
    void subscriptionEnd(Context context);
}
