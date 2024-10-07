package io.smallrye.graphql.gradle.tasks;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Repeatable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
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
import io.smallrye.graphql.api.Deprecated;
import io.smallrye.graphql.api.Entry;
import io.smallrye.graphql.api.OneOf;
import io.smallrye.graphql.api.federation.Authenticated;
import io.smallrye.graphql.api.federation.ComposeDirective;
import io.smallrye.graphql.api.federation.Extends;
import io.smallrye.graphql.api.federation.External;
import io.smallrye.graphql.api.federation.FieldSet;
import io.smallrye.graphql.api.federation.Inaccessible;
import io.smallrye.graphql.api.federation.InterfaceObject;
import io.smallrye.graphql.api.federation.Key;
import io.smallrye.graphql.api.federation.Override;
import io.smallrye.graphql.api.federation.Provides;
import io.smallrye.graphql.api.federation.Requires;
import io.smallrye.graphql.api.federation.Shareable;
import io.smallrye.graphql.api.federation.Tag;
import io.smallrye.graphql.api.federation.link.Import;
import io.smallrye.graphql.api.federation.link.Link;
import io.smallrye.graphql.api.federation.link.Purpose;
import io.smallrye.graphql.api.federation.policy.Policy;
import io.smallrye.graphql.api.federation.policy.PolicyGroup;
import io.smallrye.graphql.api.federation.policy.PolicyItem;
import io.smallrye.graphql.api.federation.requiresscopes.RequiresScopes;
import io.smallrye.graphql.api.federation.requiresscopes.ScopeGroup;
import io.smallrye.graphql.api.federation.requiresscopes.ScopeItem;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.execution.SchemaPrinter;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;

import static io.smallrye.graphql.gradle.tasks.FederationDotNames.FEDERATION_DIRECTIVES_NAMES;

/**
 * Generate schema task.
 *
 * @author Marcel Overdijk (marceloverdijk@gmail.com)
 */
public class GenerateSchemaTask extends DefaultTask {

    private String destination = new File(getProject().getBuildDir(), "generated/schema.graphql").getPath();
    private boolean includeDependencies = false;
    private boolean includeTransitiveDependencies = false;
    private List<String> configurations = Arrays.asList("implementation");
    private List<String> dependencyExtensions = Arrays.asList("jar");
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

    @Option(option = "destination", description = "The destination file where to output the schema. If no path is specified, the schema will be printed to the log.")
    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Option(option = "no-destination", description = "Prints the schema to the log.")
    public void setNoDestination(boolean destination) {
        this.destination = null;
    }

    @Input
    public boolean getIncludeDependencies() {
        return includeDependencies;
    }

    @Option(option = "include-dependencies", description = "Whether to scan project's dependencies for GraphQL model classes too. This is off by default, because it takes a relatively long time, so turn this on only if you know that part of your model is located inside dependencies.")
    public void setIncludeDependencies(boolean includeDependencies) {
        this.includeDependencies = includeDependencies;
    }

    @Input
    public boolean getIncludeTransitiveDependencies() {
        return includeTransitiveDependencies;
    }

    @Option(option = "include-transitive-dependencies", description = "Whether to include transitive dependencies to scan for GraphQL model classes.")
    public void setIncludeTransitiveDependencies(boolean includeTransitiveDependencies) {
        this.includeTransitiveDependencies = includeTransitiveDependencies;
        if (includeTransitiveDependencies) {
            this.includeDependencies = true;
        }
    }

    @Input
    public List<String> getConfigurations() {
        return configurations;
    }

    @Option(option = "configuration", description = "Configuration to scan for GraphQL model classes (can be specified more than once).")
    public void setConfigurations(List<String> configurations) {
        this.configurations = configurations;
    }

    @Input
    public List<String> getDependencyExtensions() {
        return dependencyExtensions;
    }

    @Option(option = "dependency-extension", description = "Dependency extension to scan for GraphQL mode classes (can be specified more than once).")
    public void setDependencyExtensions(List<String> dependencyExtensions) {
        this.dependencyExtensions = dependencyExtensions;
    }

    @Input
    public boolean getIncludeScalars() {
        return includeScalars;
    }

    @Option(option = "include-scalars", description = "Whether to include the scalars in the schema.")
    public void setIncludeScalars(boolean includeScalars) {
        this.includeScalars = includeScalars;
    }

    @Input
    public boolean getIncludeDirectives() {
        return includeDirectives;
    }

    @Option(option = "include-directives", description = "Whether to include the directives in the schema.")
    public void setIncludeDirectives(boolean includeDirectives) {
        this.includeDirectives = includeDirectives;
    }

    @Input
    public boolean getIncludeSchemaDefinition() {
        return includeSchemaDefinition;
    }

    @Option(option = "include-schema-definition", description = "Whether to include the schema definition in the schema.")
    public void setIncludeSchemaDefinition(boolean includeSchemaDefinition) {
        this.includeSchemaDefinition = includeSchemaDefinition;
    }

    @Input
    public boolean getIncludeIntrospectionTypes() {
        return includeIntrospectionTypes;
    }

    @Option(option = "include-introspection-types", description = "Whether to include the introspection types in the schema.")
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

    private static GradleConfig config;

