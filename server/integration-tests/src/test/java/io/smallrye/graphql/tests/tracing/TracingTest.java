package io.smallrye.graphql.tests.tracing;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.tests.SimpleGraphQLClient;

@RunWith(Arquillian.class)
public class TracingTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "tracing-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource(new StringAsset("smallrye.graphql.tracing.enabled=true"),
                        "META-INF/microprofile-config.properties")
                .addClasses(DummyGraphQLApi.class, Foo.class);
    }

    //    @Inject
    //    @RegistryType(type = MetricRegistry.Type.VENDOR)
    //    MetricRegistry metricRegistry;

    @ArquillianResource
    URL testingURL;

    //    @Test
    //    @InSequence(99)
    //    public void verifyMetricsAreRegisteredEagerly() {
    //        SimpleTimer metricForHelloQuery = metricRegistry.getSimpleTimers().get(new MetricID("mp_graphql_Query_get"));
    //        Assert.assertNotNull("Metric should be registered eagerly (Query)", metricForHelloQuery);
    //
    //        SimpleTimer metricForMutation = metricRegistry.getSimpleTimers().get(new MetricID("mp_graphql_Mutation_mutate"));
    //        Assert.assertNotNull("Metric should be registered eagerly (Mutation)", metricForMutation);
    //
    //        SimpleTimer metricForSource = metricRegistry.getSimpleTimers().get(new MetricID("mp_graphql_Source_description"));
    //        Assert.assertNotNull("Metric should be registered eagerly (Source)", metricForSource);
    //    }

    @Test
    @RunAsClient
    @InSequence(100)
    public void invokeApi() throws Exception {
        SimpleGraphQLClient client = new SimpleGraphQLClient(testingURL);
        client.query("{get {version}}");
        client.query("{get {version}}");
        client.query("mutation {mutate{version}}");
        client.query("mutation {mutate{version}}");
        client.query("{get {description}}");
    }

    //    @Test
    //    @InSequence(101)
    //    public void verifyMetricsAreUpdated() {
    //        SimpleTimer metricForGetQuery = metricRegistry.getSimpleTimers().get(new MetricID("mp_graphql_Query_get"));
    //        Assert.assertEquals("The 'get' query was called three times, this should be reflected in metric value",
    //                3, metricForGetQuery.getCount());
    //        Assert.assertTrue("Total elapsed time for query should be greater than zero",
    //                metricForGetQuery.getElapsedTime().toNanos() > 0);
    //
    //        SimpleTimer metricForMutation = metricRegistry.getSimpleTimers().get(new MetricID("mp_graphql_Mutation_mutate"));
    //        Assert.assertEquals("The query was called twice, this should be reflected in metric value",
    //                2, metricForMutation.getCount());
    //        Assert.assertTrue("Total elapsed time for query should be greater than zero",
    //                metricForMutation.getElapsedTime().toNanos() > 0);
    //
    //        SimpleTimer metricForSource = metricRegistry.getSimpleTimers().get(new MetricID("mp_graphql_Source_description"));
    //        Assert.assertEquals("The get{description} query was called once, this should be reflected in metric value",
    //                1, metricForSource.getCount());
    //        Assert.assertTrue("Total elapsed time for query should be greater than zero",
    //                metricForSource.getElapsedTime().toNanos() > 0);
    //    }

}
