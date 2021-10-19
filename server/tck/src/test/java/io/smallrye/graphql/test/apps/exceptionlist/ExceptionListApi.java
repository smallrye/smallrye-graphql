package io.smallrye.graphql.test.apps.exceptionlist;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class ExceptionListApi {

    @Query
    public String checkedException() throws MyCheckedException {
        throw new MyCheckedException("This error should not show");
    }

    @Query
    public String uncheckedException() throws MyUncheckedException {
        throw new MyUncheckedException("This error should show");
    }

}
