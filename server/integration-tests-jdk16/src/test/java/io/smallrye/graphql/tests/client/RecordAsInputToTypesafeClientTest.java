package io.smallrye.graphql.tests.client;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.typesafe.api.TypesafeGraphQLClientBuilder;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
@RunAsClient
public class RecordAsInputToTypesafeClientTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(SimpleRecord.class);
    }

    @ArquillianResource
    URL testingURL;

    @Test
    public void testSimpleRecord() {
        ClientApi client = TypesafeGraphQLClientBuilder.newBuilder()
                .endpoint(testingURL.toString() + "graphql")
                .build(ClientApi.class);
        SimpleRecord result = client.simple();
        assertEquals("a", result.a());
        assertEquals("b", result.b());
    }

    @GraphQLClientApi
    public interface ClientApi {

        @Query
        SimpleRecord simple();

    }

    @GraphQLApi
    public static class Api implements ClientApi {

        @Query
        public SimpleRecord simple() {
            return new SimpleRecord("a", "b");
        }

    }

    public record SimpleRecord(String a, String b) {
    }

}
