package io.smallrye.graphql.tests.validation;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.Valid;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;

@GraphQLApi
@ApplicationScoped
public class ValidatingGraphQLApi {

    @Mutation("update")
    public Person update(@Valid Person person) {
        return person;
    }
}
