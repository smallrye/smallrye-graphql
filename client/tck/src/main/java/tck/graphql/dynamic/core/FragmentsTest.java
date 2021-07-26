package tck.graphql.dynamic.core;

import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Fragment.fragment;
import static io.smallrye.graphql.client.core.FragmentReference.fragmentRef;
import static io.smallrye.graphql.client.core.Operation.operation;
import static io.smallrye.graphql.client.core.OperationType.QUERY;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.core.Document;
import tck.graphql.dynamic.helper.AssertGraphQL;
import tck.graphql.dynamic.helper.Utils;

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
}
