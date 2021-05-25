package io.smallrye.graphql.tests;

import java.io.StringReader;
import java.net.URL;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Using RestAssured for GraphQL
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class GraphQLAssured {

    private static final String MEDIATYPE_JSON = "application/json";
    private static final String MEDIATYPE_GRAPHQL = "application/graphql";

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
        JsonObject jsonObject = createRequestBody(query, variables);
        return jsonObject.toString();
    }

    private JsonObject createRequestBody(String graphQL, String variables) {
        // Create the request

        JsonObject vjo = Json.createObjectBuilder().build();

        if (variables != null && !variables.isEmpty()) {
            try (JsonReader jsonReader = Json.createReader(new StringReader(variables))) {
                vjo = jsonReader.readObject();
            }
        }

        JsonObjectBuilder job = Json.createObjectBuilder();
        if (graphQL != null && !graphQL.isEmpty()) {
            job.add(QUERY, graphQL);
        }

        return job.add(VARIABLES, vjo).build();
    }

    private static final String QUERY = "query";
    private static final String VARIABLES = "variables";

}
