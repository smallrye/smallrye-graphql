package io.smallrye.graphql.client.vertx.test.ssl;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClientBuilder;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.client.WebClientOptions;

public class DynamicClientSSLTest {

    public static final Duration TIMEOUT = Duration.ofSeconds(20);
    private SSLTestingTools tools = new SSLTestingTools();

    /**
     * Client requests server authentication.
     */
    @Test
    public void serverAuthentication_correctTruststore() throws Exception {
        HttpServer server = tools.runServer("classpath:ssl/server.pkcs12.keystore", "serverkeystorepassword", null, null);
        try {
            System.setProperty("myclient1/mp-graphql/truststore", "classpath:ssl/client.pkcs12.truststore");
            System.setProperty("myclient1/mp-graphql/truststorePassword", "clienttruststorepassword");
            System.setProperty("myclient1/mp-graphql/truststoreType", "PKCS12");
            try (DynamicGraphQLClient client = DynamicGraphQLClientBuilder.newBuilder()
                    .configKey("myclient1")
                    .url("https://127.0.0.1:" + server.actualPort())
                    .build()) {
                client.executeAsync("asd").await().atMost(TIMEOUT);
            }
        } finally {
            server.close();
        }
    }

    /**
     * Client requests server authentication, and server's certificate is untrusted.
     */
    @Test
    public void serverAuthentication_badKeystoreOnServer() throws Exception {
        HttpServer server = tools.runServer("classpath:ssl/server.pkcs12.wrong.keystore", "serverwrongkeystorepassword", null,
                null);
        try {
            System.setProperty("myclient2/mp-graphql/truststore", "classpath:ssl/client.pkcs12.truststore");
            System.setProperty("myclient2/mp-graphql/truststorePassword", "clienttruststorepassword");
            System.setProperty("myclient2/mp-graphql/truststoreType", "PKCS12");
            try (DynamicGraphQLClient client = DynamicGraphQLClientBuilder.newBuilder()
                    .configKey("myclient2")
                    .url("https://127.0.0.1:" + server.actualPort())
                    .build()) {
                client.executeAsync("asd").await().atMost(TIMEOUT);
                Assertions.fail("Connection to server should fail");
            } catch (Exception e) {
                // verify that the client rejected the server's certificate
                assertHasCauseContainingMessage(e, "unable to find valid certification path to requested target");
            }
        } finally {
            server.close();
        }
    }

    /**
     * Server requests client authentication.
     */
    @Test
    public void clientAuthentication_correctKeystore() throws Exception {
        HttpServer server = tools.runServer("classpath:ssl/server.pkcs12.keystore", "serverkeystorepassword",
                "classpath:ssl/server.pkcs12.truststore", "servertruststorepassword");
        try {
            System.setProperty("myclient3/mp-graphql/keystore", "classpath:ssl/client.pkcs12.keystore");
            System.setProperty("myclient3/mp-graphql/keystorePassword", "clientkeystorepassword");
            System.setProperty("myclient3/mp-graphql/keystoreType", "PKCS12");
            WebClientOptions options = new WebClientOptions();
            options.setTrustAll(true); // don't require server auth
            try (DynamicGraphQLClient client = new VertxDynamicGraphQLClientBuilder()
                    .configKey("myclient3")
                    .options(options)
                    .url("https://127.0.0.1:" + server.actualPort())
                    .build()) {
                client.executeAsync("asd").await().atMost(TIMEOUT);
            }
        } finally {
            server.close();
        }
    }

    /**
     * Server requests client authentication, and the client's certificate is untrusted.
     */
    @Test
    public void clientAuthentication_badKeystore() throws Exception {
        HttpServer server = tools.runServer("classpath:ssl/server.pkcs12.keystore", "serverkeystorepassword",
                "classpath:ssl/server.pkcs12.truststore", "servertruststorepassword");
        try {
            System.setProperty("myclient4/mp-graphql/keystore", "classpath:ssl/client.pkcs12.wrong.keystore");
            System.setProperty("myclient4/mp-graphql/keystorePassword", "clientwrongkeystorepassword");
            System.setProperty("myclient4/mp-graphql/keystoreType", "PKCS12");
            WebClientOptions options = new WebClientOptions();
            options.setTrustAll(true); // don't require server auth
            try (DynamicGraphQLClient client = new VertxDynamicGraphQLClientBuilder()
                    .configKey("myclient4")
                    .options(options)
                    .url("https://127.0.0.1:" + server.actualPort())
                    .build()) {
                client.executeAsync("asd").await().atMost(TIMEOUT);
                Assertions.fail("Connection to server should fail");
            } catch (Exception e) {
                // verify that the server rejected the client's certificate
                assertHasCauseContainingMessage(e, "Received fatal alert: bad_certificate",
                        "Received fatal alert: certificate_required");
            }
        } finally {
            server.close();
        }
    }

    private void assertHasCauseContainingMessage(Throwable t, String... messages) {
        Throwable throwable = t;
        while (throwable.getCause() != null) {
            throwable = throwable.getCause();
            for (String message : messages) {
                if (throwable.getMessage().contains(message)) {
                    t.printStackTrace();
                    return;
                }
            }
        }
        throw new RuntimeException("Unexpected exception", t);
    }

}
