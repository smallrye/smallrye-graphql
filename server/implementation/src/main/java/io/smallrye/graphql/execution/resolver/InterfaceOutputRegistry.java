package io.smallrye.graphql.execution.resolver;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import graphql.schema.GraphQLObjectType;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.Type;

/**
 * Here we register output objects that implements some interface
 * 
 * We need this to resolve the correct concrete class
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class InterfaceOutputRegistry {

    private static final Map<String, Map<String, GraphQLObjectType>> interfaceMap = new HashMap<>();

    private InterfaceOutputRegistry() {
    }

    public static void register(Type type, GraphQLObjectType graphQLObjectType) {
        if (type.hasInterfaces()) {
            Set<Reference> interfaces = type.getInterfaces();
            for (Reference i : interfaces) {
                String itype = i.getName();
                Map<String, GraphQLObjectType> concreateMap = getConcreteMap(itype);
                concreateMap.put(type.getClassName(), graphQLObjectType);
                interfaceMap.put(itype, concreateMap);
            }
        }
    }

    public static GraphQLObjectType getGraphQLObjectType(Type interfaceType, String concreateName) {
        String itype = interfaceType.getName();
        if (interfaceMap.containsKey(itype)) {
            Map<String, GraphQLObjectType> concreateMap = interfaceMap.get(itype);
            return concreateMap.get(concreateName);
        }
        return null;
    }

    private static Map<String, GraphQLObjectType> getConcreteMap(String itype) {
        if (interfaceMap.containsKey(itype)) {
            return interfaceMap.get(itype);
        } else {
            return new HashMap<>();
        }
    }

}
