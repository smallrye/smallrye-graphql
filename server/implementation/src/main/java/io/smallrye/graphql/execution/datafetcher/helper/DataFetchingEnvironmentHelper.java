package io.smallrye.graphql.execution.datafetcher.helper;

import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLType;

public class DataFetchingEnvironmentHelper {

    /**
     * Gets the name of a type.
     * <p>
     * If it's a list or a nonnull-type, the type is unwrapped first.
     *
     * @param graphQLType the type to unwrap
     * @return the real name of the type.
     */
    public static String getName(GraphQLType graphQLType) {
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
