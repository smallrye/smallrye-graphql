package io.smallrye.graphql.gradle.tasks;

import io.smallrye.graphql.spi.config.Config;

public class GradleConfigFacade implements Config {

    private boolean includeScalars;

    private boolean includeDirectives;

    private boolean includeSchemaDefinition;

    private boolean includeIntrospectionTypes;

    private boolean federationEnabled;

    // Constructor used by the ServiceLoader mechanism. When this is called, we assume that GenerateSchemaTask
    // has already produced a config based on the environment, and we just make a copy of it
    public GradleConfigFacade() {
        GradleConfig instance = GenerateSchemaTask.getConfig();
        this.includeScalars = instance.isIncludeScalars();
        this.includeDirectives = instance.isIncludeDirectives();
        this.includeSchemaDefinition = instance.isIncludeSchemaDefinition();
        this.includeIntrospectionTypes = instance.isIncludeIntrospectionTypes();
        this.federationEnabled = instance.isFederationEnabled();
    }

    public GradleConfigFacade(boolean includeScalarsInSchema,
                              boolean includeDirectivesInSchema,
                              boolean includeSchemaDefinitionInSchema,
                              boolean includeIntrospectionTypesInSchema,
                              boolean federationEnabled) {
        this.includeScalars = includeScalarsInSchema;
        this.includeDirectives = includeDirectivesInSchema;
        this.includeSchemaDefinition = includeSchemaDefinitionInSchema;
        this.includeIntrospectionTypes = includeIntrospectionTypesInSchema;
        this.federationEnabled = federationEnabled;
    }

    @Override
    public String getName() {
        return "Gradle Config Service";
    }

    @Override
    public boolean isIncludeScalarsInSchema() {
        return includeScalars;
    }

    @Override
    public boolean isIncludeDirectivesInSchema() {
        return includeDirectives;
    }

    @Override
    public boolean isIncludeSchemaDefinitionInSchema() {
        return includeSchemaDefinition;
    }

    @Override
    public boolean isIncludeIntrospectionTypesInSchema() {
        return includeIntrospectionTypes;
    }

    @Override
    public boolean isFederationEnabled() {
        return federationEnabled;
    }
}
