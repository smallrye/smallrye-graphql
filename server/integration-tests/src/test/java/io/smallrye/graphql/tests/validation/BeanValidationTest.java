package io.smallrye.graphql.tests.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.tests.GraphQLAssured;

@RunWith(Arquillian.class)
@RunAsClient
public class BeanValidationTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "validation-test.war")
                .addAsResource(new StringAsset("smallrye.graphql.validation.enabled=true"),
                        "META-INF/microprofile-config.properties")
                .addClasses(ValidatingGraphQLApi.class, Person.class);
    }

    @ArquillianResource
    URL testingURL;

    @Test
    public void shouldAcceptValidPersonData() throws Exception {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        String response = graphQLAssured
                .post("mutation {update(person: { firstName: \"Jane\", lastName: \"Doe\", age: 87 }) { firstName }}");

        assertThat(response).isEqualTo("{\"data\":{\"update\":{\"firstName\":\"Jane\"}}}");
    }

    @Test
    public void shouldFailInvalidPersonData() throws Exception {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        String response = graphQLAssured
                .post("mutation {update(person: { firstName: \"*\", lastName: \"\", age: -1 }) { firstName }}");

        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();

        assertThat(response).contains("validation failed: update.person.firstName must match");
        assertThat(response).contains("validation failed: update.person.lastName must not be empty");
        assertThat(response).contains("validation failed: update.person.age must be greater than or equal to 0");

    }
}
