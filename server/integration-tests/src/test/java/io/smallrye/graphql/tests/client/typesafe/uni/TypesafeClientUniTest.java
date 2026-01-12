package io.smallrye.graphql.tests.client.typesafe.uni;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;
import java.time.Duration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.smallrye.graphql.client.vertx.typesafe.VertxTypesafeGraphQLClientBuilder;

/**
 * Test for usage of Uni in typesafe clients.
 * Generally, to the client, it should not matter at all whether the server-side query returns Uni or a synchronous type.
 * In either case, the client-side counterpart of the method can return a synchronous or an asynchronous type.
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class TypesafeClientUniTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(UniApi.class);
    }

    @ArquillianResource
    URL testingURL;

    private UniClientApi client;

    @BeforeEach
    public void prepare() {
        client = new VertxTypesafeGraphQLClientBuilder()
                .endpoint(testingURL.toString() + "graphql")
                .build(UniClientApi.class);
    }

    /**
     * On server side, the query returns a Uni.
     * On the client side, the query is declared to return a Uni.
     */
    @Test
    public void callAsyncQueryWithAsyncClient() {
        String response = client.asyncQuery().await().atMost(Duration.ofSeconds(10));
        assertEquals("async", response);
    }

    /**
     * On server side, the query returns a Uni.
     * On the client side, the query returns the regular (synchronous) type.
     */
    @Test
    public void callAsyncQueryWithSyncClient() {
        String response = client.getSync();
        assertEquals("async", response);
    }

    /**
     * On server side, the query returns a synchronous type.
     * On the client side, the query returns a Uni.
     */
    @Test
    public void callSyncQueryWithAsyncClient() {
        String response = client.asyncMethodForSyncQuery().await().atMost(Duration.ofSeconds(10));
        assertEquals("sync", response);
    }

}
