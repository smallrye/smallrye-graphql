package io.smallrye.graphql.client.vertx.websocket.opid;

import java.util.concurrent.atomic.AtomicLong;

public class IncrementingNumberOperationIDGenerator implements OperationIDGenerator {

    private final AtomicLong generator = new AtomicLong(1);

    @Override
    public String generate() {
        return String.valueOf(generator.getAndIncrement());
    }

}
