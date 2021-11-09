package io.smallrye.graphql.tests.client.typesafe.subscription;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.client.vertx.typesafe.VertxTypesafeGraphQLClientBuilder;

@RunWith(Arquillian.class)
@RunAsClient
public class TypesafeClientSubscriptionTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "dynamic-client-subscription-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(SubscriptionApi.class);
    }

    @ArquillianResource
    URL testingURL;

    private SubscriptionClientApi client;

    @Before
    public void prepare() {
        client = new VertxTypesafeGraphQLClientBuilder()
                .endpoint(testingURL + "graphql")
                .build(SubscriptionClientApi.class);
    }

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
        List<Integer> result = client.countToFive()
                .subscribe()
                .asStream()
                .collect(Collectors.toList());
        for (int i = 0; i < 5; i++) {
            assertEquals((Integer) i, result.get(i));
        }
    }

    // TODO, failure handling is not properly implemented yet
    @Test
    public void testFailure() throws InterruptedException {
        CountDownLatch finished = new CountDownLatch(1);
        final AtomicReference<Integer> obtainedItem = new AtomicReference<>();
        client.failingImmediately()
                .subscribe()
                .with(item -> {
                    obtainedItem.set(item);
                    finished.countDown();
                }, failure -> {
                    failure.printStackTrace();
                    finished.countDown();
                }, () -> {
                    System.out.println("COMPLETE");
                    finished.countDown();
                });
        finished.await(10, TimeUnit.SECONDS);
        // how to verify that the multi failed correctly?
    }

}
