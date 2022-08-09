package io.smallrye.graphql.execution.resolver;

import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import graphql.TypeResolutionEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.TypeResolver;
import io.smallrye.graphql.schema.model.UnionType;

public class UnionResolver implements TypeResolver {

    private final UnionType unionType;

    public UnionResolver(UnionType unionType) {
        this.unionType = unionType;
    }

    @Override
    public GraphQLObjectType getType(TypeResolutionEnvironment tre) {
        String concreteClassName = tre.getObject().getClass().getName();

        GraphQLObjectType graphQLObjectType = UnionOutputRegistry.getGraphQLObjectType(unionType, concreteClassName);
        if (graphQLObjectType != null) {
            return graphQLObjectType;
        } else {
            throw msg.concreteClassNotFoundForInterface(concreteClassName, unionType.getName());
        }
    }
}
