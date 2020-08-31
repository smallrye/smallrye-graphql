package io.smallrye.graphql.test.integration;

import java.net.URL;

import javax.json.Json;
import javax.json.JsonObject;

import io.restassured.RestAssured;

/**
 * Using RestAssured for GraphQL
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class GraphQLAssured {

    private static final String MEDIATYPE_JSON = "application/json";

    protected URL testingURL;

    public GraphQLAssured(URL base) {
        this.testingURL = base;
        RestAssured.baseURI = testingURL.toString();
    }

    public String post(String request) {
        return RestAssured.given().when()
                .accept(MEDIATYPE_JSON)
                .contentType(MEDIATYPE_JSON)
                .body(getPayload(request))
                .post("/graphql")
                .then()
                .assertThat()
                .statusCode(200)
                .extract().asString();
    }

    private String getPayload(String query) {
        JsonObject jsonObject = createRequestBody(query);
        return jsonObject.toString();
    }

    private JsonObject createRequestBody(String graphQL) {
        return createRequestBody(graphQL, null);
    }

    private JsonObject createRequestBody(String graphQL, JsonObject variables) {
        // Create the request
        if (variables == null || variables.isEmpty()) {
            variables = Json.createObjectBuilder().build();
        }
        return Json.createObjectBuilder().add(QUERY, graphQL).add(VARIABLES, variables).build();
    }

    private static final String QUERY = "query";
    private static final String VARIABLES = "variables";
}
