package io.smallrye.graphql.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

import io.smallrye.graphql.gradle.tasks.GenerateSchemaTask;

/**
 * SmallRye GraphQL Gradle Plugin.
 *
 * @author Marcel Overdijk (marceloverdijk@gmail.com)
 */
public class SmallRyeGraphQLPlugin implements Plugin<Project> {

    public static final String GENERATE_SCHEMA_TASK_NAME = "generateSchema";

    @Override
    public void apply(Project project) {
        registerTasks(project);
    }

    private void registerTasks(Project project) {
        TaskContainer tasks = project.getTasks();
        tasks.create(GENERATE_SCHEMA_TASK_NAME, GenerateSchemaTask.class);
    }
}
