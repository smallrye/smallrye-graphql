package tck.graphql.dynamic.core;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Enum.gqlEnum;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Operation.operation;
import static io.smallrye.graphql.client.core.OperationType.QUERY;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.core.exceptions.BuildException;
import tck.graphql.dynamic.helper.AssertGraphQL;
import tck.graphql.dynamic.helper.Utils;

public class EnumsTest {

    @Test
    public void enumsTest() {
        String expectedRequest = Utils.getResourceFileContent("core/enums.graphql");

        Document document = document(
                operation(QUERY,
                        field("exams",
                                args(arg("result", gqlEnum("PASSED"))),
                                field("score"))));

        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    @Test
    public void invalidValue() {
        try {
            Document document = document(
                    operation(QUERY,
                            field("exams",
                                    args(arg("result", gqlEnum("wrong { }"))),
                                    field("score"))));
            fail("Invalid enum value should not be accepted");
        } catch (BuildException be) {
            // OK
        }
    }

    @Test
    public void invalidValueTrue() {
        try {
            Document document = document(
                    operation(QUERY,
                            field("exams",
                                    args(arg("result", gqlEnum("true"))),
                                    field("score"))));
            fail("Invalid enum value should not be accepted");
        } catch (BuildException be) {
            // OK
        }
    }
}
