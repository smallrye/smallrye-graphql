# SmallRye GraphQL: JSON-B to Jackson Migration

**Date:** 2026-06-15
**Target versions:** SmallRye GraphQL 3.0, Quarkus 4.0
**Status:** Draft

## Overview

Migrate SmallRye GraphQL (server and client) and the corresponding Quarkus extensions from JSON-B (Jakarta JSON Binding / Yasson) and JSON-P (Jakarta JSON Processing) to Jackson for all JSON processing. Backward compatibility with JSON-B annotations on user POJOs is preserved via a custom Jackson module. This is a major version bump with intentional breaking changes to public APIs.

## Decisions

| # | Decision | Rationale |
|---|----------|-----------|
| 1 | `EventingService.overrideJsonbConfig()` replaced with Jackson types (breaking) | Clean break, no dual-API maintenance |
| 2 | Client API (`Request`, `Response`) migrated to Jackson types (breaking) | Full migration, no JSON-P dependency |
| 3 | JSON-P response building replaced with Jackson tree model | Full migration away from JSON-P |
| 4 | JSON-P dependency (`jakarta.json-api`) removed entirely | No partial migration |
| 5 | JSON-B annotations honored at runtime via custom Jackson module | Backward compatibility for user POJOs |
| 6 | SmallRye GraphQL and Quarkus updated in lockstep | Coordinated release |
| 7 | `@JsonbTypeAdapter` removed (build-time warning logged) | Users migrate to `@AdaptWith` or Jackson |
| 8 | Custom scalar serializers rewritten as Jackson serializers | Internal, no compat concern |
| 9 | SmallRye GraphQL version: 3.0.0, Quarkus version: 4.0 | Major version for breaking changes |
| 10 | JSON Patch logic rewritten directly, no new dependency | Narrow usage doesn't justify a library |
| 11 | Jackson converter annotation support added to `AdaptWithHelper` | Parity with JSON-B adapter support |

## Architecture: Direct Jackson Replacement (Approach A)

Replace all JSON-B and JSON-P usage directly with Jackson APIs. Build a shared `jackson-jsonb-compat` module containing a custom Jackson `Module` that introspects JSON-B annotations and registers equivalent Jackson behavior. No abstraction layers.

---

## 1. Jackson JSON-B Compatibility Module

### New module: `common/jackson-jsonb-compat`

A custom Jackson `Module` that teaches Jackson to understand JSON-B annotations on user POJOs.

**Package:** `io.smallrye.graphql.jackson.jsonb`

**Key classes:**
- `JsonbCompatModule` — Jackson `Module` that registers the annotation introspector
- `JsonbAnnotationIntrospector` — reads JSON-B annotations, returns Jackson equivalents:
  - `@JsonbProperty("name")` → treated as `@JsonProperty("name")`
  - `@JsonbProperty(nillable=true)` → include-non-null override
  - `@JsonbTransient` → treated as `@JsonIgnore`
  - `@JsonbCreator` → treated as `@JsonCreator`
  - `@JsonbNillable` (class-level) → changes null inclusion for the class
  - `@JsonbDateFormat(value, locale)` → registers per-field date format via custom serializer/deserializer
  - `@JsonbNumberFormat(value, locale)` → registers per-field number format via custom serializer/deserializer
  - `@JsonbTypeInfo` / `@JsonbSubtype` → mapped to Jackson polymorphic type handling (`@JsonTypeInfo` / `@JsonSubTypes` behavior)
- `JsonbDateFormatSerializer` / `JsonbDateFormatDeserializer` — handle `@JsonbDateFormat`
- `JsonbNumberFormatSerializer` / `JsonbNumberFormatDeserializer` — handle `@JsonbNumberFormat`

**Dependencies:**
- `com.fasterxml.jackson.core:jackson-databind` (compile)
- `jakarta.json.bind:jakarta.json.bind-api` (compile — annotation classes only, no Yasson runtime)

