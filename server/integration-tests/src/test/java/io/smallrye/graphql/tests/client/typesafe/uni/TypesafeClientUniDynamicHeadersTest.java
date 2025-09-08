package io.smallrye.graphql.tests.client.typesafe.uni;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.inject.Inject;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.typesafe.api.Header;
import io.smallrye.graphql.client.typesafe.api.TypesafeGraphQLClientBuilder;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.mutiny.Uni;

@RunWith(Arquillian.class)
@RunAsClient
public class TypesafeClientUniDynamicHeadersTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(Api.class);
    }

    @ArquillianResource
    URL testingURL;

    @Test
    public void dynamicHeadersAreNotDuplicatedAcrossRetries() throws Exception {
        ClientApi client = TypesafeGraphQLClientBuilder.newBuilder()
                .endpoint(testingURL.toString() + "graphql")
                .build(ClientApi.class);

        String result = client.ping()
                .onFailure().retry().atMost(2)
                .await().indefinitely();

        assertEquals("ok", result);
    }

    @GraphQLClientApi
    public interface ClientApi {
        @Query
        @Header(name = "Test-Header", method = "provideTestHeader")
        Uni<String> ping();

        static String provideTestHeader() {
            return "Test-Header-Value";
        }
    }

    @GraphQLApi
    public static class Api {

        private static final AtomicInteger ATTEMPTS = new AtomicInteger();

        @Inject
        SmallRyeContext smallRyeContext;

        @Query
        public String ping() {
            final Map<String, List<String>> headers = smallRyeContext
                    .getDataFetchingEnvironment()
                    .getGraphQlContext()
                    .get("httpHeaders");

            final List<String> values = headers.get("Test-Header");
            final int count = values.size();
            final String first = values.get(0);

            if (!"Test-Header-Value".equals(values.get(0))) {
                throw new RuntimeException("unexpected dynamic header value: " + first);
            }

            if (count > 1) {
                throw new RuntimeException("duplicated dynamic header values: " + count);
            }

            final int n = ATTEMPTS.incrementAndGet();
            if (n < 3) {
                throw new RuntimeException("transient failure #" + n);
            }
            return "ok";
        }
    }
}
