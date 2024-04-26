package io.smallrye.graphql.client.vertx.typesafe;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.smallrye.graphql.client.impl.typesafe.QueryBuilder;
import io.smallrye.graphql.client.impl.typesafe.reflection.MethodInvocation;
import io.smallrye.graphql.client.model.MethodKey;

public class ModelReflection {
    private final ConcurrentMap<MethodKey, String> queryCache = new ConcurrentHashMap<>();

    String getOperation(MethodInvocation method) {
        return queryCache.computeIfAbsent(method.getMethodKey(), key -> new QueryBuilder(method).build());
    }
}
