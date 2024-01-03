SmallRye GraphQL Maven plugin
============

This Maven plugin allows you to generate the GraphQL Schema on build and save it as a text file.

Add this to your pom.xml:
    
```xml
<plugin>
    <artifactId>smallrye-graphql-maven-plugin</artifactId>
    <groupId>io.smallrye</groupId>
    <executions>
        <execution>
            <goals>
                <goal>generate-schema</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

The schema will appear as `target/generated/schema.graphql` by default. The goal is bound to the `process-classes` 
build phase.

Configuration options
=====================

- `destination` - To override the default `target/generated/schema.graphql` destination

- `includeDependencies` - Scan project’s dependencies for GraphQL
model classes too. This is necessary if your GraphQL endpoint exposes
classes that are not in the application's main module. Off (`false`) by default, because
scanning a potentially large dependency tree slows down execution substantially.
Usage of reactive types does not require turning this option on. 

- `includeDependenciesScopes` - If the above `includeDependencies` is
true, you can control what scopes should be included. Default is
`compile, system`

- `includeDependenciesTypes` - If the above `includeDependencies` is
true, you can control what types should be included. Default is
`jar`

- `includeScalars` - Include scalars in the schema. Default false.

- `includeDirectives` - Include directives in the schema. Default false.

- `includeSchemaDefinition` - Include the schema definition. Default false.

- `includeIntrospectionTypes` - Include the introspection types in the schema. Default false.

- `federationEnabled` - Enable GraphQL Federation. You should generally use this
in conjunction with `includeDirectives`. Default false.

- `typeAutoNameStrategy` - Strategy for transforming class names into GraphQL type names. 
Valid values are `MergeInnerClass`, `Full` and`Default`.
