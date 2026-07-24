package io.smallrye.graphql.tests;

import java.net.URL;
import java.util.Map;

import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * Using RestAssured for GraphQL
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class GraphQLAssured {

    private static final String MEDIATYPE_JSON = "application/json";
    private static final String MEDIATYPE_GRAPHQL = "application/graphql";
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();
    private static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance;

    protected URL testingURL;

    public GraphQLAssured(URL base) {
        this.testingURL = base;
        RestAssured.baseURI = testingURL.toString();
    }

    public String get(String queryparams) {
        return RestAssured.given().when()
                .accept(MEDIATYPE_JSON)
                .contentType(MEDIATYPE_JSON)
                .get("/graphql?" + queryparams)
                .then()
                .assertThat()
                .statusCode(200)
                .extract().asString();
    }

    public String post(String request) {
        return post(request, null, null);
    }

    public String post(String request, Map<String, ?> queryparams) {
        return post(request, queryparams, null);
    }

    public String post(String request, Map<String, ?> queryparams, String variables) {
        RequestSpecification requestSpecification = RestAssured.given();
        if (queryparams != null) {
            requestSpecification = requestSpecification.queryParams(queryparams);
        }

        return requestSpecification.when()
                .accept(MEDIATYPE_JSON)
                .contentType(MEDIATYPE_JSON)
                .body(getPayload(request, variables))
                .post("/graphql")
                .then()
                .assertThat()
                .statusCode(200)
                .extract().asString();
    }

    public String postAsGraphQL(String request, Map<String, ?> queryparams) {
        RequestSpecification requestSpecification = RestAssured.given().config(RestAssured.config()
                .encoderConfig(EncoderConfig.encoderConfig().encodeContentTypeAs(MEDIATYPE_GRAPHQL, ContentType.TEXT)))
                .contentType(MEDIATYPE_GRAPHQL);
        if (queryparams != null) {
            requestSpecification = requestSpecification.queryParams(queryparams);
        }

        return requestSpecification.when()
                .accept(MEDIATYPE_JSON)
                .contentType(MEDIATYPE_GRAPHQL)
                .body(request) // This is the important part.
                .post("/graphql")
                .then()
                .assertThat()
                .statusCode(200)
                .extract().asString();

    }

    private String getPayload(String query, String variables) {
        ObjectNode jsonObject = createRequestBody(query, variables);
        try {
            return OBJECT_MAPPER.writeValueAsString(jsonObject);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    private ObjectNode createRequestBody(String graphQL, String variables) {
        ObjectNode vjo = NODE_FACTORY.objectNode();

        if (variables != null && !variables.isEmpty()) {
            vjo = (ObjectNode) OBJECT_MAPPER.readTree(variables);
        }

        ObjectNode job = NODE_FACTORY.objectNode();
        if (graphQL != null && !graphQL.isEmpty()) {
            job.put(QUERY, graphQL);
        }

        job.set(VARIABLES, vjo);
        return job;
    }

    private static final String QUERY = "query";
    private static final String VARIABLES = "variables";

}
