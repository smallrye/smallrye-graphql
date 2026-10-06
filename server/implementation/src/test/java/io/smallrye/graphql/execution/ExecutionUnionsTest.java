package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.node.ObjectNode;

/**
 * Test against unions
 *
 * @author Craig Day (cday@zendesk.com)
 */
public class ExecutionUnionsTest extends ExecutionTestBase {

    @Test
    public void testBasicUnion() {
        ObjectNode data = executeAndGetData(TEST_BASIC_UNION);
        ObjectNode testObject = (ObjectNode) data.get("basicUnion");

        assertNotNull(testObject);

        assertFalse(testObject.has("name") && testObject.get("name").isNull(), "name should not be null");
        assertEquals("my name", testObject.get("name").asText());
    }

    @Test
    public void testUnionOfInterfacesReturningDirectImplementer() {
        ObjectNode data = executeAndGetData(TEST_NESTED_INTERFACE_DIRECT_IMPL);
        ObjectNode testObject = (ObjectNode) data.get("unionOfInterfacesDirectImplementor");

        assertNotNull(testObject);

        assertFalse(testObject.has("message") && testObject.get("message").isNull(), "message should not be null");
        assertEquals("im in many unions", testObject.get("message").asText());
    }

    @Test
    public void testUnionOfInterfacesReturningNestedInterfaceImpl1() {
        ObjectNode data = executeAndGetData(TEST_NESTED_INTERFACE_1);
        ObjectNode testObject = (ObjectNode) data.get("unionOfInterfacesNestedInterface1");

        assertNotNull(testObject);

        assertFalse(testObject.has("name") && testObject.get("name").isNull(), "name should not be null");
        assertEquals("my name", testObject.get("name").asText());
    }

    @Test
    public void testUnionOfInterfacesReturningNestedInterfaceImpl2() {
        ObjectNode data = executeAndGetData(TEST_NESTED_INTERFACE_2);
        ObjectNode testObject = (ObjectNode) data.get("unionOfInterfacesNestedInterface2");

        assertNotNull(testObject);

        assertFalse(testObject.has("color") && testObject.get("color").isNull(), "color should not be null");
        assertEquals("purple", testObject.get("color").asText());
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
