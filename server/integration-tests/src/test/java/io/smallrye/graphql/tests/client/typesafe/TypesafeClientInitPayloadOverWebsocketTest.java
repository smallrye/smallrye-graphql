package io.smallrye.graphql.tests.client.typesafe;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.smallrye.graphql.client.vertx.typesafe.VertxTypesafeGraphQLClientBuilder;
import io.smallrye.graphql.client.websocket.WebsocketSubprotocol;

@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class TypesafeClientInitPayloadOverWebsocketTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "init-payload-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(ServerPayloadApi.class);
    }

    @ArquillianResource
    URL testingURL;

    ClientPayloadApi client;

    final static Map<String, Object> INIT_PAYLOAD = Map.of("version", 123);

    @AfterEach
    public void cleanup() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldInitPayloadWithGraphQLTransportWSSubprotocol() {
        clientInit(WebsocketSubprotocol.GRAPHQL_TRANSPORT_WS);
        assertDoesNotThrow(() -> client.getPayloadResult("{\"version\":123}"));

    }

    @Test
    public void shouldInitPayloadWithGraphQLWSSubprotocol() {
        clientInit(WebsocketSubprotocol.GRAPHQL_WS);
        assertDoesNotThrow(() -> client.getPayloadResult("{\"version\":123}"));
    }

    private void clientInit(WebsocketSubprotocol subprotocol) {
        client = new VertxTypesafeGraphQLClientBuilder()
                .endpoint(testingURL + "graphql")
                .executeSingleOperationsOverWebsocket(true)
                .subprotocols(subprotocol)
                .initPayload(INIT_PAYLOAD)
                .build(ClientPayloadApi.class);
    }
}
