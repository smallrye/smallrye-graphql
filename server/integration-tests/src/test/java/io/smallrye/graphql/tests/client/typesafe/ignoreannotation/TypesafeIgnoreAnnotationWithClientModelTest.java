package io.smallrye.graphql.tests.client.typesafe.ignoreannotation;

import static io.smallrye.graphql.client.modelbuilder.ClientModelBuilder.build;

import java.io.IOException;

import org.jboss.jandex.Index;
import org.junit.Before;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.vertx.typesafe.VertxTypesafeGraphQLClientBuilder;
import io.smallrye.graphql.tests.client.typesafe.ignoreannotation.clientmodels.Person;

public class TypesafeIgnoreAnnotationWithClientModelTest extends TypesafeIgnoreAnnotationTest {

    @Override
    @Before
    public void prepare() {
        Index index = null;
        try {
            index = Index.of(IgnoreClientApi.class, Person.class, GraphQLClientApi.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        client = new VertxTypesafeGraphQLClientBuilder()
                .clientModels(build(index))
                .endpoint(testingURL.toString() + "graphql")
                .build(IgnoreClientApi.class);
    }
}
