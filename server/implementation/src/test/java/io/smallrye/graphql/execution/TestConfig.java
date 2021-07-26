package io.smallrye.graphql.execution;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.smallrye.graphql.spi.config.Config;
import io.smallrye.graphql.spi.config.LogPayloadOption;

/**
 * Implements the config for testing
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class TestConfig implements Config {

    @Override
    public boolean isPrintDataFetcherException() {
        return true;
    }

    @Override
    public boolean isEventsEnabled() {
        return true;
    }

    @Override
    public LogPayloadOption logPayload() {
        return LogPayloadOption.queryAndVariables;
    }

    @Override
    public Optional<List<String>> getErrorExtensionFields() {
        return Optional
                .of(Arrays.asList(new String[] { "exception", "classification", "code", "description",
                        "validationErrorType", "queryPath" }));
    }

    @Override
    public boolean isIncludeDirectivesInSchema() {
        return true;
    }

    @Override
    public <T> T getConfigValue(String key, Class<T> type, T defaultValue) {
        if (key.equals(TestEventingService.KEY)) {
            return (T) Boolean.TRUE;
        }
        return defaultValue;
    }

    @Override
    public String getName() {
        return "Test Config Service";
    }
}
