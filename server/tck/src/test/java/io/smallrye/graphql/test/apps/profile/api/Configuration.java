package io.smallrye.graphql.test.apps.profile.api;

public class Configuration {

    private String name;
    private String value;
    private ConfigurationEnum configurationEnum;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ConfigurationEnum getConfigurationEnum() {
        return configurationEnum;
    }

    public void setConfigurationEnum(ConfigurationEnum configurationEnum) {
        this.configurationEnum = configurationEnum;
    }

    public static Configuration getByName(String name) {
        Configuration configuration = new Configuration();
        configuration.setName("name");
        configuration.setValue("value");
        configuration.setConfigurationEnum(ConfigurationEnum.testing_thingy);
        return configuration;
    }
}
