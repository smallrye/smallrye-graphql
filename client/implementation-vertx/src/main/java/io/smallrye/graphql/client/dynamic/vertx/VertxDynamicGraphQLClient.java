package io.smallrye.graphql.client.dynamic.vertx;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import io.smallrye.graphql.client.Error;
import io.smallrye.graphql.client.Request;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.dynamic.ErrorImpl;
import io.smallrye.graphql.client.dynamic.RequestImpl;
import io.smallrye.graphql.client.dynamic.ResponseImpl;
import io.smallrye.graphql.client.dynamic.SmallRyeGraphQLDynamicClientLogging;
import io.smallrye.graphql.client.dynamic.SmallRyeGraphQLDynamicClientMessages;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebsocketVersion;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

public class VertxDynamicGraphQLClient implements DynamicGraphQLClient {

    private final WebClient webClient;
    private final HttpClient httpClient;
    private final String url;
    private final MultiMap headers;

    VertxDynamicGraphQLClient(Vertx vertx, String url, MultiMap headers) {
        this.httpClient = vertx.createHttpClient();
        this.webClient = WebClient.wrap(httpClient);
        this.headers = headers;
        this.url = url;
    }

    @Override
    public Response executeSync(Document document) throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(document));
    }

    @Override
    public Response executeSync(Request request) throws ExecutionException, InterruptedException {
        return executeSync(Buffer.buffer(request.toJson()));
    }

    @Override
    public Response executeSync(String request) throws ExecutionException, InterruptedException {
        return executeSync(Buffer.buffer(wrapIntoJson(request)));
    }

    private Response executeSync(Buffer buffer) throws ExecutionException, InterruptedException {
        HttpResponse<Buffer> result = webClient.postAbs(url)
                .putHeaders(headers)
                .sendBuffer(buffer)
                .toCompletionStage()
                .toCompletableFuture()
                .get();
        return readFrom(result.body());
    }

    @Override
    public Uni<Response> executeAsync(Document document) {
        return executeAsync(buildRequest(document));
    }

    @Override
    public Uni<Response> executeAsync(Request request) {
        return executeAsync(Buffer.buffer(request.toJson()));
    }

    @Override
    public Uni<Response> executeAsync(String request) {
        return executeAsync(Buffer.buffer(wrapIntoJson(request)));
    }

    private Uni<Response> executeAsync(Buffer buffer) {
        return Uni.createFrom().completionStage(
                webClient.postAbs(url)
                        .putHeaders(headers)
                        .sendBuffer(buffer)
                        .toCompletionStage())
                .map(response -> readFrom(response.body()));
    }

    @Override
    public Multi<Response> subscription(Document document) {
        return subscription0(buildRequest(document).toJson());
    }

    @Override
    public Multi<Response> subscription(Request request) {
        return subscription0(request.toJson());
    }

    @Override
    public Multi<Response> subscription(String request) {
        return subscription0(wrapIntoJson(request));
    }

    private Multi<Response> subscription0(String request) {
        String WSURL = url.replaceFirst("http", "ws");
        return Multi.createFrom()
                .emitter(e -> {
                    httpClient.webSocketAbs(WSURL, headers, WebsocketVersion.V13, new ArrayList<>(), result -> {
                        if (result.succeeded()) {
                            WebSocket socket = result.result();
                            socket.writeTextMessage(request);
                            socket.handler(message -> {
                                e.emit(readFrom(message));
                            });
                            socket.closeHandler((v) -> {
                                e.complete();
                            });
                            e.onTermination(socket::close);
                        } else {
                            e.fail(result.cause());
                        }
                    });
                });
    }

    public Request buildRequest(Document document) {
        return new RequestImpl(document.build());
    }

    private String wrapIntoJson(String input) {
        return new RequestImpl(input).toJson();
    }

    @Override
    public void close() {
        httpClient.close();
    }

    private ResponseImpl readFrom(Buffer input) {
        // FIXME: is there a more performant way to read the input?
        JsonReader jsonReader = Json.createReader(new StringReader(input.toString()));
        JsonObject jsonResponse;
        try {
            jsonResponse = jsonReader.readObject();
        } catch (Exception e) {
            throw SmallRyeGraphQLDynamicClientMessages.msg.cannotParseResponse(input.toString());
        }

        JsonObject data = null;
        if (jsonResponse.containsKey("data")) {
            if (!jsonResponse.isNull("data")) {
                data = jsonResponse.getJsonObject("data");
            } else {
                SmallRyeGraphQLDynamicClientLogging.log.noDataInResponse();
            }
        }

        List<Error> errors = null;
        if (jsonResponse.containsKey("errors")) {
            JsonArray rawErrors = jsonResponse.getJsonArray("errors");
            Jsonb jsonb = JsonbBuilder.create();
            errors = jsonb.fromJson(
                    rawErrors.toString(),
                    new ArrayList<ErrorImpl>() {
                    }.getClass().getGenericSuperclass());
            try {
                jsonb.close();
            } catch (Exception ignore) {
            }
        }

        return new ResponseImpl(data, errors);
    }

}
