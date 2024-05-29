package tck.graphql.dynamic.core;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Operation.operation;
import static io.smallrye.graphql.client.core.OperationType.QUERY;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.core.Document;
import tck.graphql.dynamic.helper.AssertGraphQL;
import tck.graphql.dynamic.helper.Utils;

public class FieldsTest {

    @Test
    public void fieldsTest() throws IOException, URISyntaxException {
        String expectedRequest = Utils.getResourceFileContent("core/fields.graphql");

        Document document = document(
                operation(QUERY,
                        field("noArgNoSubField"),
                        field("noArgWithSubField",
                                field("bool"),
                                field("string"),
                                field("double")),
                        field("withArgNoSubField", arg("anInt", 42)),
                        field("withArgWithSubField", args(
                                arg("aString", "world"),
                                arg("aDouble", 78.12d),
                                arg("aBool", false)),
                                field("bool"),
                                field("string"),
                                field("double"))));

        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    @Test
    public void fieldShouldNotThrowExceptionForValidNameTest() {
        assertDoesNotThrow(() -> field("valid_name"));
        assertDoesNotThrow(() -> field("field:name"));
        assertDoesNotThrow(() -> field("_name"));
        assertDoesNotThrow(() -> field("name_"));
        assertDoesNotThrow(() -> field("name_with_underscores"));
        assertDoesNotThrow(() -> field("name_with_underscores:name"));
        assertDoesNotThrow(() -> field("o:_"));
        assertDoesNotThrow(() -> field("o:o1"));
        assertDoesNotThrow(() -> field("o1:a"));
        assertDoesNotThrow(() -> field("_:v1"));
        assertDoesNotThrow(() -> field("___"));
        assertDoesNotThrow(() -> field("o"));
        assertDoesNotThrow(() -> field(" valid_name "));
        assertDoesNotThrow(() -> field("o1: b"));
        assertDoesNotThrow(() -> field("o12 ,,, : ,,, bbbee1"));
        assertDoesNotThrow(() -> field(",,,,valid_name,,,,"));
        assertDoesNotThrow(() -> field("foo:\tbar"));
    }

    @Test
    public void fieldShouldThrowExceptionForInvalidNameTest() {
        assertThrows(IllegalArgumentException.class, () -> field(null));
        assertThrows(IllegalArgumentException.class, () -> field(""));
        assertThrows(IllegalArgumentException.class, () -> field("invalid_nam&e"));
        assertThrows(IllegalArgumentException.class, () -> field("1invalid_name"));
        assertThrows(IllegalArgumentException.class, () -> field(":invalid_name"));
        assertThrows(IllegalArgumentException.class, () -> field("invalid name"));
        assertThrows(IllegalArgumentException.class, () -> field("invalid_name:"));
        assertThrows(IllegalArgumentException.class, () -> field("0:invalid"));
        assertThrows(IllegalArgumentException.class, () -> field("a:1"));
        assertThrows(IllegalArgumentException.class, () -> field("invalid::name"));
        assertThrows(IllegalArgumentException.class, () -> field("field:name:name2"));
        assertThrows(IllegalArgumentException.class, () -> field(" invalid,name"));
        assertThrows(IllegalArgumentException.class, () -> field("invalid\tname"));
    }
}
