package io.smallrye.graphql.mavenplugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
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

@Mojo(name = "generate-schema", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GenerateSchemaMojo extends AbstractMojo {

    /**
     * Destination file where to output the schema.
     * If no path is specified, the schema will be printed to the log.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated/schema.graphql", property = "destination")
    private String destination;

    /**
     * Scan project's dependencies for GraphQL model classes too. This is off by default, because
     * it takes a relatively long time, so turn this on only if you know that part of your
     * model is located inside dependencies.
     */
    @Parameter(defaultValue = "false", property = "includeDependencies")
    private boolean includeDependencies;

    @Parameter(defaultValue = "false", property = "includeScalars")
    private boolean includeScalars;

    @Parameter(defaultValue = "false", property = "includeDirectives")
    private boolean includeDirectives;

    @Parameter(defaultValue = "false", property = "includeSchemaDefinition")
    private boolean includeSchemaDefinition;

    @Parameter(defaultValue = "false", property = "includeIntrospectionTypes")
    private boolean includeIntrospectionTypes;

    @Parameter(defaultValue = "${project}")
    private MavenProject mavenProject;

    /**
     * Compiled classes of the project.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", property = "classesDir")
    private File classesDir;

    @Override
    public void execute() throws MojoExecutionException {
        IndexView index = createIndex();
        String schema = generateSchema(index);
        write(schema);
    }

    private IndexView createIndex() throws MojoExecutionException {
        IndexView moduleIndex;
        try {
            moduleIndex = indexModuleClasses();
        } catch (IOException e) {
            throw new MojoExecutionException("Can't compute index", e);
        }
        if (includeDependencies) {
            List<IndexView> indexes = new ArrayList<>();
            indexes.add(moduleIndex);
            for (Object a : mavenProject.getArtifacts()) {
                Artifact artifact = (Artifact) a;
                getLog().debug("Indexing file " + ((Artifact) a).getFile());
                try {
                    Result result = JarIndexer.createJarIndex(artifact.getFile(), new Indexer(),
                            false, false, false);
                    indexes.add(result.getIndex());
                } catch (Exception e) {
                    getLog().error("Can't compute index of " + artifact.getFile().getAbsolutePath() + ", skipping", e);
                }
            }
            return CompositeIndex.create(indexes);
        } else {
            return moduleIndex;
        }
    }

    // index the classes of this Maven module
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

    private void write(String schema) throws MojoExecutionException {
        try {
            if (destination == null || destination.isEmpty()) {
                // no destination file specified => print to stdout
                getLog().info(schema);
            } else {
                Path path = new File(destination).toPath();
                path.toFile().getParentFile().mkdirs();
                Files.write(path, schema.getBytes(),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
                getLog().info("Wrote the schema to " + path.toAbsolutePath().toString());
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Can't write the result", e);
        }
    }

}
