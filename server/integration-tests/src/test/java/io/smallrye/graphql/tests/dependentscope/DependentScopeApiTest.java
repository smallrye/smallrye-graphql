package io.smallrye.graphql.tests.dependentscope;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.Dependent;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.tests.GraphQLAssured;

/**
 * Test to verify that a @Dependent-scoped GraphQL API will get each instance destroyed after use,
 * to prevent leaks
 */
@RunWith(Arquillian.class)
public class DependentScopeApiTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "dependent-scope-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
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
        Assert.assertTrue("Instance of the API class has to be destroyed after each request",
                DependentScopedApi.DESTROYED.get());
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
