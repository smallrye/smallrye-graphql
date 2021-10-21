package io.smallrye.graphql.client;

import java.util.List;
import java.util.Map;

import javax.json.JsonObject;

public interface Response {

    JsonObject getData();

    List<GraphQLError> getErrors();

    <T> List<T> getList(Class<T> dataType, String rootField);

    <T> T getObject(Class<T> dataType, String rootField);

    boolean hasData();

    boolean hasError();

    List<Map.Entry<String, String>> getHeaders();
}
