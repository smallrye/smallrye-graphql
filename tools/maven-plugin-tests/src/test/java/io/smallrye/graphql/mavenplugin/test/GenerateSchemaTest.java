package io.smallrye.graphql.mavenplugin.test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GenerateSchemaTest {

    private final Path SCHEMA_FILE_PATH = Paths.get("testing-project", "target", "generated", "schema.graphql");
    private final Path SCHEMA_FILE_PATH_FEDERATION = Paths.get("testing-project-federation", "target", "generated",
            "schema.graphql");
    private final Path SCHEMA_FILE_PATH_MULTI_MODULE = Paths.get("testing-project-multi-module", "api", "target", "generated",
            "schema.graphql");

    @Before
    public void before() {
        // for safety, delete the schema file before each test
        SCHEMA_FILE_PATH.toFile().delete();
    }

    @Test
    public void testDefaults() throws Exception {
        String schema = execute(Collections.emptyMap(), "testing-project", SCHEMA_FILE_PATH);
        assertThat("Directives should not be included",
                schema, not(containsString("directive @skip")));
        assertThat("Introspection types should not be included",
                schema, not(containsString("type __Schema")));
        assertThat("Schema definition should not be included",
                schema, not(containsString("schema {\n  query: Query")));
        assertThat("Short class names should be used for GraphQL types",
                schema, not(containsString("type org_acme_Foo")));
        assertThat("Federation should be disabled by default",
                schema, not(containsString("type _Service")));
    }

    @Test
    public void testFederationDetectedAutomatically() throws Exception {
        String schema = execute(Collections.emptyMap(), "testing-project-federation", SCHEMA_FILE_PATH_FEDERATION);
        assertThat("Federation should be activated automatically if any related annotations are present",
                schema, containsString("type _Service"));
    }

    @Test
    public void testIncludeDirectives() throws Exception {
        String schema = execute(Collections.singletonMap("includeDirectives", "true"), "testing-project", SCHEMA_FILE_PATH);
        assertThat("Directives should be included",
                schema, containsString("directive @skip"));
    }

    @Test
    public void testIncludeIntrospectionTypes() throws Exception {
        String schema = execute(Collections.singletonMap("includeIntrospectionTypes", "true"), "testing-project",
                SCHEMA_FILE_PATH);
        assertThat("Introspection types should be included",
                schema, containsString("type __Schema"));
    }

    @Test
    public void testIncludeSchemaDefinition() throws Exception {
        String schema = execute(Collections.singletonMap("includeSchemaDefinition", "true"), "testing-project",
                SCHEMA_FILE_PATH);
        assertThat("Schema definition should be included",
                schema, containsString("schema {\n  query: Query"));
    }

    @Test
    public void testTypeAutoNameStrategy() throws Exception {
        String schema = execute(Collections.singletonMap("typeAutoNameStrategy", "Full"), "testing-project", SCHEMA_FILE_PATH);
        assertThat("Fully qualified class names should be used for GraphQL types",
                schema, containsString("type org_acme_Foo"));
    }

    @Test
    public void testFederationEnabledExplicitly() throws Exception {
        String schema = execute(Collections.singletonMap("federationEnabled", "true"), "testing-project", SCHEMA_FILE_PATH);
        assertThat("Federation should be enabled when explicitly requested, even though there are no annotations",
                schema, containsString("type _Service"));
    }

    @Test
    public void testMultiModuleProject() throws Exception {
        SCHEMA_FILE_PATH_MULTI_MODULE.toFile().delete();

        Verifier verifier = new Verifier(new File("testing-project-multi-module").getAbsolutePath());
        verifier.setSystemProperty("plugin.version", System.getProperty("plugin.version"));

        List<String> goals = new ArrayList<>();
        goals.add("clean");
        goals.add("package");
        goals.add("process-classes");
        verifier.executeGoals(goals);

        verifier.verifyErrorFreeLog();
        verifier.verifyTextInLog("Wrote the schema to ");

        Assert.assertTrue("File " + SCHEMA_FILE_PATH_MULTI_MODULE.toAbsolutePath() + " expected but not found",
                SCHEMA_FILE_PATH_MULTI_MODULE.toFile().exists());
    }

    private String execute(Map<String, String> properties,
            String pathToProject,
            Path pathToExpectedGeneratedSchemaFile)
            throws VerificationException, IOException {
        Verifier verifier = new Verifier(new File(pathToProject).getAbsolutePath());
        verifier.setSystemProperty("plugin.version", System.getProperty("plugin.version"));
        properties.forEach(verifier::setSystemProperty);

        List<String> goals = new ArrayList<>();
        goals.add("clean");
        goals.add("package");
        goals.add("io.smallrye:smallrye-graphql-maven-plugin:generate-schema");
        verifier.executeGoals(goals);

        verifier.verifyErrorFreeLog();
        verifier.verifyTextInLog("Wrote the schema to ");

        Assert.assertTrue("File " + pathToExpectedGeneratedSchemaFile.toAbsolutePath() + " expected but not found",
                pathToExpectedGeneratedSchemaFile.toFile().exists());
        return String.join("\n", Files.readAllLines(pathToExpectedGeneratedSchemaFile));
    }
}
