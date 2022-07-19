package io.smallrye.graphql.execution.resolver;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import graphql.schema.GraphQLObjectType;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.Type;
import io.smallrye.graphql.schema.model.UnionType;

public class UnionOutputRegistry {

    private static final Map<String, Map<String, GraphQLObjectType>> unionMap = new HashMap<>();

    private UnionOutputRegistry() {
    }

    public static void register(Type type, GraphQLObjectType graphQLObjectType) {
        if (type.hasUnionMemberships()) {
            Set<Reference> memberships = type.getUnionMemberships();
            for (Reference i : memberships) {
                String union = i.getName();
                Map<String, GraphQLObjectType> concreteMap = getConcreteMap(union);
                concreteMap.put(type.getClassName(), graphQLObjectType);
                unionMap.put(union, concreteMap);
            }
        }
    }

    public static GraphQLObjectType getGraphQLObjectType(UnionType unionType, String concreteName) {
        String union = unionType.getName();
        if (unionMap.containsKey(union)) {
            return unionMap.get(union).get(concreteName);
        }
        return null;
    }

    private static Map<String, GraphQLObjectType> getConcreteMap(String union) {
        if (unionMap.containsKey(union)) {
            return unionMap.get(union);
        } else {
            return new HashMap<>();
        }
    }
}
