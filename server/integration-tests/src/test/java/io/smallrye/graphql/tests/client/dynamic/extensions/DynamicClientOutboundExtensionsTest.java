package io.smallrye.graphql.tests.client.dynamic.extensions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.smallrye.graphql.client.Request;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.impl.RequestImpl;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;
import io.smallrye.graphql.execution.context.SmallRyeContext;

/**
 * Test that extensions can be sent from the dynamic client to the server.
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class DynamicClientOutboundExtensionsTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(ExtensionEchoApi.class);
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
    public void sendExtensionsToServerTest() throws ExecutionException, InterruptedException {
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("myKey", "myValue");
        extensions.put("number", 42);

        Request request = RequestImpl.builder("{ echoExtension(key: \"myKey\") }")
                .extensions(extensions)
                .build();

        Response response = client.executeSync(request);
        assertEquals("myValue", response.getData().getString("echoExtension"));
    }

    @Test
    public void sendNoExtensionsToServerTest() throws ExecutionException, InterruptedException {
        Request request = RequestImpl.builder("{ echoExtension(key: \"myKey\") }").build();

        Response response = client.executeSync(request);
        assertEquals("null", response.getData().getString("echoExtension"));
    }

    @GraphQLApi
    public static class ExtensionEchoApi {

        @Inject
        SmallRyeContext smallRyeContext;

        @Query
        public String echoExtension(String key) {
            Map<String, Object> extensions = smallRyeContext.getExtensionsFromClient();
            if (extensions == null) {
                return "null";
            }
            Object value = extensions.get(key);
            return value != null ? value.toString() : "null";
        }
    }
}
