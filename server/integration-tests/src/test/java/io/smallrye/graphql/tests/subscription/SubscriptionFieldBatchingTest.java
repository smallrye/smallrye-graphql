package io.smallrye.graphql.tests.subscription;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.json.Json;
import jakarta.json.JsonObject;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.api.Subscription;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;
import io.smallrye.mutiny.Multi;

@RunWith(Arquillian.class)
public class SubscriptionFieldBatchingTest {
    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "subscription-field-batching-test.war")
                .addClasses(Bar.class, MyApi.class);
    }

    @ArquillianResource
    URL testingURL;

    @GraphQLApi
    public static class MyApi {

        public List<Integer> sourceBatchField(@Source List<Bar> bars) {
            if (bars.size() != 1) {
                throw new IllegalStateException("Expected a batch size of 1");
            }
            return bars
                    .stream()
                    .map(Bar::getField1)
                    // 111 -> 1, 222 -> 2, 333 -> 3
                    .map(field1 -> Integer.divideUnsigned(field1, 100))
                    .collect(Collectors.toList());
        }

        @Subscription
        public Multi<Bar> subscriptionOperation() {
            return Multi.createFrom().items(new Bar(111, "bar1"), new Bar(222, "bar2"), new Bar(333, "bar3"));
        }
    }

    public static class Bar {
        private int field1;
        private String field2;

        public Bar() {
        }

        public Bar(int field1, String field2) {
            this.field1 = field1;
            this.field2 = field2;
        }

        public int getField1() {
            return field1;
        }

        public void setField1(int field1) {
            this.field1 = field1;
        }

        public String getField2() {
            return field2;
        }

        public void setField2(String field2) {
            this.field2 = field2;
        }
    }

    @Test
    public void subscriptionFieldBatchingTest() throws Exception {
        try (DynamicGraphQLClient dynamicGraphQLClient = new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql")
                .build()) {
            List<Response> responses = dynamicGraphQLClient
                    .subscription(
                            "subscription subscriptionOperation { subscriptionOperation { field1 field2 sourceBatchField } }")
                    .subscribe()
                    .asStream()
                    .collect(Collectors.toList());

            assertThat(responses.size()).isEqualTo(3);
            for (int i = 0; i < responses.size(); i++) {
                Response response = responses.get(i);
                assertThat(response.getData())
                        .isNotNull();
                assertThat(response.getErrors()).isNull();

                JsonObject content = response.getData().get("subscriptionOperation").asJsonObject();
                assertThat(content).isNotNull();

                assertThat(content.containsKey("field1")).isTrue();
                assertThat(content.containsKey("field2")).isTrue();
                assertThat(content.containsKey("sourceBatchField")).isTrue();

                assertThat(content.get("field1")).isEqualTo(Json.createValue(111 * (i + 1)));
                assertThat(content.get("field2")).isEqualTo(Json.createValue("bar" + (i + 1)));
                assertThat(content.get("sourceBatchField")).isEqualTo(Json.createValue(i + 1));
            }
        }
    }
}
