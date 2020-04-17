package io.smallrye.graphql.client.impl.json;

import io.smallrye.graphql.client.impl.reflection.TypeInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
class Location {
    private final TypeInfo type;
    private final String description;

    @Override
    public String toString() {
        return type.getTypeName() + " value for " + description;
    }
}
