package io.smallrye.graphql.client.dynamic;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import io.smallrye.graphql.client.Error;
import io.smallrye.graphql.client.Response;

public class ResponseImpl implements Response {

    private final JsonObject data;
    private final List<Error> errors;
    private Jsonb jsonb;

    public ResponseImpl(JsonObject data, List<Error> errors) {
        this.data = data;
        this.errors = errors;
    }

    public <T> T getObject(Class<T> dataType, String rootField) {
        JsonObject jsonObject = data.getJsonObject(rootField);
        return getJsonb().fromJson(jsonObject.toString(), dataType);
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

        jsonArray.forEach(o -> {
            result.add(getJsonb().fromJson(o.toString(), dataType));
        });

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
        return "GraphQLResponse{" + "data=" + data + ", errors=" + errors + ", jsonb=" + jsonb + '}';
    }

    private Jsonb getJsonb() {
        if (jsonb == null) {
            jsonb = JsonbBuilder.create();
        }
        return jsonb;
    }
}
