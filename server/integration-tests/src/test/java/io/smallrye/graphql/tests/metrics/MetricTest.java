package io.smallrye.graphql.tests.metrics;

import java.net.URL;

import jakarta.inject.Inject;

import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.SimpleTimer;
import org.eclipse.microprofile.metrics.Tag;
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

import io.smallrye.graphql.tests.GraphQLAssured;

@RunWith(Arquillian.class)
public class MetricTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "metrics-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource(new StringAsset("smallrye.graphql.metrics.enabled=true"),
                        "META-INF/microprofile-config.properties")
                .addClasses(DummyGraphQLApi.class, Foo.class);
    }

    @Inject
    @RegistryType(type = MetricRegistry.Type.VENDOR)
    MetricRegistry metricRegistry;

    @ArquillianResource
    URL testingURL;

    //@Test
    //@InSequence(99)
    public void verifyMetricsAreRegisteredEagerly() {
        SimpleTimer metricForHelloQuery = metricRegistry.getSimpleTimers().get(new MetricID("mp_graphql",
                new Tag("type", "QUERY"), new Tag("name", "get"), new Tag("source", "false")));
        Assert.assertNotNull("Metric should be registered eagerly (Query)", metricForHelloQuery);

        SimpleTimer metricForMutation = metricRegistry.getSimpleTimers().get(new MetricID("mp_graphql",
                new Tag("type", "MUTATION"), new Tag("name", "mutate"), new Tag("source", "false")));
        Assert.assertNotNull("Metric should be registered eagerly (Mutation)", metricForMutation);

        SimpleTimer metricForSource = metricRegistry.getSimpleTimers().get(new MetricID("mp_graphql",
                new Tag("type", "QUERY"), new Tag("name", "description"), new Tag("source", "true")));
        Assert.assertNotNull("Metric should be registered eagerly (Source)", metricForSource);
    }

    @Test
    @RunAsClient
    @InSequence(100)
    public void invokeApi() throws Exception {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        graphQLAssured.post("{get {version}}");
        graphQLAssured.post("{get {version}}");
        graphQLAssured.post("mutation {mutate{version}}");
        graphQLAssured.post("mutation {mutate{version}}");
        graphQLAssured.post("{get {description}}");
    }

    //@Test
    //@InSequence(101)
    public void verifyMetricsAreUpdated() {
        SimpleTimer metricForGetQuery = metricRegistry.getSimpleTimers().get(new MetricID("mp_graphql",
                new Tag("type", "QUERY"), new Tag("name", "get"), new Tag("source", "false")));
        Assert.assertEquals("The 'get' query was called three times, this should be reflected in metric value",
                3, metricForGetQuery.getCount());
        Assert.assertTrue("Total elapsed time for query should be greater than zero",
                metricForGetQuery.getElapsedTime().toNanos() > 0);

        SimpleTimer metricForMutation = metricRegistry.getSimpleTimers().get(new MetricID("mp_graphql",
                new Tag("type", "MUTATION"), new Tag("name", "mutate"), new Tag("source", "false")));
        Assert.assertEquals("The query was called twice, this should be reflected in metric value",
                2, metricForMutation.getCount());
        Assert.assertTrue("Total elapsed time for query should be greater than zero",
                metricForMutation.getElapsedTime().toNanos() > 0);

        SimpleTimer metricForSource = metricRegistry.getSimpleTimers().get(new MetricID("mp_graphql",
                new Tag("type", "QUERY"), new Tag("name", "description"), new Tag("source", "true")));
        Assert.assertEquals("The get{description} query was called once, this should be reflected in metric value",
                1, metricForSource.getCount());
        Assert.assertTrue("Total elapsed time for query should be greater than zero",
                metricForSource.getElapsedTime().toNanos() > 0);
    }

}
