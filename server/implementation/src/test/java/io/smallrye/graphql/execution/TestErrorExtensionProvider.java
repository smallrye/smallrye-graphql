package io.smallrye.graphql.execution;

import io.smallrye.graphql.api.ErrorExtensionProvider;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

public class TestErrorExtensionProvider implements ErrorExtensionProvider {
    @Override
    public String getKey() {
        return "test-extension";
    }

    @Override
    public JsonNode mapValueFrom(Throwable exception) {
        return JsonNodeFactory.instance.numberNode(exception.getClass().getSimpleName().length());
    }
}
