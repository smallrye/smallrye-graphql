package io.smallrye.graphql.execution;

import jakarta.json.Json;
import jakarta.json.JsonNumber;

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
