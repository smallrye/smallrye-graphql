package io.smallrye.graphql.tests.client.typesafe.uni;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.typesafe.api.TypesafeGraphQLClientBuilder;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

@RunWith(Arquillian.class)
@RunAsClient
public class TypesafeClientUniRetryOverHttpTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(Api.class);
    }

    @ArquillianResource
    URL testingURL;

    @Test
    public void retriesCreateNewHttpRequestsWithoutDynamicHeaders() {
        ClientApi client = TypesafeGraphQLClientBuilder.newBuilder()
                .endpoint(testingURL.toString() + "graphql")
                .build(ClientApi.class);

        // Server fails twice, then succeeds. We allow 2 retries (3 total attempts).
        String result = client.flaky()
                .onFailure().retry().atMost(2)
                .await().indefinitely();

        assertEquals("ok", result);
    }

    @GraphQLClientApi
    public interface ClientApi {
        @Query
        Uni<String> flaky();
    }

    @GraphQLApi
    public static class Api {
        private static final AtomicInteger ATTEMPTS = new AtomicInteger();

        @Inject
        SmallRyeContext smallRyeContext;

        @Query
        public String flaky() {
            final int n = ATTEMPTS.incrementAndGet();
            if (n < 3) {
                throw new RuntimeException("transient failure #" + n);
            }
            return "ok";
        }
    }
}
