package io.smallrye.graphql.tests.client.typesafe.ignoreannotation;

import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.tests.client.typesafe.ignoreannotation.clientmodels.Person;

@GraphQLClientApi
public interface IgnoreClientApi {
    @Query
    Person person();
}
