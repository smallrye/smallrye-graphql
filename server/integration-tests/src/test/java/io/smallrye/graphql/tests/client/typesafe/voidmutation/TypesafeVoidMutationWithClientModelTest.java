package io.smallrye.graphql.tests.client.typesafe.voidmutation;

import static io.smallrye.graphql.client.modelbuilder.ClientModelBuilder.build;

import java.io.IOException;

import org.jboss.jandex.Index;
import org.junit.jupiter.api.BeforeEach;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.vertx.typesafe.VertxTypesafeGraphQLClientBuilder;
import io.smallrye.graphql.tests.client.typesafe.voidmutation.client.RectangleClientApi;

public class TypesafeVoidMutationWithClientModelTest extends TypesafeVoidMutationTest {

    private boolean onlyOnce = false;

    @BeforeEach
    @Override
    public void prepare() {
        if (!onlyOnce) {
            Index index = null;
            try {
                index = Index.of(RectangleClientApi.class, Rectangle.class, GraphQLClientApi.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            client = new VertxTypesafeGraphQLClientBuilder()
                    .clientModels(build(index))
                    .endpoint(testingURL.toString() + "graphql")
                    .build(RectangleClientApi.class);
            client.resetRectangles();
        }
    }
}
