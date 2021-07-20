package io.smallrye.graphql.client;

import java.util.List;
import java.util.Map;

import javax.json.JsonValue;

public interface Error {

    String getMessage();

    List<Map<String, Integer>> getLocations();

    Object[] getPath();

    Map<String, Object> getExtensions();

    /**
     * Any other fields beyond message, locations, path and extensions. These are discouraged by the spec,
     * but if a GraphQL service adds them, they will appear in this map.
     */
    Map<String, JsonValue> getOtherFields();
}
