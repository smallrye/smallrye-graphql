package io.smallrye.graphql.client.dynamic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;

import io.smallrye.graphql.client.Error;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.typesafe.impl.json.JsonReader;
import io.smallrye.graphql.client.typesafe.impl.reflection.TypeInfo;

public class ResponseImpl implements Response {

    private final JsonObject data;
    private final List<Error> errors;
    private List<Map.Entry<String, String>> headers;

    public ResponseImpl(JsonObject data, List<Error> errors, List<Map.Entry<String, String>> headers) {
        this.data = data;
        this.errors = errors;
        this.headers = Collections.unmodifiableList(headers);
    }

    public <T> T getObject(Class<T> dataType, String rootField) {
        JsonObject jsonObject = data.getJsonObject(rootField);
        return (T) JsonReader.readJson(rootField, TypeInfo.of(dataType), jsonObject, null);
    }

    public <T> List<T> getList(Class<T> dataType, String rootField) {
        List<T> result = new ArrayList<T>();

        Object item = data.get(rootField);
        if (item instanceof JsonObject) {
            // A single Object can be returned as a mono-element List
            result.add(getObject(dataType, rootField));
            return result;
        }

        JsonArray jsonArray = (JsonArray) item;
        TypeInfo type = TypeInfo.of(dataType);
        jsonArray.forEach(o -> result.add((T) JsonReader.readJson(rootField, type, o, null)));

        return result;
    }

    public JsonObject getData() {
        return data;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public boolean hasData() {
        return data != null;
    }

    public boolean hasError() {
        return errors != null;
    }

    public String toString() {
        return "GraphQLResponse{" + "data=" + data + ", errors=" + errors + '}';
    }

    public List<Map.Entry<String, String>> getHeaders() {
        return headers;
    }
}
