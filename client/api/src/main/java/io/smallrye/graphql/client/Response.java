package io.smallrye.graphql.client;

import java.util.List;
import java.util.Map;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public interface Response {

    /**
     * The 'data' object contained in the response.
     * Can be JsonValue.NULL if the response contains an empty field, or `null` if the response
     * does not contain this field at all.
     */
    JsonObject getData();

    /**
     * List of errors contained in this response.
     */
    List<GraphQLError> getErrors();

    /**
     * List of user-made extensions contained in this response.
     */
    JsonObject getExtensions();

    /**
     * Transform the contents of the `rootField` from this response into a list of objects
     * of the requested type.
     *
     * If the response contains this field but it is a single object rather than a list, this method throws an exception.
     * If the response does not contain this field at all, this method throws an exception.
     * If the response contains this field but with a null value, this method returns null.
     */
    <T> List<T> getList(Class<T> dataType, String rootField);

    /**
     * Transform the contents of the `rootField` from this response into an object
     * of the requested type.
     *
     * If the response does not contain this field at all, this method throws an exception.
     * If the response contains this field but it is null, this method returns null.
     */
    <T> T getObject(Class<T> dataType, String rootField);

    /**
     * If this response contains any data, this returns `true`; `false` otherwise.
     */
    boolean hasData();

    /**
     * If this response contains at least one error, this returns `true`; `false` otherwise.
     */
    boolean hasError();

    /**
     * If this response contains any extensions, this returns `true`; `false` otherwise.
     */
    boolean hasExtensions();

    /**
     * Get transport-specific metadata that came from the server with this response.
     */
    Map<String, List<String>> getTransportMeta();
}
