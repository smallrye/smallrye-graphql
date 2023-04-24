package io.smallrye.graphql.tests.client.typesafe.voidmutation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URL;

import jakarta.json.Json;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.client.typesafe.api.TypesafeResponse;
import io.smallrye.graphql.client.vertx.typesafe.VertxTypesafeGraphQLClientBuilder;
import io.smallrye.graphql.tests.client.typesafe.voidmutation.client.RectangleClientApi;
import io.smallrye.graphql.tests.client.typesafe.voidmutation.server.RectangleResources;
import io.smallrye.graphql.tests.client.typesafe.voidmutation.server.RectangleService;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;

@RunWith(Arquillian.class)
@RunAsClient
public class TypesafeVoidMutationTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "typesafeVoidMutation.war")
                .addClasses(RectangleResources.class, RectangleService.class,
                        Rectangle.class);
    }

    @ArquillianResource
    URL testingURL;

    private RectangleClientApi client;

    @Before
    public void prepare() {
        client = new VertxTypesafeGraphQLClientBuilder()
                .endpoint(testingURL.toString() + "graphql")
                .build(RectangleClientApi.class);
        client.resetRectangles();
    }

    @Test
    public void mutationWithReturnTypeVoidTest() {
        Rectangle rectangle = new Rectangle(3.14159, 6.28);
        Void result = client.createRectangle(rectangle);
        assertNull(result);
        assertThat(client.findAllRectangles().size()).isEqualTo(4);
        assertThat(client.findAllRectangles()).contains(rectangle);
    }

    @Test
    public void errorMutationWithReturnTypeVoidTest() {
        Rectangle rectangle = new Rectangle(3.14159, 6.28);
        assertThrows(RuntimeException.class,
                () -> client.createRectangleError(
                        rectangle));
        assertThat(client.findAllRectangles().size()).isEqualTo(3);
        assertThat(client.findAllRectangles()).doesNotContain(rectangle);
    }

    @Test
    public void mutationWithReturnTypeUniVoidTest() {
        UniAssertSubscriber<Void> subscriber = UniAssertSubscriber.create();
        client.someUniMutation(new Rectangle(3.14159, 6.28))
                .subscribe().withSubscriber(subscriber)
                .awaitItem()
                .assertCompleted();
    }

    @Test
    public void errorMutationWithReturnTypeUniVoidTest() {
        UniAssertSubscriber<Void> subscriber = UniAssertSubscriber.create();
        client.someUniMutationThrowsError(new Rectangle(3.14159, 6.28))
                .subscribe().withSubscriber(subscriber)
                .awaitFailure()
                .assertFailedWith(RuntimeException.class);
    }

    @Test
    public void typeSafeResponseVoidMutationTest() {
        Rectangle rectangle = new Rectangle(3.14159, 6.28);
        TypesafeResponse<Void> result = client.typeSafeCreateRectangle(rectangle);
        assertNull(result.get());
        assertThat(client.findAllRectangles().size()).isEqualTo(4);
        assertThat(client.findAllRectangles()).contains(rectangle);
        assertThat(result.getExtensions()).isEqualTo(Json.createObjectBuilder()
                .add("pi", 3.1415)
                .build());
    }

    @Test
    public void mutationWithPrimitiveReturnTypeVoidTest() {
        Rectangle rectangle = new Rectangle(8.81, 57.28);
        assertDoesNotThrow(() -> client.primitiveCreateRectangle(rectangle));
        assertThat(client.findAllRectangles().size()).isEqualTo(4);
        assertThat(client.findAllRectangles()).contains(rectangle);
    }

    @Test
    public void errorMutationWithPrimitiveReturnTypeVoidTest() {
        Rectangle rectangle = new Rectangle(333, 42);
        assertThrows(RuntimeException.class,
                () -> client.primitiveCreateRectangleError(
                        rectangle));
        assertThat(client.findAllRectangles().size()).isEqualTo(3);
        assertThat(client.findAllRectangles()).doesNotContain(rectangle);
    }
}
