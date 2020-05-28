package io.smallrye.graphql.tests.validation;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;

import javax.json.bind.JsonbBuilder;
import javax.json.bind.annotation.JsonbProperty;

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
import io.smallrye.graphql.tests.validation.BeanValidationTest.BeanValidationUpdateTestResponse.ResponseError;
import io.smallrye.graphql.tests.validation.BeanValidationTest.BeanValidationUpdateTestResponse.ResponseError.Location;
import io.smallrye.graphql.tests.validation.BeanValidationTest.BeanValidationUpdateTestResponse.ResponseError.ValidationExtensions;
import io.smallrye.graphql.tests.validation.BeanValidationTest.BeanValidationUpdateTestResponse.ResponseError.ValidationExtensions.ViolationConstraint;

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

        BeanValidationUpdateTestResponse response = JsonbBuilder.create().fromJson(client
                .query("mutation {update(person: { firstName: \"*\", lastName: \"\", age: -1 }) { firstName }}"),
                BeanValidationUpdateTestResponse.class);

        assertThat(response.data.update).isNull();
        List<Location> locations = asList(new Location(1, 11), new Location(1, 18));
        assertThat(response.errors).usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                        new ResponseError("validation failed: update.arg0.firstName must match \"\\w+\"", locations,
                                asList("update", "person"),
                                new ValidationExtensions("must match \"\\w+\"", asList("update", "arg0", "firstName"), "*",
                                        new ViolationConstraint("{javax.validation.constraints.Pattern.message}", "\\w+"))),
                        new ResponseError("validation failed: update.arg0.lastName must not be empty", locations,
                                asList("update", "person"),
                                new ValidationExtensions("must not be empty", asList("update", "arg0", "lastName"), "",
                                        new ViolationConstraint("{javax.validation.constraints.NotEmpty.message}", null))),
                        new ResponseError("validation failed: update.arg0.age must be greater than or equal to 0", locations,
                                asList("update", "person"),
                                new ValidationExtensions("must be greater than or equal to 0", asList("update", "arg0", "age"),
                                        new BigDecimal(-1),
                                        new ViolationConstraint("{javax.validation.constraints.PositiveOrZero.message}",
                                                null))));
    }

    public static class BeanValidationUpdateTestResponse {
        public Data data;
        public List<ResponseError> errors;

        public static class Data {
            public String update;
        }

        public static class ResponseError {
            public String message;
            public List<Location> locations;
            public List<String> path;
            public ValidationExtensions extensions;

            public ResponseError() {
            }

            public ResponseError(String message, List<Location> locations, List<String> path, ValidationExtensions extensions) {
                this.message = message;
                this.locations = locations;
                this.path = path;
                this.extensions = extensions;
            }

            @Override
            public String toString() {
                return "ResponseError{message:'" + message + "', locations:" + locations + ", path:" + path + ", extensions:"
                        + extensions + "}";
            }

            public static class Location {
                public int line, column;

                public Location() {
                }

                public Location(int line, int column) {
                    this.line = line;
                    this.column = column;
                }

                @Override
                public String toString() {
                    return "line:" + line + ", column:" + column;
                }
            }

            public static class ValidationExtensions {
                public String classification;

                @JsonbProperty("violation.message")
                public String message;

                @JsonbProperty("violation.propertyPath")
                public List<String> path;

                @JsonbProperty("violation.invalidValue")
                public Object invalidValue;

                @JsonbProperty("violation.constraint")
                public ViolationConstraint constraint;

                public ValidationExtensions() {
                }

                public ValidationExtensions(String message, List<String> path, Object invalidValue,
                        ViolationConstraint constraint) {
                    this.constraint = constraint;
                    this.classification = "ValidationError";
                    this.message = message;
                    this.path = path;
                    this.invalidValue = invalidValue;
                }

                @Override
                public String toString() {
                    return "{message:" + message + ", path:" + path + ", invalidValue:" + invalidValue + "}";
                }

                public static class ViolationConstraint {
                    public String message;
                    // groups:[]
                    // payload:[]
                    public String regexp;
                    // flags:[]

                    public ViolationConstraint() {
                    }

                    public ViolationConstraint(String message, String regexp) {
                        this.regexp = regexp;
                        this.message = message;
                    }
                }
            }
        }
    }
}
