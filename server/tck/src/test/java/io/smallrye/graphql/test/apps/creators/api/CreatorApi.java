package io.smallrye.graphql.test.apps.creators.api;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class CreatorApi {

    @Query
    public WithJsonbCreator withJsonbCreator(WithJsonbCreator input) {
        return input;
    }

    @Query
    public CreatorWithTransformation creatorWithTransformation(CreatorWithTransformation input) {
        return input;
    }

    @Query
    public CreatorWithFieldDefault creatorWithFieldDefault(CreatorWithFieldDefault input) {
        return input;
    }

    @Query
    public CreatorWithParameterDefault creatorWithParameterDefault(CreatorWithParameterDefault input) {
        return input;
    }

    @Query
    public CreatorWithMultipleParameters creatorWithMultipleParameters(CreatorWithMultipleParameters input) {
        return input;
    }

    @Query
    public WithStaticFactory withStaticFactory(WithStaticFactory input) {
        return input;
    }

}
