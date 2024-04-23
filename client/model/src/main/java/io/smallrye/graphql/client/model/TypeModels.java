package io.smallrye.graphql.client.model;

import java.util.List;
import java.util.Map;

/**
 * This is just a first step into having a more complete model of the GraphQL types in the client.
 * It currently only holds the subtypes of interfaces that are used to model unions.
 * In the long run, this should contain a complete model of all Java types relevant for the typesafe
 * GraphQL client.
 */
public class TypeModels {
    private static Map<String, List<Class<?>>> IMPLEMENTORS;

    public static void setImplementors(Map<String, List<Class<?>>> IMPLEMENTORS) {
        TypeModels.IMPLEMENTORS = IMPLEMENTORS;
    }

    public static List<Class<?>> getAllImplementorsOf(String interfaceTypeName) {
        return IMPLEMENTORS.get(interfaceTypeName);
    }
}
