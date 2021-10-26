package io.smallrye.graphql.execution;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Optional;

import io.smallrye.graphql.execution.event.TestEventingService;
import io.smallrye.graphql.spi.config.Config;
import io.smallrye.graphql.spi.config.LogPayloadOption;

/**
 * Implements the config for testing
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class TestConfig implements Config {

    public boolean federationEnabled;

    public TestConfig() {
        reset();
    }

    @Override
    public boolean isPrintDataFetcherException() {
        return true;
    }

    @Override
    public boolean isEventsEnabled() {
        return true;
    }

    @Override
    public boolean isFederationEnabled() {
        return federationEnabled;
    }

    @Override
    public LogPayloadOption logPayload() {
        return LogPayloadOption.queryAndVariables;
    }

    @Override
    public Optional<List<String>> getErrorExtensionFields() {
        return Optional.of(Arrays.asList(
                "exception", "classification", "code", "description",
                "validationErrorType", "queryPath", "test-extension"));
    }

    @Override
    public boolean isIncludeDirectivesInSchema() {
        return true;
    }

    @Override
    public <T> T getConfigValue(String key, Class<T> type, T defaultValue) {
        if (key.equals(TestEventingService.KEY)) {
            //noinspection unchecked
            return (T) Boolean.TRUE;
        }
        return defaultValue;
    }

    @Override
    public String getName() {
        return "Test Config Service";
    }

    public void reset() {
        this.federationEnabled = false;
    }
}
