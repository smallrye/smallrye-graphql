package io.smallrye.graphql.gradle.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GradlePluginGenerateSchemaIncludeDirectivesTest {

    private static final File PROJECT_DIR = new File("testing-project");
    private static final File SCHEMA_FILE = new File("testing-project/build/generated/schema.graphql");

    @BeforeEach
    public void cleanup() {
        SCHEMA_FILE.delete();
    }

    @Test
    public void testIncludeDirectives() throws IOException {
        String schema = execute(List.of(
                "-DincludeDirectives=true",
                "-DincludeScalars=true",
                "-DincludeSchemaDefinition=true"));
        assertTrue(schema.contains("directive @skip"), "Directives should be included: " + schema);
        assertTrue(schema.contains("_entities(representations"));
        assertTrue(schema.contains("type Foo @key(fields : \"id\")"));
    }

    private String execute(List<String> arguments) throws IOException {
        List<String> allArguments = new ArrayList<>(arguments);
        allArguments.add("--stacktrace");
        allArguments.add("--debug");
        allArguments.add("clean");
        allArguments.add("compileJava");
        allArguments.add("generateSchema");
        BuildResult result = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(PROJECT_DIR)
                .withArguments(allArguments)
                .forwardOutput()
                .build();

        BuildTask task = result.task(":generateSchema");
        assertNotNull(task, "Task generateSchema might not have been executed for some reason?");
        assertEquals(TaskOutcome.SUCCESS, task.getOutcome());
        assertTrue(SCHEMA_FILE.isFile());
        return String.join("\n", Files.readAllLines(SCHEMA_FILE.toPath()));
    }

}
