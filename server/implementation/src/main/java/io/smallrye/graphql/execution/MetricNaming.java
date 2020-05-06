package io.smallrye.graphql.execution;

import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLType;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.OperationType;

public class MetricNaming {

    public static String fromTypeAndName(GraphQLType type, String name) {
        return "mp_graphql_" + getName(type) + "_" + name;
    }

    public static String fromOperation(Operation operation) {
        if (operation.getOperationType() == OperationType.Mutation) {
            return "mp_graphql_Mutation_" + operation.getName();
        } else if (operation.getOperationType() == OperationType.Query) {
            return "mp_graphql_Query_" + operation.getName();
        } else {
            return "mp_graphql_" + operation.getContainingType().getName() + "_" + operation.getName();
        }
    }

    private static String getName(GraphQLType graphQLType) {
        if (graphQLType instanceof GraphQLNamedType) {
            return ((GraphQLNamedType) graphQLType).getName();
        } else if (graphQLType instanceof GraphQLNonNull) {
            return getName(((GraphQLNonNull) graphQLType).getWrappedType());
        } else if (graphQLType instanceof GraphQLList) {
            return getName(((GraphQLList) graphQLType).getWrappedType());
        }
        return "";
    }

}
