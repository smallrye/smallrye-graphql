package io.smallrye.graphql.schema.creator;

import org.eclipse.microprofile.graphql.Query;

public class TestApi {

    @Query
    String nonPublicQuery() {
        return null;
    }

    @Query
    public String publicQuery() {
        return null;
    }

}
