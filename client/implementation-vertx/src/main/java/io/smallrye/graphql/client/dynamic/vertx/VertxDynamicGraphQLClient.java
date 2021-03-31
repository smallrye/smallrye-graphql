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
import io.smallrye.mutiny.Uni;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class VertxDynamicGraphQLClient implements DynamicGraphQLClient {

    private final WebClient webClient;
    private final String url;
    private final MultiMap headers;

    VertxDynamicGraphQLClient(WebClientOptions options, Vertx vertx, String url, MultiMap headers) {
        this.webClient = WebClient.create(vertx, options);
        this.headers = headers;
        this.url = url;
    }

    @Override
    public Response executeSync(Document document) throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(document));
    }

    @Override
    public Response executeSync(Request request) throws ExecutionException, InterruptedException {
        HttpResponse<Buffer> result = webClient.postAbs(url)
                .putHeaders(headers)
                .sendBuffer(Buffer.buffer(request.toJson()))
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
        return Uni.createFrom().completionStage(
                webClient.postAbs(url)
                        .putHeaders(headers)
                        .sendBuffer(Buffer.buffer(request.toJson()))
                        .toCompletionStage())
                .map(response -> readFrom(response.body()));
    }

    public Request buildRequest(Document document) {
        return new RequestImpl(document.build());
    }

    @Override
    public void close() {
        webClient.close();
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
