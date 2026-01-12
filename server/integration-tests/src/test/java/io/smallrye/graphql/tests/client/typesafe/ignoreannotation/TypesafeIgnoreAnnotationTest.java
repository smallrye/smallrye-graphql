package io.smallrye.graphql.tests.client.typesafe.ignoreannotation;

import java.math.BigDecimal;
import java.net.URL;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.smallrye.graphql.client.vertx.typesafe.VertxTypesafeGraphQLClientBuilder;
import io.smallrye.graphql.tests.client.typesafe.ignoreannotation.clientmodels.Person;

@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class TypesafeIgnoreAnnotationTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "annotationIgnore.war")
                .addClasses(IgnoreApi.class,
                        io.smallrye.graphql.tests.client.typesafe.ignoreannotation.servermodels.Person.class);
    }

    @ArquillianResource
    URL testingURL;

    protected IgnoreClientApi client;

    @BeforeEach
    public void prepare() {
        client = new VertxTypesafeGraphQLClientBuilder()
                .endpoint(testingURL.toString() + "graphql")
                .build(IgnoreClientApi.class);
    }

    @Test
    public void ignoreAnnotationsTest() {
        Person person = client.person();
        MatcherAssert.assertThat(person, Matchers.equalTo(
                new Person(BigDecimal.valueOf(314_159_265),
                        null,
                        null,
                        null,
                        null)));
    }

}
