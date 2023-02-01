package io.smallrye.graphql.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.impl.RequestImpl;

/**
 * Validates the JSON serialization of the query and variables in a RequestImpl
 * <p>
 * See https://github.com/smallrye/smallrye-graphql/issues/1704
 */
public class RequestImplTest {
    @Test
    public void testPrimitiveTypesToJson() {
        RequestImpl request = new RequestImpl("example");

        request.setVariable("key", 5);
        assertEquals("{\"query\":\"example\",\"variables\":{\"key\":5}}", request.toJson());

        request.setVariable("key", true);
        assertEquals("{\"query\":\"example\",\"variables\":{\"key\":true}}", request.toJson());

        request.setVariable("key", null);
        assertEquals("{\"query\":\"example\",\"variables\":{\"key\":null}}", request.toJson());

        request.setVariable("key", "foo");
        assertEquals("{\"query\":\"example\",\"variables\":{\"key\":\"foo\"}}", request.toJson());
    }

    @Test
    public void testComplexTypesToJson() {
        RequestImpl request = new RequestImpl("example");

        int[] arr = { 1, 2 };
        request.setVariable("key", arr);
        assertEquals("{\"query\":\"example\",\"variables\":{\"key\":[1,2]}}", request.toJson());

        HashMap<String, int[]> map = new HashMap<String, int[]>();
        map.put("ids", arr);
        request.setVariable("key", map);
        assertEquals("{\"query\":\"example\",\"variables\":{\"key\":{\"ids\":[1,2]}}}", request.toJson());
    }
}
