package io.smallrye.graphql.tests;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A very stupid client for sending testing GraphQL queries. Once there's a spec-based client available,
 * we might want to switch to that one instead.
 */
public class SimpleGraphQLClient {

    private final URL baseURL;
    private final OkHttpClient client;

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public SimpleGraphQLClient(URL url) throws URISyntaxException, MalformedURLException {
        this.baseURL = url.toURI().resolve(url.getPath() + "graphql").toURL();
        this.client = new OkHttpClient();
    }

    public String query(String query) throws IOException {
        JsonObject queryObject = Json.createObjectBuilder().add("query", query).build();
        Response response = client.newCall(new Request.Builder()
                .url(baseURL)
                .post(RequestBody.create(queryObject.toString(), JSON))
                .build()).execute();
        return response.body().string();
    }

}
