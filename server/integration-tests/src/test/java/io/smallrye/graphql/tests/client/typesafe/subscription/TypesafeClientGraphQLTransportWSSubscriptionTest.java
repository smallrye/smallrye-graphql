package io.smallrye.graphql.tests.client.typesafe.subscription;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import io.smallrye.graphql.client.vertx.typesafe.VertxTypesafeGraphQLClientBuilder;
import io.smallrye.graphql.client.websocket.WebsocketSubprotocol;

/**
 * Test subscriptions with a typesafe client and graphql-transport-ws subprotocol
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class TypesafeClientGraphQLTransportWSSubscriptionTest extends AbstractTypesafeClientSubscriptionTest {

    private boolean onlyOnce = false;

    @BeforeEach
    public void prepare() {
        if (!onlyOnce) {
            client = new VertxTypesafeGraphQLClientBuilder()
                    .endpoint(testingURL + "graphql")
                    .subprotocols(WebsocketSubprotocol.GRAPHQL_TRANSPORT_WS)
                    .build(SubscriptionClientApi.class);
            onlyOnce = true;
        }
    }

}
