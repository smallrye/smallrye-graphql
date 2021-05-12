package io.smallrye.graphql.tests.client.dynamic.subscription;

import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.Assert.*;

import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.json.JsonValue;

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

import io.smallrye.graphql.client.Error;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.dynamic.vertx.VertxDynamicGraphQLClient;
import io.smallrye.graphql.client.dynamic.vertx.VertxDynamicGraphQLClientBuilder;

@RunWith(Arquillian.class)
@RunAsClient
public class DynamicClientSubscriptionTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "dynamic-client-subscription-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(DynamicClientSubscriptionApi.class);
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
    public void testCounting() {
        Document document = document(
                operation(OperationType.SUBSCRIPTION,
                        field("countToFive")));
        List<Response> responses = client.subscription(document)
                .subscribe()
                .asStream()
                .collect(Collectors.toList());
        for (int i = 0; i < 5; i++) {
            Response response = responses.get(i);
            assertEquals(i, response.getData().getInt("countToFive"));
            assertNoErrors(response.getErrors());
        }
    }

    @Test
    public void testFailingImmediately() throws InterruptedException {
        Document document = document(
                operation(OperationType.SUBSCRIPTION,
                        field("failingImmediately")));
        AtomicReference<Response> response = new AtomicReference<>();
        CountDownLatch finished = new CountDownLatch(1);
        client.subscription(document)
                .subscribe()
                .with(item -> {
                    response.set(item);
                }, throwable -> {
                    // nothing
                }, () -> {
                    finished.countDown();
                });
        finished.await(10, TimeUnit.SECONDS);
        Response actualResponse = response.get();
        assertNotNull("One response was expected to arrive", actualResponse);
        Assert.assertEquals(JsonValue.NULL, actualResponse.getData().get("failingImmediately"));
        // FIXME: add an assertion about the contained error message
        // right now, there is no error message present, which is a bug
    }

    private void assertNoErrors(List<Error> errors) {
        if (errors != null && !errors.isEmpty()) {
            fail("No error expected, but there was: " + errors);
        }
    }

}
