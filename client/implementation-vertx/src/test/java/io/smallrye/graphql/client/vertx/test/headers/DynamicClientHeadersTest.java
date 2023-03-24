package io.smallrye.graphql.client.vertx.test.headers;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import graphql.Assert;
import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.impl.headers.HeadersMultiMap;

public class DynamicClientHeadersTest {

    private static HttpServer httpServer;

    private static AtomicReference<HttpServerRequest> LAST_REQUEST_SENT = new AtomicReference<>();

    /**
     * Start a super dummy http server that only saves a reference to the last
     * request that it received, and responds with 200 and an empty body
     */
    @BeforeAll
    public static void startHttpServer() throws Throwable {
        httpServer = Vertx.vertx().createHttpServer()
                .requestHandler(event -> {
                    LAST_REQUEST_SENT.set(event);
                    event.response().end();
                })
                .listen(0)
                .toCompletionStage().toCompletableFuture().get();
    }

    @AfterAll
    public static void stopHttpServer() throws ExecutionException, InterruptedException {
        httpServer.close().toCompletionStage().toCompletableFuture().get();
    }

    @Test
    public void testDynamicHeader() throws ExecutionException, InterruptedException {
        VertxDynamicGraphQLClient client = (VertxDynamicGraphQLClient) new VertxDynamicGraphQLClientBuilder()
                .url("http://localhost:" + httpServer.actualPort())
                .build();
        HeadersMultiMap headers = new HeadersMultiMap();
        headers.add("Header1", "Value1");
        try {
            client.executeSync("{foo {bar}}", headers);
        } catch (InvalidResponseException e) {
            // ok
        }
        Assert.assertTrue(LAST_REQUEST_SENT.get().headers().get("Header1").equals("Value1"));
    }
}
