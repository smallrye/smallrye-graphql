package io.smallrye.graphql.test.apps.jsonp.api;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

@GraphQLApi
public class JsonPApi {

    @Query("jsonpObject")
    public ObjectNode getRawToken() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("foo", "bar");
        return node;
    }

    @Query("jsonpArray")
    public ArrayNode getRawTokens() {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        array.add("foo");
        array.add("bar");
        return array;
    }

    @Query("jsonpField")
    public Token getToken() {
        Token t = new Token();
        t.name = "foobar";
        t.value = getRawToken();
        return t;
    }
}
