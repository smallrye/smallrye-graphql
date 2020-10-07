package io.smallrye.graphql.client.typesafe.impl;

import static java.util.stream.Collectors.joining;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;

import java.io.StringReader;
import java.util.Stack;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReaderFactory;
import javax.json.JsonValue;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientException;
import io.smallrye.graphql.client.typesafe.impl.json.JsonReader;
import io.smallrye.graphql.client.typesafe.impl.reflection.FieldInfo;
import io.smallrye.graphql.client.typesafe.impl.reflection.MethodInfo;
import io.smallrye.graphql.client.typesafe.impl.reflection.TypeInfo;

class GraphQlClientProxy {
    private static final Logger log = LoggerFactory.getLogger(GraphQlClientProxy.class);

    private static final JsonBuilderFactory jsonObjectFactory = Json.createBuilderFactory(null);
    private static final JsonReaderFactory jsonReaderFactory = Json.createReaderFactory(null);

    private final WebTarget target;
    private final Stack<String> typeStack = new Stack<>();

    GraphQlClientProxy(WebTarget target) {
        this.target = target;
    }

    Object invoke(Class<?> api, MethodInfo method) {
        MultivaluedMap<String, Object> headers = new HeaderBuilder(api, method).build();
        String request = request(method);

        log.info("request graphql: {}", request);
        String response = post(request, headers);
        log.info("response graphql: {}", response);

        return fromJson(method, request, response);
    }

    private String request(MethodInfo method) {
        JsonObjectBuilder request = jsonObjectFactory.createObjectBuilder();
        request.add("query",
                operation(method)
                        + " { "
                        + new RequestBuilder(method).build()
                        + fields(method.getReturnType())
                        + " }");
        return request.build().toString();
    }

    private String operation(MethodInfo method) {
        return method.isQuery() ? "query" : "mutation";
    }

    private String fields(TypeInfo type) {
        if (typeStack.contains(type.getTypeName()))
            throw new GraphQlClientException("field recursion found");
        try {
            typeStack.push(type.getTypeName());

            return recursionCheckedFields(type);
        } finally {
            typeStack.pop();
        }
    }

    private String recursionCheckedFields(TypeInfo type) {
        while (type.isOptional())
            type = type.getItemType();

        if (type.isScalar())
            return "";
        if (type.isCollection())
            return fields(type.getItemType());
        return type.fields()
                .map(this::field)
                .collect(joining(" ", " {", "}"));
    }

    private String field(FieldInfo field) {
        TypeInfo type = field.getType();
        if (type.isScalar() || type.isCollection() && type.getItemType().isScalar()) {
            return field.getName();
        } else {
            return field.getName() + fields(type);
        }
    }

    private String post(String request, MultivaluedMap<String, Object> headers) {
        Response response = target
                .request(APPLICATION_JSON_TYPE)
                .headers(headers)
                .post(Entity.json(request));
        StatusType status = response.getStatusInfo();
        if (status.getFamily() != SUCCESSFUL)
            throw new GraphQlClientException("expected successful status code but got " +
                    status.getStatusCode() + " " + status.getReasonPhrase() + ":\n" +
                    response.readEntity(String.class));
        return response.readEntity(String.class);
    }

    private Object fromJson(MethodInfo method, String request, String response) {
        JsonObject responseJson = readResponse(request, response);
        JsonValue value = getData(method, responseJson);
        return JsonReader.readFrom(method, value);
    }

    private JsonObject readResponse(String request, String response) {
        JsonObject responseJson = jsonReaderFactory.createReader(new StringReader(response)).readObject();
        if (responseJson.containsKey("errors") && !isEmpty(responseJson.getJsonArray("errors")))
            throw new GraphQlClientException("errors from service: " + responseJson.getJsonArray("errors") + ":\n  " + request);
        return responseJson;
    }

    private boolean isEmpty(JsonArray array) {
        return array == null || array.isEmpty();
    }

    private JsonValue getData(MethodInfo method, JsonObject responseJson) {
        JsonObject data = responseJson.getJsonObject("data");
        if (!data.containsKey(method.getName()))
            throw new GraphQlClientException("no data for '" + method.getName() + "':\n  " + data);
        return data.get(method.getName());
    }
}
