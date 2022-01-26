package io.smallrye.graphql.client.vertx.test;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;
import io.smallrye.graphql.client.websocket.WebsocketSubprotocol;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.ServerWebSocket;

public class SubscriptionInitializationTimeoutTest {

    static Vertx vertx = Vertx.vertx();

    @Test
    public void test() throws ExecutionException, InterruptedException, TimeoutException {
        HttpServer server = runMockServer();
        try {
            DynamicGraphQLClient client = new VertxDynamicGraphQLClientBuilder()
                    .subprotocols(WebsocketSubprotocol.GRAPHQL_WS)
                    .subscriptionInitializationTimeout(100)
                    .url("http://localhost:" + server.actualPort())
                    .build();
            Uni<Throwable> testFinish = Uni.createFrom().emitter(emitter -> {
                Multi<Response> subscriptionMulti = client.subscription("{}");
                subscriptionMulti.subscribe().with(item -> {
                    emitter.complete(null);
                }, failure -> {
                    emitter.complete(failure);
                });
            });
            Throwable testResult = testFinish.await().atMost(Duration.ofSeconds(3));
            Assertions.assertNotNull(testResult);
            Assertions.assertTrue(testResult.getMessage().contains("Server did not send a connection_ack message"));
        } finally {
            server.close();
        }
    }

    // mock server with websocket support which does not send any messages to the client,
    // so this should cause a timeout while waiting for the subscription to be acknowledged
    private HttpServer runMockServer() throws ExecutionException, InterruptedException, TimeoutException {
        HttpServerOptions options = new HttpServerOptions();
        options.setHost("localhost");
        options.addWebSocketSubProtocol("graphql-ws");
        HttpServer server = vertx.createHttpServer(options);
        server.webSocketHandler(new Handler<ServerWebSocket>() {
            @Override
            public void handle(ServerWebSocket event) {
                System.out.println("INITIALIZED");
            }
        });
        return server.listen(0).toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    }
}
