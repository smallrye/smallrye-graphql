package io.smallrye.graphql.test.apps.nonnull.api;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.test.apps.nonnull.api.nonnull_package.NonNullPackageClass;

@GraphQLApi
public class NonNullApi {

    @Query
    public NonNullClass nonNullClass(NonNullClass nonNullClass) {
        return new NonNullClass();
    }

    @Query
    public NonNullPackageClass nonNullPackageClass(NonNullPackageClass nonNullPackageClass) {
        return new NonNullPackageClass();
    }

}
