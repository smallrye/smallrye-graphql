package io.smallrye.graphql.client.vertx.test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jakarta.json.JsonObject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;

/**
 * Make sure than when executing a sync operation with a dynamic client and
 * it throws an unchecked exception, the resulting exception will contain a
 * reference to the application class that invoked the call.
 */
public class DynamicClientExceptionTest {

    static Vertx vertx = Vertx.vertx();

    @Test
    public void test() throws ExecutionException, InterruptedException, TimeoutException {
        HttpServer server = runMockServer();
        try {
            DynamicGraphQLClient client = new VertxDynamicGraphQLClientBuilder()
                    .url("http://localhost:" + server.actualPort())
                    .build();
            try {
                JsonObject data = client.executeSync("{something-whatever}").getData();
                Assertions.fail("Expected an exception");
            } catch (Exception e) {
                Assertions.assertTrue(e instanceof InvalidResponseException);
                StackTraceElement element = findStackTraceElementThatContainsClass("DynamicClientExceptionTest",
                        e.getStackTrace());
                Assertions.assertNotNull(element, () -> {
                    e.printStackTrace();
                    return "Expected the stack trace to contain a reference to the test class";
                });
            }
        } finally {
            server.close();
        }
    }

    private StackTraceElement findStackTraceElementThatContainsClass(String string, StackTraceElement[] stackTrace) {
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (stackTraceElement.getClassName().contains(string)) {
                return stackTraceElement;
            }
        }
        return null;
    }

    private HttpServer runMockServer() throws ExecutionException, InterruptedException, TimeoutException {
        HttpServerOptions options = new HttpServerOptions();
        options.setHost("localhost");
        HttpServer server = vertx.createHttpServer(options);
        server.requestHandler(new Handler<io.vertx.core.http.HttpServerRequest>() {
            @Override
            public void handle(io.vertx.core.http.HttpServerRequest event) {
                event.response().end("BAD RESPONSE");
            }
        });
        return server.listen(0).toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    }

    @AfterAll
    public static void cleanup() {
        vertx.close();
    }
}
