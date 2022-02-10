package io.smallrye.graphql.tests.client.dynamic.subscription;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.runner.RunWith;

import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;
import io.smallrye.graphql.client.websocket.WebsocketSubprotocol;

/**
 * Test subscriptions with a dynamic client and graphql-ws subprotocol
 */
@RunWith(Arquillian.class)
@RunAsClient
public class DynamicClientGraphQLWSSubscriptionTest extends AbstractDynamicClientSubscriptionTest {

    @Before
    public void prepare() {
        client = (VertxDynamicGraphQLClient) new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql")
                .subprotocols(WebsocketSubprotocol.GRAPHQL_WS)
                .build();
    }

}
