package io.smallrye.graphql.gradle.tasks;

public class GradleConfig {

    private final boolean includeScalars;

    private final boolean includeDirectives;

    private final boolean includeSchemaDefinition;

    private final boolean includeIntrospectionTypes;

    public GradleConfig(boolean includeScalars, boolean includeDirectives, boolean includeSchemaDefinition, boolean includeIntrospectionTypes) {
        this.includeScalars = includeScalars;
        this.includeDirectives = includeDirectives;
        this.includeSchemaDefinition = includeSchemaDefinition;
        this.includeIntrospectionTypes = includeIntrospectionTypes;
    }

    public boolean isIncludeScalars() {
        return includeScalars;
    }

    public boolean isIncludeDirectives() {
        return includeDirectives;
    }

    public boolean isIncludeSchemaDefinition() {
        return includeSchemaDefinition;
    }

    public boolean isIncludeIntrospectionTypes() {
        return includeIntrospectionTypes;
    }
}
