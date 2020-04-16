package io.smallrye.graphql.execution.resolver;

import graphql.TypeResolutionEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.TypeResolver;
import io.smallrye.graphql.schema.model.InterfaceType;
import io.smallrye.graphql.x.typeresolver.ConcreteImplementationNotFoundException;

/**
 * Resolve an interface.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class InterfaceResolver implements TypeResolver {

    private final InterfaceType interfaceType;

    public InterfaceResolver(InterfaceType interfaceType) {
        this.interfaceType = interfaceType;
    }

    @Override
    public GraphQLObjectType getType(TypeResolutionEnvironment tre) {

        String concreateClassName = tre.getObject().getClass().getName();

        GraphQLObjectType graphQLObjectType = InterfaceOutputRegistry.getGraphQLObjectType(interfaceType.getClassName(),
                concreateClassName);
        if (graphQLObjectType != null) {
            return graphQLObjectType;
        } else {
            throw new ConcreteImplementationNotFoundException(
                    "No concrete class named [" + concreateClassName + "] found for interface ["
                            + interfaceType.getClassName() + "]");

        }
    }

}
