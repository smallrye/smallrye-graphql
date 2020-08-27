package io.smallrye.graphql.bootstrap;

import org.dataloader.DataLoaderRegistry;

import graphql.schema.GraphQLSchema;

/**
 * The result from a bootstrap.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class BootstrapedResult {

    private GraphQLSchema graphQLSchema = null;
    private DataLoaderRegistry dataLoaderRegistry = null;

    public BootstrapedResult() {
    }

    public BootstrapedResult(GraphQLSchema graphQLSchema, DataLoaderRegistry dataLoaderRegistry) {
        this.graphQLSchema = graphQLSchema;
        this.dataLoaderRegistry = dataLoaderRegistry;
    }

    public GraphQLSchema getGraphQLSchema() {
        return graphQLSchema;
    }

    public DataLoaderRegistry getDataLoaderRegistry() {
        return dataLoaderRegistry;
    }
}
