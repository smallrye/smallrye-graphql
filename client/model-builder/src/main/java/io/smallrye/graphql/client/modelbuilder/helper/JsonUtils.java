package io.smallrye.graphql.client.modelbuilder.helper;

import jakarta.json.Json;
import jakarta.json.stream.JsonParser;

import java.io.Reader;

public class JsonUtils {
    public static boolean IS_PARSON_PRESENT = checkParsson();

    public static boolean checkParsson() {
        try {
            // is there any JsonParserImpl present?
            try (JsonParser parser = Json.createParser(Reader.nullReader())) {
                return true;
            }
        } catch (jakarta.json.JsonException ex1) {
            return false;
        }
    }
}
