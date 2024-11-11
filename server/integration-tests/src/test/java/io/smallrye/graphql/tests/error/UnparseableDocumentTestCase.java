package io.smallrye.graphql.tests.error;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import java.net.URL;

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
        assertThat(response, containsString(exceptionMessage));
        // The response should not contain the "data" field.
        // See: https://spec.graphql.org/draft/#sec-Response-Format
        //// If the request included execution, the response map must contain an entry with key data.
        //// The value of this entry is described in the "Data" section.
        //// If the request failed before execution due to a syntax error, missing information,
        //// or validation error, this entry must not be present.
        assertThat(response, not(containsString("\"data\":")));
    }

}
