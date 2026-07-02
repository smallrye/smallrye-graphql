package io.smallrye.graphql.json;

import java.util.Map;

import tools.jackson.databind.PropertyNamingStrategies;

/**
 * Naming strategy that takes GraphQL annotations into account
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class GraphQLNamingStrategy extends PropertyNamingStrategies.NamingBase {
    private final Map<String, String> customFieldNameMapping;

    public GraphQLNamingStrategy(Map<String, String> customFieldNameMapping) {
        this.customFieldNameMapping = customFieldNameMapping;
    }

    @Override
    public String translate(String name) {
        // Name mapping for input transformation
        return customFieldNameMapping.getOrDefault(name, name);
    }
}
