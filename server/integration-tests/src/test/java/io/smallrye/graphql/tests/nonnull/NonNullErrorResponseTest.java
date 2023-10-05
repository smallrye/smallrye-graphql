package io.smallrye.graphql.tests.nonnull;

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
public class NonNullErrorResponseTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "nonNull-test.war")
                .addClasses(SomeApi.class);
    }

    @ArquillianResource
    URL testingURL;

    @Test
    public void queryShouldNotReturnNonNullError() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        String response = graphQLAssured
                .post("{ echoNumber(number: \"something\") }");
        assertThat(response).contains("Can not parse a number from [StringValue{value='something'}]");
        assertThat(response).doesNotContain("NullValueInNonNullableField");

        response = graphQLAssured
                .post("{ echoMessage(message: 314159) }");

        assertThat(response)
                .contains("argument 'message' with value 'IntValue{value=314159}' is not a valid 'String'")
                .doesNotContain("NullValueInNonNullableField");
    }

    @Test
    public void mutationShouldNotReturnNonNullError() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        String response = graphQLAssured
                .post("mutation { add(a: \"one\", b: \"two\") }");
        assertThat(response).contains("Can not parse a number from [StringValue{value='one'}]")
                .contains("Can not parse a number from [StringValue{value='two'}]")
                .doesNotContain("NullValueInNonNullableField");
    }

    @Test
    public void queryShouldReturnNonNullError() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String response = graphQLAssured
                .post("{echoBigDecimal}");
        assertThat(response).contains("NullValueInNonNullableField");
    }

}
