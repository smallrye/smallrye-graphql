package io.smallrye.graphql.client.vertx.test.ssl;

import java.io.Closeable;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.typesafe.api.TypesafeGraphQLClientBuilder;
import io.smallrye.graphql.client.vertx.typesafe.VertxTypesafeGraphQLClientBuilder;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.client.WebClientOptions;

public class TypesafeClientSSLTest {

    private SSLTestingTools tools = new SSLTestingTools();

    @GraphQLClientApi
    interface MyClient extends Closeable {

        Integer[] numbers();

    }

    private final Integer[] EXPECTED_RESPONSE_NUMBERS = new Integer[] { 32, 33 };

    /**
     * Client requests server authentication.
     */
    @Test
    public void serverAuthentication_correctTruststore() throws Exception {
        HttpServer server = tools.runServer("classpath:ssl/server.pkcs12.keystore", "serverkeystorepassword", null, null);
        try {
            System.setProperty("ts/mp-graphql/truststore", "classpath:ssl/client.pkcs12.truststore");
            System.setProperty("ts/mp-graphql/truststorePassword", "clienttruststorepassword");
            System.setProperty("ts/mp-graphql/truststoreType", "PKCS12");
            try (MyClient client = TypesafeGraphQLClientBuilder.newBuilder()
                    .configKey("ts")
                    .endpoint("https://127.0.0.1:" + server.actualPort())
                    .build(MyClient.class)) {
                Assertions.assertArrayEquals(EXPECTED_RESPONSE_NUMBERS, client.numbers());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            server.close();
            System.clearProperty("ts/mp-graphql/truststore");
            System.clearProperty("ts/mp-graphql/truststorePassword");
            System.clearProperty("ts/mp-graphql/truststoreType");
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
            System.setProperty("ts1/mp-graphql/truststore", "classpath:ssl/client.pkcs12.truststore");
            System.setProperty("ts1/mp-graphql/truststorePassword", "clienttruststorepassword");
            System.setProperty("ts1/mp-graphql/truststoreType", "PKCS12");
            try (MyClient client = new VertxTypesafeGraphQLClientBuilder()
                    .configKey("ts1")
                    .endpoint("https://127.0.0.1:" + server.actualPort())
                    .build(MyClient.class)) {
                client.numbers();
                Assertions.fail("Should not be able to connect");
            } catch (Exception e) {
                // verify that the server rejected the client's certificate
                assertHasCauseContainingMessage(e, "unable to find valid certification path to requested target");
            }
        } finally {
            server.close();
            System.clearProperty("ts1/mp-graphql/truststore");
            System.clearProperty("ts1/mp-graphql/truststorePassword");
            System.clearProperty("ts1/mp-graphql/truststoreType");
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
            System.setProperty("ts2/mp-graphql/keystore", "classpath:ssl/client.pkcs12.keystore");
            System.setProperty("ts2/mp-graphql/keystorePassword", "clientkeystorepassword");
            System.setProperty("ts2/mp-graphql/keystoreType", "PKCS12");
            WebClientOptions options = new WebClientOptions();
            options.setTrustAll(true); // don't require server auth
            try (MyClient client = new VertxTypesafeGraphQLClientBuilder()
                    .configKey("ts2")
                    .options(options)
                    .endpoint("https://127.0.0.1:" + server.actualPort())
                    .build(MyClient.class)) {
                Assertions.assertArrayEquals(EXPECTED_RESPONSE_NUMBERS, client.numbers());
            }
        } finally {
            server.close();
            System.clearProperty("ts2/mp-graphql/keystore");
            System.clearProperty("ts2/mp-graphql/keystorePassword");
            System.clearProperty("ts2/mp-graphql/keystoreType");
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
            System.setProperty("ts3/mp-graphql/keystore", "classpath:ssl/client.pkcs12.wrong.keystore");
            System.setProperty("ts3/mp-graphql/keystorePassword", "clientwrongkeystorepassword");
            System.setProperty("ts3/mp-graphql/keystoreType", "PKCS12");
            WebClientOptions options = new WebClientOptions();
            options.setTrustAll(true); // don't require server auth
            try (MyClient client = new VertxTypesafeGraphQLClientBuilder()
                    .configKey("ts3")
                    .options(options)
                    .endpoint("https://127.0.0.1:" + server.actualPort())
                    .build(MyClient.class)) {
                client.numbers();
                Assertions.fail("Should not be able to connect");
            } catch (Exception e) {
                // verify that the server rejected the client's certificate
                assertHasCauseContainingMessage(e, "Received fatal alert: bad_certificate");
            }
        } finally {
            server.close();
            System.clearProperty("ts3/mp-graphql/keystore");
            System.clearProperty("ts3/mp-graphql/keystorePassword");
            System.clearProperty("ts3/mp-graphql/keystoreType");
        }
    }

    private void assertHasCauseContainingMessage(Throwable t, String message) {
        Throwable throwable = t;
        while (throwable.getCause() != null) {
            throwable = throwable.getCause();
            if (throwable.getMessage().contains(message)) {
                t.printStackTrace();
                return;
            }
        }
        throw new RuntimeException("Unexpected exception", t);
    }

}
