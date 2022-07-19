package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.json.JsonObject;

import org.junit.jupiter.api.Test;

/**
 * Test against unions
 *
 * @author Craig Day (cday@zendesk.com)
 */
public class ExecutionUnionsTest extends ExecutionTestBase {

    @Test
    public void testBasicUnion() {
        JsonObject data = executeAndGetData(TEST_BASIC_UNION);
        JsonObject testObject = data.getJsonObject("basicUnion");

        assertNotNull(testObject);

        assertFalse(testObject.isNull("name"), "name should not be null");
        assertEquals("my name", testObject.getString("name"));
    }

    @Test
    public void testUnionOfInterfacesReturningDirectImplementer() {
        JsonObject data = executeAndGetData(TEST_NESTED_INTERFACE_DIRECT_IMPL);
        JsonObject testObject = data.getJsonObject("unionOfInterfacesDirectImplementor");

        assertNotNull(testObject);

        assertFalse(testObject.isNull("message"), "message should not be null");
        assertEquals("im in many unions", testObject.getString("message"));
    }

    @Test
    public void testUnionOfInterfacesReturningNestedInterfaceImpl1() {
        JsonObject data = executeAndGetData(TEST_NESTED_INTERFACE_1);
        JsonObject testObject = data.getJsonObject("unionOfInterfacesNestedInterface1");

        assertNotNull(testObject);

        assertFalse(testObject.isNull("name"), "name should not be null");
        assertEquals("my name", testObject.getString("name"));
    }

    @Test
    public void testUnionOfInterfacesReturningNestedInterfaceImpl2() {
        JsonObject data = executeAndGetData(TEST_NESTED_INTERFACE_2);
        JsonObject testObject = data.getJsonObject("unionOfInterfacesNestedInterface2");

        assertNotNull(testObject);

        assertFalse(testObject.isNull("color"), "color should not be null");
        assertEquals("purple", testObject.getString("color"));
    }

    private static final String TEST_BASIC_UNION = "{\n" +
            "  basicUnion {\n" +
            "    ... on UnionMember {\n" +
            "      name\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final String TEST_NESTED_INTERFACE_DIRECT_IMPL = "{\n" +
            "  unionOfInterfacesDirectImplementor {\n" +
            "    ... on MemberOfManyUnions {\n" +
            "      message\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final String TEST_NESTED_INTERFACE_1 = "{\n" +
            "  unionOfInterfacesNestedInterface1 {\n" +
            "    ... on ObjectWithName {\n" +
            "      name\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final String TEST_NESTED_INTERFACE_2 = "{\n" +
            "  unionOfInterfacesNestedInterface2 {\n" +
            "    ... on ObjectWithColor {\n" +
            "      color\n" +
            "    }\n" +
            "  }\n" +
            "}";
}
