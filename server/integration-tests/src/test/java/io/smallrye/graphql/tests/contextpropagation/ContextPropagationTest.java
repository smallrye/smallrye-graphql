package io.smallrye.graphql.tests.contextpropagation;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import java.net.URL;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.tests.GraphQLAssured;

@RunWith(Arquillian.class)
public class ContextPropagationTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "cp-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(CPApi.class);
    }

    @ArquillianResource
    URL testingURL;

    @Test
    @RunAsClient
    public void invokeApi() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String response1 = graphQLAssured.post("{query1 {message additionalMessage}}");
        assertThat(response1, containsString("OK query1"));
        assertThat(response1, containsString("ADDITIONAL additionalMessage"));

        String response2 = graphQLAssured.post("{query2 {message additionalMessage}}");
        assertThat(response2, containsString("OK query2"));
        assertThat(response2, containsString("ADDITIONAL additionalMessage"));
    }

    public static class Dummy {

        private final String message;

        public Dummy(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    @GraphQLApi
    @ApplicationScoped
    public static class CPApi {

        @Inject
        Context context;

        @Query
        public Dummy query1() {
            return new Dummy("OK " + context.getFieldName());
        }

        @Query
        public Dummy query2() {
            return new Dummy("OK " + context.getFieldName());
        }

        public String additionalMessage(@Source Dummy dummy) {
            return "ADDITIONAL " + context.getFieldName();
        }

    }
}
