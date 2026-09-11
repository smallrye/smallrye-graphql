package io.smallrye.graphql.tests.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Subscription;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.Cancellable;
import io.smallrye.mutiny.unchecked.Unchecked;

/**
 * Verify that metrics are produced as expected.
 */
@ExtendWith(ArquillianExtension.class)
public class MicrometerMetricsTestCase {

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

    DynamicGraphQLClient client;

    @BeforeEach
    public void before() {
        client = new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql")
                .build();
        //Needs to add this registry in order to maintain count state across timer counts
        Metrics.addRegistry(new SimpleMeterRegistry());
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
        assertThat(meterRegistry.getMeters()).hasSizeGreaterThanOrEqualTo(1);
        Gauge gauge = meterRegistry.get("mp_graphql_subscription").tags(Tags.of("name", "counting")).gauge();
        assertThat(gauge).isNotNull();
        assertThat(gauge.value()).isEqualTo(1L);
        cancellable.cancel();
        assertTrue(Foo.CANCELLED.await(10, TimeUnit.SECONDS));
        assertThat(gauge.value()).isEqualTo(0L);
    }
}
