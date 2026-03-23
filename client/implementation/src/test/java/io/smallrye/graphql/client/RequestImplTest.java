package io.smallrye.graphql.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.HashMap;
import java.util.Map;

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

        request.setVariable("key", 5L);
        assertEquals("{\"query\":\"example\",\"variables\":{\"key\":5}}", request.toJson());

        request.setVariable("key", 5.5);
        assertEquals("{\"query\":\"example\",\"variables\":{\"key\":5.5}}", request.toJson());
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

        request.setVariable("key", new TestRecord(TestEnum.TEST));
        assertEquals("{\"query\":\"example\",\"variables\":{\"key\":{\"enumValue\":\"TEST\"}}}", request.toJson());

        request.setVariable("key", TestEnum.TEST);
        assertEquals("{\"query\":\"example\",\"variables\":{\"key\":\"TEST\"}}", request.toJson());
    }

    public record TestRecord(TestEnum enumValue) {
    }

    public enum TestEnum {
        TEST
    }

    @Test
    public void testBuilderQueryOnly() {
        RequestImpl request = RequestImpl.builder("{ hello }").build();
        assertEquals("{\"query\":\"{ hello }\"}", request.toJson());
    }

    @Test
    public void testBuilderWithVariables() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "world");
        RequestImpl request = RequestImpl.builder("query($name: String) { hello(name: $name) }")
                .variables(variables)
                .build();
        assertEquals(
                "{\"query\":\"query($name: String) { hello(name: $name) }\",\"variables\":{\"name\":\"world\"}}",
                request.toJson());
    }

    @Test
    public void testBuilderWithOperationName() {
        RequestImpl request = RequestImpl.builder("query MyOp { hello }")
                .operationName("MyOp")
                .build();
        assertEquals(
                "{\"query\":\"query MyOp { hello }\",\"operationName\":\"MyOp\"}",
                request.toJson());
    }

    @Test
    public void testBuilderWithExtensions() {
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("traceId", "abc-123");
        extensions.put("priority", 1);
        RequestImpl request = RequestImpl.builder("{ hello }")
                .extensions(extensions)
                .build();
        jakarta.json.JsonObject obj = request.toJsonObject();
        assertEquals("{ hello }", obj.getString("query"));
        jakarta.json.JsonObject ext = obj.getJsonObject("extensions");
        assertEquals("abc-123", ext.getString("traceId"));
        assertEquals(1, ext.getInt("priority"));
    }

    @Test
    public void testBuilderWithAllFields() {
        Map<String, Object> variables = Map.of("id", 42);
        Map<String, Object> extensions = Map.of("token", "secret");
        RequestImpl request = RequestImpl.builder("query GetItem($id: Int) { item(id: $id) }")
                .variables(variables)
                .operationName("GetItem")
                .extensions(extensions)
                .build();
        jakarta.json.JsonObject obj = request.toJsonObject();
        assertEquals("query GetItem($id: Int) { item(id: $id) }", obj.getString("query"));
        assertEquals("GetItem", obj.getString("operationName"));
        assertEquals(42, obj.getJsonObject("variables").getInt("id"));
        assertEquals("secret", obj.getJsonObject("extensions").getString("token"));
    }

    @Test
    public void testBuilderNoExtensionsOmitsField() {
        RequestImpl request = RequestImpl.builder("{ hello }").build();
        jakarta.json.JsonObject obj = request.toJsonObject();
        assertFalse(obj.containsKey("extensions"));
    }

    @Test
    public void testBuilderEmptyExtensionsOmitsField() {
        RequestImpl request = RequestImpl.builder("{ hello }")
                .extensions(new HashMap<>())
                .build();
        jakarta.json.JsonObject obj = request.toJsonObject();
        assertFalse(obj.containsKey("extensions"));
    }
}
