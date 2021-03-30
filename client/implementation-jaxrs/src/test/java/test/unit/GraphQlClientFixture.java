package test.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientBuilder;
import io.smallrye.graphql.client.typesafe.jaxrs.JaxRsTypesafeGraphQLClientBuilder;

class GraphQlClientFixture {
    private final Client mockClient = Mockito.mock(Client.class);
    private final WebTarget mockWebTarget = Mockito.mock(WebTarget.class);
    private final Invocation.Builder mockInvocationBuilder = Mockito.mock(Invocation.Builder.class);
    private Response response;
    private Entity<JsonObject> entitySent;

    GraphQlClientFixture() {
        BDDMockito.given(mockClient.target(ArgumentMatchers.any(URI.class))).willReturn(mockWebTarget);
        BDDMockito.given(mockWebTarget.request(ArgumentMatchers.any(MediaType.class))).willReturn(mockInvocationBuilder);
        BDDMockito.given(mockInvocationBuilder.headers(ArgumentMatchers.any())).willReturn(mockInvocationBuilder);
        BDDMockito.given(mockInvocationBuilder.post(ArgumentMatchers.any())).will(i -> response);
    }

    public <T> T build(Class<T> apiClass) {
        return builder().build(apiClass);
    }

    GraphQlClientBuilder builder() {
        return builderWithoutEndpointConfig().endpoint("urn:dummy-endpoint");
    }

    GraphQlClientBuilder builderWithoutEndpointConfig() {
        JaxRsTypesafeGraphQLClientBuilder impl = (JaxRsTypesafeGraphQLClientBuilder) GraphQlClientBuilder.newBuilder();
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
        return entitySent().getEntity().getString("query").replace('\"', '\'');
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

    public Client client() {
        return mockClient;
    }

    public void verifyClosed() {
        verify(mockClient).close();
    }
}
