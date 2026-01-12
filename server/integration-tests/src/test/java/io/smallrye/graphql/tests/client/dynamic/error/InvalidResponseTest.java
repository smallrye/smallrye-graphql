package io.smallrye.graphql.tests.client.dynamic.error;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;

/**
 * Verify that the client allows access to HTTP response headers when an invalid response is received.
 * In this test, a servlet is used to return a 418 I'm a teapot response with a custom header.
 * The client then makes a request and verifies that the InvalidResponseException contains the custom header.
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class InvalidResponseTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "invalid-response-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(ErrorServlet.class);
    }

    @WebServlet(urlPatterns = { "/graphql" })
    public static class ErrorServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setStatus(418); // I'm a teapot
            resp.setHeader("Teapot-Color", "Yellow");
        }
    }

    @ArquillianResource
    URL testingURL;

    private static VertxDynamicGraphQLClient client;

    @BeforeEach
    public void prepare() {
        client = (VertxDynamicGraphQLClient) new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql")
                .build();
    }

    @Test
    public void receiveImATeapotResponse() throws ExecutionException, InterruptedException {
        try {
            client.executeSync("{}");
            Assertions.fail();
        } catch (InvalidResponseException ex) {
            Assertions.assertEquals("Yellow", ex.getTransportMeta().get("Teapot-Color").get(0));
        }
    }

}
