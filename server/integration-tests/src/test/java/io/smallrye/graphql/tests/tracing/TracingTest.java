package io.smallrye.graphql.tests.tracing;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.tests.SimpleGraphQLClient;

@RunWith(Arquillian.class)
public class TracingTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "tracing-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource(new StringAsset("smallrye.graphql.tracing.enabled=true"),
                        "META-INF/microprofile-config.properties")
                .addClasses(DummyGraphQLApi.class, Foo.class);
    }

    @ArquillianResource
    URL testingURL;

    @Test
    @RunAsClient
    @InSequence(100)
    public void invokeApi() throws Exception {
        SimpleGraphQLClient client = new SimpleGraphQLClient(testingURL);
        String response = client.query("{"
                + " get {"
                + "     version"
                + "     tracerString"
                + " }"
                + "}");

        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();
        assertThat(response).contains("GraphQL:Query.get");
    }
}
