package io.smallrye.graphql;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.jandex.Index;

import graphql.schema.GraphQLSchema;
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

    @Produces
    private GraphQLSchema graphQLSchema;

    public GraphQLSchema generateSchema() {
        return generateSchema(indexInitializer.createIndex());
    }

    public GraphQLSchema generateSchema(Index index) {
        this.index = index;
        this.graphQLSchema = graphQLSchemaInitializer.createGraphQLSchema();
        return this.graphQLSchema;
    }

}
