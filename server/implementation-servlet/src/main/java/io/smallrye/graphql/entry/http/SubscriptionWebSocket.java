package io.smallrye.graphql.entry.http;

import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import graphql.ExecutionResult;
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
    private final ConcurrentHashMap<String, AtomicReference<Subscription>> subscriptionRefs = new ConcurrentHashMap<>();

    @Inject
    ExecutionService executionService;

    @Inject
    GraphQLConfig config;

    @OnClose
    public void onClose(Session session) {
        unsubscribe(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) throws IOException {
        throwable.printStackTrace();
        unsubscribe(session.getId());
        if (session.isOpen()) {
            session.close();
        }
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
                        AtomicReference<Subscription> subRef = subscriptionRefs.get(session.getId());
                        if (subRef == null) {
                            subRef = new AtomicReference<>(s);
                            subscriptionRefs.put(session.getId(), subRef);
                            s.request(1);
                            return;
                        }
                        if (subRef.compareAndSet(null, s)) {
                            s.request(1);
                        } else {
                            s.cancel();
                        }
                    }

                    @Override
                    public void onNext(ExecutionResult er) {
                        try {
                            if (session.isOpen()) {
                                ExecutionResponse executionResponse = new ExecutionResponse(er, config);
                                session.getBasicRemote().sendText(executionResponse.getExecutionResultAsString());
                                Subscription s = subscriptionRefs.get(session.getId()).get();
                                s.request(1);
                            } else {
                                unsubscribe(session.getId());
                            }
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                        unsubscribe(session.getId());
                        try {
                            session.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onComplete() {
                        unsubscribe(session.getId());
                        try {
                            session.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    private void unsubscribe(String sessionId) {
        AtomicReference<Subscription> subscription = subscriptionRefs.get(sessionId);
        subscriptionRefs.remove(sessionId);

        if (subscription != null && subscription.get() != null) {
            subscription.get().cancel();
            subscription.set(null);
        }
    }

}
