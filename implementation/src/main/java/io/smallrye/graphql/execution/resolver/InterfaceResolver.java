package io.smallrye.graphql.execution.resolver;

import graphql.TypeResolutionEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.TypeResolver;

/**
 * Resolve an interface.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class InterfaceResolver implements TypeResolver {

    private final String interfaceName;

    public InterfaceResolver(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    @Override
    public GraphQLObjectType getType(TypeResolutionEnvironment tre) {
        return null;
    }

}
