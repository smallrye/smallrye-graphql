package io.smallrye.graphql.gradle.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.JarIndexer;
import org.jboss.jandex.Result;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.execution.SchemaPrinter;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;

/**
 * Generate schema task.
 *
 * @author Marcel Overdijk (marceloverdijk@gmail.com)
 */
public class GenerateSchemaTask extends DefaultTask {

    private String destination = new File(getProject().getBuildDir(), "generated/schema.graphql").getPath();
    private boolean includeDependencies = false;
    private boolean includeScalars = false;
    private boolean includeDirectives = false;
    private boolean includeSchemaDefinition = false;
    private boolean includeIntrospectionTypes = false;
    private File classesDir = new File(getProject().getBuildDir(), "classes");

    @Optional
    @OutputFile
    public String getDestination() {
        return destination;
    }

    @Option(description = "The destination file where to output the schema. If no path is specified, the schema will be printed to the log.", option = "destination")
    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Input
    public boolean getIncludeDependencies() {
        return includeDependencies;
    }

    @Option(description = "Whether to scan project's dependencies for GraphQL model classes too. This is off by default, because it takes a relatively long time, so turn this on only if you know that part of your model is located inside dependencies.", option = "include-dependencies")
    public void setIncludeDependencies(boolean includeDependencies) {
        this.includeDependencies = includeDependencies;
    }

    @Input
    public boolean getIncludeScalars() {
        return includeScalars;
    }

    @Option(description = "Whether to include the scalars in the generated schema.", option = "include-scalars")
    public void setIncludeScalars(boolean includeScalars) {
        this.includeScalars = includeScalars;
    }

    @Input
    public boolean getIncludeDirectives() {
        return includeDirectives;
    }

    @Option(description = "Whether to include the directives in the generated schema.", option = "include-directives")
    public void setIncludeDirectives(boolean includeDirectives) {
        this.includeDirectives = includeDirectives;
    }

    @Input
    public boolean getIncludeSchemaDefinition() {
        return includeSchemaDefinition;
    }

    @Option(description = "Whether to include the schema definition in the generated schema.", option = "include-schema-definition")
    public void setIncludeSchemaDefinition(boolean includeSchemaDefinition) {
        this.includeSchemaDefinition = includeSchemaDefinition;
    }

    @Input
    public boolean getIncludeIntrospectionTypes() {
        return includeIntrospectionTypes;
    }

    @Option(description = "Whether to include the introspection types in the generated schema.", option = "include-introspection-types")
    public void setIncludeIntrospectionTypes(boolean includeIntrospectionTypes) {
        this.includeIntrospectionTypes = includeIntrospectionTypes;
    }

    @Optional
    @InputDirectory
    public File getClassesDir() {
        return classesDir;
    }

    public void setClassesDir(File classesDir) {
        this.classesDir = classesDir;
    }

    @TaskAction
    public void generateSchema() {
        IndexView index = createIndex();
        String schema = generateSchema(index);
        write(schema);
    }

    private IndexView createIndex() {
        IndexView moduleIndex;
        try {
            moduleIndex = indexModuleClasses();
        } catch (IOException e) {
            throw new GradleException("Can't compute index", e);
        }
        if (includeDependencies) {
            List<IndexView> indexes = new ArrayList<>();
            indexes.add(moduleIndex);
            Collection<Configuration> configurations = getProject().getConfigurations();
            for (Configuration configuration : configurations) {
                if (configuration.isCanBeResolved()) {
                    ResolvedConfiguration resolvedConfiguration = configuration.getResolvedConfiguration();
                    Set<ResolvedArtifact> artifacts = resolvedConfiguration.getResolvedArtifacts();
                    for (ResolvedArtifact artifact : artifacts) {
                        getLogger().debug("Indexing file " + artifact.getFile());
                        try {
                            Result result = JarIndexer.createJarIndex(artifact.getFile(), new Indexer(), false, false, false);
                            indexes.add(result.getIndex());
                        } catch (Exception e) {
                            getLogger().error("Can't compute index of " + artifact.getFile().getAbsolutePath() + ", skipping",
                                    e);
                        }
                    }
                }
            }
            return CompositeIndex.create(indexes);
        } else {
            return moduleIndex;
        }
    }

    private Index indexModuleClasses() throws IOException {
        Indexer indexer = new Indexer();
        List<Path> classFiles = Files.walk(classesDir.toPath())
                .filter(path -> path.toString().endsWith(".class"))
                .collect(Collectors.toList());
        for (Path path : classFiles) {
            indexer.index(Files.newInputStream(path));
        }
        return indexer.complete();
    }

    private String generateSchema(IndexView index) {
        Config config = new Config() {
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
        };
        Schema internalSchema = SchemaBuilder.build(index);
        GraphQLSchema graphQLSchema = Bootstrap.bootstrap(internalSchema);
        return new SchemaPrinter(config).print(graphQLSchema);
    }

    private void write(String schema) {
        try {
            if (destination == null || destination.isEmpty()) {
                // no destination file specified => print to stdout
                getLogger().info(schema);
            } else {
                Path path = new File(destination).toPath();
                path.toFile().getParentFile().mkdirs();
                Files.write(path, schema.getBytes(),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
                getLogger().info("Wrote the schema to " + path.toAbsolutePath().toString());
            }
        } catch (IOException e) {
            throw new GradleException("Can't write the result", e);
        }
    }
}
