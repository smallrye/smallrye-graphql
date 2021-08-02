package io.smallrye.graphql.tests.client.parsing;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

@GraphQLClientApi
public interface FormatAnnotationsClientApi {

    ObjectWithFormattedFields something();

}
