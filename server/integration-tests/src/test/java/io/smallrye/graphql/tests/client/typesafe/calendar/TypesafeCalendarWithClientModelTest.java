package io.smallrye.graphql.tests.client.typesafe.calendar;

import static io.smallrye.graphql.client.model.ClientModelBuilder.build;

import java.io.IOException;

import org.jboss.jandex.Index;
import org.junit.Before;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.vertx.typesafe.VertxTypesafeGraphQLClientBuilder;

public class TypesafeCalendarWithClientModelTest extends TypesafeCalendarTest {
    private boolean onlyOnce = false;

    @Override
    @Before
    public void prepare() {
        if (!onlyOnce) {
            Index index = null;
            try {
                index = Index.of(ClientSomeApi.class, GraphQLClientApi.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            client = new VertxTypesafeGraphQLClientBuilder()
                    .clientModels(build(index))
                    .endpoint(testingURL.toString() + "graphql")
                    .build(ClientSomeApi.class);
            onlyOnce = true;
        }
    }
}
