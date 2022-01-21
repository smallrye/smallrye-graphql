package io.smallrye.graphql.client.vertx.test.ssl;

import java.security.KeyStore;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.smallrye.graphql.client.vertx.ssl.SSLTools;
import io.vertx.core.Vertx;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;

public class SSLTestingTools {

    static Vertx vertx = Vertx.vertx();

    public HttpServer runServer(String keystorePath, String keystorePassword,
            String truststorePath, String truststorePassword)
            throws InterruptedException, ExecutionException, TimeoutException {
        HttpServerOptions options = new HttpServerOptions();
        options.setSsl(true);
        options.setHost("localhost");

        if (keystorePath != null) {
            JksOptions keystoreOptions = new JksOptions();
            KeyStore keyStore = SSLTools.createKeyStore(keystorePath, "PKCS12", keystorePassword);
            keystoreOptions.setValue(SSLTools.asBuffer(keyStore, keystorePassword.toCharArray()));
            keystoreOptions.setPassword(keystorePassword);
            options.setKeyStoreOptions(keystoreOptions);
        }
        if (truststorePath != null) {
            options.setClientAuth(ClientAuth.REQUIRED);
            JksOptions truststoreOptions = new JksOptions();
            KeyStore trustStore = SSLTools.createKeyStore(truststorePath, "PKCS12", truststorePassword);
            truststoreOptions.setValue(SSLTools.asBuffer(trustStore, truststorePassword.toCharArray()));
            truststoreOptions.setPassword(truststorePassword);
            options.setTrustStoreOptions(truststoreOptions);
        }

        HttpServer server = vertx.createHttpServer(options);
        server.requestHandler(request -> {
            request.response().send("{\n" +
                    "  \"data\": {\n" +
                    "    \"numbers\": [32, 33],\n" +
                    "    \"strings\": [\"hello\", \"bye\"]\n" +
                    "  }\n" +
                    "}");
        });

        return server.listen(0).toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    }

}
