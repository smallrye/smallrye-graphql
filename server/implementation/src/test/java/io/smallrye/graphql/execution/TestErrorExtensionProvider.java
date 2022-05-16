package io.smallrye.graphql.execution;

import javax.json.Json;
import javax.json.JsonNumber;

import io.smallrye.graphql.api.ErrorExtensionProvider;

public class TestErrorExtensionProvider implements ErrorExtensionProvider {
    @Override
    public String getKey() {
        return "test-extension";
    }

    @Override
    public JsonNumber mapValueFrom(Throwable exception) {
        return Json.createValue(exception.getClass().getSimpleName().length());
    }
}
