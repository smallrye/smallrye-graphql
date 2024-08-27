package io.smallrye.graphql.execution.error;

import static io.smallrye.graphql.JsonProviderHolder.JSON_PROVIDER;

import jakarta.json.JsonString;

import io.smallrye.graphql.api.ErrorExtensionProvider;
import io.smallrye.graphql.spi.config.Config;

public class ExceptionNameErrorExtensionProvider implements ErrorExtensionProvider {
    @Override
    public String getKey() {
        return Config.ERROR_EXTENSION_EXCEPTION;
    }

    @Override
    public JsonString mapValueFrom(Throwable exception) {
        return JSON_PROVIDER.createValue(exception.getClass().getName());
    }
}
