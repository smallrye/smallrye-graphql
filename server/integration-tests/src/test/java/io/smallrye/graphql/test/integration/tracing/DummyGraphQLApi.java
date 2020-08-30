package io.smallrye.graphql.test.integration.tracing;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import io.opentracing.Tracer;

@GraphQLApi
@ApplicationScoped
public class DummyGraphQLApi {

    private Foo foo = new Foo();

    @Inject
    Tracer tracer;

    @Query(value = "get")
    public Foo helloQuery() {

        foo.setTracerString(tracer.activeSpan().toString());
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
