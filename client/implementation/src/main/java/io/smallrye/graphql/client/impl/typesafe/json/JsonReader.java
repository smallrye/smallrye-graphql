package io.smallrye.graphql.client.impl.typesafe.json;

import static io.smallrye.graphql.client.impl.typesafe.json.JsonUtils.isListOf;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import io.smallrye.graphql.client.GraphQLClientException;
import io.smallrye.graphql.client.GraphQLError;
import io.smallrye.graphql.client.impl.ResponseReader;
import io.smallrye.graphql.client.impl.typesafe.reflection.FieldInfo;
import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;
import io.smallrye.graphql.client.typesafe.api.ErrorOr;

public class JsonReader extends Reader<JsonValue> {
    public static Object readJson(String description, TypeInfo type, JsonValue value, FieldInfo field) {
        return readJson(new Location(type, description), type, value, field);
    }

    static Object readJson(Location location, TypeInfo type, JsonValue value, FieldInfo field) {
        return new JsonReader(type, location, value, field).read();
    }

    private JsonReader(TypeInfo type, Location location, JsonValue value, FieldInfo field) {
        super(type, location, value, field);
    }

    @Override
    Object read() {
        if (type.isOptional())
            return Optional.ofNullable(readJson(location, type.getItemType(), value, field));
        if (type.isAsync()) {
            return readJson(location, type.getItemType(), value, field);
        }
        if (type.isErrorOr())
            return readErrorOr();
        if (isListOfErrors(value) && !isGraphQlErrorsType())
            throw cantApplyErrors(readGraphQlClientErrors());
        return reader(location).read();
    }

    private ErrorOr<Object> readErrorOr() {
        if (isListOfErrors(value))
            return ErrorOr.ofErrors(readGraphQlClientErrors());
        return ErrorOr.of(readJson(location, type.getItemType(), value, field));
    }

    private List<GraphQLError> readGraphQlClientErrors() {
        return value.asJsonArray().stream()
                .map(ResponseReader::readError)
                .collect(toList());
    }

    private boolean isListOfErrors(JsonValue jsonValue) {
        return isListOf(jsonValue, ErrorOr.class.getSimpleName());
    }

    private boolean isGraphQlErrorsType() {
        return GraphQLError.class.isAssignableFrom(type.getRawType());
    }

    private GraphQLClientException cantApplyErrors(List<GraphQLError> errors) {
        return new GraphQLClientException("errors from service (and we can't apply them to a " + location + "; see ErrorOr)",
                errors);
    }

    private Reader<?> reader(Location location) {
        switch (value.getValueType()) {
            case ARRAY:
                return new JsonArrayReader(type, location, (JsonArray) value, field);
            case OBJECT:
                return new JsonObjectReader(type, location, (JsonObject) value, field);
            case STRING:
                return new JsonStringReader(type, location, (JsonString) value, field);
            case NUMBER:
                return new JsonNumberReader(type, location, (JsonNumber) value, field);
            case TRUE:
            case FALSE:
                return new JsonBooleanReader(type, location, value, field);
            case NULL:
                return new JsonNullReader(type, location, value, field);
        }
        return null;
    }
}