The module is registered on every `ObjectMapper` created by SmallRye GraphQL (server and client).

---

## 2. Server-Side Migration

### 2A. Input Deserialization

**Current flow:** `Map → Jsonb.toJson(map) → JSON string → Jsonb.fromJson(string, type) → POJO`

**New flow:** `Map → ObjectMapper.convertValue(map, type) → POJO`

Jackson's `convertValue` goes directly from Map to POJO without an intermediate JSON string — a performance improvement.

**Class changes:**
- `JsonBCreator` → `JacksonCreator` (or rename in place)
  - `getJsonB()` → `getObjectMapper()` returning default `ObjectMapper`
  - `getJsonB(String className)` → `getObjectMapper(String className)` returning class-specific `ObjectMapper`
  - Default `ObjectMapper` configured with: JSON-B compat module, custom scalar serializers, null inclusion, pretty printing
  - Per-class `ObjectMapper` instances with custom `PropertyNamingStrategy` (rewritten `GraphQLNamingStrategy` as Jackson naming strategy)
- `JsonInputRegistry` → `InputRegistry` (or rename), delegates to `JacksonCreator`
- `GraphQLNamingStrategy` — rewrite from `jakarta.json.bind.config.PropertyNamingStrategy` to `com.fasterxml.jackson.databind.PropertyNamingStrategy`
- `InputFieldsInfo` — no JSON-B dependency, keeps field transformation/adaptation metadata as-is

**Files affected:**
- `server/implementation/src/main/java/io/smallrye/graphql/json/JsonBCreator.java`
- `server/implementation/src/main/java/io/smallrye/graphql/json/JsonInputRegistry.java`
- `server/implementation/src/main/java/io/smallrye/graphql/json/GraphQLNamingStrategy.java`
- `server/implementation/src/main/java/io/smallrye/graphql/execution/datafetcher/helper/ArgumentHelper.java`
- `server/implementation/src/main/java/io/smallrye/graphql/execution/datafetcher/helper/DefaultMapAdapter.java`
- `server/implementation/src/main/java/io/smallrye/graphql/bootstrap/Bootstrap.java`

### 2B. Response Building

Replace JSON-P builders with Jackson tree model.

- `ExecutionResponse`: `JsonObjectBuilder`/`JsonArrayBuilder`/`JsonValue` → `ObjectNode`/`ArrayNode`/`JsonNode`
  - `toJsonValue(Object pojo)` → `toJsonNode(Object pojo)` using `ObjectMapper.valueToTree()` or manual node construction
  - `getExecutionResultAsString()` uses `ObjectMapper.writeValueAsString()` instead of JSON-P writer
- `ExecutionErrorsService`: `Jsonb.toJson(error.toSpecification())` + `JsonReader` → `ObjectMapper.valueToTree(error.toSpecification())` (direct, no string round-trip)

**Files affected:**
- `server/implementation/src/main/java/io/smallrye/graphql/execution/ExecutionResponse.java`
- `server/implementation/src/main/java/io/smallrye/graphql/execution/error/ExecutionErrorsService.java`

### 2C. EventingService SPI Change

```java
// Before
default Map<String, Jsonb> overrideJsonbConfig() {
    return Collections.emptyMap();
}

// After
default Map<String, ObjectMapper> overrideObjectMapperConfig() {
    return Collections.emptyMap();
}
```

**Files affected:**
- `server/api/src/main/java/io/smallrye/graphql/spi/EventingService.java`
- `server/implementation/src/main/java/io/smallrye/graphql/execution/event/EventEmitter.java`

### 2D. Custom Scalar Serializers

Rewrite the three custom scalar serializer/deserializer pairs in `JsonBCreator` as Jackson equivalents:
- `CustomStringScalar` — serialize via `stringValueForSerialization()`, deserialize via `new CustomStringScalar(String)`
- `CustomIntScalar` — serialize via `intValueForSerialization()`, deserialize via `new CustomIntScalar(Integer)`
- `CustomFloatScalar` — serialize via `floatValueForSerialization()`, deserialize via `new CustomFloatScalar(BigDecimal)`

