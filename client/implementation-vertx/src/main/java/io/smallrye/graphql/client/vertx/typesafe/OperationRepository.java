package io.smallrye.graphql.client.vertx.typesafe;

import io.smallrye.graphql.client.impl.typesafe.reflection.MethodInvocation;

@FunctionalInterface
public interface OperationRepository {
    String findOperation(MethodInvocation methodInvocation);
}
