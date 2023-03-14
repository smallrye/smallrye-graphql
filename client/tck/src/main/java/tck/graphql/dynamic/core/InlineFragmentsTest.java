package tck.graphql.dynamic.core;

import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.InlineFragment.on;
import static io.smallrye.graphql.client.core.Operation.operation;
import static io.smallrye.graphql.client.core.OperationType.QUERY;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.core.Document;
import tck.graphql.dynamic.helper.AssertGraphQL;
import tck.graphql.dynamic.helper.Utils;

public class InlineFragmentsTest {

    @Test
    public void inlineFragmentTest() {
        String expectedRequest = Utils.getResourceFileContent("core/inlinefragments.graphql");

        Document document = document(
                operation(QUERY,
                        field("regularField"),
                        on("Type1",
                                field("type1Field1"),
                                field("type1Field2")),
                        on("Type2",
                                field("type2Field"))));

        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    @Test
    public void inlineFragmentWithoutTypeConditionTest() {
        String expectedRequest = Utils.getResourceFileContent("core/" +
                "inlinefragmentsNoTypeCondition.graphql");

        Document document = document(
                operation(QUERY, "inlineFragmentNoType",
                        field("user",
                                field("id"),
                                field("name"),
                                on(
                                        field("firstName"),
                                        field("lastName"),
                                        field("birthday")))));

        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    @Test
    public void inlineFragmentsShouldNotThrowExceptionForInvalidNameTest() {
        assertDoesNotThrow(() -> on("name"));
        assertDoesNotThrow(() -> on("name_one"));
        assertDoesNotThrow(() -> on("__typename"));
        assertDoesNotThrow(() -> on("super_long_name"));
        assertDoesNotThrow(() -> on("on"));
        assertDoesNotThrow(() -> on("one"));
        assertDoesNotThrow(() -> on(""));
        assertDoesNotThrow(() -> {
            var inlineFragment = on((String) null);
            assertEquals(inlineFragment.getType(), "");
        });

    }

    @Test
    public void inlineFragmentsShouldThrowExceptionForInvalidNameTest() {
        assertThrows(IllegalArgumentException.class, () -> on("   "));
        assertThrows(IllegalArgumentException.class, () -> on(":name"));
        assertThrows(IllegalArgumentException.class, () -> on("na me"));
        assertThrows(IllegalArgumentException.class, () -> on("name:"));
        assertThrows(IllegalArgumentException.class, () -> on("na:me"));
        assertThrows(IllegalArgumentException.class, () -> on("...MyFragment"));
        assertThrows(IllegalArgumentException.class, () -> on("name:one:two"));
    }
}
