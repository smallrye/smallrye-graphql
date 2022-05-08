package io.smallrye.graphql.cdi.event;

import java.lang.annotation.Annotation;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.cdi.config.ConfigKey;
import io.smallrye.graphql.execution.event.InvokeInfo;
import io.smallrye.graphql.execution.event.Priorities;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.spi.EventingService;

/**
 * Implements the EventingService interface and use CDI Events
 * This allows users to take part in the events.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Priority(Priorities.FIRST_IN_LAST_OUT + 200)
public class EventsService implements EventingService {

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
    public GraphQL.Builder beforeGraphQLBuild(GraphQL.Builder builder) {
        fire(builder);
        return builder;
    }

    @Override
    public void beforeExecute(Context context) {
        fire(context, BeforeExecute.LITERAL);
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
    public void beforeInvoke(InvokeInfo invokeInfo) throws Exception {
        fire(invokeInfo);
    }

    @Override
    public void afterDataFetch(Context context) {
        fire(context, AfterDataFetch.LITERAL);
    }

    @Override
    public void errorExecute(String executionId, Throwable t) {
        fire(new ErrorInfo(executionId, t), ErrorExecute.LITERAL);
    }

    @Override
    public void errorDataFetch(String executionId, Throwable t) {
        fire(new ErrorInfo(executionId, t), ErrorDataFetch.LITERAL);
    }

    @Override
    public String getConfigKey() {
        return ConfigKey.ENABLE_EVENTS;
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
