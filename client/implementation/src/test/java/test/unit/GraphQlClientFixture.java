package test.unit;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.StringReader;
import java.net.URI;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientBuilder;
import io.smallrye.graphql.client.typesafe.impl.GraphQlClientBuilderImpl;

class GraphQlClientFixture {
    private final Client mockClient = mock(Client.class);
    private final WebTarget mockWebTarget = mock(WebTarget.class);
    private final Invocation.Builder mockInvocationBuilder = mock(Invocation.Builder.class);
    private Response response;
    private Entity<JsonObject> entitySent;

    GraphQlClientFixture() {
        given(mockClient.target(any(URI.class))).willReturn(mockWebTarget);
        given(mockWebTarget.request(any(MediaType.class))).willReturn(mockInvocationBuilder);
        given(mockInvocationBuilder.headers(any())).willReturn(mockInvocationBuilder);
        given(mockInvocationBuilder.post(any())).will(i -> response);
    }

    GraphQlClientBuilder builder() {
        return builderWithoutEndpointConfig().endpoint("urn:dummy-endpoint");
    }

    GraphQlClientBuilder builderWithoutEndpointConfig() {
        GraphQlClientBuilderImpl impl = (GraphQlClientBuilderImpl) GraphQlClientBuilder.newBuilder();
        impl.client(mockClient);
        return impl;
    }

    void returnsData(String data) {
        returns(Response.ok("{\"data\":{" + data.replace('\'', '\"') + "}}"));
    }

    void returns(ResponseBuilder response) {
        this.response = response.build();
    }

    String variables() {
        return rawVariables().replace('\"', '\'');
    }

    String rawVariables() {
        JsonObject variables = entitySent().getEntity().getJsonObject("variables");
        return String.valueOf(variables);
    }

    String operationName() {
        return entitySent().getEntity().getString("operationName", "null");
    }

    String query() {
        return queryBody(entitySent().getEntity(), "query").replace('\"', '\'');
    }

    String mutation() {
        return queryBody(entitySent().getEntity(), "mutation").replace('\"', '\'');
    }

    private Entity<JsonObject> entitySent() {
        if (entitySent == null) {
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Entity<String>> captor = ArgumentCaptor.forClass(Entity.class);
            BDDMockito.then(mockInvocationBuilder).should().post(captor.capture());
            Entity<String> stringEntity = captor.getValue();
            JsonObject jsonObject = Json.createReader(new StringReader(stringEntity.getEntity())).readObject();
            entitySent = Entity.entity(jsonObject, stringEntity.getMediaType());
        }
        return entitySent;
    }

    private String queryBody(JsonObject response, String operation) {
        String query = response.getString("query");
        then(query).startsWith(operation);
        query = query.substring(operation.length()).trim();
        then(query).startsWith("{").endsWith("}");
        return query.substring(1, query.length() - 1).trim();
    }

    Object sentHeader(String name) {
        return sentHeaders().getFirst(name);
    }

    URI endpointUsed() {
        ArgumentCaptor<URI> captor = ArgumentCaptor.forClass(URI.class);
        BDDMockito.then(mockClient).should().target(captor.capture());
        return captor.getValue();
    }

    MultivaluedMap<String, Object> sentHeaders() {
        MultivaluedMap<String, Object> map = captureExplicitHeaders();
        map.putSingle("Accept", captureAcceptHeader());
        map.putSingle("Content-Type", entitySent().getMediaType());
        return map;
    }

    private MultivaluedMap<String, Object> captureExplicitHeaders() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<MultivaluedMap<String, Object>> captor = ArgumentCaptor.forClass(MultivaluedMap.class);
        BDDMockito.then(mockInvocationBuilder).should().headers(captor.capture());
        MultivaluedMap<String, Object> map = captor.getValue();
        return (map == null) ? new MultivaluedHashMap<>() : map;
    }

    private MediaType captureAcceptHeader() {
        ArgumentCaptor<MediaType> captor = ArgumentCaptor.forClass(MediaType.class);
        BDDMockito.then(mockWebTarget).should().request(captor.capture());
        return captor.getValue();
    }
}
