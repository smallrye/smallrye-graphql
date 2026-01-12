package io.smallrye.graphql.tests.dependentscope;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.Dependent;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.smallrye.graphql.tests.GraphQLAssured;

/**
 * Test to verify that a @Dependent-scoped GraphQL API will get each instance destroyed after use,
 * to prevent leaks
 */
@ExtendWith(ArquillianExtension.class)
public class DependentScopeApiTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "dependent-scope-test.war")
                .addClasses(DependentScopedApi.class);
    }

    @ArquillianResource
    URL testingURL;

    @Test
    public void test() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String response = graphQLAssured
                .post("query {hello}");

        assertThat(response).isEqualTo("{\"data\":{\"hello\":\"hi\"}}");
        Assertions.assertTrue(DependentScopedApi.DESTROYED.get(),
                "Instance of the API class has to be destroyed after each request");
    }

    @GraphQLApi
    @Dependent
    public static class DependentScopedApi {

        public static AtomicBoolean DESTROYED = new AtomicBoolean();

        @Query
        public String hello() {
            return "hi";
        }

        @PreDestroy
        public void onDestroy() {
            DESTROYED.set(true);
        }

    }
}
