package io.smallrye.graphql.tests.client.typesafe.subscription;

import static io.smallrye.graphql.client.modelbuilder.ClientModelBuilder.build;

import java.io.Closeable;
import java.io.IOException;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.vertx.typesafe.VertxTypesafeGraphQLClientBuilder;
import io.smallrye.graphql.client.websocket.WebsocketSubprotocol;

/**
 * Test subscriptions with a typesafe client and graphql-ws subprotocol
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class TypesafeClientGraphQLWSSubscriptionWithClientModelTest extends AbstractTypesafeClientSubscriptionTest {
    private boolean onlyOnce = false;

    @BeforeEach
    public void prepare() {
        if (!onlyOnce) {
            Index index = null;
            try {
                index = Index.of(SubscriptionClientApi.class,
                        Dummy.class, DummyWithSourceField.class,
                        DummyWithErrorOrOnFailingSourceField.class,
                        GraphQLClientApi.class, Closeable.class, AutoCloseable.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            client = new VertxTypesafeGraphQLClientBuilder()
                    .clientModels(build(index))
                    .endpoint(testingURL + "graphql")
                    .subprotocols(WebsocketSubprotocol.GRAPHQL_WS)
                    .build(SubscriptionClientApi.class);

            onlyOnce = true;
        }

    }

}
