package io.smallrye.graphql.test.apps.jsonp.api;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

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
