package io.smallrye.graphql.tests.client.dynamic;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.InputObject.inputObject;
import static io.smallrye.graphql.client.core.InputObjectField.prop;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;
import java.util.Set;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;

/**
 * This test verifies that the server side can handle Java records in GraphQL apis, both as input and as output types.
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class RecordTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(SimpleRecord.class);
    }

    @ArquillianResource
    URL testingURL;

    @Test
    public void testSimpleRecord() throws Exception {
        try (DynamicGraphQLClient client = new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql").build()) {
            Document query = document(operation(
                    field("simple",
                            args(arg("input",
                                    inputObject(prop("a", "a"), prop("b", "b")))),
                            field("a"),
                            field("b"))));
            Response response = client.executeSync(query);
            ObjectNode simple = (ObjectNode) response.getData().get("simple");
            assertEquals("a", simple.get("a").asText());
            assertEquals("b", simple.get("b").asText());
        }
    }

    @Test
    public void testSimpleRecordWithFactory() throws Exception {
        try (DynamicGraphQLClient client = new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql").build()) {
            Document query = document(operation(
                    field("simpleWithFactory",
                            args(arg("input",
                                    inputObject(prop("a", "a"),
                                            prop("b", "b"),
                                            prop("c", new String[] { "c", "cc" }),
                                            prop("d", new String[] { "d", "dd" })))),
                            field("a"),
                            field("b"),
                            field("c"),
                            field("d"))));
            Response response = client.executeSync(query);
            System.out.println(response);
            System.out.println("query.build() = " + query.build());
            ObjectNode simpleWithFactory = (ObjectNode) response.getData().get("simpleWithFactory");
            assertEquals("a", simpleWithFactory.get("a").asText());
            assertEquals("b", simpleWithFactory.get("b").asText());
            ArrayNode c = (ArrayNode) simpleWithFactory.get("c");
            assertEquals("c", c.get(0).asText());
            assertEquals("cc", c.get(1).asText());
            ArrayNode d = (ArrayNode) simpleWithFactory.get("d");
            assertEquals("dd", d.get(0).asText());
            assertEquals("d", d.get(1).asText());
        }
    }

    @Test
    public void testSimpleRecordWithParameterizedConstructor() throws Exception {
        try (DynamicGraphQLClient client = new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql").build()) {
            Document query = document(operation(
                    field("simpleWithParameterizedConstructor",
                            args(arg("input",
                                    inputObject(prop("b", "b")))),
                            field("a"),
                            field("b"))));
            Response response = client.executeSync(query);
            System.out.println(response);
            System.out.println("query.build() = " + query.build());
            ObjectNode result = (ObjectNode) response.getData().get("simpleWithParameterizedConstructor");
            assertEquals(1, result.get("a").asInt());
            assertEquals("b", result.get("b").asText());
        }
    }

    @GraphQLApi
    public static class Api {

        @Query
        public SimpleRecord simple(SimpleRecord input) {
            return input;
        }

        @Query
        public SimpleRecordWithFactory simpleWithFactory(SimpleRecordWithFactory input) {
            return input;
        }

        @Query
        public SimpleRecordWithParameterizedConstructor simpleWithParameterizedConstructor(
                SimpleRecordWithParameterizedConstructor input) {
            return input;
        }

    }

    public record SimpleRecord(String a, String b) {

    }

    public record SimpleRecordWithFactory(String a, String b, String[] c, Set<String> d) {

        public static SimpleRecordWithFactory build(String a, String b, String[] c, Set<String> d) {
            return new SimpleRecordWithFactory(a, b, c, d);
        }

    }

    public record SimpleRecordWithParameterizedConstructor(@NonNull Integer a, @NonNull String b) {

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public SimpleRecordWithParameterizedConstructor(@JsonProperty("b") String b) {
            this(1, b);
        }

    }

}
