package io.smallrye.graphql.test.resolver;

import org.eclipse.microprofile.graphql.GraphQLApi;

import io.smallrye.graphql.api.federation.Resolver;

@GraphQLApi
public class ExtendedApi {
    @Resolver
    public ExtendedType extendedTypeById(String id) {
        ExtendedType extendedType = new ExtendedType();
        extendedType.setId(id);
        return extendedType;
    }

    @Resolver
    public ExtendedType extendedTypeByIdNameKey(String id, String name, String key) {
        ExtendedType extendedType = new ExtendedType();
        extendedType.setId(id);
        extendedType.setName(name);
        extendedType.setKey(key);
        extendedType.setValue(id + name + key);
        return extendedType;
    }
}
