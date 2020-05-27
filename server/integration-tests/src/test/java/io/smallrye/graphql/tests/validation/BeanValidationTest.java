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

import io.smallrye.graphql.tests.SimpleGraphQLClient;

@RunWith(Arquillian.class)
@RunAsClient
public class BeanValidationTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "metrics-test.war")
                .addAsResource(new StringAsset("smallrye.graphql.validation.enabled=true"),
                        "META-INF/microprofile-config.properties")
                .addClasses(ValidatingGraphQLApi.class, Person.class);
    }

    @ArquillianResource
    URL testingURL;

    @Test
    public void shouldAcceptValidPersonData() throws Exception {
        SimpleGraphQLClient client = new SimpleGraphQLClient(testingURL);

        String response = client
                .query("mutation {update(person: { firstName: \"Jane\", lastName: \"Doe\", age: 87 }) { firstName }}");

        assertThat(response).isEqualTo("{\"data\":{\"update\":{\"firstName\":\"Jane\"}}}");
    }

    @Test
    public void shouldFailInvalidPersonData() throws Exception {
        SimpleGraphQLClient client = new SimpleGraphQLClient(testingURL);

        String response = client
                .query("mutation {update(person: { firstName: \"*\", lastName: \"\", age: -1 }) { firstName }}");

        assertThat(response)
                .startsWith("{\"errors\":[")
                .contains("{\"message\":\"validation failed: lastName must not be empty\"," +
                /**/"\"locations\":[{\"line\":1,\"column\":18}]," +
                /**/"\"path\":[\"update\",\"person\",\"lastName\"]," +
                /**/"\"extensions\":{" +
                /*  */"\"violation.propertyPath\":[\"lastName\"]," +
                /*  */"\"violation.invalidValue\":\"\"," +
                /*  */"\"violation.constraint\":{" +
                /*    */"\"groups\":[]," +
                /*    */"\"message\":\"{javax.validation.constraints.NotEmpty.message}\"," +
                /*    */"\"payload\":[]" +
                /*  */"}," +
                /**/"\"violation.message\":\"must not be empty\"," +
                /**/"\"classification\":\"ValidationError\"" +
                /**/"}" +
                        "}")
                .contains("{\"message\":\"validation failed: age must be greater than or equal to 0\"," +
                /**/"\"locations\":[{\"line\":1,\"column\":18}]," +
                /**/"\"path\":[\"update\",\"person\",\"age\"]," +
                /**/"\"extensions\":{" +
                /*  */"\"violation.propertyPath\":[\"age\"]," +
                /*  */"\"violation.invalidValue\":-1," +
                /*  */"\"violation.constraint\":{" +
                /*    */"\"groups\":[]," +
                /*    */"\"message\":\"{javax.validation.constraints.PositiveOrZero.message}\"," +
                /*    */"\"payload\":[]" +
                /*  */"}," +
                /*  */"\"violation.message\":\"must be greater than or equal to 0\"," +
                /*  */"\"classification\":\"ValidationError\"" +
                /**/"}" +
                        "}")
                .contains("{\"message\":\"validation failed: firstName must match \\\"\\\\w+\\\"\"," +
                /**/"\"locations\":[{\"line\":1,\"column\":18}]," +
                /**/"\"path\":[\"update\",\"person\",\"firstName\"]," +
                /**/"\"extensions\":{" +
                /*  */"\"violation.propertyPath\":[\"firstName\"]," +
                /*  */"\"violation.invalidValue\":\"*\"," +
                /*  */"\"violation.constraint\":{" +
                /*    */"\"flags\":[]," +
                /*    */"\"groups\":[]," +
                /*    */"\"regexp\":\"\\\\w+\"," +
                /*    */"\"message\":\"{javax.validation.constraints.Pattern.message}\"," +
                /*    */"\"payload\":[]" +
                /*  */"}," +
                /*  */"\"violation.message\":\"must match \\\"\\\\w+\\\"\"," +
                /*  */"\"classification\":\"ValidationError\"" +
                /*  */"}" +
                /**/"}")
                .endsWith("],\"data\":{\"update\":null}}");
    }
}
