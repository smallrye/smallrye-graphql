package io.smallrye.graphql.execution;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import graphql.schema.GraphQLSchema;

/**
 * Produces the GraphQL Schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class GraphQLProducer {

    private GraphQLSchema graphQLSchema;

    public void setGraphQLSchema(GraphQLSchema graphQLSchema) {
        this.graphQLSchema = graphQLSchema;
    }

    @Produces
    public GraphQLSchema getGraphQLSchema() {
        return this.graphQLSchema;
    }
}