These are registered as a Jackson `Module` or directly on the `ObjectMapper`.

### 2E. JsonProviderHolder Removal

`server/implementation/src/main/java/io/smallrye/graphql/JsonProviderHolder.java` — deleted.

### 2F. Servlet and WebSocket Modules

**`server/implementation-servlet`:**
- `ExecutionServlet`: JSON-P `JsonReader` for request parsing → `ObjectMapper.readTree()`
- Response writing → `ObjectMapper.writeValue()` to servlet output stream

**WebSocket handlers (server-side):**
- Message framing (parsing incoming, building `connection_ack`/`next`/`complete`) switches from JSON-P to Jackson tree model

**`server/implementation-cdi`:**
- Check for JSON-B producers/configuration in `GraphQLProducer` — switch to Jackson `ObjectMapper` setup if present

---

## 3. Client-Side Migration

### 3A. Public API Type Changes (`client/api`)

| Interface | Method | Before | After |
|-----------|--------|--------|-------|
| `Request` | `toJsonObject()` | `jakarta.json.JsonObject` | `com.fasterxml.jackson.databind.node.ObjectNode` |
| `Response` | `getData()` | `jakarta.json.JsonObject` | `ObjectNode` |
| `Response` | various | `JsonValue`, `JsonArray` | `JsonNode`, `ArrayNode` |
| `TypesafeResponse` | `getExtensions()` | `jakarta.json.JsonObject` | `ObjectNode` |

`GraphQLError` extensions stay as `Map<String, Object>` (unchanged).

### 3B. Request Serialization (`RequestImpl`)

Replace static `Jsonb` + `JsonBuilderFactory` with `ObjectMapper`.

- Primitive variables → directly into `ObjectNode`
- Complex objects → `objectMapper.valueToTree(v)` (eliminates `Jsonb.toJson()` + re-parse round-trip)

### 3C. Response Parsing (`ResponseImpl`, `ResponseReader`)

- `JsonReaderFactory` + `JsonReader` → `ObjectMapper.readTree(string)` returning `JsonNode`
- Error decoding: `JsonString`/`JsonNumber`/`JsonValue` dispatch → `JsonNode.isTextual()`/`isNumber()`/`isArray()` etc.

### 3D. Typesafe Client Reader Hierarchy

The entire `json/` reader package is rewritten to use Jackson `JsonNode` types:

| Reader | Before (JSON-P) | After (Jackson) |
|--------|-----------------|-----------------|
| `Reader<T>` | `extends JsonValue` | `extends JsonNode` |
| `JsonReader` | dispatches on `JsonValue.ValueType` | dispatches on `JsonNodeType` |
| `JsonStringReader` | reads `JsonString` | reads `TextNode` |
| `JsonNumberReader` | reads `JsonNumber` | reads `NumericNode` |
| `JsonObjectReader` | reads `JsonObject` | reads `ObjectNode` |
| `JsonArrayReader` | reads `JsonArray` | reads `ArrayNode` |
| `JsonBooleanReader` | reads `TRUE`/`FALSE` | reads `BooleanNode` |
| `JsonNullReader` | reads `NULL` | reads `NullNode` |

Annotation reading (`@JsonbDateFormat`, `@JsonbNumberFormat`) in these readers stays — these are read via Java reflection on the target type, not via JSON-B runtime.

### 3E. ResultBuilder — JSON Pointer and Patch Replacement

- `jakarta.json.JsonPointer` → `com.fasterxml.jackson.core.JsonPointer` (Jackson built-in)
- `jakarta.json.JsonPatch` → rewrite directly using Jackson tree manipulation (set error markers at specific paths). No new dependency.

### 3F. Vert.x Client Transport (`client/implementation-vertx`)

Dynamic client and WebSocket subprotocol handlers: JSON-P message construction/parsing → Jackson.

---

