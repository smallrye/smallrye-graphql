package io.smallrye.graphql.tests.inheritance;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Vetoed;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.tests.GraphQLAssured;

@RunWith(Arquillian.class)
public class InheritedGraphQLApiMethodsTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "inherited.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(Subclass.class, Superclass.class);
    }

    @ArquillianResource
    URL testingURL;

    /**
     * Here the GraphQLApi class inherits an operation and DOESN'T override it,
     * so the implementation from the superclass should be used at runtime.
     */
    @Test
    public void testWithoutOverride() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String response = graphQLAssured
                .post("query {helloSuper}");
        assertThat(response).isEqualTo("{\"data\":{\"helloSuper\":\"super\"}}");

    }

    /**
     * Here the GraphQLApi class inherits an operation and DOES override it,
     * so the implementation from the subclass should be used at runtime.
     */
    @Test
    public void testWithOverride() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String response = graphQLAssured
                .post("query {overridden}");
        assertThat(response).isEqualTo("{\"data\":{\"overridden\":\"overridden\"}}");
    }

    @GraphQLApi
    @ApplicationScoped
    public static class Subclass extends Superclass {

        @Override
        public String overridden() {
            return "overridden";
        }

    }

    // Here we have to prevent this from being a CDI bean because Jetty makes it a CDI bean by default,
    // which makes the runtime resolution ambiguous - only the GraphQLApi class itself should be a bean.
    // This is not needed in Quarkus.
    @Vetoed
    public static class Superclass {

        @Query
        public String helloSuper() {
            return "super";
        }

        @Query
        public String overridden() {
            return "not overridden";
        }

    }
}
