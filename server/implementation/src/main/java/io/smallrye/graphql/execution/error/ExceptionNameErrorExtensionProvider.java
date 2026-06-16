package io.smallrye.graphql.execution.error;

import jakarta.json.JsonString;
import jakarta.json.spi.JsonProvider;

import io.smallrye.graphql.api.ErrorExtensionProvider;
import io.smallrye.graphql.spi.config.Config;

public class ExceptionNameErrorExtensionProvider implements ErrorExtensionProvider {

    private static final JsonProvider JSON_PROVIDER = JsonProvider.provider();

    @Override
    public String getKey() {
        return Config.ERROR_EXTENSION_EXCEPTION;
    }

    @Override
    public JsonString mapValueFrom(Throwable exception) {
        return JSON_PROVIDER.createValue(exception.getClass().getName());
    }
}
