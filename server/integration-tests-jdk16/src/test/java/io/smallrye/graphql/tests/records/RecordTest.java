package io.smallrye.graphql.tests.records;

import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.json.bind.annotation.JsonbCreator;
import java.net.URL;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.InputObject.inputObject;
import static io.smallrye.graphql.client.core.InputObjectField.prop;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.Assert.assertEquals;

/**
 * This test verifies that the server side can handle Java records in GraphQL apis, both as input and as output types.
 */
@RunWith(Arquillian.class)
@RunAsClient
public class RecordTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
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
            assertEquals("a", response.getData().getJsonObject("simple").getString("a"));
            assertEquals("b", response.getData().getJsonObject("simple").getString("b"));
        }
    }

    @Test
    public void testSimpleRecordWithFactory() throws Exception {
        try (DynamicGraphQLClient client = new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql").build()) {
            Document query = document(operation(
                    field("simpleWithFactory",
                            args(arg("input",
                                    inputObject(prop("a", "a"), prop("b", "b")))),
                            field("a"),
                            field("b"))));
            Response response = client.executeSync(query);
            System.out.println(response);
            System.out.println("query.build() = " + query.build());
            assertEquals("a", response.getData().getJsonObject("simpleWithFactory").getString("a"));
            assertEquals("b", response.getData().getJsonObject("simpleWithFactory").getString("b"));
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

    }

    public record SimpleRecord(String a, String b) {

        // FIXME: until Yasson receives proper support for records, we have
        // to use this hack to tell Yasson to use the canonical constructor when
        // deserializing from JSON. Otherwise it will try to find and use a no-arg constructor, and
        // that does not exist.
        @JsonbCreator
        public SimpleRecord {
        }

    }

    public record SimpleRecordWithFactory(String a, String b) {

        @JsonbCreator
        public static SimpleRecordWithFactory build(String a, String b) {
            return new SimpleRecordWithFactory(a, b);
        }

    }

}
