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
                String iclass = i.getClassName();
                Map<String, GraphQLObjectType> concreateMap = getConcreteMap(iclass);
                concreateMap.put(type.getClassName(), graphQLObjectType);
                interfaceMap.put(iclass, concreateMap);
            }
        }
    }

    public static GraphQLObjectType getGraphQLObjectType(String interfaceClassName, String concreateName) {
        if (interfaceMap.containsKey(interfaceClassName)) {
            Map<String, GraphQLObjectType> concreateMap = interfaceMap.get(interfaceClassName);
            return concreateMap.get(concreateName);
        }
        return null;
    }

    private static Map<String, GraphQLObjectType> getConcreteMap(String interfaceClassName) {
        if (interfaceMap.containsKey(interfaceClassName)) {
            return interfaceMap.get(interfaceClassName);
        } else {
            return new HashMap<>();
        }
    }

}
