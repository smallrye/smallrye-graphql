package io.smallrye.graphql.tests.client.dynamic.extensions;

import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;
import io.smallrye.graphql.execution.context.SmallRyeContext;

/**
 * Test extension(s) features of the dynamic client
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class DynamicClientExtensionsTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(PoolApi.class, Pool.class);
    }

    @ArquillianResource
    URL testingURL;

    private static VertxDynamicGraphQLClient client;

    @BeforeEach
    public void prepare() {
        client = (VertxDynamicGraphQLClient) new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql")
                .build();
    }

    @Test
    public void emptyExtensionsTest() throws ExecutionException, InterruptedException {
        Response response = client.executeSync(document(operation(
                field("poolWithoutExtensions",
                        field("volume")))));
        assertEquals(null, response.getExtensions());
    }

    @Test
    public void addedExtensionsTest() throws ExecutionException, InterruptedException {
        Response response = client.executeSync(document(operation(
                field("poolWithExtensions",
                        field("volume")))));
        assertEquals(getJsonMap(), response.getExtensions());
    }

    @GraphQLApi
    public static class PoolApi {

        @Inject
        SmallRyeContext smallRyeContext;

        @Query
        public Pool poolWithoutExtensions() {
            return new Pool(1);
        }

        @Query
        public Pool poolWithExtensions() {
            smallRyeContext.getAddedExtensions().putAll(getMap());
            return new Pool(23);
        }
    }

    public static class Pool {

        private Integer volume;

        public Pool(Integer volume) {
            setVolume(volume);
        }

        public Pool() {
        }

        public Integer getVolume() {
            return volume;
        }

        public void setVolume(Integer volume) {
            this.volume = volume;
        }
    }

    private static Map<String, Object> getMap() {
        Map<String, Object> map = new HashMap();
        map.put("int", 23);
        map.put("double", 3.1415926535);
        map.put("list of strings", List.of("1", "2", "3", "4"));
        Map<String, String> subMap = new HashMap<>();
        subMap.put("1", "Paul");
        subMap.put("3", "John");
        map.put("subMap", subMap);
        return map;
    }

    private JsonObject getJsonMap() {
        JsonObject result = Json.createObjectBuilder()
                .add("int", 23)
                .add("double", 3.1415926535)
                .add("list of strings", Json.createArrayBuilder().add("1").add("2").add("3").add("4").build())
                .add("subMap", Json.createObjectBuilder()
                        .add("1", "Paul")
                        .add("3", "John")
                        .build())
                .build();
        return result;
    }
}
