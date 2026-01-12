package io.smallrye.graphql.tests.client.dynamic.subscription;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;
import io.smallrye.graphql.client.websocket.WebsocketSubprotocol;

/**
 * Test subscriptions with a dynamic client and graphql-ws subprotocol
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class DynamicClientGraphQLWSSubscriptionTest extends AbstractDynamicClientSubscriptionTest {

    @BeforeEach
    public void prepare() {
        client = (VertxDynamicGraphQLClient) new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql")
                .subprotocols(WebsocketSubprotocol.GRAPHQL_WS)
                .build();
    }

}
