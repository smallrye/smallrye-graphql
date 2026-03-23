# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SmallRye GraphQL is a Java implementation of Eclipse MicroProfile GraphQL, GraphQL over HTTP, GraphQL over WebSocket (both `graphql-ws` and `graphql-transport-ws` subprotocols), and Apollo GraphQL Federation. It targets the Jakarta namespace (not javax — enforced by maven-enforcer-plugin banning `javax.*`). Java 17+ is required.

## Build Commands

```bash
# Full build with tests
mvn clean install

# CI build (what GitHub Actions runs on JDK 17, 21, and 25)
mvn -B formatter:validate impsort:check javadoc:javadoc install

# Build without tests
mvn clean install -DskipTests

# Build a single module (e.g. server implementation)
mvn clean install -pl server/implementation

# Build a module and its dependencies
mvn clean install -pl server/implementation -am

# Run a single test class
mvn test -pl server/implementation -Dtest=ExecutionTest

# Run a single test method
mvn test -pl server/implementation -Dtest=ExecutionTest#testBasicQuery

# Build with coverage (for Sonar)
mvn clean install -Pcoverage

# Locale-sensitive tests may fail on non-English locales
LANG=C mvn clean install
```

## Code Quality Enforcement

The CI enforces these checks — run before submitting PRs:
- **Formatter**: `mvn formatter:validate` — code formatting rules
- **Import sorting**: `mvn impsort:check` — import statement ordering
- **Javadoc**: `mvn javadoc:javadoc` — Javadoc must compile

Fix formatting/imports automatically: `mvn formatter:format impsort:sort`

## Module Architecture

```
common/
  schema-model/     -> Serializable schema model (Schema, Type, Field, Operation, etc.)
  schema-builder/   -> Builds schema model from Jandex bytecode index (not reflection)

server/
  api/              -> SmallRye-specific API annotations extending MicroProfile GraphQL
  implementation/   -> Core server: Bootstrap, ExecutionService, DataFetchers, scalar types
  implementation-cdi/   -> CDI-based GraphQL endpoint discovery and config via MicroProfile Config
  implementation-servlet/ -> Servlet transport (ExecutionServlet, SchemaServlet, WebSocket)
  tck/              -> MicroProfile GraphQL TCK runner
  runner/           -> Manual TCK testing with GraphiQL
  integration-tests/    -> Integration tests (Arquillian + Jetty)


client/
  api/              -> MicroProfile GraphQL Client API
  implementation/   -> Core client: typesafe client, dynamic client, response reader
  implementation-vertx/ -> Vert.x-based HTTP transport for client
  generator/        -> Code generator (Java types from GraphQL schema)
  generator-test/   -> Generator integration tests
  tck/              -> Client TCK runner

tools/
  maven-plugin/     -> Generates GraphQL schema from compiled classes at build time (process-classes phase)
  maven-plugin-tests/ -> Maven plugin integration tests
  gradle-plugin/    -> Same for Gradle

ui/graphiql/        -> GraphiQL UI component
```

## Key Architectural Concepts

**Schema Pipeline**: Annotated Java classes -> Jandex index -> `SchemaBuilder.build(IndexView)` -> `Schema` model -> `Bootstrap.bootstrap(Schema)` -> graphql-java `GraphQLSchema`

**Server execution flow**: HTTP request -> `ExecutionServlet` (or WebSocket) -> `ExecutionService` -> graphql-java engine -> DataFetchers (reflection-based method invocation) -> response JSON

**Client modes**: Two client styles exist:
- **Typesafe client**: Java interface with `@GraphQLClientApi`, methods map to operations
- **Dynamic client**: String-based queries via `DynamicGraphQLClient`

**Reactive support**: Mutiny (`Uni<T>`, `Multi<T>`) is the reactive library. `Multi<T>` is used for subscriptions over WebSocket. `CompletionStage<T>` and Reactive Streams `Publisher<T>` are also supported via dedicated DataFetcher implementations.

**Federation**: Apollo Federation support via `graphql-java-federation` library, integrated in `Bootstrap`.

**SPI extensibility**: The server uses a Service Provider Interface pattern (`spi/` package) for pluggable lookup (`LookupService`), class loading (`ClassloadingService`), eventing (`EventingService`), metrics (`MetricsService`), and data fetching (`DataFetcherService`). The CDI module provides the CDI-based implementations.

## Key Base Packages

- `io.smallrye.graphql.schema.model` — Schema model classes (common/schema-model)
- `io.smallrye.graphql.schema` — Schema builder from Jandex (common/schema-builder)
- `io.smallrye.graphql.bootstrap` — Converts schema model to graphql-java executable schema
- `io.smallrye.graphql.execution` — Server runtime (ExecutionService, DataFetchers, error handling)
- `io.smallrye.graphql.api` — Server API annotations, federation annotations (`api.federation` subpackage)
- `io.smallrye.graphql.client.impl` — Client implementation (typesafe, dynamic, core)
- `io.smallrye.graphql.entry.http` — Servlet entry points (ExecutionServlet, SchemaServlet)

## Testing

- **JUnit 5** (Jupiter) with **Mockito** for unit tests
- **Arquillian** with embedded **Jetty** for server integration tests
- **Weld** for CDI tests (`weld-junit5`)
- Server execution tests typically extend `ExecutionTestBase` and use test schemas defined in test resources
- The `server/tck` and `client/tck` modules run the official MicroProfile GraphQL TCK
- Integration tests in `server/integration-tests/` cover client (dynamic + typesafe), subscriptions, context propagation, error handling, and HTTP transport

## Important Conventions

- Jakarta namespace only — the build enforces a ban on `javax.*` dependencies via maven-enforcer-plugin
- Logging uses `jboss-logging` with message localization (`SmallRyeGraphQLServerLogging`, `SmallRyeGraphQLServerMessages`)
- JSON binding uses JSON-B (Yasson implementation), not Jackson
- The `.mvn/` directory is gitignored and may contain local Maven settings (repo path, mirrors)
- The graphql-java library (currently v21.1) is the underlying GraphQL execution engine
