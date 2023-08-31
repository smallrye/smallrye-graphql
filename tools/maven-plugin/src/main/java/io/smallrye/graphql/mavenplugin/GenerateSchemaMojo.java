package io.smallrye.graphql.mavenplugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import io.smallrye.graphql.execution.SchemaPrinter;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.helper.TypeAutoNameStrategy;
import io.smallrye.graphql.schema.model.Schema;

@Mojo(name = "generate-schema", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GenerateSchemaMojo extends AbstractMojo {
    private static MavenConfig mavenConfig;

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

    /**
     * When you include dependencies, we only look at compile and system scopes (by default)
     * You can change that here.
     * Valid options are: compile, provided, runtime, system, test, import
     */
    @Parameter(defaultValue = "compile,system", property = "includeDependenciesScopes")
    private List<String> includeDependenciesScopes;

    /**
     * When you include dependencies, we only look at jars (by default)
     * You can change that here.
     */
    @Parameter(defaultValue = "jar", property = "includeDependenciesTypes")
    private List<String> includeDependenciesTypes;

    /**
     * When you include dependencies, we include all dependencies by default.
     * You can change that here.
     */
    @Parameter(defaultValue = "", property = "includeDependenciesGroupIds")
    private List<String> includeDependenciesGroupIds;

    @Parameter(defaultValue = "false", property = "includeScalars")
    private boolean includeScalars;

    @Parameter(defaultValue = "false", property = "includeDirectives")
    private boolean includeDirectives;

    @Parameter(defaultValue = "false", property = "includeSchemaDefinition")
    private boolean includeSchemaDefinition;

    @Parameter(defaultValue = "false", property = "includeIntrospectionTypes")
    private boolean includeIntrospectionTypes;

    @Parameter(defaultValue = "Default", property = "typeAutoNameStrategy")
    private String typeAutoNameStrategy;

    @Parameter(defaultValue = "${project}", required = true)
    private MavenProject mavenProject;

    @Parameter(property = "project.compileClasspathElements", required = true, readonly = true)
    private List<String> classpath;

    @Parameter(defaultValue = "false", property = "skip")
    private boolean skip;

    /**
     * Compiled classes of the project.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", property = "classesDir")
    private File classesDir;

    /**
     * @return the configuration set by the plugin's parameters
     */
    public static MavenConfig getMavenConfig() {
        return mavenConfig;
    }

    @Override
    public void execute() throws MojoExecutionException {
        mavenConfig = new MavenConfig(includeScalars, includeDirectives, includeSchemaDefinition, includeIntrospectionTypes,
                TypeAutoNameStrategy.valueOf(typeAutoNameStrategy));
        if (!skip) {
            ClassLoader classLoader = getClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);

            IndexView index = createIndex();
            String schema = generateSchema(index);
            if (schema != null) {
                write(schema);
            } else {
                getLog().warn("No Schema generated. Check that your code contains the MicroProfile GraphQL Annotations");
            }
        }
    }

    private IndexView createIndex() throws MojoExecutionException {
        List<IndexView> indexes = new ArrayList<>();
        try {
            IndexView moduleIndex = indexModuleClasses(classesDir);
            indexes.add(moduleIndex);
        } catch (IOException e) {
            throw new MojoExecutionException("Can't compute index", e);
        }

        // always include Mutiny if it is present in the dependencies,
        // even if includeDependencies=false
        Predicate<Artifact> isMutiny = a -> a.getGroupId().equals("io.smallrye.reactive") &&
                a.getArtifactId().equals("mutiny");
        Predicate<Artifact> isSmallRyeGraphQLApi = a -> a.getGroupId().equals("io.smallrye") &&
                a.getArtifactId().equals("smallrye-graphql-api");
        mavenProject.getArtifacts()
                .stream()
                .filter(a -> isMutiny.test((Artifact) a) || isSmallRyeGraphQLApi.test((Artifact) a))
                .forEach(a -> {
                    Result r = indexJar(((Artifact) a).getFile());
                    if (r != null) {
                        indexes.add(r.getIndex());
                    }
                });

        if (includeDependencies) {
            for (Object a : mavenProject.getArtifacts()) {
                Artifact artifact = (Artifact) a;
                if (includeDependenciesScopes.contains(artifact.getScope())
                        && includeDependenciesTypes.contains(artifact.getType())
                        && (includeDependenciesGroupIds.isEmpty() ||
                                includeDependenciesGroupIds.contains(artifact.getGroupId()))
                        && !isMutiny.test(artifact) && !isSmallRyeGraphQLApi.test(artifact)) {

                    Index index = indexArtifact(artifact);
                    if (index != null) {
                        indexes.add(index);
                    }
                }
            }
        }
        return CompositeIndex.create(indexes);
    }

    private Index indexArtifact(Artifact artifact) throws MojoExecutionException {
        if (artifact.getFile().isDirectory()) {
            try {
                return indexModuleClasses(artifact.getFile());
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to index classes of artifact " + artifact, e);
            }
        } else {
            Result result = indexJar(artifact.getFile());

            if (result != null) {
                return result.getIndex();
            } else {
                return null;
            }
        }
    }

    private Result indexJar(File file) {
        try {
            getLog().debug("Indexing file " + file);
            return JarIndexer.createJarIndex(file, new Indexer(),
                    false, false, false);
        } catch (IOException e) {
            getLog().error("Can't compute index of " + file.getAbsolutePath() + ", skipping", e);
            return null;
        }
    }

    // index the classes of a Maven module
    private Index indexModuleClasses(File classesDir) throws IOException {
        Indexer indexer = new Indexer();
        try (Stream<Path> classesDirStream = Files.walk(classesDir.toPath())) {
            List<Path> classFiles = classesDirStream
                    .filter(path -> path.toString().endsWith(".class"))
                    .collect(Collectors.toList());
            for (Path path : classFiles) {
                indexer.index(Files.newInputStream(path));
            }
        }
        return indexer.complete();
    }

    private String generateSchema(IndexView index) {
        Schema internalSchema = SchemaBuilder.build(index, mavenConfig.typeAutoNameStrategy);
        GraphQLSchema graphQLSchema = Bootstrap.bootstrap(internalSchema, true);
        if (graphQLSchema != null) {
            return new SchemaPrinter().print(graphQLSchema);
        }
        return null;
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

    private ClassLoader getClassLoader() {
        Set<URL> urls = new HashSet<>();

        for (String element : classpath) {
            try {
                urls.add(new File(element).toURI().toURL());
            } catch (MalformedURLException ex) {
                throw new RuntimeException(ex);
            }
        }

        return URLClassLoader.newInstance(
                urls.toArray(new URL[0]),
                Thread.currentThread().getContextClassLoader());

    }

    public static class MavenConfig {
        private final boolean includeScalars;
        private final boolean includeDirectives;
        private final boolean includeSchemaDefinition;
        private final boolean includeIntrospectionTypes;
        private final TypeAutoNameStrategy typeAutoNameStrategy;

        public MavenConfig(boolean includeScalars, boolean includeDirectives, boolean includeSchemaDefinition,
                boolean includeIntrospectionTypes, TypeAutoNameStrategy typeAutoNameStrategy) {
            this.includeScalars = includeScalars;
            this.includeDirectives = includeDirectives;
            this.includeSchemaDefinition = includeSchemaDefinition;
            this.includeIntrospectionTypes = includeIntrospectionTypes;
            this.typeAutoNameStrategy = typeAutoNameStrategy;
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

        public TypeAutoNameStrategy getTypeAutoNameStrategy() {
            return typeAutoNameStrategy;
        }
    }
}
