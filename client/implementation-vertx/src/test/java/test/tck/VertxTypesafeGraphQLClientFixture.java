package test.tck;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.net.URI;

import javax.json.Json;
import javax.json.JsonObject;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import io.smallrye.graphql.client.typesafe.api.TypesafeGraphQLClientBuilder;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.future.SucceededFuture;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import tck.graphql.typesafe.TypesafeGraphQLClientFixture;

public class VertxTypesafeGraphQLClientFixture implements TypesafeGraphQLClientFixture {
    private final WebClient mockWebClient = Mockito.mock(WebClient.class);
    private final HttpRequest<Buffer> mockHttpRequest = (HttpRequest<Buffer>) Mockito.mock(HttpRequest.class);
    private final HttpResponse<Buffer> mockHttpResponse = (HttpResponse<Buffer>) Mockito.mock(HttpResponse.class);

    private Integer statusCode;
    private String statusMessage;
    private String response;
    private JsonObject requestSent;

    public VertxTypesafeGraphQLClientFixture() {
        given(mockWebClient.postAbs(any(String.class))).willReturn(mockHttpRequest);
        given(mockHttpRequest.putHeader(any(String.class), any(String.class))).willReturn(mockHttpRequest);
        given(mockHttpRequest.putHeaders(any(MultiMap.class))).willReturn(mockHttpRequest);
        given(mockHttpRequest.sendBuffer(any(Buffer.class))).willReturn(new SucceededFuture<>(mockHttpResponse));
        given(mockHttpResponse.bodyAsString()).will(i -> response);
        given(mockHttpResponse.statusCode()).will(i -> statusCode);
        given(mockHttpResponse.statusMessage()).will(i -> statusMessage);
    }

    @Override
    public <T> T build(Class<T> apiClass) {
        return builder().build(apiClass);
    }

    @Override
    public TypesafeGraphQLClientBuilder builder() {
        return builderWithoutEndpointConfig().endpoint("urn:dummy-endpoint");
    }

    @Override
    public TypesafeGraphQLClientBuilder builderWithoutEndpointConfig() {
        TypesafeGraphQLClientBuilder builder = TypesafeGraphQLClientBuilder.newBuilder();
        try {
            Method method = builder.getClass().getMethod("client", WebClient.class);
            method.invoke(builder, mockWebClient);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("can't set client on builder", e);
        }
        return builder;
    }

    @Override
    public void returnsData(String data) {
        returns("{\"data\":{" + data.replace('\'', '\"') + "}}");
    }

    @Override
    public void returns(String response) {
        this.response = response;
        this.statusCode = 200;
    }

    @Override
    public void returnsServerError() {
        this.response = "failed";
        this.statusCode = 500;
        this.statusMessage = "Internal Server Error";
    }

    @Override
    public String variables() {
        return rawVariables().replace('\"', '\'');
    }

    @Override
    public String rawVariables() {
        JsonObject variables = requestSent().getJsonObject("variables");
        return String.valueOf(variables);
    }

    @Override
    public String operationName() {
        return requestSent().getString("operationName", "null");
    }

    @Override
    public String query() {
        return requestSent().getString("query").replace('\"', '\'');
    }

    private JsonObject requestSent() {
        if (requestSent == null) {
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Buffer> captor = ArgumentCaptor.forClass(Buffer.class);
            then(mockHttpRequest).should().sendBuffer(captor.capture());
            String requestString = captor.getValue().toString();
            requestSent = Json.createReader(
                    new StringReader(requestString)).readObject();
        }
        return requestSent;
    }

    @Override
    public Object sentHeader(String name) {
        return sentHeaders().get(name);
    }

    @Override
    public URI endpointUsed() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        then(mockWebClient).should().postAbs(captor.capture());
        return URI.create(captor.getValue());
    }

    private MultiMap sentHeaders() {
        MultiMap map = captureExplicitHeaders();
        map.set("Accept", captureAcceptHeader());
        map.set("Content-Type", "application/json;charset=utf-8");
        return map;
    }

    private MultiMap captureExplicitHeaders() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<MultiMap> captor = ArgumentCaptor.forClass(MultiMap.class);
        then(mockHttpRequest).should().putHeaders(captor.capture());
        return captor.getValue();
    }

    private String captureAcceptHeader() {
        ArgumentCaptor<MultiMap> captor = ArgumentCaptor.forClass(MultiMap.class);
        then(mockHttpRequest).should().putHeaders(captor.capture());
        return captor.getValue().get("Accept");
    }

    @Override
    public void verifyClosed() {
        verify(mockWebClient).close();
    }
}
