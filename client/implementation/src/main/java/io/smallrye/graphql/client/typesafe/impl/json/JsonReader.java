package io.smallrye.graphql.client.typesafe.impl.json;

import static io.smallrye.graphql.client.typesafe.impl.json.JsonUtils.isListOf;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import io.smallrye.graphql.client.typesafe.api.ErrorOr;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientError;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientException;
import io.smallrye.graphql.client.typesafe.impl.reflection.TypeInfo;

public class JsonReader extends Reader<JsonValue> {
    public static Object readJson(String description, TypeInfo type, JsonValue value) {
        return readJson(new Location(type, description), type, value);
    }

    static Object readJson(Location location, TypeInfo type, JsonValue value) {
        return new JsonReader(type, location, value).read();
    }

    private JsonReader(TypeInfo type, Location location, JsonValue value) {
        super(type, location, value);
    }

    @Override
    Object read() {
        if (type.isOptional())
            return Optional.ofNullable(readJson(location, type.getItemType(), value));
        if (type.isErrorOr())
            return readErrorOr();
        if (isListOfErrors(value) && !isGraphQlErrorsType())
            throw cantApplyErrors(readGraphQlClientErrors());
        return reader(location).read();
    }

    private ErrorOr<Object> readErrorOr() {
        if (isListOfErrors(value))
            return ErrorOr.ofErrors(readGraphQlClientErrors());
        return ErrorOr.of(readJson(location, type.getItemType(), value));
    }

    private List<GraphQlClientError> readGraphQlClientErrors() {
        return value.asJsonArray().stream()
                .map(item -> (GraphQlClientError) readJson(location, TypeInfo.of(GraphQlClientErrorImpl.class), item))
                .collect(toList());
    }

    private boolean isListOfErrors(JsonValue jsonValue) {
        return isListOf(jsonValue, ErrorOr.class.getSimpleName());
    }

    private boolean isGraphQlErrorsType() {
        return GraphQlClientError.class.isAssignableFrom(type.getRawType());
    }

    private GraphQlClientException cantApplyErrors(List<GraphQlClientError> errors) {
        return new GraphQlClientException("can't apply errors to " + location, errors);
    }

    private Reader<?> reader(Location location) {
        switch (value.getValueType()) {
            case ARRAY:
                return new JsonArrayReader(type, location, (JsonArray) value);
            case OBJECT:
                return new JsonObjectReader(type, location, (JsonObject) value);
            case STRING:
                return new JsonStringReader(type, location, (JsonString) value);
            case NUMBER:
                return new JsonNumberReader(type, location, (JsonNumber) value);
            case TRUE:
            case FALSE:
                return new JsonBooleanReader(type, location, value);
            case NULL:
                return new JsonNullReader(type, location, value);
        }
        throw new GraphQlClientException("unreachable code");
    }
}
