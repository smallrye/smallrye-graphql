package test.unit;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.StringReader;
import java.net.URI;

import javax.json.Json;
import javax.json.JsonReader;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;

import io.smallrye.graphql.client.api.GraphQlClientBuilder;
import io.smallrye.graphql.client.impl.GraphQlClientBuilderImpl;

class GraphQlClientFixture {
    private final Client mockClient = mock(Client.class);
    private final Invocation.Builder mockInvocationBuilder = mock(Invocation.Builder.class);
    private Response response;

    GraphQlClientFixture() {
        WebTarget mockWebTarget = mock(WebTarget.class);

        given(mockClient.target(any(URI.class))).willReturn(mockWebTarget);
        given(mockWebTarget.request(APPLICATION_JSON_TYPE)).willReturn(mockInvocationBuilder);
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
        this.response = Response.ok("{\"data\":{" + data.replace('\'', '\"') + "}}").build();
    }

    void returns(Response response) {
        this.response = response;
    }

    String query() {
        return rawQuery().replace('\"', '\'');
    }

    String rawQuery() {
        return queryBody(captureRequestEntity(), "query");
    }

    String mutation() {
        return queryBody(captureRequestEntity(), "mutation").replace('\"', '\'');
    }

    private String captureRequestEntity() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Entity<String>> captor = ArgumentCaptor.forClass(Entity.class);
        BDDMockito.then(mockInvocationBuilder).should().post(captor.capture());
        return captor.getValue().getEntity();
    }

    private String queryBody(String response, String operation) {
        JsonReader reader = Json.createReader(new StringReader(response));
        String query = reader.readObject().getString("query");
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
        @SuppressWarnings("unchecked")
        ArgumentCaptor<MultivaluedMap<String, Object>> captor = ArgumentCaptor.forClass(MultivaluedMap.class);
        BDDMockito.then(mockInvocationBuilder).should().headers(captor.capture());
        return captor.getValue();
    }
}
