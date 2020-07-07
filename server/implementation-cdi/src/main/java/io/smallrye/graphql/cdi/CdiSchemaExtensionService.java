package io.smallrye.graphql.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.spi.SchemaBuildingExtensionService;

/**
 * Implements the SchemaBuildingExtensionService interface and use CDI Events to extends the Schema Building
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class CdiSchemaExtensionService implements SchemaBuildingExtensionService {

    @Override
    public GraphQLSchema.Builder beforeBuild(GraphQLSchema.Builder builder) {
        CDI<Object> current = CDI.current();
        if (current != null) {
            BeanManager beanManager = current.getBeanManager();
            if (beanManager != null) {
                beanManager.fireEvent(builder);
            }
        }
        return builder;
    }
}