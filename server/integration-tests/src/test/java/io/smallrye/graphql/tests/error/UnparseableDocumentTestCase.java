package io.smallrye.graphql.tests.error;

import java.net.URL;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.tests.GraphQLAssured;

@RunWith(Arquillian.class)
@RunAsClient
public class UnparseableDocumentTestCase {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "unparseable.war")
                .addClasses(Foo.class);
    }

    @GraphQLApi
    static class Foo {
        @Query
        public String foo() {
            return "bar";
        }
    }

    @ArquillianResource
    URL testingURL;

    @Test
    public void tryUnparsableDocumentRequest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String request = "query Foo {\n" +
                "    foo {\n" +
                "}";
        String exceptionMessage = "Unparseable input document";

        String response = graphQLAssured.post(request);
        MatcherAssert.assertThat(response, Matchers.containsString(exceptionMessage));
    }

}
