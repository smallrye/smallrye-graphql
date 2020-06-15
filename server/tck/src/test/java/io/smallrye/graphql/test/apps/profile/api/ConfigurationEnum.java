package io.smallrye.graphql.test.apps.profile.api;

import org.eclipse.microprofile.graphql.Description;

@org.eclipse.microprofile.graphql.Enum("ConfigurationEnum")
@Description("The available configuration names")
public enum ConfigurationEnum {
    allocation_projection("allocation.projection.maxHistoryDays"),
    testing_thingy("testing.graphql.config.service.save");

    private final String configurationValue;

    private ConfigurationEnum(String configurationValue) {
        this.configurationValue = configurationValue;
    }

    @Override
    public String toString() {
        return this.configurationValue;
    }
}