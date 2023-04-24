package io.smallrye.graphql.json;

import java.util.Map;

import jakarta.json.bind.config.PropertyNamingStrategy;

/**
 * Naming strategy that take GraphQL annotations into account
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class GraphQLNamingStrategy implements PropertyNamingStrategy {
    private final Map<String, String> customFieldNameMapping;

    public GraphQLNamingStrategy(Map<String, String> customFieldNameMapping) {
        this.customFieldNameMapping = customFieldNameMapping;
    }

    @Override
    public String translateName(String string) {
        // Name mapping for input transformation
        if (customFieldNameMapping.containsKey(string)) {
            return customFieldNameMapping.get(string);
        }
        return string;
    }
}
