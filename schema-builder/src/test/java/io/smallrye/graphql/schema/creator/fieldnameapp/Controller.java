package io.smallrye.graphql.schema.creator.fieldnameapp;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class Controller {

    @Query
    public SomeObjectAnnotatedGetters someObjectAnnotatedGetters() {
        return new SomeObjectAnnotatedGetters();
    }
}