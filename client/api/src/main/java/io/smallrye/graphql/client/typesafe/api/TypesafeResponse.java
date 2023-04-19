package io.smallrye.graphql.client.typesafe.api;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.json.JsonObject;

import io.smallrye.graphql.client.GraphQLError;

public final class TypesafeResponse<T> extends ErrorOr<T> {
    private Map<String, List<String>> transportMeta;
    private JsonObject extensions;

    public static <T> TypesafeResponse<T> of(T value) {
        return new TypesafeResponse<>(value, null);
    }

    public static <T> TypesafeResponse<T> ofErrors(List<GraphQLError> errors) {
        if (errors == null)
            throw new NullPointerException("errors must not be null");
        if (errors.isEmpty())
            throw new IllegalArgumentException("errors must not be empty");
        return new TypesafeResponse<>(null, unmodifiableList(new ArrayList<>(errors)));
    }

    private TypesafeResponse(T value, List<GraphQLError> errors) {
        super(value, errors);
    }

    private TypesafeResponse(TypesafeResponse<T> typesafeResponse,
            Map<String, List<String>> transportMeta,
            JsonObject extensions) {
        super(
                (typesafeResponse.isPresent()) ? typesafeResponse.get() : null,
                (typesafeResponse.hasErrors()) ? typesafeResponse.getErrors() : null);
        this.transportMeta = transportMeta;
        this.extensions = extensions;
    }

    public static <T> TypesafeResponse<T> withTransportMetaAndExtensions(TypesafeResponse<T> typesafeResponse,
            Map<String, List<String>> transportMeta,
            JsonObject responseExtensions) {
        return new TypesafeResponse(typesafeResponse, transportMeta, responseExtensions);
    }

    /**
     * Returns a map containing transport metadata as key-value pairs,
     * where the key is a String and the value is a List of Strings.
     */
    public Map<String, List<String>> getTransportMeta() {
        return transportMeta;
    }

    /**
     * Returns a JsonObject containing extensions to the GraphQL response, if any.
     */
    public JsonObject getExtensions() {
        return extensions;
    }

    @Override
    public String toString() {
        return "TypesafeResponse(" +
                (isPresent() ? "value=" + get() : "errors=" + getErrors()) +
                ", transportMeta=" + transportMeta +
                ", responseExtensions=" + extensions +
                ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        TypesafeResponse<?> that = (TypesafeResponse<?>) o;
        return Objects.equals(transportMeta, that.transportMeta)
                && Objects.equals(extensions, that.extensions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), transportMeta, extensions);
    }
}
