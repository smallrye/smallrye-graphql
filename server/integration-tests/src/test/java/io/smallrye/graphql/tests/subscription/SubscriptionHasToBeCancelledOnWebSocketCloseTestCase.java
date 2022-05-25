package io.smallrye.graphql.tests.subscription;

import java.lang.reflect.Field;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.api.Subscription;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;
import io.smallrye.mutiny.Multi;
import io.vertx.core.http.HttpClient;

/**
 * Verify that subscription gets cancelled properly on the server side when a client websocket connection
 * is closed, even though the client didn't send a COMPLETE message for the subscription.
 *
 * This means that the internally created subscriber of the server-side Multi has
 * to receive an `onCancellation` callback.
 */
@RunWith(Arquillian.class)
public class SubscriptionHasToBeCancelledOnWebSocketCloseTestCase {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "dependent-scope-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(Foo.class);
    }

    @ArquillianResource
    URL testingURL;

    @GraphQLApi
    public static class Foo {

        static AtomicBoolean CANCELLED = new AtomicBoolean(false);

        @Subscription
        public Multi<Long> counting() {
            return Multi.createFrom()
                    .ticks()
                    .every(Duration.ofSeconds(1L))
                    .onCancellation().invoke(() -> {
                        // this has to be called on the internal subscriber when a client's websocket connection ends
                        CANCELLED.set(true);
                    });
        }

        @Query
        public String dummy() {
            return null;
        }

    }

    @Test
    public void testSubscriptionCancel() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        DynamicGraphQLClient client = new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql")
                .build();
        Multi<Response> multi = client.subscription("subscription {counting}");
        multi.subscribe().with(response -> {
            System.out.println(response);
        }, error -> {
            error.printStackTrace();
        });

        // wait until some items are produced
        TimeUnit.SECONDS.sleep(3);

        System.out.println("Forcibly closing the websocket....");
        closeUnderlyingHttpClient(client);

        TimeUnit.SECONDS.sleep(1);
        // assert that the internal subscriber of the Multi on the server side was closed
        Assert.assertTrue("The server-side Multi's internal subscriber has to be cancelled " +
                "after the client websocket connection is closed",
                Foo.CANCELLED.get());
    }

    private void closeUnderlyingHttpClient(DynamicGraphQLClient client) throws NoSuchFieldException, IllegalAccessException {
        Field httpClientField = VertxDynamicGraphQLClient.class.getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        HttpClient httpClient = (HttpClient) httpClientField.get(client);
        httpClient.close();
    }
}
