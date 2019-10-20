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

    public String generateSchema() {
        return generateSchema(indexInitializer.createIndex());
    }

    public String generateSchema(Index index) {
        this.index = index;
        return graphQLSchemaInitializer.createGraphQLSchema();
    }

}
