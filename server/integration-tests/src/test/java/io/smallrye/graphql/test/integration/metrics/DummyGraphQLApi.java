package io.smallrye.graphql.test.integration.metrics;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

@GraphQLApi
@ApplicationScoped
public class DummyGraphQLApi {

    private Foo foo = new Foo();

    @Query(value = "get")
    public Foo helloQuery() {
        return foo;
    }

    @Mutation(value = "mutate")
    public Foo mutation() {
        foo.update();
        return foo;
    }

    @Name("description")
    public String source(@Source Foo foo) {
        return "Awesome";
    }

}
