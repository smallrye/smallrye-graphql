package io.smallrye.graphql.tests.stork;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.vertx.typesafe.VertxTypesafeGraphQLClientBuilder;
import io.smallrye.stork.Stork;

@RunWith(Arquillian.class)
@RunAsClient
public class StorkTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(DummyApi.class);
    }

    @ArquillianResource
    URL testingURL;

    private DummyClientApi client;

    @Before
    public void prepare() {
        try {
            System.out.println("CORRECT TESTING URL = " + testingURL);
            System.setProperty("stork.myservice.service-discovery.type", "static");
            System.setProperty("stork.myservice.service-discovery.address-list",
                    testingURL.getHost() + ":" + testingURL.getPort());
            Stork.initialize();
            client = new VertxTypesafeGraphQLClientBuilder()
                    .endpoint("stork://myservice" + testingURL.getPath() + "graphql")
                    .build(DummyClientApi.class);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    @Test
    public void callThroughStork() {
        try {
            assertEquals("ok", client.q());
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    @GraphQLApi
    public static class DummyApi {

        @Query
        public String q() {
            return "ok";
        }

    }

    @GraphQLClientApi
    public interface DummyClientApi {

        @Query
        String q();

    }

}
