package io.smallrye.graphql.tests.tracing;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import io.opentracing.Tracer;
import io.smallrye.mutiny.Uni;

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

    @Query(value = "foo")
    public Foo foo() {
        return new Foo();
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

    public Foo2 foo2(@Source Foo foo) {
        return new Foo2(4);
    }

    public Uni<Foo2> foo2uni(@Source Foo foo) {
        return Uni.createFrom().item(new Foo2(3));
    }

    public Foo3 foo3(@Source Foo2 foo2) {
        return new Foo3(5);
    }

}
