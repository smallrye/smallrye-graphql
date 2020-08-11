package io.smallrye.graphql.cdi.event;

import java.lang.annotation.Annotation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.cdi.event.annotation.AfterDataFetch;
import io.smallrye.graphql.cdi.event.annotation.AfterExecute;
import io.smallrye.graphql.cdi.event.annotation.BeforeDataFetch;
import io.smallrye.graphql.cdi.event.annotation.BeforeExecute;
import io.smallrye.graphql.cdi.event.annotation.ErrorDataFetch;
import io.smallrye.graphql.cdi.event.annotation.ErrorExecute;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.spi.EventingService;

/**
 * Implements the EventingService interface and use CDI Events
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class CdiEventingService implements EventingService {

    @Override
    public GraphQLSchema.Builder beforeSchemaBuild(GraphQLSchema.Builder builder) {
        fire(builder);
        return builder;
    }

    @Override
    public Operation createOperation(Operation operation) {
        fire(operation);
        return operation;
    }

    @Override
    public void beforeExecute(Context context) {
        fire(context, BeforeExecute.LITERAL);
    }

    @Override
    public void errorExecute(Context context) {
        fire(context, ErrorExecute.LITERAL);
    }

    @Override
    public void afterExecute(Context context) {
        fire(context, AfterExecute.LITERAL);
    }

    @Override
    public void beforeDataFetch(Context context) {
        fire(context, BeforeDataFetch.LITERAL);
    }

    @Override
    public void errorDataFetch(Context context) {
        fire(context, ErrorDataFetch.LITERAL);
    }

    @Override
    public void afterDataFetch(Context context) {
        fire(context, AfterDataFetch.LITERAL);
    }

    private void fire(Object o, Annotation... annotation) {
        try {
            CDI<Object> current = CDI.current();
            if (current != null) {
                BeanManager beanManager = current.getBeanManager();
                if (beanManager != null) {
                    beanManager.fireEvent(o, annotation);
                }
            }
        } catch (java.lang.IllegalStateException ise) {
            ise.printStackTrace();
        }
    }
}
