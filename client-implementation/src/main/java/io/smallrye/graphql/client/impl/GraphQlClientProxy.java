package io.smallrye.graphql.client.impl;

import static io.smallrye.graphql.client.impl.CollectionUtils.toMultivaluedMap;
import static java.util.stream.Collectors.joining;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static lombok.AccessLevel.PACKAGE;

import java.io.StringReader;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import io.smallrye.graphql.client.api.GraphQlClientException;
import io.smallrye.graphql.client.api.GraphQlClientHeader;
import io.smallrye.graphql.client.impl.json.JsonReader;
import io.smallrye.graphql.client.impl.reflection.FieldInfo;
import io.smallrye.graphql.client.impl.reflection.MethodInfo;
import io.smallrye.graphql.client.impl.reflection.TypeInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = PACKAGE)
class GraphQlClientProxy {

    private final WebTarget target;
    private final List<GraphQlClientHeader> headers;

    Object invoke(MethodInfo method) {
        String request = request(method);

        log.info("request graphql: {}", request);
        String response = post(request);
        log.info("response graphql: {}", response);

        return fromJson(method, request, response);
    }

    private String request(MethodInfo method) {
        JsonObjectBuilder request = Json.createObjectBuilder();
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
        while (type.isOptional())
            type = type.getItemType();
        if (type.isScalar()) {
            return "";
        } else if (type.isCollection()) {
            return fields(type.getItemType());
        } else {
            return type.fields()
                    .map(this::field)
                    .collect(joining(" ", " {", "}"));
        }
    }

    private String field(FieldInfo field) {
        TypeInfo type = field.getType();
        if (type.isScalar() || type.isCollection() && type.getItemType().isScalar()) {
            return field.getName();
        } else {
            return field.getName() + fields(type);
        }
    }

    private String post(String request) {
        Response response = target
                .request(APPLICATION_JSON_TYPE)
                .headers(buildHeaders())
                .post(Entity.json(request));
        StatusType status = response.getStatusInfo();
        if (status.getFamily() != SUCCESSFUL)
            throw new GraphQlClientException("expected successful status code but got " +
                    status.getStatusCode() + " " + status.getReasonPhrase() + ":\n" +
                    response.readEntity(String.class));
        return response.readEntity(String.class);
    }

    private MultivaluedMap<String, Object> buildHeaders() {
        return headers.stream()
                .peek(header -> log.debug("add header '{}'", header.getName())) // don't log values; could contain tokens
                .map(GraphQlClientHeader::toEntry)
                .collect(toMultivaluedMap());
    }

    private Object fromJson(MethodInfo method, String request, String response) {
        JsonObject responseJson = readResponse(request, response);
        JsonValue value = getData(method, responseJson);
        return JsonReader.readFrom(method, value);
    }

    private JsonObject readResponse(String request, String response) {
        JsonObject responseJson = Json.createReader(new StringReader(response)).readObject();
        if (responseJson.containsKey("errors") && !responseJson.isNull("errors"))
            throw new GraphQlClientException("errors from service: " + responseJson.getJsonArray("errors") + ":\n  " + request);
        return responseJson;
    }

    private JsonValue getData(MethodInfo method, JsonObject responseJson) {
        JsonObject data = responseJson.getJsonObject("data");
        if (!data.containsKey(method.getName()))
            throw new GraphQlClientException("no data for '" + method.getName() + "':\n  " + data);
        return data.get(method.getName());
    }
}
