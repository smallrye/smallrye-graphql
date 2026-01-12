package io.smallrye.graphql.tests.extensions;

import java.net.URL;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.smallrye.graphql.tests.GraphQLAssured;

@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class UserSupportedExtensionsTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "user-extensions.war")
                .addClasses(Shirt.class, ShirtResource.class);
    }

    @ArquillianResource
    URL testingURL;

    @Test
    public void userAddedExtension() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String request = "query { shirts { size } }";
        String response = graphQLAssured.post(request);
        MatcherAssert.assertThat(response, Matchers.containsString("\"extensions\":"));
        MatcherAssert.assertThat(response, Matchers.containsString("\"bar\":\"This is test for extensions\""));
        MatcherAssert.assertThat(response, Matchers.containsString("\"foo\":3.14159"));
    }
}
