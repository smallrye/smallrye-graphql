package io.smallrye.graphql.execution.resolver;

import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import graphql.TypeResolutionEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.TypeResolver;
import io.smallrye.graphql.schema.model.InterfaceType;

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

        String concreteClassName = tre.getObject().getClass().getName();

        GraphQLObjectType graphQLObjectType = InterfaceOutputRegistry.getGraphQLObjectType(interfaceType,
                concreteClassName);
        if (graphQLObjectType != null) {
            return graphQLObjectType;
        } else {
            throw msg.concreteClassNotFoundForInterface(concreteClassName,
                    interfaceType.getName());
        }
    }

}
