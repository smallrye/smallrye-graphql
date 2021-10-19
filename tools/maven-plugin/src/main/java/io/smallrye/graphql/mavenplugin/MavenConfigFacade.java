package io.smallrye.graphql.mavenplugin;

import io.smallrye.graphql.spi.config.Config;

/**
 * A hack that allows to use this class a service-provider for the {@link Config} interface (otherwise, to have
 * access to the MOJO's parameters, this class should have been an inner-class of {@link GenerateSchemaMojo}, which
 * isn't allowed for a service-provider).
 * <p>
 * Parameters which are injected to the MOJO are stored in {@link GenerateSchemaMojo} as a static field and later
 * been wrapped by this facade.
 */
public class MavenConfigFacade implements Config {

    /**
     * No-args constructor to be used by a {@link java.util.ServiceLoader}.
     */
    public MavenConfigFacade() {
        this.mavenConfig = GenerateSchemaMojo.getMavenConfig();
    }

    private final GenerateSchemaMojo.MavenConfig mavenConfig;

    @Override
    public String getName() {
        return "Maven Config Service";
    }

    @Override
    public boolean isIncludeScalarsInSchema() {
        return mavenConfig.isIncludeScalars();
    }

    @Override
    public boolean isIncludeDirectivesInSchema() {
        return mavenConfig.isIncludeDirectives();
    }

    @Override
    public boolean isIncludeSchemaDefinitionInSchema() {
        return mavenConfig.isIncludeSchemaDefinition();
    }

    @Override
    public boolean isIncludeIntrospectionTypesInSchema() {
        return mavenConfig.isIncludeIntrospectionTypes();
    }
}
