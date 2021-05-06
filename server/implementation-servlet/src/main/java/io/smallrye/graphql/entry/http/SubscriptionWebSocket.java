package io.smallrye.graphql.entry.http;

import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import graphql.ExecutionResult;
import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.cdi.config.GraphQLConfig;
import io.smallrye.graphql.execution.ExecutionResponse;
import io.smallrye.graphql.execution.ExecutionService;

/**
 * Executing the GraphQL request
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ServerEndpoint("/graphql")
public class SubscriptionWebSocket {

    private static final JsonReaderFactory jsonReaderFactory = Json.createReaderFactory(null);
    private final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();

    @Inject
    ExecutionService executionService;

    @Inject
    GraphQLSchema graphQLSchema;

    @Inject
    GraphQLConfig config;

    @OnClose
    public void onClose(Session session) throws IOException {
        subscriptionRef.set(null);
    }

    @OnError
    public void onError(Session session, Throwable throwable) throws IOException {
        session.getBasicRemote().sendText(throwable.getMessage());
    }

    @OnMessage
    public void handleMessage(Session session, String message) {
        try (JsonReader jsonReader = jsonReaderFactory.createReader(new StringReader(message))) {
            JsonObject jsonInput = jsonReader.readObject();

            ExecutionResponse executionResponse = executionService.execute(jsonInput);

            Publisher<ExecutionResult> stream = executionResponse.getExecutionResult().getData();

            if (stream != null) { // TODO: How to handle when null ?
                stream.subscribe(new Subscriber<ExecutionResult>() {

                    @Override
                    public void onSubscribe(Subscription s) {
                        subscriptionRef.set(s);
                        request(1, session);
                    }

                    @Override
                    public void onNext(ExecutionResult er) {

                        try {
                            Object response = er.getData();
                            if (session.isOpen()) {
                                session.getBasicRemote().sendText(JSONB.toJson(response));
                            }
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        request(1, session);

                    }

                    @Override
                    public void onError(Throwable t) {
                        try {
                            session.getBasicRemote().sendText(t.getMessage());
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }

                    @Override
                    public void onComplete() {
                        try {
                            session.close();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });
            }
        }

    }

    private void request(int n, Session session) {
        Subscription subscription = subscriptionRef.get();
        if (subscription != null && session.isOpen()) {
            subscription.request(n);
        }
    }

    private static final Jsonb JSONB = JsonbBuilder.create(new JsonbConfig().withNullValues(true));
}
