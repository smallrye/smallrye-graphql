package io.smallrye.graphql.tests.optional;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.smallrye.graphql.tests.GraphQLAssured;

@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class OptionalNumberTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "optionalNumber-test.war")
                .addClasses(Book.class, BookService.class, BookResources.class);
    }

    @ArquillianResource
    URL testingURL;

    @BeforeEach
    public void resetDatabase() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String response = graphQLAssured
                .post("mutation { resetBooks }");
    }

    @Test
    public void optionalIntAsAnOutput() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        String response = graphQLAssured
                .post("query Foo { books { name\n yearOfRelease\n pageCount } }");
        assertThat(response).isEqualTo("{\"data\":{\"books\":[{\"name\":" +
                "\"BOTR\",\"yearOfRelease\":1959,\"pageCount\":1400},{\"name\":\"" +
                "LOTR\",\"yearOfRelease\":1954,\"pageCount\":400},{\"name\":\"HOB" +
                "IT\",\"yearOfRelease\":1953,\"pageCount\":300}]}}");
    }

    @Test
    public void optionalIntAsInputParameter() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        String response = graphQLAssured
                .post("query Bar { book0: bookByPageCount(pageCount: 400) { name\n yearOfRelease\n pageCount } }");
        assertThat(response).isEqualTo("{\"data\":{\"book0\":{\"name\":\"LOTR\",\"yearOfRelease\":1954,\"pageCount\":400}}}");
    }

    @Test
    public void optionalLongAsAnOutput() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        String response = graphQLAssured
                .post("query Lorem { books { name\n yearOfRelease\n sales } }");
        assertThat(response).isEqualTo("{\"data\":{\"books\":[{\"name\":\"BOTR" +
                "\",\"yearOfRelease\":1959,\"sales\":1133424341},{\"name\":\"LOTR\",\"y" +
                "earOfRelease\":1954,\"sales\":2053224341},{\"name\":\"HOBIT\",\"yearOf" +
                "Release\":1953,\"sales\":132568448}]}}");
    }

    @Test
    public void optionalLongAsInputParameter() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        String response = graphQLAssured
                .post("query Bar { book0: bookBySales(sales: 2053224341) { name\n sales} }");
        assertThat(response).isEqualTo("{\"data\":{\"book0\":{\"name\":\"LOTR\",\"sales\":2053224341}}}");
    }

    @Test
    public void optionalDoubleAsAnOutput() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        String response = graphQLAssured
                .post("query Lorem { books { name\n yearOfRelease\n rating } }");
        assertThat(response).isEqualTo("{\"data\":{\"books\":[{\"name\":\"BOTR" +
                "\",\"yearOfRelease\":1959,\"rating\":4.5},{\"name\":\"LOTR\",\"y" +
                "earOfRelease\":1954,\"rating\":2.3},{\"name\":\"HOBIT\",\"yearOf" +
                "Release\":1953,\"rating\":5.0}]}}");
    }

    @Test
    public void optionalDoubleAsInputParameter() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        String response = graphQLAssured
                .post("query Ipsum { book2: bookByRating(rating: 2.30000) { name\n rating } }");
        assertThat(response).isEqualTo("{\"data\":{\"book2\":{\"name\":\"LOTR\",\"rating\":2.3}}}");
    }

    @Test
    public void optionalCreateMutation() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        String response = graphQLAssured
                .post("mutation addBook {\n createBook (book: { name: \"batman\"," +
                        "yearOfRelease: 1934, pageCount: 435, sales: 1213123, rating: " +
                        "5.000 }) { name yearOfRelease pageCount sales rating }} ");
        assertThat(response).isEqualTo(
                "{\"data\":{\"createBook\":{\"name\":\"batman\",\"yearOfRelease\":1934,\"pageCount\":435,\"sales\":1213123,\"rating\":5.0}}}");
        String check = graphQLAssured
                .post("query { books { name yearOfRelease pageCount sales rating } }");
        // ------
        assertThat(check).doesNotContain("\"errors\"");
        assertThat(check).contains("batman");
        // ------
    }

    @Test
    public void optionalDeleteMutation() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String response = graphQLAssured
                .post("mutation deleteBook { deleteBookByPageCount(pageCount: 1400) { name yearOfRelease pageCount sales rating } }");
        assertThat(response).isEqualTo(
                "{\"data\":{\"deleteBookByPageCount\":{\"name\":\"BOTR\",\"yearOfRelease\":1959,\"pageCount\":1400,\"sales\":1133424341,\"rating\":4.5}}}");
        String check = graphQLAssured
                .post("query { books { name yearOfRelease pageCount sales rating } }");
        // ------
        assertThat(check).doesNotContain("\"errors\"");
        assertThat(check).doesNotContain("BOTR");
        // ------
    }
}
