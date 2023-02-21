package io.smallrye.graphql.tests.extensions;

import java.util.List;

import jakarta.inject.Inject;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.execution.context.SmallRyeContext;

@GraphQLApi
public class ShirtResource {

    @Inject
    SmallRyeContext smallRyeContext;

    @Query
    public List<Shirt> getShirts() {
        smallRyeContext.addExtension("bar", "This is test for extensions");
        smallRyeContext.addExtension("foo", 3.14159);
        return null;
    }
}
