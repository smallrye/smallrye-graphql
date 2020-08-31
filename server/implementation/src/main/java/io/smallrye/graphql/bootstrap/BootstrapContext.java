package io.smallrye.graphql.bootstrap;

import org.dataloader.BatchLoaderWithContext;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.execution.batchloader.SourceBatchLoader;
import io.smallrye.graphql.execution.batchloader.SourceBatchLoaderHelper;
import io.smallrye.graphql.schema.model.Operation;

/**
 * Some context while bootstrapping
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class BootstrapContext {

    private GraphQLSchema graphQLSchema;
    private final DataLoaderRegistry dataLoaderRegistry = new DataLoaderRegistry();
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();

    public void setGraphQLSchema(GraphQLSchema graphQLSchema) {
        this.graphQLSchema = graphQLSchema;
    }

    public void registerBatchLoader(Operation operation, Config config) {
        BatchLoaderWithContext<Object, Object> batchLoader = new SourceBatchLoader(operation, config);
        this.dataLoaderRegistry.register(SourceBatchLoaderHelper.getName(operation), DataLoader.newDataLoader(batchLoader));
    }

    public void registerDataLoader(String name, DataLoader<?, ?> dataLoader) {
        this.dataLoaderRegistry.register(name, dataLoader);
    }

    public GraphQLSchema getGraphQLSchema() {
        return this.graphQLSchema;
    }

    public DataLoaderRegistry getDataLoaderRegistry() {
        return this.dataLoaderRegistry;
    }

    public GraphQLCodeRegistry.Builder getCodeRegistryBuilder() {
        return this.codeRegistryBuilder;
    }
}
