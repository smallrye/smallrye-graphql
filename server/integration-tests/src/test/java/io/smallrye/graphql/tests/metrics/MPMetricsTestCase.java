package io.smallrye.graphql.tests.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Tag;
import org.eclipse.microprofile.metrics.annotation.RegistryType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.Cancellable;
import io.smallrye.mutiny.unchecked.Unchecked;

/**
 * Verify that metrics are produced as expected.
 */
@RunWith(Arquillian.class)
public class MPMetricsTestCase {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "metrics-test.war")
                .addAsResource(new StringAsset("smallrye.graphql.metrics.enabled=true"),
                        "META-INF/microprofile-config.properties")
                .addClasses(Foo.class);
    }

    @ArquillianResource
    URL testingURL;

    @GraphQLApi
    public static class Foo {

        static CountDownLatch CANCELLED = new CountDownLatch(1);

        @Subscription
        public Multi<Long> counting() {
            return Multi.createFrom()
                    .ticks()
                    .every(Duration.ofSeconds(1L))
                    .onCancellation().invoke(() -> CANCELLED.countDown());
        }

        @Query
        public Long count() {
            return CANCELLED.getCount();
        }

    }

    MeterRegistry meterRegistry = Metrics.globalRegistry;

    @Inject
    @RegistryType(type = MetricRegistry.Type.VENDOR)
    MetricRegistry metricRegistry;

    DynamicGraphQLClient client;

    @Before
    public void before() {
        client = new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql")
                .build();
    }

    @Test
    public void testSubscriptionMetrics() throws Exception {
        Multi<Response> multi = client.subscription("subscription {counting}");
        CountDownLatch items = new CountDownLatch(2);
        Cancellable cancellable = multi.subscribe().with(
                response -> items.countDown(),
                Unchecked.consumer(error -> {
                    throw new AssertionError("Subscription failed", error);
                }));

        assertTrue(items.await(10, TimeUnit.SECONDS));

        assertThat(metricRegistry.getGauges()).hasSize(1);
        MetricID metricID = new MetricID("mp_graphql_subscription", new Tag("name", "counting"));
        Gauge<?> gauge = metricRegistry.getGauge(metricID);
        assertThat(gauge).isNotNull();
        assertThat(gauge.getValue()).isEqualTo(1L);

        cancellable.cancel();
        assertTrue(Foo.CANCELLED.await(10, TimeUnit.SECONDS));
        assertThat(gauge.getValue()).isEqualTo(0L);
    }
}
