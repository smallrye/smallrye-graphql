package tck.graphql.typesafe;

import static org.assertj.core.api.BDDAssertions.then;

import org.eclipse.microprofile.graphql.Mutation;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

class NillableBehavior {
    private final TypesafeGraphQLClientFixture fixture = TypesafeGraphQLClientFixture.load();

    @GraphQLClientApi
    interface StringMutationApi {
        @Mutation
        String createSome(SomeInput s);
    }

    @Test
    void nillableFieldsAreSet() {
        fixture.returnsData("'createSome':'foo'");
        StringMutationApi api = fixture.build(StringMutationApi.class);

        String response = api.createSome(new SomeInput("all", "a", "b", "c"));

        then(fixture.query()).isEqualTo("mutation createSome($s: SomeInput) { createSome(s: $s) }");
        then(fixture.variables()).isEqualTo("{'s':{'name':'all','first':'a','second':'b','third':'c'}}");
        then(response).isEqualTo("foo");
    }

    @Test
    void nillableFieldsAreNull() {
        fixture.returnsData("'createSome':'bar'");
        StringMutationApi api = fixture.build(StringMutationApi.class);

        String response = api.createSome(new SomeInput("none"));

        then(fixture.query()).isEqualTo("mutation createSome($s: SomeInput) { createSome(s: $s) }");
        then(fixture.variables()).isEqualTo("{'s':{'name':'none','second':null,'third':null}}");
        then(response).isEqualTo("bar");
    }
}
