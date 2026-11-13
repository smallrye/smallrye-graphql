package io.smallrye.graphql.tests.client.typesafe.generics;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.vertx.typesafe.VertxTypesafeGraphQLClientBuilder;
import io.smallrye.graphql.tests.client.typesafe.generics.clientmodels.FullAnimal;
import io.smallrye.graphql.tests.client.typesafe.generics.clientmodels.SimpleAnimal;
import io.smallrye.graphql.tests.client.typesafe.generics.servermodels.Animal;
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

import java.net.URL;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(Arquillian.class)
@RunAsClient
public class TypesafeClientGenericsTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "annotationIgnore.war")
                .addClasses(AnimalApi.class, Animal.class);
    }

    @ArquillianResource
    URL testingURL;

    private <T> T createClient(Class<T> clientClass) {
        return new VertxTypesafeGraphQLClientBuilder()
                .endpoint(testingURL.toString() + "graphql")
                .build(clientClass);
    }

    @GraphQLClientApi
    interface SimpleAnimalClientApi extends AnimalClientApi<SimpleAnimal> {

    }

    @Test
    public void singleObjectReturnedSimple() {
        SimpleAnimalClientApi client = createClient(SimpleAnimalClientApi.class);

        SimpleAnimal response = client.animalWithName("elephant");

        assertEquals(elephantSimple, response);
    }

    @Test
    public void uniReturnedSimple() {
        SimpleAnimalClientApi client = createClient(SimpleAnimalClientApi.class);

        SimpleAnimal response = client.animalWithNameUni("elephant").await().atMost(Duration.ofSeconds(10));

        assertEquals(elephantSimple, response);
    }

    @Test
    public void listReturnedSimple() {
        SimpleAnimalClientApi client = createClient(SimpleAnimalClientApi.class);

        List<SimpleAnimal> response = client.allAnimals();

        MatcherAssert.assertThat(response, Matchers.hasItems(elephantSimple, catSimple));
    }

    @Test
    public void uniListReturnedSimple() {
        SimpleAnimalClientApi client = createClient(SimpleAnimalClientApi.class);

        List<SimpleAnimal> response = client.allAnimalsUni().await().atMost(Duration.ofSeconds(10));

        MatcherAssert.assertThat(response,
                Matchers.hasItems(elephantSimple, catSimple));
    }

    @Test
    public void subscriptionSimple() {
        SimpleAnimalClientApi client = createClient(SimpleAnimalClientApi.class);

        List<SimpleAnimal> response = client.animalsSubscription()
                .collect().asList()
                .await().atMost(Duration.ofSeconds(10));

        MatcherAssert.assertThat(response,
                Matchers.hasItems(elephantSimple, elephantSimple));
    }

    @GraphQLClientApi
    interface FullAnimalClientApi extends AnimalClientApi<FullAnimal> {

    }

    @Test
    public void singleObjectReturnedFull() {
        FullAnimalClientApi client = createClient(FullAnimalClientApi.class);

        FullAnimal response = client.animalWithName("elephant");
        assertEquals(elephantFull, response);
    }

    @Test
    public void uniReturnedFull() {
        FullAnimalClientApi client = createClient(FullAnimalClientApi.class);

        FullAnimal response = client.animalWithNameUni("elephant").await().atMost(Duration.ofSeconds(10));

        assertEquals(elephantFull, response);
    }

    @Test
    public void listReturnedFull() {
        FullAnimalClientApi client = createClient(FullAnimalClientApi.class);

        List<FullAnimal> response = client.allAnimals();

        MatcherAssert.assertThat(response, Matchers.hasItems(elephantFull, catFull));
    }

    @Test
    public void uniListReturnedFull() {
        FullAnimalClientApi client = createClient(FullAnimalClientApi.class);

        List<FullAnimal> response = client.allAnimalsUni().await().atMost(Duration.ofSeconds(10));

        MatcherAssert.assertThat(response,
                Matchers.hasItems(elephantFull, catFull));
    }

    @Test
    public void subscriptionFull() {
        FullAnimalClientApi client = createClient(FullAnimalClientApi.class);

        List<FullAnimal> response = client.animalsSubscription()
                .collect().asList()
                .await().atMost(Duration.ofSeconds(10));

        MatcherAssert.assertThat(response,
                Matchers.hasItems(elephantFull, catFull));
    }

    private final SimpleAnimal elephantSimple = new SimpleAnimal("elephant");
    private final SimpleAnimal catSimple = new SimpleAnimal("cat");
    private final FullAnimal elephantFull = new FullAnimal("elephant", 34, 5000, "A very big animal");
    private final FullAnimal catFull = new FullAnimal("cat", 3, 4, "A very cute animal");
}
