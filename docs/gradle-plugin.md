SmallRye GraphQL Gradle plugin
=======================

This Gradle plugin allows you to generate the GraphQL Schema on build and save it as a text file.


Add this to your build.gradle:

    plugins {
        id 'io.smallrye.graphql' version 'PLUGIN_VERSION'
    }

The list of existing plugin versions (to substitute the
`PLUGIN_VERSION` placeholder) can be found
[here](https://plugins.gradle.org/plugin/io.smallrye.graphql).

The schema will appear as `build/generated/schema.graphql` by default.

The plugin’s `generateSchema` task can customized like:

    generateSchema {
        destination = null
        includeTransitiveDependencies = true
        includeScalars = true
    }

This will include the project’s dependencies (including the transitive
dependencies) to scan for GraphQL model classes. As the destination is
set to `null` the generated schema will only be output to the console
and saved to a file.

In case external dependencies need to be scanned for GraphQL model
classes a more advanced configuration could look like:

    configrations {
        graphQLSchema
    }

    dependencies {
        graphQLSchema ..
    }

    generateSchema {
        includeDependencies = true
        configurations = ["graphQLSchema"]
    }

This defines a separate `graphQLSchema` configuration. In the
dependencies block the external dependencies containing the GraphQL mode
class should then be registered for the `graphQLSchema` configuration.
The `generateSchema` task is then customized to only include
dependencies from the `graphQLSchema` configuration. Also note that we
set `includeDependencies` to `true` (and not
`includeTransitiveDependencies`). This will make sure only direct
dependencies will be be scanned and not possible transitive
dependencies.

Configuration options
=====================

-   `destination` - To override the default
    `build/generated/schema.graphql` destination.

-   `includeDependencies` - Scan project’s dependencies for GraphQL
    model classes too. Default false.

-   `includeTransitiveDependencies` - Scan project’s dependencies
    (including the transitive dependencies) for GraphQL model classes
    too. Default false.

-   `configurations` - If the above `includeDependencies` or
    `includeTransitiveDependencies` is true, you can control what
    configurations should be included. Default is `implementation`.

-   `dependencyExtensions` - If the above `includeDependencies` or
    `includeTransitiveDependencies` is true, you can control what
    dependency extensions should be included. Default is `jar`.

-   `includeScalars` - Include scalars in the schema. Default false.

-   `includeDirectives` - Include directives in the schema. Default
    false.

-   `includeSchemaDefinition` - Include the schema definition. Default
    false.

-   `includeIntrospectionTypes` - Include the introspection types in the
    schema. Default false.
