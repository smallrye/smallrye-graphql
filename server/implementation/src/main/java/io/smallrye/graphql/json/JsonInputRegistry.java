package io.smallrye.graphql.json;

import java.util.Map;

import jakarta.json.bind.Jsonb;

import io.smallrye.graphql.schema.model.InputType;

/**
 * Here we register input objects to be used when creating method calls
 * 
 * For now we need to
 * - hold a custom JsonB map for custom name mapping and
 * - hold a map of all fields in input types that needs transforming
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class JsonInputRegistry {

    private JsonInputRegistry() {
    }

    public static void register(InputType inputType) {
        JsonBCreator.register(inputType);
        InputFieldsInfo.register(inputType);
    }

    /**
     * Override Jsonb config for particular classes by the Jsonb instances
     * supplied by the user via an EventingService
     */
    public static void override(Map<String, Jsonb> overrides) {
        JsonBCreator.override(overrides);
    }
}
