package io.smallrye.graphql.tests.customscalars;

import static org.assertj.core.api.Assertions.assertThat;

import io.smallrye.graphql.tests.GraphQLAssured;
import java.net.URL;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * TODO bdupras these tests currently only focus on inputs. More tests are needed to
 *   be complete. A full suite would test all the combinations of:
 *     - Input vs output
 *     - Standalone scalar vs an object field
 *     - Singular vs a List
 *     - Nullable vs non-nullable
 *     - Null value vs non-null value
 *     - Default value vs no default value
 */
@RunWith(Arquillian.class)
@RunAsClient
public class CustomScalarTest {

  @Deployment
  public static WebArchive deployment() {
    return ShrinkWrap.create(WebArchive.class, "customscalar-test.war")
        .addClasses(SomeApi.class, BigDecimalString.class);
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
}
