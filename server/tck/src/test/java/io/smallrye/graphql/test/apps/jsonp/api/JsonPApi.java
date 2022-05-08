package io.smallrye.graphql.test.apps.jsonp.api;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class JsonPApi {

    @Query("jsonpObject")
    public JsonObject getRawToken() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("foo", "bar");
        return builder.build();
    }

    @Query("jsonpArray")
    public JsonArray getRawTokens() {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        builder.add("foo");
        builder.add("bar");
        return builder.build();
    }

    @Query("jsonpField")
    public Token getToken() {
        Token t = new Token();
        t.name = "foobar";
        t.value = getRawToken();
        return t;
    }
}
