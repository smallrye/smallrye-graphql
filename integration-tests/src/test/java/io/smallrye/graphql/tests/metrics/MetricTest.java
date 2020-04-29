package io.smallrye.graphql.tests.metrics;

import java.net.URL;

import javax.inject.Inject;

import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.SimpleTimer;
import org.eclipse.microprofile.metrics.annotation.RegistryType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.tests.SimpleGraphQLClient;

@RunWith(Arquillian.class)
public class MetricTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "metrics-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource(new StringAsset("smallrye.graphql.metrics.enabled=true"),
                        "META-INF/microprofile-config.properties")
                .addClass(DummyGraphQLApi.class);
    }

    @Inject
    @RegistryType(type = MetricRegistry.Type.VENDOR)
    MetricRegistry metricRegistry;

    @ArquillianResource
    URL testingURL;

    @Test
    @InSequence(99)
    public void verifyMetricsAreRegisteredEagerly() {
        SimpleTimer metricForHelloQuery = metricRegistry.getSimpleTimers().get(new MetricID("mp_graphql_hello"));
        Assert.assertNotNull("Metric should be registered eagerly", metricForHelloQuery);

        SimpleTimer metricForMutation = metricRegistry.getSimpleTimers().get(new MetricID("mp_graphql_mutate"));
        Assert.assertNotNull("Metric should be registered eagerly", metricForMutation);
    }

    @Test
    @RunAsClient
    @InSequence(100)
    public void invokeApi() throws Exception {
        SimpleGraphQLClient client = new SimpleGraphQLClient(testingURL);
        client.query("{hello}");
        client.query("mutation {mutate}");
        client.query("mutation {mutate}");
    }

    @Test
    @InSequence(101)
    public void verifyMetricsAreUpdated() {
        SimpleTimer metricForHelloQuery = metricRegistry.getSimpleTimers().get(new MetricID("mp_graphql_hello"));
        Assert.assertEquals("The query was called once, this should be reflected in metric value",
                1, metricForHelloQuery.getCount());
        Assert.assertTrue("Total elapsed time for query should be greater than zero",
                metricForHelloQuery.getElapsedTime().toNanos() > 0);

        SimpleTimer metricForHelloMutation = metricRegistry.getSimpleTimers().get(new MetricID("mp_graphql_mutate"));
        Assert.assertEquals("The query was called twice, this should be reflected in metric value",
                2, metricForHelloMutation.getCount());
        Assert.assertTrue("Total elapsed time for query should be greater than zero",
                metricForHelloMutation.getElapsedTime().toNanos() > 0);
    }

}
