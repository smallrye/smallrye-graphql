package io.smallrye.graphql.tests.client.typesafe.subscription;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.List;
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
        return ShrinkWrap.create(WebArchive.class, "typesafe-client-subscription-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(SubscriptionApi.class, Dummy.class, DummyWithErrorOrOnFailingSourceField.class);
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
        List<DummyWithErrorOrOnFailingSourceField> items = client.countToFiveWithFailingSourceField(false)
                .subscribe().asStream().collect(Collectors.toList());
        for (int i = 0; i < 5; i++) {
            DummyWithErrorOrOnFailingSourceField item = items.get(i);
            assertTrue(item.getFailingSourceField().hasErrors());
            assertEquals(item.getNumber(), (Integer) i);
        }
    }

}
