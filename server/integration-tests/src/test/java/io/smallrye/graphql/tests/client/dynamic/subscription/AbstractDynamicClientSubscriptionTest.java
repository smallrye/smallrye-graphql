package io.smallrye.graphql.tests.client.dynamic.subscription;

import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.Assert.*;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Test;

import io.smallrye.graphql.client.GraphQLClientException;
import io.smallrye.graphql.client.GraphQLError;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClient;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;

public abstract class AbstractDynamicClientSubscriptionTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "dynamic-client-subscription-test.war")
                .addClasses(DynamicClientSubscriptionApi.class);
    }

    static Duration DURATION = Duration.ofSeconds(5);

    @ArquillianResource
    URL testingURL;

    protected static VertxDynamicGraphQLClient client;

    @After
    public void cleanup() {
        client.close();
    }

    @Test
    public void testCounting() {
        Document document = document(
                operation(OperationType.SUBSCRIPTION,
                        field("countToFive")));
        List<Response> responses = client.subscription(document)
                .subscribe()
                .asStream()
                .collect(Collectors.toList());
        for (int i = 0; i < 5; i++) {
            Response response = responses.get(i);
            assertEquals(i, response.getData().getInt("countToFive"));
            assertNoErrors(response.getErrors());
        }
    }

    @Test
    public void testFailingImmediately() throws InterruptedException {
        Document document = document(
                operation(OperationType.SUBSCRIPTION,
                        field("failingImmediately")));
        AssertSubscriber<Response> subscriber = new AssertSubscriber<>(10);
        client.subscription(document).subscribe(subscriber);
        List<Response> messages = subscriber
                .awaitNextItem(DURATION)
                .awaitCompletion(DURATION)
                .assertTerminated()
                .getItems();
        assertEquals(1, messages.size());
        assertEquals("System error", messages.get(0).getErrors().get(0).getMessage());
    }

    @Test
    public void testThrowingExceptionDirectly() throws InterruptedException {
        Document document = document(
                operation(OperationType.SUBSCRIPTION,
                        field("throwingExceptionDirectly")));
        AssertSubscriber<Response> subscriber = new AssertSubscriber<>(10);
        client.subscription(document).subscribe(subscriber);
        Throwable failure = subscriber.awaitFailure(DURATION)
                .getFailure();
        assertTrue(failure instanceof GraphQLClientException);
        assertEquals("System error", ((GraphQLClientException) failure).getErrors().get(0).getMessage());
    }

    private void assertNoErrors(List<GraphQLError> errors) {
        if (errors != null && !errors.isEmpty()) {
            fail("No error expected, but there was: " + errors);
        }
    }

}
