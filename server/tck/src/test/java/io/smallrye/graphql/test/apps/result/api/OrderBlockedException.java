package io.smallrye.graphql.test.apps.result.api;

public class OrderBlockedException extends RuntimeException {
    private final OrderBlockedReason reason;

    public OrderBlockedException(OrderBlockedReason reason) {
        this.reason = reason;
    }

    public OrderBlockedReason getReason() {
        return reason;
    }
}
