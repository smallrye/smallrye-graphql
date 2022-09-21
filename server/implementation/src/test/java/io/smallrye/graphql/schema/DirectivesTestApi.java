package io.smallrye.graphql.schema;

import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;

@GraphQLApi
public class DirectivesTestApi {

    @Query
    @OperationDirective
    public TestTypeWithDirectives queryWithDirectives(@ArgumentDirective List<String> arg) {
        return null;
    }

    @Mutation
    @OperationDirective
    public TestTypeWithDirectives mutationWithDirectives(@ArgumentDirective List<String> arg) {
        return null;
    }

    @Subscription
    @OperationDirective
    public Multi<TestTypeWithDirectives> subscriptionWithDirectives(@ArgumentDirective List<String> arg) {
        return Multi.createFrom().empty();
    }

    @Query
    public TestInterfaceDirective queryWithInterfaceDirectives() {
        return new TestInterfaceDirectiveImpl();
    }

}
