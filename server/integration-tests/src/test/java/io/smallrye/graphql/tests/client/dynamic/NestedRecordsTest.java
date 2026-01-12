package io.smallrye.graphql.tests.client.dynamic;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.InputObject.inputObject;
import static io.smallrye.graphql.client.core.InputObjectField.prop;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.net.URL;
import java.util.List;

import jakarta.json.JsonArray;
import jakarta.json.JsonValue;
import jakarta.json.bind.annotation.JsonbCreator;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.core.InputObject;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;

@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class NestedRecordsTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(ParentRecord.class, ParentRecordWithList.class, ParentRecordWithArray.class, TestRecord.class);
    }

    @ArquillianResource
    URL testingURL;

    @Test
    public void testNestedRecordWithMissingFieldInQuery() throws Exception {
        try (DynamicGraphQLClient client = new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql").build()) {
            Document query = document(operation(
                    field("testParent",
                            args(arg("parent",
                                    inputObject(
                                            prop("testRecord",
                                                    inputObject(prop("needed", "bla")))))),
                            field("testRecord",
                                    field("needed"),
                                    field("notNeeded")))));
            Response response = client.executeSync(query);
            assertEquals("bla", response.getData().getJsonObject("testParent").getJsonObject("testRecord").getString("needed"));
            assertEquals(JsonValue.NULL,
                    response.getData().getJsonObject("testParent").getJsonObject("testRecord").get("notNeeded"));
            assertNull(response.getData().getJsonObject("testParent").get("s"));
        }
    }

    @Test
    public void testNestedRecordWithListWithMissingFieldInQuery() throws Exception {
        int major = Integer.parseInt(System.getProperty("java.version").split("\\.")[0]);
        assumeFalse(
                major == 21 || major == 22,
                "Skipping testNestedRecordWithListWithMissingFieldInQuery because of bug in JDK 21 and 22, see https://bugs.openjdk.org/browse/JDK-8320575");
        try (DynamicGraphQLClient client = new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql").build()) {
            Document query = document(operation(
                    field("testParentWithList",
                            args(arg("parent",
                                    inputObject(
                                            prop("testRecords",
                                                    new InputObject[] {
                                                            inputObject(prop("needed", "bla")),
                                                            inputObject(prop("needed", "bla2"))
                                                    })))),
                            field("testRecords",
                                    field("needed"),
                                    field("notNeeded")))));
            Response response = client.executeSync(query);
            JsonArray echoedRecords = response.getData().getJsonObject("testParentWithList").getJsonArray("testRecords");

            assertEquals("bla", echoedRecords.get(0).asJsonObject().getString("needed"));
            assertEquals(JsonValue.NULL, echoedRecords.get(0).asJsonObject().get("notNeeded"));
            assertEquals("bla2", echoedRecords.get(1).asJsonObject().getString("needed"));
            assertEquals(JsonValue.NULL, echoedRecords.get(1).asJsonObject().get("notNeeded"));
        }
    }

    @Test
    public void testNestedRecordWithArrayWithMissingFieldInQuery() throws Exception {
        try (DynamicGraphQLClient client = new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql").build()) {
            Document query = document(operation(
                    field("testParentWithArray",
                            args(arg("parent",
                                    inputObject(
                                            prop("testRecords",
                                                    new InputObject[] {
                                                            inputObject(prop("needed", "bla")),
                                                            inputObject(prop("needed", "bla2"))
                                                    })))),
                            field("testRecords",
                                    field("needed"),
                                    field("notNeeded")))));
            Response response = client.executeSync(query);
            JsonArray echoedRecords = response.getData().getJsonObject("testParentWithArray").getJsonArray("testRecords");

            assertEquals("bla", echoedRecords.get(0).asJsonObject().getString("needed"));
            assertEquals(JsonValue.NULL, echoedRecords.get(0).asJsonObject().get("notNeeded"));
            assertEquals("bla2", echoedRecords.get(1).asJsonObject().getString("needed"));
            assertEquals(JsonValue.NULL, echoedRecords.get(1).asJsonObject().get("notNeeded"));
        }
    }

    @Test
    public void testSimpleRecordWithMissingFieldInQuery() throws Exception {
        try (DynamicGraphQLClient client = new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql").build()) {
            Document query = document(operation(
                    field("echo",
                            args(arg("testRecord",
                                    inputObject(
                                            prop("needed", "bla")))),
                            field("needed"),
                            field("notNeeded"))));
            Response response = client.executeSync(query);
            assertEquals("bla", response.getData().getJsonObject("echo").getString("needed"));
            assertEquals(JsonValue.NULL, response.getData().getJsonObject("echo").get("notNeeded"));
        }
    }

    @GraphQLApi
    public static class Api {

        @Query
        public ParentRecord testParent(ParentRecord parent) {
            return parent;
        }

        @Query
        public ParentRecordWithList testParentWithList(ParentRecordWithList parent) {
            return parent;
        }

        @Query
        public ParentRecordWithArray testParentWithArray(ParentRecordWithArray parent) {
            return parent;
        }

        @Query
        public TestRecord echo(TestRecord testRecord) {
            return testRecord;
        }

    }

    public record ParentRecord(String s,
            TestRecord testRecord) {
        @JsonbCreator
        public ParentRecord {
        }
    }

    public record ParentRecordWithList(String s,
            List<TestRecord> testRecords) {
        @JsonbCreator
        public ParentRecordWithList {
        }
    }

    public record ParentRecordWithArray(String s,
            TestRecord[] testRecords) {
        @JsonbCreator
        public ParentRecordWithArray {
        }
    }

    public record TestRecord(String needed,
            String notNeeded) {
        @JsonbCreator
        public TestRecord {
        }
    }

}
