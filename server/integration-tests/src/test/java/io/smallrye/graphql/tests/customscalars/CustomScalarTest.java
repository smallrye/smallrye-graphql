package io.smallrye.graphql.tests.customscalars;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.tests.GraphQLAssured;

@RunWith(Arquillian.class)
@RunAsClient
public class CustomScalarTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "customscalar-test.war")
                .addClasses(SomeApi.class, BigDecimalString.class, TwiceTheFloat.class);
    }

    @ArquillianResource
    URL testingURL;

    @Test
    public void inAsScalarNullableTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        assertThat(graphQLAssured
                .post("query { inAsScalarNullable(scalar: \"1234567890.987654321\") }"))
                .contains("{\"data\":{\"inAsScalarNullable\":\"1234567890.987654321\"}}")
                .doesNotContain("error");
        assertThat(graphQLAssured
                .post("query { inAsFScalarNullable(fScalar: 10.0) }"))
                .contains("{\"data\":{\"inAsFScalarNullable\":10.0}}")
                .doesNotContain("error");

        assertThat(graphQLAssured
                .post("query { inAsScalarNullable(scalar: null) }"))
                .contains("{\"data\":{\"inAsScalarNullable\":null}}")
                .doesNotContain("error");

        assertThat(graphQLAssured
                .post("query { inAsScalarNullable }"))
                .contains("{\"data\":{\"inAsScalarNullable\":null}}")
                .doesNotContain("error");
    }

    @Test
    public void inAsScalarNullableDefaultNonNullTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        assertThat(graphQLAssured
                .post("query { inAsScalarNullableDefaultNonNull }"))
                .contains("{\"data\":{\"inAsScalarNullableDefaultNonNull\":\"1234567.89\"}}")
                .doesNotContain("error");
    }

    @Test
    public void inAsScalarNullableDefaultNullTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        assertThat(graphQLAssured
                .post("query { inAsScalarNullableDefaultNull }"))
                .contains("{\"data\":{\"inAsScalarNullableDefaultNull\":null}}")
                .doesNotContain("error");
    }

    @Test
    public void inAsScalarRequiredTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        assertThat(graphQLAssured
                .post("query { inAsScalarRequired(scalar: \"1234567890.987654321\") }"))
                .contains("{\"data\":{\"inAsScalarRequired\":\"1234567890.987654321\"}}")
                .doesNotContain("error");

        assertThat(graphQLAssured
                .post("query { inAsScalarRequired(scalar: null) }"))
                .contains("NullValueForNonNullArgument@[inAsScalarRequired]")
                .contains("\"data\":null");

        assertThat(graphQLAssured
                .post("query { inAsScalarRequired }"))
                .contains("MissingFieldArgument@[inAsScalarRequired]")
                .contains("\"data\":null");
    }

    @Test
    public void inAsScalarRequiredDefaultTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        assertThat(graphQLAssured
                .post("query { inAsScalarRequiredDefault }"))
                .contains("{\"data\":{\"inAsScalarRequiredDefault\":\"1234567.89\"}}")
                .doesNotContain("error");

    }

    @Test
    public void inAsScalarListNullableTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        assertThat(graphQLAssured
                .post("query { inAsScalarListNullable(scalars: [\"1234567890.987654321\"]) }"))
                .contains("{\"data\":{\"inAsScalarListNullable\":[\"1234567890.987654321\"]}}")
                .doesNotContain("error");

        assertThat(graphQLAssured
                .post("query { inAsScalarListNullable(scalars: [null]) }"))
                .contains("{\"data\":{\"inAsScalarListNullable\":[null]}}")
                .doesNotContain("error");
    }

    @Test
    public void inAsScalarListRequiredTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        assertThat(graphQLAssured
                .post("query { inAsScalarListRequired(scalars: [\"1234567890.987654321\"]) }"))
                .contains("{\"data\":{\"inAsScalarListRequired\":[\"1234567890.987654321\"]}}")
                .doesNotContain("error");

        assertThat(graphQLAssured
                .post("query { inAsScalarListRequired(scalars: [null]) }"))
                .contains("WrongType@[inAsScalarListRequired]")
                .contains("must not be null")
                .contains("\"data\":null");
    }

    @Test
    public void inAsFieldNullableTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        assertThat(graphQLAssured
                .post("query { inAsFieldNullable(input: { scalar: \"1234567890.987654321\" } ) }"))
                .contains("{\"data\":{\"inAsFieldNullable\":\"1234567890.987654321\"}}")
                .doesNotContain("error");

        assertThat(graphQLAssured
                .post("query { inAsFieldNullable(input: { scalar: null } ) }"))
                .contains("{\"data\":{\"inAsFieldNullable\":null}}")
                .doesNotContain("error");

        assertThat(graphQLAssured
                .post("query { inAsFieldNullable(input: { } ) }"))
                .contains("{\"data\":{\"inAsFieldNullable\":null}}")
                .doesNotContain("error");
    }

    @Test
    public void inAsFloatFieldNullableTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        assertThat(graphQLAssured
                .post("query { inAsFFieldNullable(input: { fScalar: 20.0 } ) }"))
                .contains("{\"data\":{\"inAsFFieldNullable\":20.0}}")
                .doesNotContain("error");

        assertThat(graphQLAssured
                .post("query { inAsFFieldNullable(input: { fScalar: null } ) }"))
                .contains("{\"data\":{\"inAsFFieldNullable\":null}}")
                .doesNotContain("error");

        assertThat(graphQLAssured
                .post("query { inAsFFieldNullable(input: { } ) }"))
                .contains("{\"data\":{\"inAsFFieldNullable\":null}}")
                .doesNotContain("error");
    }

    @Test
    public void outputTwiceTheFloatTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        assertThat(graphQLAssured
                .post("query { outputFloat }"))
                .contains("{\"data\":{\"outputFloat\":10.0}}")
                .doesNotContain("error");
    }

    @Test
    public void outputScalarObjectTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        assertThat(graphQLAssured
                .post("query { outputScalars {fScalar, sScalar}}"))
                .contains("{\"data\":{\"outputScalars\":{\"fScalar\":30.0,\"sScalar\":\"98765.56789\"}}}")
                .doesNotContain("error");
    }
}
