package io.smallrye.graphql.client.impl.typesafe.reflection;

public interface NamedElement {
    String getName();

    String getRawName();

    boolean isRenamed();
}
