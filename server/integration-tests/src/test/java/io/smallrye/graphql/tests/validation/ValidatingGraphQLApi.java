package io.smallrye.graphql.tests.validation;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.Valid;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.Context;

@GraphQLApi
@ApplicationScoped
public class ValidatingGraphQLApi {

    @Query
    public Person person() {
        return new Person();
    }

    // we inject a Context to make sure that it doesn't break the validation, see https://github.com/quarkusio/quarkus/issues/24183
    @Mutation("update")
    public Person update(Context context, @Valid Person person) {
        return person;
    }
}
