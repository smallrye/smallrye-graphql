package io.smallrye.graphql.tests.client.dynamic;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;
import io.vertx.ext.web.client.WebClientOptions;

@RunWith(Arquillian.class)
@RunAsClient
public class DynamicClientSingleOperationsOverWebsocketTest extends DynamicClientSingleOperationsTestBase {

    @Before
    public void prepare() {
        client = (VertxDynamicGraphQLClient) new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql")
                .options(new WebClientOptions().setMaxWebSocketMessageSize(Integer.MAX_VALUE))
                .executeSingleOperationsOverWebsocket(true)
                .build();
    }

    @After
    public void cleanup() {
        client.close();
    }

}