    public static GradleConfig getConfig() {
        return config;
    }

    @TaskAction
    public void generateSchema() throws Exception {
        config = new GradleConfig(includeScalars, includeDirectives, includeSchemaDefinition, includeIntrospectionTypes);
        ClassLoader classLoader = getClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        IndexView index = createIndex();

        if (hasFederationDirectives(index)) {
            config.setFederationEnabled(true);
            System.setProperty("smallrye.graphql.federation.enabled", "true");
            index = CompositeIndex.create(index, createFederationApiIndex());
        }

        String schema = generateSchema(index);
        if (schema != null) {
            write(schema);
        } else {
            getLogger().warn("No Schema generated. Check that your code contains the MicroProfile GraphQL Annotations");
        }
    }

    private static boolean hasFederationDirectives(IndexView index) {
        return index.getKnownClasses().stream()
                .anyMatch(classInfo -> FEDERATION_DIRECTIVES_NAMES.stream()
                        .anyMatch(classInfo::hasAnnotation)
                );
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
            ConfigurationContainer configurationContainer = getProject().getConfigurations();
            for (String name : configurations) {
                Configuration configuration = configurationContainer.getByName(name);
                Configuration copiedConfiguration = configuration.copyRecursive();
                copiedConfiguration.setCanBeResolved(true);
                copiedConfiguration.setTransitive(includeTransitiveDependencies);
                ResolvedConfiguration resolvedConfiguration = copiedConfiguration.getResolvedConfiguration();
                Set<ResolvedArtifact> artifacts = resolvedConfiguration.getResolvedArtifacts();
                for (ResolvedArtifact artifact : artifacts) {
                    if (dependencyExtensions.contains(artifact.getExtension())) {
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

    private IndexView createFederationApiIndex() throws IOException {
        Indexer indexer = new Indexer();
        indexer.indexClass(Map.class);
        indexer.indexClass(Entry.class);
        indexer.indexClass(Repeatable.class);

        indexer.indexClass(Deprecated.class);
        indexer.indexClass(OneOf.class);

        // directives from the API module
        indexer.indexClass(Authenticated.class);
        indexer.indexClass(ComposeDirective.class);
        indexer.indexClass(Extends.class);
        indexer.indexClass(External.class);
        indexer.indexClass(FieldSet.class);
        indexer.indexClass(Inaccessible.class);
        indexer.indexClass(InterfaceObject.class);
        indexer.indexClass(Key.class);
        indexer.indexClass(Override.class);
        indexer.indexClass(Provides.class);
        indexer.indexClass(Requires.class);
        indexer.indexClass(Shareable.class);
        indexer.indexClass(Tag.class);

        indexer.indexClass(Link.class);
        indexer.indexClass(Import.class);
        indexer.indexClass(Purpose.class);

        indexer.indexClass(Policy.class);
        indexer.indexClass(PolicyGroup.class);
        indexer.indexClass(PolicyItem.class);

        indexer.indexClass(RequiresScopes.class);
        indexer.indexClass(ScopeGroup.class);
        indexer.indexClass(ScopeItem.class);

        return indexer.complete();
    }

    // index the classes of this Gradle module
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
        Schema internalSchema = SchemaBuilder.build(index);
        GraphQLSchema graphQLSchema = Bootstrap.bootstrap(internalSchema, true);
        if(graphQLSchema!=null){
            return new SchemaPrinter().print(graphQLSchema);
        }
        return null;
    }

    private void write(String schema) {
        try {
            if (destination == null || destination.isEmpty()) {
                // no destination file specified => print to stdout
                getLogger().quiet(schema);
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

    private ClassLoader getClassLoader() throws MalformedURLException {
        Set<URL> urls = new HashSet<>();
        ConfigurationContainer configurationContainer = getProject().getConfigurations();
        for (String name : configurations) {
            Configuration configuration = configurationContainer.getByName(name);
            Configuration copiedConfiguration = configuration.copyRecursive();
            copiedConfiguration.setCanBeResolved(true);
            copiedConfiguration.setTransitive(includeTransitiveDependencies);
            ResolvedConfiguration resolvedConfiguration = copiedConfiguration.getResolvedConfiguration();
            Set<ResolvedArtifact> artifacts = resolvedConfiguration.getResolvedArtifacts();
            for (ResolvedArtifact artifact : artifacts) {
                if (dependencyExtensions.contains(artifact.getExtension())) {
                    getLogger().debug("Adding to classloader: " + artifact.getFile());
                    urls.add(artifact.getFile().toURI().toURL());
                }
            }
        }
        Path classes = Paths.get(classesDir.getAbsolutePath(), "java", "main");
        if(classes.toFile().exists()) {
            getLogger().debug("Adding classes directory: " + classes);
            urls.add(classes.toUri().toURL());
        }

        Path classesKotlin = Paths.get(classesDir.getAbsolutePath(), "kotlin", "main");
        if(classesKotlin.toFile().exists()) {
            getLogger().debug("Adding classes directory: " + classesKotlin);
            urls.add(classesKotlin.toUri().toURL());
        }

        return URLClassLoader.newInstance(
            urls.toArray(new URL[0]),
            Thread.currentThread().getContextClassLoader());

    }

}
