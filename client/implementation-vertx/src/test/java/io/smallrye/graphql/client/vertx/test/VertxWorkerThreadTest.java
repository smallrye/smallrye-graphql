package io.smallrye.graphql.client.vertx.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.microprofile.graphql.Query;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.vertx.typesafe.VertxTypesafeGraphQLClientBuilder;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.VertxThread;

/**
 * Verifies that synchronous typesafe client calls work when invoked from a
 * Vert.x worker thread (e.g. Quarkus {@code @Blocking} endpoints).
 *
 * Vert.x {@code Future.await()} throws {@link IllegalStateException} on
 * worker threads, so the client must not use it for blocking.
 */
public class VertxWorkerThreadTest {

    static Vertx vertx = Vertx.vertx();

    @GraphQLClientApi
    interface GreetingApi {
        @Query
        String greeting();
    }

    @Test
    void typesafeClientSyncCallOnWorkerThread() throws Exception {
        HttpServer server = startMockGraphQLServer();
        try {
            GreetingApi client = new VertxTypesafeGraphQLClientBuilder()
                    .endpoint("http://localhost:" + server.actualPort())
                    .build(GreetingApi.class);

            AtomicReference<Object> resultRef = new AtomicReference<>();
            AtomicReference<Throwable> errorRef = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            // Simulate a Quarkus @Blocking worker thread
            VertxThread workerThread = new VertxThread(() -> {
                try {
                    Object result = client.greeting();
                    resultRef.set(result);
                } catch (Throwable t) {
                    errorRef.set(t);
                } finally {
                    latch.countDown();
                }
            }, "test-worker-thread", true, 60, TimeUnit.SECONDS);

            workerThread.start();
            if (!latch.await(30, TimeUnit.SECONDS)) {
                fail("Timed out waiting for worker thread");
            }

            if (errorRef.get() != null) {
                fail("Sync call from Vert.x worker thread should succeed but threw: "
                        + errorRef.get().getClass().getName() + ": " + errorRef.get().getMessage(),
                        errorRef.get());
            }
            assertEquals("hello", resultRef.get());
        } finally {
            server.close();
        }
    }

    private HttpServer startMockGraphQLServer() throws Exception {
        HttpServerOptions options = new HttpServerOptions().setHost("localhost");
        HttpServer server = vertx.createHttpServer(options);
        server.requestHandler(request -> request.body().onSuccess(body -> {
            request.response()
                    .putHeader("Content-Type", "application/json")
                    .end("{\"data\":{\"greeting\":\"hello\"}}");
        }));
        return server.listen(0).await(10, TimeUnit.SECONDS);
    }

    @AfterAll
    static void cleanup() {
        vertx.close();
    }
}
