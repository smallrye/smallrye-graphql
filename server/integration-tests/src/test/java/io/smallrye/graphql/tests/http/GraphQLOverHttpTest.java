package io.smallrye.graphql.tests.http;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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
public class GraphQLOverHttpTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "over-http-test.war")
                .addAsResource(new StringAsset("smallrye.graphql.allowGet=true\n"
                        + "smallrye.graphql.allowPostWithQueryParameters=true"),
                        "META-INF/microprofile-config.properties")
                .addClasses(GraphQLOverHttpApi.class, User.class);
    }

    @ArquillianResource
    URL testingURL;

    @Test
    public void httpGetTest() throws Exception {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        String response = graphQLAssured
                .get("query=query(%24id%3A%20ID!)%7Buser(id%3A%24id)%7Bname%7D%7D&variables=%7B%22id%22%3A%22QVBJcy5ndXJ1%22%7D");

        assertThat(response).isEqualTo("{\"data\":{\"user\":{\"name\":\"Koos\"}}}");
    }

    @Test
    public void httpPostWithQueryInQueryParamTest() throws Exception {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        Map<String, String> queryparams = new HashMap<>();
        queryparams.put("query", "query ($id: ID!) {  user(id:$id) {    id    name  surname}}");

        String variables = "{\"id\": \"1\"}";

        String response = graphQLAssured
                .post(null, queryparams, variables);

        assertThat(response).isEqualTo("{\"data\":{\"user\":{\"id\":\"1\",\"name\":\"Koos\",\"surname\":\"van der Merwe\"}}}"); // query in query parameter
    }

    @Test
    public void httpPostWithQueryInQueryParamAndBodyTest() throws Exception {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        Map<String, String> queryparams = new HashMap<>();
        queryparams.put("query", "query ($id: ID!) {  user(id:$id) {    id    name  surname}}");

        String variables = "{\"id\": \"1\"}";

        String request = "query ($id: ID!) {\n" +
                "  user(id:$id) {\n" +
                "    name\n" +
                "  }\n" +
                "}";

        String response = graphQLAssured
                .post(request, queryparams, variables);

        assertThat(response).isEqualTo("{\"data\":{\"user\":{\"id\":\"1\",\"name\":\"Koos\",\"surname\":\"van der Merwe\"}}}"); // query in query parameter win
    }

    @Test
    public void httpPostWithVariablesQueryParamTest() throws Exception {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        String request = "query ($id: ID!) {\n" +
                "  user(id:$id) {\n" +
                "    id\n" +
                "    name\n" +
                "  }\n" +
                "}";

        String variables = "{\"id\": \"1\"}";

        Map<String, String> queryparams = new HashMap<>();
        queryparams.put("variables", "{\"id\": \"QVBJcy5ndXJ1\"}");

        String response = graphQLAssured
                .post(request, queryparams, variables);

        assertThat(response).isEqualTo("{\"data\":{\"user\":{\"id\":\"QVBJcy5ndXJ1\",\"name\":\"Koos\"}}}"); // id in query parameter win
    }

    @Test
    public void httpPostWithContentTypeHeader() throws Exception {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        Map<String, String> queryparams = new HashMap<>();
        queryparams.put("variables", "{\"id\": \"QVBJcy5ndXJ1\"}");

        String request = "query ($id: ID!) {\n" +
                "  user(id:$id) {\n" +
                "    id\n" +
                "    name\n" +
                "    surname\n" +
                "  }\n" +
                "}";

        String response = graphQLAssured
                .postAsGraphQL(request, queryparams);

        assertThat(response)
                .isEqualTo("{\"data\":{\"user\":{\"id\":\"QVBJcy5ndXJ1\",\"name\":\"Koos\",\"surname\":\"van der Merwe\"}}}");
    }

}
