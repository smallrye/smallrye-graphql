package test.unit;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;

import java.io.StringReader;
import java.lang.reflect.Method;
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

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientBuilder;

/**
 * Builds {@link GraphQlClientBuilder} instances with mocked backend and helps testing that.
 * Only this class relies on the JAX-RS implementation, but the tests are independent of that detail.
 */
class GraphQlClientFixture {
    private final Client mockClient = Mockito.mock(Client.class);
    private final WebTarget mockWebTarget = Mockito.mock(WebTarget.class);
    private final Invocation.Builder mockInvocationBuilder = Mockito.mock(Invocation.Builder.class);
    private Response response;
    private Entity<JsonObject> entitySent;

    GraphQlClientFixture() {
        given(mockClient.target(any(URI.class))).willReturn(mockWebTarget);
        given(mockWebTarget.request(any(MediaType.class))).willReturn(mockInvocationBuilder);
        given(mockInvocationBuilder.headers(any())).willReturn(mockInvocationBuilder);
        given(mockInvocationBuilder.post(any())).will(i -> response);
    }

    public <T> T build(Class<T> apiClass) {
        return builder().build(apiClass);
    }

    GraphQlClientBuilder builder() {
        return builderWithoutEndpointConfig().endpoint("urn:dummy-endpoint");
    }

    GraphQlClientBuilder builderWithoutEndpointConfig() {
        GraphQlClientBuilder builder = GraphQlClientBuilder.newBuilder();
        try {
            Method method = builder.getClass().getMethod("client", Client.class);
            method.invoke(builder, mockClient);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("can't set client on builder", e);
        }
        return builder;
    }

    void returnsData(String data) {
        returns("{\"data\":{" + data.replace('\'', '\"') + "}}");
    }

    void returns(String response) {
        this.response = Response.ok(response).build();
    }

    public void returnsServerError() {
        this.response = Response.serverError().type(TEXT_PLAIN_TYPE).entity("failed").build();
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
            then(mockInvocationBuilder).should().post(captor.capture());
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
        then(mockClient).should().target(captor.capture());
        return captor.getValue();
    }

    private MultivaluedMap<String, Object> sentHeaders() {
        MultivaluedMap<String, Object> map = captureExplicitHeaders();
        map.putSingle("Accept", captureAcceptHeader());
        map.putSingle("Content-Type", entitySent().getMediaType());
        return map;
    }

    private MultivaluedMap<String, Object> captureExplicitHeaders() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<MultivaluedMap<String, Object>> captor = ArgumentCaptor.forClass(MultivaluedMap.class);
        then(mockInvocationBuilder).should().headers(captor.capture());
        MultivaluedMap<String, Object> map = captor.getValue();
        return (map == null) ? new MultivaluedHashMap<>() : map;
    }

    private MediaType captureAcceptHeader() {
        ArgumentCaptor<MediaType> captor = ArgumentCaptor.forClass(MediaType.class);
        then(mockWebTarget).should().request(captor.capture());
        return captor.getValue();
    }

    public void verifyClosed() {
        verify(mockClient).close();
    }
}
