package io.smallrye.graphql.execution.error;

import static io.smallrye.graphql.JsonProviderHolder.JSON_PROVIDER;
import static java.util.Locale.ROOT;

import jakarta.json.JsonValue;

import io.smallrye.graphql.api.ErrorExtensionProvider;
import io.smallrye.graphql.schema.model.ErrorInfo;
import io.smallrye.graphql.spi.config.Config;

public class ErrorCodeExtensionProvider implements ErrorExtensionProvider {
    @Override
    public String getKey() {
        return Config.ERROR_EXTENSION_CODE;
    }

    @Override
    public JsonValue mapValueFrom(Throwable exception) {
        return JSON_PROVIDER.createValue(errorCode(exception));
    }

    private String errorCode(Throwable exception) {
        ErrorInfo errorInfo = ErrorInfoMap.getErrorInfo(exception.getClass().getName());
        if (errorInfo == null) {
            return camelToKebab(exception.getClass().getSimpleName().replaceAll("Exception$", ""));
        } else {
            return errorInfo.getErrorCode();
        }
    }

    private static String camelToKebab(String input) {
        return String.join("-", input.split("(?=\\p{javaUpperCase})"))
                .toLowerCase(ROOT);
    }
}
