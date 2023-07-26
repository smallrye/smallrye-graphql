package io.smallrye.graphql.spi;

import io.smallrye.graphql.api.Context;

public interface MetricsService {
    void start(Long measurementId, Context context);

    void end(Long measurementId);
}