## 4. Schema Builder Changes (`common/schema-builder`)

### What stays (unchanged)

- All JSON-B annotation DotName constants in `Annotations.java` — these are string-based Jandex lookups, no runtime JSON-B dependency
- Recognition of `@JsonbProperty`, `@JsonbTransient`, `@JsonbCreator`, `@JsonbDateFormat`, `@JsonbNumberFormat` in `FieldCreator`, `IgnoreHelper`, `FormatHelper`, `InputTypeCreator`
- All Jackson annotation constants and recognition

### What changes

- **`@JsonbTypeAdapter` removed from `AdaptWithHelper`** — both `JAKARTA_JSONB_TYPE_ADAPTER` and `JAVAX_JSONB_TYPE_ADAPTER` recognition removed
- **Build-time warning** logged when `@JsonbTypeAdapter` is found, directing users to `@AdaptWith` or Jackson annotations
- **Jackson converter support added to `AdaptWithHelper`** — recognize `@JsonSerialize(converter=...)` / `@JsonDeserialize(converter=...)` for schema-level type adaptation

### Dependency change

- `jakarta.json.bind-api` dependency removed from `common/schema-builder/pom.xml` — DotName constants are just strings

---

## 5. Quarkus Extension Changes

### 5A. Dependency Swap

| Module | Remove | Add |
|--------|--------|-----|
| `extensions/smallrye-graphql/runtime` | `quarkus-jsonb` | `quarkus-jackson` |
| `extensions/smallrye-graphql/deployment` | `quarkus-jsonb-deployment` | `quarkus-jackson-deployment` |
| `extensions/smallrye-graphql-client/runtime` | `quarkus-jsonb` | `quarkus-jackson` |
| `extensions/smallrye-graphql-client/deployment` | `quarkus-jsonb-deployment` | `quarkus-jackson-deployment` |

The `jakarta.json` exclusion on `smallrye-graphql-client-implementation-vertx` is no longer needed.

### 5B. `SmallRyeGraphQLProcessor.java` (deployment)

- Remove `@JsonbTypeAdapter` adapter scanning
- Keep `@AdaptWith` scanning
- Add scanning for Jackson `@JsonSerialize`/`@JsonDeserialize` converter annotations

### 5C. `SmallRyeGraphQLAbstractHandler.java` (runtime)

- Replace `io.quarkus.jsonp.JsonProviderHolder.jsonProvider()` and `JsonReaderFactory` with Jackson `ObjectMapper`

### 5D. `SmallRyeGraphQLExecutionHandler.java` (runtime)

- Replace JSON-P `createMergePatch` (merging query parameter JSON with body JSON) with Jackson tree merge (`ObjectNode.setAll()` or similar)
- Adjust `VertxExecutionResponseWriter` for Jackson types from `ExecutionResponse`

### 5E. WebSocket Auth Handlers

- `SmallRyeAuthGraphQLWSHandler`, `SmallRyeAuthGraphQLWSSubprotocolHandler`, `SmallRyeAuthGraphQLTransportWSSubprotocolHandler` — replace `jakarta.json.JsonString`/`JsonObject` with Jackson for extracting authorization tokens from connection_init payloads

### 5F. Test Updates

- `AbstractGraphQLTest` and deployment tests: replace JSON-P request/response construction with Jackson
- Adapter tests: update any `@JsonbTypeAdapter` tests to `@AdaptWith`

---

## 6. Module Structure and Dependencies

### New Module

```
common/jackson-jsonb-compat/
  pom.xml
  src/main/java/io/smallrye/graphql/jackson/jsonb/
    JsonbCompatModule.java
    JsonbAnnotationIntrospector.java
    JsonbDateFormatSerializer.java
    JsonbDateFormatDeserializer.java
    JsonbNumberFormatSerializer.java
    JsonbNumberFormatDeserializer.java
```

### Dependency Changes

