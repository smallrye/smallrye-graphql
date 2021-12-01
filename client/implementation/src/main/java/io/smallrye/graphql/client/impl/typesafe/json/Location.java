package io.smallrye.graphql.client.impl.typesafe.json;

import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;

class Location {
    private final TypeInfo type;
    private final String description;

    public Location(TypeInfo type, String description) {
        this.type = type;
        this.description = description;
    }

    @Override
    public String toString() {
        return type.getTypeName() + " value for " + description;
    }

    public TypeInfo getType() {
        return this.type;
    }

    public String getDescription() {
        return this.description;
    }
}
