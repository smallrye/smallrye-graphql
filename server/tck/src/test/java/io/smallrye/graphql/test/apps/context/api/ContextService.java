package io.smallrye.graphql.test.apps.context.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import io.smallrye.graphql.api.Context;

/**
 * Some downstream service
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@RequestScoped
public class ContextService {

    @Inject
    Context context;

    public String getOperationType() {
        return context.getOperationType();
    }

}
