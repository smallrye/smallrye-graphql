package io.smallrye.graphql.tests.client.typesafe.subscription;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Test;

import io.smallrye.graphql.client.GraphQLClientException;
import io.smallrye.mutiny.Multi;

public abstract class AbstractTypesafeClientSubscriptionTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "typesafe-client-subscription-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(SubscriptionApi.class, Dummy.class, DummyWithErrorOrOnFailingSourceField.class);
    }

    @ArquillianResource
    URL testingURL;

    protected SubscriptionClientApi client;

    @After
    public void cleanup() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCounting() {
        List<Integer> result = client.countToFive(false)
                .subscribe()
                .asStream()
                .map(Dummy::getNumber)
                .collect(Collectors.toList());
        for (int i = 0; i < 5; i++) {
            assertEquals((Integer) i, result.get(i));
        }
    }

    @Test
    public void testErrorOrOnSourceField() {
        List<DummyWithErrorOrOnFailingSourceField> items = client.countToFiveWithFailingSourceFieldInErrorOr(false)
                .subscribe().asStream().collect(Collectors.toList());
        for (int i = 0; i < 5; i++) {
            DummyWithErrorOrOnFailingSourceField item = items.get(i);
            assertTrue(item.getFailingSourceField().hasErrors());
            assertEquals(item.getNumber(), (Integer) i);
        }
    }

    /**
     * A source field is failing, and it's not wrapped in `ErrorOr`, so the subscription
     * will fail on the client side. The server might still be sending more items after that, but these will be ignored!
     */
    @Test
    public void failingSourceFieldWithoutErrorOr() throws InterruptedException {
        CountDownLatch end = new CountDownLatch(1);
        AtomicReference<DummyWithSourceField> receivedItem = new AtomicReference<>();
        AtomicReference<Throwable> receivedThrowable = new AtomicReference<>();
        client.countToFiveWithFailingSourceField(false)
                .subscribe().with(
                        dummy -> {
                            receivedItem.set(dummy);
                        }, t -> {
                            receivedThrowable.set(t);
                            end.countDown();
                        });
        end.await(20, TimeUnit.SECONDS);
        assertNull("Should not receive any valid item", receivedItem.get());
        assertEquals(GraphQLClientException.class, receivedThrowable.get().getClass());
    }

    @Test
    public void failingSubscriptionShouldCloseClient() throws InterruptedException {
        Multi<Dummy> items = client.countToFive(true);
        CountDownLatch end = new CountDownLatch(1);
        items.subscribe().with(item -> {
            System.out.println("onItem callback received");
        }, failure -> {
            end.countDown();
        }, () -> {
            System.out.println("onComplete callback received");
        });
        boolean ended = end.await(10, TimeUnit.SECONDS);
        assertTrue("The client-side multi should receive onFailure after the subscription fails due" +
                " to an exception in server-side processing", ended);
    }

}