| Module | Remove | Add |
|--------|--------|-----|
| `common/schema-builder` | `jakarta.json.bind-api`, `jakarta.json-api` | _(nothing)_ |
| `common/jackson-jsonb-compat` | — | `jackson-databind`, `jakarta.json.bind-api` (annotations only) |
| `server/implementation` | `jakarta.json.bind-api`, `jakarta.json-api`, `org.eclipse:yasson` | `jackson-databind`, `common/jackson-jsonb-compat` |
| `server/api` | `jakarta.json.bind-api` | `jackson-databind` |
| `client/api` | `jakarta.json-api` | `jackson-databind` |
| `client/implementation` | `jakarta.json.bind-api`, `jakarta.json-api`, `org.eclipse:yasson` | `jackson-databind`, `common/jackson-jsonb-compat` |
| `client/implementation-vertx` | `jakarta.json-api` | `jackson-databind` |
| `server/implementation-servlet` | `jakarta.json-api` | `jackson-databind` |

### Version Management

- Parent POM version: `3.0.0-SNAPSHOT`
- Add `version.jackson` property aligned with Quarkus 4.0's Jackson version
- Add `jackson-databind`, `jackson-core`, `jackson-annotations` to `dependencyManagement`

---

## 7. Breaking Changes Summary

### Public API Breaks

1. `client/api` — `Request`, `Response`, `TypesafeResponse` return Jackson types instead of JSON-P types
2. `server/api` — `EventingService.overrideJsonbConfig()` replaced with `overrideObjectMapperConfig()` returning `Map<String, ObjectMapper>`
3. `@JsonbTypeAdapter` no longer recognized — build-time warning logged

### Dependency Breaks

4. `jakarta.json-api` (JSON-P) no longer transitively provided
5. `jakarta.json.bind-api` (JSON-B) no longer transitively provided as a runtime dependency
6. `org.eclipse:yasson` (JSON-B RI) no longer a dependency

### NOT Breaking

- `@JsonbProperty`, `@JsonbTransient`, `@JsonbCreator`, `@JsonbDateFormat`, `@JsonbNumberFormat`, `@JsonbNillable` on user POJOs — work via compat module
- `@AdaptWith` — unchanged
- Jackson annotations — work natively
- MicroProfile GraphQL annotations (`@Name`, `@Ignore`, `@Query`, `@Mutation`, etc.) — unchanged
- GraphQL schema generation — unchanged

---

## 8. Test and Verification Requirements

### Must-pass test suites

1. **MicroProfile GraphQL TCK** (`tcks/microprofile-graphql` in Quarkus) — hard requirement
2. **SmallRye GraphQL server tests** — including `jsonbCreator/` test package verifying JSON-B annotation backward compat
3. **SmallRye GraphQL client TCK** — `AnnotationBehavior`, `InterfaceBehavior`, `UnionBehavior` verifying JSON-B annotations on client side
4. **Quarkus deployment tests** — `AbstractGraphQLTest` and friends
5. **Quarkus integration tests** — full stack verification

### Backward compatibility verification matrix

| Scenario | Schema | Runtime |
|----------|--------|---------|
| POJO with `@JsonbProperty` only | field renamed | deserialization respects name |
| POJO with `@JsonbCreator` only | constructor used | Jackson invokes annotated constructor |
| POJO with `@JsonbTransient` only | field hidden | field excluded from serialization |
| POJO with `@JsonbDateFormat` | format metadata | date formatted correctly |
| POJO with `@JsonbNumberFormat` | format metadata | number formatted correctly |
| POJO with Jackson annotations only | field renamed/hidden | Jackson handles natively |
| POJO with no JSON annotations | default behavior | default behavior |
| POJO with mixed JSON-B + Jackson | both recognized | both honored, MicroProfile `@Name`/`@Ignore` take priority |
| POJO with `@JsonbTypeInfo`/`@JsonbSubtype` | polymorphic types | polymorphic deserialization works |
| POJO with `@JsonbTypeAdapter` | build-time warning | not functional — user must migrate |
