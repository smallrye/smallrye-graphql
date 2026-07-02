package io.smallrye.graphql.tests.client.dynamic.fragments;

import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Fragment.fragment;
import static io.smallrye.graphql.client.core.FragmentReference.fragmentRef;
import static io.smallrye.graphql.client.core.InlineFragment.on;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.URL;
import java.util.concurrent.ExecutionException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class DynamicClientFragmentTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "fragment-test.war")
                .addClasses(VehicleApi.class, Bicycle.class, Car.class, Vehicle.class);
    }

    @ArquillianResource
    URL testingURL;

    private static VertxDynamicGraphQLClient client;

    @BeforeEach
    public void prepare() {
        client = (VertxDynamicGraphQLClient) new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql")
                .build();
    }

    @AfterEach
    public void cleanup() {
        client.close();
    }

    @Test
    public void testInlineFragment() throws ExecutionException, InterruptedException {
        Document document = document(
                operation(
                        field("vehicles",
                                field("wheelsCount"),
                                on("Car", field("engineCylinders")),
                                on("Bicycle", field("frameSize")))));
        validateResponse(client.executeSync(document));
    }

    @Test
    public void testNamedFragment() throws ExecutionException, InterruptedException {
        Document document = document(
                operation(
                        field("vehicles",
                                field("wheelsCount"),
                                fragmentRef("bicycleFields"),
                                fragmentRef("carFields"))),
                fragment("bicycleFields").on("Bicycle",
                        field("frameSize")),
                fragment("carFields").on("Car",
                        field("engineCylinders")));
        validateResponse(client.executeSync(document));
    }

    private void validateResponse(Response response) {
        if (response.hasError()) {
            Assertions.fail("Error was returned: " + response.getErrors());
        }

        ArrayNode vehicles = (ArrayNode) response.getData().get("vehicles");

        ObjectNode car = (ObjectNode) vehicles.get(0);
        assertEquals(4, car.get("wheelsCount").asInt());
        assertEquals(8, car.get("engineCylinders").asInt());
        assertNull(car.get("frameSize"));

        ObjectNode bicycle = (ObjectNode) vehicles.get(1);
        assertEquals(2, bicycle.get("wheelsCount").asInt());
        assertEquals(15, bicycle.get("frameSize").asInt());
        assertNull(bicycle.get("engineCylinders"));
    }

}
