package io.smallrye.graphql.tests.client.typesafe.directives;

import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.tests.client.typesafe.directives.model.SomeClassClient;

@GraphQLClientApi
public interface ClientApi {

    @Query
    @FieldDirective(fields = 1)
    @FieldDirective(fields = { 2, 3 })
    @VariableDefinitionDirective(fields = "should ignore")
    SomeClassClient getQuerySomeClass(
            @FieldDirective(fields = 999) /* will ignore */ SomeClassClient someObject,
            @VariableDefinitionDirective @VariableDefinitionDirective(fields = "a") boolean simpleType);

}
