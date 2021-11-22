Configuration properties
========================

These are properties understood directly by SmallRye GraphQL. If you're using SmallRye GraphQL through
Quarkus, these will generally work, but Quarkus offers its own counterparts for most of these, 
and it is recommended to use the `quarkus.*` properties. Refer to the Quarkus documentation: 
[Server](https://quarkus.io/guides/all-config#quarkus-smallrye-graphql_quarkus-smallrye-graphql-smallrye-graphql) and
[Client](https://quarkus.io/guides/all-config#quarkus-smallrye-graphql-client_quarkus-smallrye-graphql-client-smallrye-graphql-client)
side Quarkus properties.

From MicroProfile GraphQL
-------------------------

| Property | Default value | Meaning |
| ------------ | ------------- | ------------ |
| `mp.graphql.defaultErrorMessage` | `Server Error`  | Error message for hidden exceptions |
| `mp.graphql.hideErrorMessage` | Runtime exceptions  | Exceptions that will be hidden |
| `mp.graphql.showErrorMessage` | Checked exceptions  | Exceptions that will not be hidden. |

From SmallRye GraphQL
---------------------


| Property | Default value | Meaning |
| ------------ | ------------- | ------------ |
| `smallrye.graphql.printDataFetcherException` | `false`  | Include the stacktrace of the data fetching exception in the log output |
| `smallrye.graphql.allowGet` | `false`  | Allow HTTP GET Method |
| `smallrye.graphql.metrics.enabled` | `false` | Enable metrics |
| `smallrye.graphql.tracing.enabled` | `false` | Enable tracing |
| `smallrye.graphql.validation.enabled` | `false` | Enable Bean Validation |
| `smallrye.graphql.events.enabled`| `true` if one of metrics, tracing or bean validation is true | Enable eventing |
| `smallrye.graphql.logPayload`| `false` | Log the payload in the log file |
| `smallrye.graphql.fieldVisibility` |   | To control the field visibility on introspection |
| `smallrye.graphql.schema.includeScalars`| `true` | Include Scalar definitions in the schema |
| `smallrye.graphql.schema.includeSchemaDefinition` | `false` | Include Schema definition |
| `smallrye.graphql.schema.includeDirectives` | `false` | Include directives in the schema |
| `smallrye.graphql.schema.includeIntrospectionTypes` | `false` |  Include Introspection types in the schema |