package io.smallrye.graphql.client;

import java.util.List;

import javax.json.JsonObject;

public interface Response {

    JsonObject getData();

    List<Error> getErrors();

    <T> List<T> getList(Class<T> dataType, String rootField);

    <T> T getObject(Class<T> dataType, String rootField);

    boolean hasData();

    boolean hasError();
}
