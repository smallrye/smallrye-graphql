package io.smallrye.graphql.tests.client.typesafe.subscription;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.runner.RunWith;

import io.smallrye.graphql.client.vertx.typesafe.VertxTypesafeGraphQLClientBuilder;
import io.smallrye.graphql.client.websocket.WebsocketSubprotocol;

/**
 * Test subscriptions with a typesafe client and graphql-transport-ws subprotocol
 */
@RunWith(Arquillian.class)
@RunAsClient
public class TypesafeClientGraphQLTransportWSSubscriptionTest extends AbstractTypesafeClientSubscriptionTest {

    @Before
    public void prepare() {
        client = new VertxTypesafeGraphQLClientBuilder()
                .endpoint(testingURL + "graphql")
                .subprotocols(WebsocketSubprotocol.GRAPHQL_TRANSPORT_WS)
                .build(SubscriptionClientApi.class);
    }

}
