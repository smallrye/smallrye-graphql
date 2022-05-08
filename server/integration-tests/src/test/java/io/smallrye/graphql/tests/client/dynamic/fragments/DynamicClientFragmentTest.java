package io.smallrye.graphql.tests.client.dynamic.fragments;

import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Fragment.fragment;
import static io.smallrye.graphql.client.core.FragmentReference.fragmentRef;
import static io.smallrye.graphql.client.core.InlineFragment.on;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URL;
import java.util.concurrent.ExecutionException;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;

@RunWith(Arquillian.class)
@RunAsClient
public class DynamicClientFragmentTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "fragment-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(VehicleApi.class, Bicycle.class, Car.class, Vehicle.class);
    }

    @ArquillianResource
    URL testingURL;

    private static VertxDynamicGraphQLClient client;

    @Before
    public void prepare() {
        client = (VertxDynamicGraphQLClient) new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql")
                .build();
    }

    @After
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
            Assert.fail("Error was returned: " + response.getErrors());
        }

        JsonArray vehicles = response.getData().getJsonArray("vehicles");

        JsonObject car = vehicles.getJsonObject(0);
        assertEquals(4, car.getInt("wheelsCount"));
        assertEquals(8, car.getInt("engineCylinders"));
        assertNull(car.get("frameSize"));

        JsonObject bicycle = vehicles.getJsonObject(1);
        assertEquals(2, bicycle.getInt("wheelsCount"));
        assertEquals(15, bicycle.getInt("frameSize"));
        assertNull(bicycle.get("engineCylinders"));
    }

}
