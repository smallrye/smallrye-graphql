package io.smallrye.graphql.tests.subscription;

import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonReaderFactory;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testing a subscription through a pure websocket client (no usage of GraphQL clients).
 */
@RunWith(Arquillian.class)
@RunAsClient
public class SubscriptionTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "subscription-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(DummySubscriptionApi.class);
    }

    @ArquillianResource
    URL testingURL;

    // Marks the moment when the websocket is closed and the client has finished receiving messages
    static CountDownLatch webSocketFinish = new CountDownLatch(1);

    // The client expects to receive numbers 1,2,3,4,5 and will store them here
    static List<Integer> receivedNumbers = new ArrayList<>();

    @Test
    public void testSubscription() throws Exception {
        String correctUrl = testingURL.toString().replaceFirst("http", "ws") + "graphql";

        WebSocketClient client = new WebSocketClient();
        client.start();
        client.connect(new MyWebSocketClient(), URI.create(correctUrl), new ClientUpgradeRequest());

        if (!webSocketFinish.await(10, TimeUnit.SECONDS)) {
            Assert.fail("Timeout waiting for the websocket communication to finish");
        }
        List<Integer> expectedNumbers = new ArrayList<>();
        expectedNumbers.add(1);
        expectedNumbers.add(2);
        expectedNumbers.add(3);
        expectedNumbers.add(4);
        expectedNumbers.add(5);
        Assert.assertEquals(expectedNumbers, receivedNumbers);
    }

    @WebSocket
    public static class MyWebSocketClient {

        private JsonReaderFactory jsonReaderFactory = Json.createReaderFactory(new HashMap<>());

        @OnWebSocketConnect
        public void onConnect(Session session) {
            System.out.printf("Got connect: %s%n", session);
            try {
                session.getRemote().sendString("{\"query\":\"subscription { countToFive }\"}");
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            System.out.println("Connection closed. Status code = " + statusCode + ", reason = " + reason);
            webSocketFinish.countDown();
        }

        @OnWebSocketMessage
        public void onMessage(String msg) {
            System.out.printf("Got msg: %s%n", msg);
            try {
                int receivedNumber = jsonReaderFactory.createReader(new StringReader(msg))
                        .readObject()
                        .getJsonObject("data")
                        .getInt("countToFive");
                receivedNumbers.add(receivedNumber);
            } catch (Throwable t) {
                System.out.println("WARNING: received message has an unexpected format: " + msg);
            }
        }

    }

}
