package io.smallrye.graphql.tests.client.typesafe.subscription;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.runner.RunWith;

import io.smallrye.graphql.client.vertx.typesafe.VertxTypesafeGraphQLClientBuilder;
import io.smallrye.graphql.client.websocket.WebsocketSubprotocol;

/**
 * Test subscriptions with a typesafe client and graphql-ws subprotocol
 */
@RunWith(Arquillian.class)
@RunAsClient
public class TypesafeClientGraphQLWSSubscriptionTest extends AbstractTypesafeClientSubscriptionTest {

    @Before
    public void prepare() {
        client = new VertxTypesafeGraphQLClientBuilder()
                .endpoint(testingURL + "graphql")
                .subprotocols(WebsocketSubprotocol.GRAPHQL_WS)
                .build(SubscriptionClientApi.class);
    }

}
