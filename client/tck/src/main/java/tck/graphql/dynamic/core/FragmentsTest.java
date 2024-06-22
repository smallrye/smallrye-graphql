package tck.graphql.dynamic.core;

import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Fragment.fragment;
import static io.smallrye.graphql.client.core.FragmentReference.fragmentRef;
import static io.smallrye.graphql.client.core.Operation.operation;
import static io.smallrye.graphql.client.core.OperationType.QUERY;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.core.Document;
import tck.graphql.dynamic.helper.AssertGraphQL;
import tck.graphql.dynamic.helper.Utils;

/**
 * This class tests creating fragments (via DSL).
 */
public class FragmentsTest {

    @Test
    public void fragmentTest() {
        String expectedRequest = Utils.getResourceFileContent("core/fragments.graphql");

        Document document = document(
                operation(QUERY,
                        field("people",
                                fragmentRef("sensitiveFields"))),
                fragment("sensitiveFields").on("Person",
                        field("age"),
                        field("religion")));

        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    @Test
    public void fragmentsShouldNotThrowExceptionForValidNameTest() {
        assertDoesNotThrow(() -> fragment("MyFragment"));
        assertDoesNotThrow(() -> fragment("_myFragment"));
        assertDoesNotThrow(() -> fragment("my_fragment"));
        assertDoesNotThrow(() -> fragment("my123Fragment"));
        assertDoesNotThrow(() -> fragment("f"));
        assertDoesNotThrow(() -> fragment("_"));
        assertDoesNotThrow(() -> fragment("frag_ment"));
        assertDoesNotThrow(() -> fragment("one"));
        assertDoesNotThrow(() -> fragment(",,,two "));
    }

    @Test
    public void fragmentsShouldThrowExceptionForInvalidNameTest() {
        assertThrows(IllegalArgumentException.class, () -> fragment(""));
        assertThrows(IllegalArgumentException.class, () -> fragment(null));
        assertThrows(IllegalArgumentException.class, () -> fragment(" "));
        assertThrows(IllegalArgumentException.class, () -> fragment("in valid"));
        assertThrows(IllegalArgumentException.class, () -> fragment("invalid!"));
        assertThrows(IllegalArgumentException.class, () -> fragment(":invalid"));
        assertThrows(IllegalArgumentException.class, () -> fragment("in:valid"));
        assertThrows(IllegalArgumentException.class, () -> fragment("...fragmentinvalid"));
        assertThrows(IllegalArgumentException.class, () -> fragment("inv@lid"));
        assertThrows(IllegalArgumentException.class, () -> fragment("on"));
        assertThrows(IllegalArgumentException.class, () -> fragment("frag,ment"));
    }
}
