package io.smallrye.graphql.client.impl.typesafe.reflection;

public interface NamedElement {
    String getName();

    String getRawName();

    default boolean isRenamed() {
        return !getName().equals(getRawName());
    }
}
