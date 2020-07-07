package io.smallrye.graphql.spi;

import java.util.Iterator;
import java.util.ServiceLoader;

import graphql.schema.GraphQLSchema;

/**
 * Schema Building Extension service, that allows developers to
 * programmatically add GraphQL-Java models etc to the schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface SchemaBuildingExtensionService {

    static GraphQLSchema.Builder fireBeforeBuild(GraphQLSchema.Builder builder) {
        ServiceLoader<SchemaBuildingExtensionService> sl = ServiceLoader.load(SchemaBuildingExtensionService.class);
        Iterator<SchemaBuildingExtensionService> it = sl.iterator();

        while (it.hasNext()) {
            SchemaBuildingExtensionService extensionService = it.next();
            builder = extensionService.beforeBuild(builder);
        }

        return builder;
    }

    default GraphQLSchema.Builder beforeBuild(GraphQLSchema.Builder builder) {
        return builder;
    }
}
