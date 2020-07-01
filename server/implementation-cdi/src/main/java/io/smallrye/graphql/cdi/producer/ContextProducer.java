package io.smallrye.graphql.cdi.producer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.execution.context.SmallRyeContext;

/**
 * Produces the Context
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class ContextProducer {

    @Produces
    @RequestScoped
    public Context getContext() {
        return SmallRyeContext.getContext();
    }
}
