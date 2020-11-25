package io.smallrye.graphql.tests.validation;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.Valid;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
@ApplicationScoped
public class ValidatingGraphQLApi {

    @Query
    public Person person() {
        return new Person();
    }

    @Mutation("update")
    public Person update(@Valid Person person) {
        return person;
    }
}
