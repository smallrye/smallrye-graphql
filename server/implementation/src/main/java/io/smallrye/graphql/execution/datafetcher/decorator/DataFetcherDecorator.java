package io.smallrye.graphql.execution.datafetcher.decorator;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;

public interface DataFetcherDecorator {

    void before(DataFetchingEnvironment env);

    void after(DataFetchingEnvironment env, GraphQLContext newGraphQLContext);

}
