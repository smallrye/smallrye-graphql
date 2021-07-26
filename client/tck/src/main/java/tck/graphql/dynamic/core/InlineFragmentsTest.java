package tck.graphql.dynamic.core;

import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.InlineFragment.on;
import static io.smallrye.graphql.client.core.Operation.operation;
import static io.smallrye.graphql.client.core.OperationType.QUERY;

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
}
