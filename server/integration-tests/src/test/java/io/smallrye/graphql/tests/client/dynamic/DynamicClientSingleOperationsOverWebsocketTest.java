package io.smallrye.graphql.tests.client.dynamic;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;
import io.vertx.ext.web.client.WebClientOptions;

@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class DynamicClientSingleOperationsOverWebsocketTest extends DynamicClientSingleOperationsTestBase {

    @BeforeEach
    public void prepare() {
        client = (VertxDynamicGraphQLClient) new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql")
                .options(new WebClientOptions().setMaxWebSocketMessageSize(Integer.MAX_VALUE))
                .executeSingleOperationsOverWebsocket(true)
                .build();
    }

    @AfterEach
    public void cleanup() {
        client.close();
    }

}
