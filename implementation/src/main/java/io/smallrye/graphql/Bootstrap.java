package io.smallrye.graphql;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.jandex.Index;

import io.smallrye.graphql.index.IndexInitializer;
import io.smallrye.graphql.schema.GraphQLSchemaInitializer;

/**
 * Bootstrap MicroProfile GraphQL
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class Bootstrap {

    @Inject
    private IndexInitializer indexInitializer;

    @Inject
    private GraphQLSchemaInitializer graphQLSchemaInitializer;

    @Produces
    private Index index;

    public void bootstrap() {
        bootstrap(indexInitializer.createIndex());
    }

    public void bootstrap(Index index) {
        this.index = index;

        graphQLSchemaInitializer.createGraphQLSchema();
    }

}
