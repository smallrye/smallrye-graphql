package io.smallrye.graphql.execution.error;

import io.smallrye.graphql.api.ErrorExtensionProvider;
import io.smallrye.graphql.spi.config.Config;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

public class ExceptionNameErrorExtensionProvider implements ErrorExtensionProvider {

    @Override
    public String getKey() {
        return Config.ERROR_EXTENSION_EXCEPTION;
    }

    @Override
    public JsonNode mapValueFrom(Throwable exception) {
        return JsonNodeFactory.instance.textNode(exception.getClass().getName());
    }
}
