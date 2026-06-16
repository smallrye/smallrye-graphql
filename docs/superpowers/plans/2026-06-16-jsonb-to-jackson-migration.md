# JSON-B to Jackson Migration — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace JSON-B and JSON-P with Jackson for all JSON processing in SmallRye GraphQL (server + client), while preserving backward compatibility with JSON-B annotations via a custom Jackson module.

**Architecture:** Direct Jackson replacement. A new `common/jackson-jsonb-compat` module provides a Jackson `Module` that introspects JSON-B annotations (`@JsonbProperty`, `@JsonbCreator`, `@JsonbTransient`, `@JsonbDateFormat`, `@JsonbNumberFormat`, `@JsonbTypeInfo`/`@JsonbSubtype`) and maps them to equivalent Jackson behavior. All JSON-P (`jakarta.json`) and JSON-B (`jakarta.json.bind`) runtime dependencies are removed. Public APIs in `client/api` and `server/api` change to use Jackson types.

**Tech Stack:** Jackson 2.21.3 (jackson-databind, jackson-core, jackson-annotations), Jakarta JSON-B API (annotations only, no runtime), Jandex, JUnit 5, Mockito

**SmallRye GraphQL codebase:** `/home/jmartisk/Workspace/smallrye-graphql`
**Quarkus codebase:** `/home/jmartisk/Workspace/quarkus`

---

## Phase 1: Foundation — POM Changes and Compat Module

### Task 1: Update parent POM — promote Jackson from test to compile scope

The Jackson BOM is already in `dependencyManagement` at version 2.21.3 but scoped as `import` under the test-like section. We need to make it available as a compile dependency across all modules.

**Files:**
- Modify: `pom.xml` (root)

- [ ] **Step 1: Move Jackson BOM out of test scope in root POM**

In `pom.xml`, the Jackson BOM is already imported (line 376-381). It uses `<scope>import</scope>` with `<type>pom</type>`, which is correct for a BOM import — it doesn't have a test scope limitation. But we should also remove the `version.yasson` and `version.jakarta-json` properties and the `yasson` and `jakarta.json-api` entries from `dependencyManagement` since they're being phased out.

Edit `pom.xml`:
- Remove property `<version.yasson>3.0.4</version.yasson>` (line 32)
- Remove property `<version.jakarta-json>2.1.3</version.jakarta-json>` (line 31)
- Remove the `yasson` dependency from `dependencyManagement` (lines 154-157)
- Remove the `jakarta.json-api` dependency from `dependencyManagement` (lines 250-253)
- Add a new managed dependency for `jakarta.json.bind-api` (annotations-only, for the compat module):

```xml
<dependency>
    <groupId>jakarta.json.bind</groupId>
    <artifactId>jakarta.json.bind-api</artifactId>
    <version>3.0.1</version>
</dependency>
```

- Add a new project module entry for `jackson-jsonb-compat` in `dependencyManagement`:

```xml
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>smallrye-graphql-jackson-jsonb-compat</artifactId>
    <version>${project.version}</version>
</dependency>
```

- [ ] **Step 2: Verify the root POM builds**

Run: `mvn -pl . validate`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add pom.xml
git commit -m "chore: update root POM for Jackson migration — remove yasson/json-p managed deps, add jackson-jsonb-compat module"
```

---

### Task 2: Create `common/jackson-jsonb-compat` module — POM and empty module

**Files:**
- Create: `common/jackson-jsonb-compat/pom.xml`
- Modify: `common/pom.xml` (add module)

- [ ] **Step 1: Create the module POM**

Create `common/jackson-jsonb-compat/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>io.smallrye</groupId>
        <artifactId>smallrye-graphql-common-parent</artifactId>
        <version>3.0.0.Beta4-SNAPSHOT</version>
    </parent>

    <artifactId>smallrye-graphql-jackson-jsonb-compat</artifactId>
    <name>SmallRye: GraphQL Jackson JSON-B Compatibility</name>
    <description>Jackson module that honors JSON-B annotations for backward compatibility</description>

    <dependencies>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jdk8</artifactId>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
        </dependency>
        <dependency>
            <groupId>jakarta.json.bind</groupId>
            <artifactId>jakarta.json.bind-api</artifactId>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 2: Add module to common/pom.xml**

In `common/pom.xml`, add `<module>jackson-jsonb-compat</module>` to the `<modules>` section.

- [ ] **Step 3: Verify the module builds**

Run: `mvn -pl common/jackson-jsonb-compat compile`
Expected: BUILD SUCCESS (empty module compiles)

- [ ] **Step 4: Commit**

```bash
git add common/jackson-jsonb-compat/pom.xml common/pom.xml
git commit -m "chore: add empty jackson-jsonb-compat module"
```

---

### Task 3: Implement `JsonbAnnotationIntrospector` — core annotation mapping

This is the heart of backward compatibility. A Jackson `AnnotationIntrospector` that reads JSON-B annotations and returns Jackson-equivalent metadata.

**Files:**
- Create: `common/jackson-jsonb-compat/src/main/java/io/smallrye/graphql/jackson/jsonb/JsonbAnnotationIntrospector.java`
- Test: `common/jackson-jsonb-compat/src/test/java/io/smallrye/graphql/jackson/jsonb/JsonbAnnotationIntrospectorTest.java`

- [ ] **Step 1: Write the failing test for @JsonbProperty**

Create test file `common/jackson-jsonb-compat/src/test/java/io/smallrye/graphql/jackson/jsonb/JsonbAnnotationIntrospectorTest.java`:

```java
package io.smallrye.graphql.jackson.jsonb;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.json.bind.annotation.JsonbProperty;

class JsonbAnnotationIntrospectorTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JsonbCompatModule());

    // -- @JsonbProperty --

    static class PropertyBean {
        @JsonbProperty("custom_name")
        public String originalName = "value";
    }

    @Test
    void jsonbPropertyRenamesField() throws Exception {
        String json = mapper.writeValueAsString(new PropertyBean());
        assertThat(json).contains("\"custom_name\"").doesNotContain("\"originalName\"");

        PropertyBean deserialized = mapper.readValue("{\"custom_name\":\"hello\"}", PropertyBean.class);
        assertThat(deserialized.originalName).isEqualTo("hello");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -pl common/jackson-jsonb-compat -Dtest=JsonbAnnotationIntrospectorTest#jsonbPropertyRenamesField -Dsurefire.failIfNoSpecifiedTests=false`
Expected: FAIL — `JsonbCompatModule` class doesn't exist yet

- [ ] **Step 3: Implement JsonbAnnotationIntrospector with @JsonbProperty support**

Create `common/jackson-jsonb-compat/src/main/java/io/smallrye/graphql/jackson/jsonb/JsonbAnnotationIntrospector.java`:

```java
package io.smallrye.graphql.jackson.jsonb;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbNillable;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

public class JsonbAnnotationIntrospector extends NopAnnotationIntrospector {

    private static final long serialVersionUID = 1L;

    @Override
    public PropertyName findNameForSerialization(Annotated a) {
        JsonbProperty prop = a.getAnnotation(JsonbProperty.class);
        if (prop != null && !prop.value().isEmpty()) {
            return PropertyName.construct(prop.value());
        }
        return null;
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated a) {
        JsonbProperty prop = a.getAnnotation(JsonbProperty.class);
        if (prop != null && !prop.value().isEmpty()) {
            return PropertyName.construct(prop.value());
        }
        return null;
    }

    @Override
    public boolean hasIgnoreMarker(AnnotatedMember m) {
        return m.hasAnnotation(JsonbTransient.class);
    }

    @Override
    public JsonCreator.Mode findCreatorAnnotation(MapperConfig<?> config, Annotated a) {
        if (a.hasAnnotation(JsonbCreator.class)) {
            return JsonCreator.Mode.PROPERTIES;
        }
        return null;
    }
}
```

- [ ] **Step 4: Create JsonbCompatModule**

Create `common/jackson-jsonb-compat/src/main/java/io/smallrye/graphql/jackson/jsonb/JsonbCompatModule.java`:

```java
package io.smallrye.graphql.jackson.jsonb;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class JsonbCompatModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    public JsonbCompatModule() {
        super("JsonbCompatModule");
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.insertAnnotationIntrospector(new JsonbAnnotationIntrospector());
    }
}
```

- [ ] **Step 5: Run the test**

Run: `mvn test -pl common/jackson-jsonb-compat -Dtest=JsonbAnnotationIntrospectorTest#jsonbPropertyRenamesField`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add common/jackson-jsonb-compat/src/
git commit -m "feat: add JsonbAnnotationIntrospector with @JsonbProperty support"
```

---

### Task 4: Add @JsonbTransient and @JsonbCreator support to introspector

**Files:**
- Modify: `common/jackson-jsonb-compat/src/main/java/io/smallrye/graphql/jackson/jsonb/JsonbAnnotationIntrospector.java`
- Modify: `common/jackson-jsonb-compat/src/test/java/io/smallrye/graphql/jackson/jsonb/JsonbAnnotationIntrospectorTest.java`

- [ ] **Step 1: Write failing tests for @JsonbTransient and @JsonbCreator**

Add to `JsonbAnnotationIntrospectorTest.java`:

```java
    // -- @JsonbTransient --

    static class TransientBean {
        public String visible = "yes";
        @jakarta.json.bind.annotation.JsonbTransient
        public String hidden = "no";
    }

    @Test
    void jsonbTransientExcludesField() throws Exception {
        String json = mapper.writeValueAsString(new TransientBean());
        assertThat(json).contains("\"visible\"").doesNotContain("\"hidden\"");
    }

    // -- @JsonbCreator --

    static class CreatorBean {
        private final String name;
        private final int age;

        @jakarta.json.bind.annotation.JsonbCreator
        public CreatorBean(
                @JsonbProperty("name") String name,
                @JsonbProperty("age") int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public int getAge() { return age; }
    }

    @Test
    void jsonbCreatorUsesAnnotatedConstructor() throws Exception {
        CreatorBean bean = mapper.readValue("{\"name\":\"Alice\",\"age\":30}", CreatorBean.class);
        assertThat(bean.getName()).isEqualTo("Alice");
        assertThat(bean.getAge()).isEqualTo(30);
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test -pl common/jackson-jsonb-compat -Dtest=JsonbAnnotationIntrospectorTest`
Expected: @JsonbTransient test should PASS (already implemented). @JsonbCreator test should PASS (already implemented). If not, fix.

- [ ] **Step 3: Run all tests and verify**

Run: `mvn test -pl common/jackson-jsonb-compat`
Expected: All 3 tests PASS

- [ ] **Step 4: Commit**

```bash
git add common/jackson-jsonb-compat/src/
git commit -m "test: verify @JsonbTransient and @JsonbCreator support in compat module"
```

---

### Task 5: Add @JsonbNillable support to introspector

**Files:**
- Modify: `common/jackson-jsonb-compat/src/main/java/io/smallrye/graphql/jackson/jsonb/JsonbAnnotationIntrospector.java`
- Modify: `common/jackson-jsonb-compat/src/test/java/io/smallrye/graphql/jackson/jsonb/JsonbAnnotationIntrospectorTest.java`

- [ ] **Step 1: Write failing test for @JsonbNillable**

Add to `JsonbAnnotationIntrospectorTest.java`:

```java
    // -- @JsonbNillable --

    @jakarta.json.bind.annotation.JsonbNillable
    static class NillableBean {
        public String name = null;
        public String other = "value";
    }

    @Test
    void jsonbNillableIncludesNullFields() throws Exception {
        String json = mapper.writeValueAsString(new NillableBean());
        assertThat(json).contains("\"name\":null").contains("\"other\":\"value\"");
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -pl common/jackson-jsonb-compat -Dtest=JsonbAnnotationIntrospectorTest#jsonbNillableIncludesNullFields`
Expected: FAIL — null fields excluded by default

- [ ] **Step 3: Add @JsonbNillable handling to introspector**

Add to `JsonbAnnotationIntrospector.java`:

```java
import com.fasterxml.jackson.annotation.JsonInclude;

    @Override
    public JsonInclude.Value findPropertyInclusion(Annotated a) {
        // Class-level @JsonbNillable means include nulls
        if (a.hasAnnotation(JsonbNillable.class)) {
            return JsonInclude.Value.construct(JsonInclude.Include.ALWAYS, JsonInclude.Include.ALWAYS);
        }
        // Field-level @JsonbProperty(nillable=true)
        JsonbProperty prop = a.getAnnotation(JsonbProperty.class);
        if (prop != null && prop.nillable()) {
            return JsonInclude.Value.construct(JsonInclude.Include.ALWAYS, JsonInclude.Include.ALWAYS);
        }
        return null;
    }
```

- [ ] **Step 4: Run tests**

Run: `mvn test -pl common/jackson-jsonb-compat`
Expected: All tests PASS

- [ ] **Step 5: Commit**

```bash
git add common/jackson-jsonb-compat/src/
git commit -m "feat: add @JsonbNillable and @JsonbProperty(nillable) support"
```

---

### Task 6: Add @JsonbDateFormat support — custom serializers

**Files:**
- Create: `common/jackson-jsonb-compat/src/main/java/io/smallrye/graphql/jackson/jsonb/JsonbDateFormatSerializer.java`
- Create: `common/jackson-jsonb-compat/src/main/java/io/smallrye/graphql/jackson/jsonb/JsonbDateFormatDeserializer.java`
- Modify: `common/jackson-jsonb-compat/src/main/java/io/smallrye/graphql/jackson/jsonb/JsonbAnnotationIntrospector.java`
- Modify: `common/jackson-jsonb-compat/src/test/java/io/smallrye/graphql/jackson/jsonb/JsonbAnnotationIntrospectorTest.java`

- [ ] **Step 1: Write failing test for @JsonbDateFormat**

Add to test file:

```java
import java.time.LocalDate;
import jakarta.json.bind.annotation.JsonbDateFormat;

    // -- @JsonbDateFormat --

    static class DateFormatBean {
        @JsonbDateFormat("dd.MM.yyyy")
        public LocalDate date = LocalDate.of(2026, 6, 15);
    }

    @Test
    void jsonbDateFormatSerializesWithPattern() throws Exception {
        String json = mapper.writeValueAsString(new DateFormatBean());
        assertThat(json).contains("\"15.06.2026\"");
    }

    @Test
    void jsonbDateFormatDeserializesWithPattern() throws Exception {
        DateFormatBean bean = mapper.readValue("{\"date\":\"15.06.2026\"}", DateFormatBean.class);
        assertThat(bean.date).isEqualTo(LocalDate.of(2026, 6, 15));
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test -pl common/jackson-jsonb-compat -Dtest=JsonbAnnotationIntrospectorTest#jsonbDateFormatSerializesWithPattern`
Expected: FAIL

- [ ] **Step 3: Implement date format support**

The approach: override `findSerializer`/`findDeserializer` in the introspector to detect `@JsonbDateFormat` and return custom serializer/deserializer instances configured with the format pattern.

Create `JsonbDateFormatSerializer.java`:

```java
package io.smallrye.graphql.jackson.jsonb;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class JsonbDateFormatSerializer extends JsonSerializer<TemporalAccessor> {

    private final DateTimeFormatter formatter;

    public JsonbDateFormatSerializer(String pattern, String locale) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern(pattern);
        if (locale != null && !locale.isEmpty() && !locale.equals("##default")) {
            f = f.withLocale(Locale.forLanguageTag(locale));
        }
        this.formatter = f;
    }

    @Override
    public void serialize(TemporalAccessor value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(formatter.format(value));
    }
}
```

Create `JsonbDateFormatDeserializer.java`:

```java
package io.smallrye.graphql.jackson.jsonb;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

public class JsonbDateFormatDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {

    private final DateTimeFormatter formatter;
    private final Class<?> targetType;

    public JsonbDateFormatDeserializer() {
        this.formatter = null;
        this.targetType = null;
    }

    public JsonbDateFormatDeserializer(String pattern, String locale, Class<?> targetType) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern(pattern);
        if (locale != null && !locale.isEmpty() && !locale.equals("##default")) {
            f = f.withLocale(Locale.forLanguageTag(locale));
        }
        this.formatter = f;
        this.targetType = targetType;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        TemporalAccessor parsed = formatter.parse(text);
        if (targetType == LocalDate.class) return LocalDate.from(parsed);
        if (targetType == LocalDateTime.class) return LocalDateTime.from(parsed);
        if (targetType == LocalTime.class) return LocalTime.from(parsed);
        if (targetType == OffsetDateTime.class) return OffsetDateTime.from(parsed);
        if (targetType == ZonedDateTime.class) return ZonedDateTime.from(parsed);
        if (targetType == Instant.class) return Instant.from(parsed);
        if (targetType == OffsetTime.class) return OffsetTime.from(parsed);
        return parsed;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        if (property != null) {
            jakarta.json.bind.annotation.JsonbDateFormat ann =
                    property.getAnnotation(jakarta.json.bind.annotation.JsonbDateFormat.class);
            if (ann != null) {
                return new JsonbDateFormatDeserializer(ann.value(), ann.locale(), property.getType().getRawClass());
            }
        }
        return this;
    }
}
```

Add to `JsonbAnnotationIntrospector.java`:

```java
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import jakarta.json.bind.annotation.JsonbDateFormat;

    @Override
    public Object findSerializer(Annotated a) {
        JsonbDateFormat dateFormat = a.getAnnotation(JsonbDateFormat.class);
        if (dateFormat != null) {
            return new JsonbDateFormatSerializer(dateFormat.value(), dateFormat.locale());
        }
        return null;
    }

    @Override
    public Object findDeserializer(Annotated a) {
        JsonbDateFormat dateFormat = a.getAnnotation(JsonbDateFormat.class);
        if (dateFormat != null) {
            return new JsonbDateFormatDeserializer(dateFormat.value(), dateFormat.locale(),
                    a instanceof AnnotatedField ? ((AnnotatedField) a).getRawType() : Object.class);
        }
        return null;
    }
```

- [ ] **Step 4: Run tests**

Run: `mvn test -pl common/jackson-jsonb-compat`
Expected: All tests PASS

- [ ] **Step 5: Commit**

```bash
git add common/jackson-jsonb-compat/src/
git commit -m "feat: add @JsonbDateFormat support with custom serializer/deserializer"
```

---

### Task 7: Add @JsonbNumberFormat support

**Files:**
- Create: `common/jackson-jsonb-compat/src/main/java/io/smallrye/graphql/jackson/jsonb/JsonbNumberFormatSerializer.java`
- Create: `common/jackson-jsonb-compat/src/main/java/io/smallrye/graphql/jackson/jsonb/JsonbNumberFormatDeserializer.java`
- Modify: `common/jackson-jsonb-compat/src/main/java/io/smallrye/graphql/jackson/jsonb/JsonbAnnotationIntrospector.java`
- Modify: `common/jackson-jsonb-compat/src/test/java/io/smallrye/graphql/jackson/jsonb/JsonbAnnotationIntrospectorTest.java`

- [ ] **Step 1: Write failing test**

```java
import jakarta.json.bind.annotation.JsonbNumberFormat;

    // -- @JsonbNumberFormat --

    static class NumberFormatBean {
        @JsonbNumberFormat("#,##0.00")
        public double price = 1234.5;
    }

    @Test
    void jsonbNumberFormatSerializesWithPattern() throws Exception {
        String json = mapper.writeValueAsString(new NumberFormatBean());
        // Locale-dependent — US format
        assertThat(json).contains("\"1,234.50\"");
    }
```

- [ ] **Step 2: Implement number format serializer/deserializer**

Create `JsonbNumberFormatSerializer.java` — uses `DecimalFormat` with the given pattern to format numbers as strings.

Create `JsonbNumberFormatDeserializer.java` — parses formatted number strings back to the target numeric type.

Update `JsonbAnnotationIntrospector.findSerializer()`/`findDeserializer()` to also check for `@JsonbNumberFormat`.

- [ ] **Step 3: Run tests**

Run: `mvn test -pl common/jackson-jsonb-compat`
Expected: All tests PASS

- [ ] **Step 4: Commit**

```bash
git add common/jackson-jsonb-compat/src/
git commit -m "feat: add @JsonbNumberFormat support with custom serializer/deserializer"
```

---

### Task 8: Add @JsonbTypeInfo / @JsonbSubtype polymorphic support

**Files:**
- Modify: `common/jackson-jsonb-compat/src/main/java/io/smallrye/graphql/jackson/jsonb/JsonbAnnotationIntrospector.java`
- Modify: `common/jackson-jsonb-compat/src/test/java/io/smallrye/graphql/jackson/jsonb/JsonbAnnotationIntrospectorTest.java`

- [ ] **Step 1: Write failing test**

```java
import jakarta.json.bind.annotation.JsonbSubtype;
import jakarta.json.bind.annotation.JsonbTypeInfo;

    // -- @JsonbTypeInfo / @JsonbSubtype --

    @JsonbTypeInfo(key = "__typename", value = {
        @JsonbSubtype(alias = "Dog", type = Dog.class),
        @JsonbSubtype(alias = "Cat", type = Cat.class)
    })
    interface Animal {
        String getName();
    }

    static class Dog implements Animal {
        public String name;
        public String breed;
        public String getName() { return name; }
    }

    static class Cat implements Animal {
        public String name;
        public int lives;
        public String getName() { return name; }
    }

    @Test
    void jsonbTypeInfoDeserializesPolymorphicType() throws Exception {
        String json = "{\"__typename\":\"Dog\",\"name\":\"Rex\",\"breed\":\"Labrador\"}";
        Animal animal = mapper.readValue(json, Animal.class);
        assertThat(animal).isInstanceOf(Dog.class);
        assertThat(((Dog) animal).breed).isEqualTo("Labrador");
    }
```

- [ ] **Step 2: Implement @JsonbTypeInfo support in introspector**

Override the following methods in `JsonbAnnotationIntrospector`:
- `findTypeResolver()` — when `@JsonbTypeInfo` is present, return a type resolver that uses the `key` property as discriminator
- `findSubtypes()` — read `@JsonbSubtype` entries and register them as named subtypes

This maps to Jackson's `@JsonTypeInfo(use=NAME, property="key")` + `@JsonSubTypes({...})` behavior.

- [ ] **Step 3: Run tests**

Run: `mvn test -pl common/jackson-jsonb-compat`
Expected: All tests PASS

- [ ] **Step 4: Commit**

```bash
git add common/jackson-jsonb-compat/src/
git commit -m "feat: add @JsonbTypeInfo/@JsonbSubtype polymorphic support"
```

---

## Phase 2: Server API and SPI Changes

### Task 9: Update `server/api` — replace JSON-B with Jackson in SPI

**Files:**
- Modify: `server/api/pom.xml`
- Modify: `server/api/src/main/java/io/smallrye/graphql/spi/EventingService.java`

- [ ] **Step 1: Update server/api POM**

In `server/api/pom.xml`:
- Remove `jakarta.json-api` dependency (provided)
- Add `jackson-databind` dependency (provided scope — the SPI just references the type):

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <scope>provided</scope>
</dependency>
```

- [ ] **Step 2: Update EventingService**

In `EventingService.java`, change:

```java
// Before
import jakarta.json.bind.Jsonb;
default Map<String, Jsonb> overrideJsonbConfig() { return Collections.emptyMap(); }

// After
import com.fasterxml.jackson.databind.ObjectMapper;
default Map<String, ObjectMapper> overrideObjectMapperConfig() { return Collections.emptyMap(); }
```

- [ ] **Step 3: Verify compilation**

Run: `mvn compile -pl server/api`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add server/api/
git commit -m "feat!: replace EventingService.overrideJsonbConfig() with overrideObjectMapperConfig()"
```

---

### Task 10: Update `server/implementation` POM — dependency swap

**Files:**
- Modify: `server/implementation/pom.xml`

- [ ] **Step 1: Update dependencies**

In `server/implementation/pom.xml`:
- Remove `jakarta.json.bind-api` dependency
- Remove `jakarta.json-api` dependency (if present)
- Add `jackson-databind`:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jdk8</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

- Add `jackson-jsonb-compat`:

```xml
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>smallrye-graphql-jackson-jsonb-compat</artifactId>
</dependency>
```

- In test dependencies, replace `yasson` with nothing (no longer needed for tests)

- [ ] **Step 2: Verify POM is valid**

Run: `mvn -pl server/implementation validate`
Expected: BUILD SUCCESS (code won't compile yet — that's expected)

- [ ] **Step 3: Commit**

```bash
git add server/implementation/pom.xml
git commit -m "chore: swap JSON-B/JSON-P for Jackson in server/implementation POM"
```

---

## Phase 3: Server Implementation Migration

### Task 11: Rewrite `JsonBCreator` → `JacksonCreator`

This is the central JSON processing class for the server.

**Files:**
- Modify: `server/implementation/src/main/java/io/smallrye/graphql/json/JsonBCreator.java` (rename/rewrite)

- [ ] **Step 1: Rewrite JsonBCreator to use Jackson**

Rename the class to `JacksonCreator` (or keep filename and rename class). Replace all JSON-B types with Jackson equivalents:

- `Jsonb` → `ObjectMapper`
- `JsonbBuilder.create(config)` → `new ObjectMapper().registerModule(new JsonbCompatModule())` with configuration
- `JsonbConfig` → `ObjectMapper` configuration methods
- Custom scalar serializers: rewrite as Jackson `JsonSerializer`/`JsonDeserializer` and register via a `SimpleModule`
- `GraphQLNamingStrategy` → rewrite to extend Jackson's `PropertyNamingStrategy`

Key method changes:
- `getJsonB()` → `getObjectMapper()` returning default `ObjectMapper`
- `getJsonB(String className)` → `getObjectMapper(String className)`
- `register(InputType)` — same logic but builds Jackson `ObjectMapper` with custom naming
- `override(Map<String, Jsonb>)` → `override(Map<String, ObjectMapper>)`
- `createJsonB(Map)` → `createObjectMapper(Map)` — creates mapper with `PropertyNamingStrategy`

The default ObjectMapper configuration:
```java
ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new JsonbCompatModule());
mapper.registerModule(new JavaTimeModule());
mapper.registerModule(customScalarsModule);
mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
mapper.enable(SerializationFeature.INDENT_OUTPUT);
mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
```

- [ ] **Step 2: Rewrite GraphQLNamingStrategy**

Change `GraphQLNamingStrategy` from implementing `jakarta.json.bind.config.PropertyNamingStrategy` to extending `com.fasterxml.jackson.databind.PropertyNamingStrategies.NamingBase`:

```java
public class GraphQLNamingStrategy extends PropertyNamingStrategies.NamingBase {
    private final Map<String, String> customFieldNameMapping;

    public GraphQLNamingStrategy(Map<String, String> customFieldNameMapping) {
        this.customFieldNameMapping = customFieldNameMapping;
    }

    @Override
    public String translate(String propertyName) {
        return customFieldNameMapping.getOrDefault(propertyName, propertyName);
    }
}
```

- [ ] **Step 3: Update JsonInputRegistry**

Change `JsonInputRegistry.override(Map<String, Jsonb>)` → `override(Map<String, ObjectMapper>)`.
Change `register` delegation to use `JacksonCreator.register()`.

- [ ] **Step 4: Verify compilation of the json package**

Run: `mvn compile -pl server/implementation` (will likely fail due to downstream consumers — that's fine for this step, we just want the json package to be internally consistent)

- [ ] **Step 5: Commit**

```bash
git add server/implementation/src/main/java/io/smallrye/graphql/json/
git commit -m "feat: rewrite JsonBCreator to JacksonCreator using ObjectMapper"
```

---

### Task 12: Update `ArgumentHelper` — use Jackson for input deserialization

**Files:**
- Modify: `server/implementation/src/main/java/io/smallrye/graphql/execution/datafetcher/helper/ArgumentHelper.java`

- [ ] **Step 1: Update imports and method calls**

Replace:
- `JsonBCreator.getJsonB(className).toJson(m)` → `JacksonCreator.getObjectMapper(className).writeValueAsString(m)` (or better: use `convertValue` to skip string round-trip)
- `jsonb.fromJson(jsonString, type)` → `objectMapper.readValue(jsonString, typeRef)` or `objectMapper.convertValue(map, type)`
- `JsonbException` → `com.fasterxml.jackson.databind.JsonMappingException`
- `includeNullCreatorParameters` — review if still needed (Jackson handles `@JsonCreator` parameters differently from Yasson)

The key optimization: replace the `Map → JSON string → POJO` flow with `Map → ObjectMapper.convertValue(map, type) → POJO`.

- [ ] **Step 2: Update DefaultMapAdapter**

Replace `JsonBCreator.getJsonB().toJson(t)` and `jsonb.fromJson(jsonString, clazz)` calls with Jackson equivalents.

- [ ] **Step 3: Verify compilation**

Run: `mvn compile -pl server/implementation`

- [ ] **Step 4: Commit**

```bash
git add server/implementation/src/main/java/io/smallrye/graphql/execution/datafetcher/helper/
git commit -m "feat: migrate ArgumentHelper and DefaultMapAdapter to Jackson"
```

---

### Task 13: Rewrite `ExecutionResponse` — replace JSON-P with Jackson

**Files:**
- Modify: `server/implementation/src/main/java/io/smallrye/graphql/execution/ExecutionResponse.java`

- [ ] **Step 1: Replace JSON-P types with Jackson**

Replace:
- `JsonBuilderFactory` → `ObjectMapper` (for tree model operations)
- `JsonObjectBuilder` → `ObjectNode`
- `JsonArrayBuilder` → `ArrayNode`
- `JsonValue` → `JsonNode`
- `JSON_PROVIDER.createValue(...)` → `mapper.getNodeFactory().textNode(...)`, `.numberNode(...)`, etc.
- `toJsonValue(Object pojo)` → `toJsonNode(Object pojo)` using `mapper.valueToTree(pojo)` or manual construction
- Response string generation: `mapper.writeValueAsString(node)` instead of JSON-P StringWriter

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -pl server/implementation`

- [ ] **Step 3: Commit**

```bash
git add server/implementation/src/main/java/io/smallrye/graphql/execution/ExecutionResponse.java
git commit -m "feat: rewrite ExecutionResponse to use Jackson tree model"
```

---

### Task 14: Rewrite `ExecutionErrorsService` — replace JSON-B/JSON-P

**Files:**
- Modify: `server/implementation/src/main/java/io/smallrye/graphql/execution/error/ExecutionErrorsService.java`

- [ ] **Step 1: Replace JSON-B and JSON-P types**

Replace:
- `JSONB.toJson(error.toSpecification())` + `JsonReader` → `mapper.valueToTree(error.toSpecification())` returning `ObjectNode` directly
- `JsonReaderFactory` → removed
- `JsonObjectBuilder` → `ObjectNode`
- All JSON-P builder patterns → Jackson `ObjectNode.put()` / `ArrayNode.add()` patterns

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -pl server/implementation`

- [ ] **Step 3: Commit**

```bash
git add server/implementation/src/main/java/io/smallrye/graphql/execution/error/ExecutionErrorsService.java
git commit -m "feat: rewrite ExecutionErrorsService to use Jackson"
```

---

### Task 15: Update `EventEmitter` and `Bootstrap` — Jackson integration

**Files:**
- Modify: `server/implementation/src/main/java/io/smallrye/graphql/execution/event/EventEmitter.java`
- Modify: `server/implementation/src/main/java/io/smallrye/graphql/bootstrap/Bootstrap.java`

- [ ] **Step 1: Update EventEmitter**

Change `fireOverrideJsonbConfig()` → `fireOverrideObjectMapperConfig()`:
- Return type: `Map<String, ObjectMapper>` instead of `Map<String, Jsonb>`
- Call `service.overrideObjectMapperConfig()` instead of `service.overrideJsonbConfig()`

- [ ] **Step 2: Update Bootstrap**

- Change `JsonInputRegistry.override(overrides)` to accept `Map<String, ObjectMapper>`
- Change `JsonBCreator.getJsonB(...)` calls to `JacksonCreator.getObjectMapper(...)`
- Update any direct Jsonb usage for default value deserialization

- [ ] **Step 3: Delete JsonProviderHolder**

Delete `server/implementation/src/main/java/io/smallrye/graphql/JsonProviderHolder.java`

- [ ] **Step 4: Verify full server/implementation compiles**

Run: `mvn compile -pl server/implementation`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add server/implementation/src/
git commit -m "feat: migrate EventEmitter, Bootstrap to Jackson; remove JsonProviderHolder"
```

---

### Task 16: Run server tests and fix failures

**Files:**
- Modify: various test files in `server/implementation/src/test/`

- [ ] **Step 1: Remove yasson test dependency if not already done**

In `server/implementation/pom.xml`, remove or comment out the `yasson` test dependency.

- [ ] **Step 2: Run all server tests**

Run: `mvn test -pl server/implementation`
Expected: Some tests may fail due to JSON-B specific assertions or test infrastructure

- [ ] **Step 3: Fix test failures**

The `jsonbCreator/` test package should continue to work — the POJOs use `@JsonbCreator` and `@JsonbProperty`, which the compat module handles. If tests fail, investigate whether the issue is in the compat module or in the test expectations.

Update any test that directly uses `Jsonb` or `JsonbBuilder` to use `ObjectMapper` instead.

- [ ] **Step 4: Verify all tests pass**

Run: `mvn test -pl server/implementation`
Expected: All tests PASS

- [ ] **Step 5: Commit**

```bash
git add server/implementation/
git commit -m "test: fix server tests for Jackson migration"
```

---

## Phase 4: Client Migration

### Task 17: Update `client/api` POM and public interfaces

**Files:**
- Modify: `client/api/pom.xml`
- Modify: `client/api/src/main/java/io/smallrye/graphql/client/Request.java`
- Modify: `client/api/src/main/java/io/smallrye/graphql/client/Response.java`
- Modify: `client/api/src/main/java/io/smallrye/graphql/client/typesafe/api/TypesafeResponse.java`

- [ ] **Step 1: Update client/api POM**

Replace `jakarta.json-api` (provided) with `jackson-databind` (provided):

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <scope>provided</scope>
</dependency>
```

- [ ] **Step 2: Update Request interface**

Replace `jakarta.json.JsonObject` return type with `com.fasterxml.jackson.databind.node.ObjectNode`:
- `JsonObject toJsonObject()` → `ObjectNode toJsonObject()`

Remove any other JSON-P type references.

- [ ] **Step 3: Update Response interface**

Replace all JSON-P types:
- `JsonObject getData()` → `ObjectNode getData()`
- Any `JsonValue` → `JsonNode`
- Any `JsonArray` → `ArrayNode`

- [ ] **Step 4: Update TypesafeResponse**

Replace `JsonObject getExtensions()` → `ObjectNode getExtensions()`

- [ ] **Step 5: Verify compilation**

Run: `mvn compile -pl client/api`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add client/api/
git commit -m "feat!: replace JSON-P types with Jackson in client API"
```

---

### Task 18: Update `client/implementation` POM

**Files:**
- Modify: `client/implementation/pom.xml`

- [ ] **Step 1: Swap dependencies**

- Remove `jakarta.json-api`, `jakarta.json.bind-api`
- Remove `yasson` from test scope
- Add:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jdk8</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>smallrye-graphql-jackson-jsonb-compat</artifactId>
</dependency>
```

- [ ] **Step 2: Commit**

```bash
git add client/implementation/pom.xml
git commit -m "chore: swap JSON-B/JSON-P for Jackson in client/implementation POM"
```

---

### Task 19: Rewrite `RequestImpl` — replace JSON-B/JSON-P with Jackson

**Files:**
- Modify: `client/implementation/src/main/java/io/smallrye/graphql/client/impl/RequestImpl.java`

- [ ] **Step 1: Replace JSON-B and JSON-P types**

Replace:
- `private static final Jsonb JSONB = JsonbBuilder.create();` → `private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JsonbCompatModule());`
- `private static final JsonBuilderFactory JSON = ...` → removed (use ObjectMapper for tree construction)
- `JsonObjectBuilder` → `ObjectNode`
- Variable serialization: `JSONB.toJson(v)` + re-parse → `MAPPER.valueToTree(v)`
- `toJsonObject()` return type: `JsonObject` → `ObjectNode`
- `toJson()`: use `MAPPER.writeValueAsString(node)`

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -pl client/implementation` (may fail due to other files — focus on RequestImpl)

- [ ] **Step 3: Commit**

```bash
git add client/implementation/src/main/java/io/smallrye/graphql/client/impl/RequestImpl.java
git commit -m "feat: rewrite RequestImpl to use Jackson"
```

---

### Task 20: Rewrite `ResponseReader` and `ResponseImpl`

**Files:**
- Modify: `client/implementation/src/main/java/io/smallrye/graphql/client/impl/ResponseReader.java`
- Modify: `client/implementation/src/main/java/io/smallrye/graphql/client/impl/ResponseImpl.java`

- [ ] **Step 1: Rewrite ResponseReader**

Replace:
- `JsonReaderFactory` → `ObjectMapper`
- `jsonReaderFactory.createReader(...)` → `mapper.readTree(input)`
- `JsonObject` → `ObjectNode`
- `JsonString` → `node.isTextual()` / `node.asText()`
- `JsonNumber` → `node.isNumber()` / `node.numberValue()`
- Error parsing: decode `JsonValue` → decode `JsonNode`

- [ ] **Step 2: Rewrite ResponseImpl**

Replace:
- `JsonObject data` field → `ObjectNode data`
- `JsonArray` → `ArrayNode`
- `getScalarValue()` — switch from `JsonValue.ValueType` to `JsonNodeType`
- Internal `JsonReader.readJson()` calls should still work (we'll update JsonReader next)

- [ ] **Step 3: Commit**

```bash
git add client/implementation/src/main/java/io/smallrye/graphql/client/impl/ResponseReader.java
git add client/implementation/src/main/java/io/smallrye/graphql/client/impl/ResponseImpl.java
git commit -m "feat: rewrite ResponseReader and ResponseImpl to use Jackson"
```

---

### Task 21: Rewrite typesafe client reader hierarchy

**Files:**
- Modify: `client/implementation/src/main/java/io/smallrye/graphql/client/impl/typesafe/json/Reader.java`
- Modify: `client/implementation/src/main/java/io/smallrye/graphql/client/impl/typesafe/json/JsonReader.java`
- Modify: `client/implementation/src/main/java/io/smallrye/graphql/client/impl/typesafe/json/JsonStringReader.java`
- Modify: `client/implementation/src/main/java/io/smallrye/graphql/client/impl/typesafe/json/JsonNumberReader.java`
- Modify: `client/implementation/src/main/java/io/smallrye/graphql/client/impl/typesafe/json/JsonObjectReader.java`
- Modify: `client/implementation/src/main/java/io/smallrye/graphql/client/impl/typesafe/json/JsonArrayReader.java`
- Modify: `client/implementation/src/main/java/io/smallrye/graphql/client/impl/typesafe/json/JsonBooleanReader.java`
- Modify: `client/implementation/src/main/java/io/smallrye/graphql/client/impl/typesafe/json/JsonNullReader.java`
- Modify: `client/implementation/src/main/java/io/smallrye/graphql/client/impl/typesafe/json/JsonMapReader.java` (if exists)
- Modify: `client/implementation/src/main/java/io/smallrye/graphql/client/impl/typesafe/json/JsonUtils.java` (if exists)

- [ ] **Step 1: Update base Reader class**

Change `Reader<T extends JsonValue>` → `Reader<T extends JsonNode>`.

- [ ] **Step 2: Update JsonReader dispatcher**

Change dispatch from `JsonValue.ValueType` to `JsonNodeType`:
- `ValueType.OBJECT` → `JsonNodeType.OBJECT`
- `ValueType.ARRAY` → `JsonNodeType.ARRAY`
- `ValueType.STRING` → `JsonNodeType.STRING`
- `ValueType.NUMBER` → `JsonNodeType.NUMBER`
- `ValueType.TRUE`/`FALSE` → `JsonNodeType.BOOLEAN`
- `ValueType.NULL` → `JsonNodeType.NULL`

- [ ] **Step 3: Update each specialized reader**

Each reader changes its JSON-P type to Jackson equivalent:
- `JsonString` → `TextNode` (or `JsonNode` with `.asText()`)
- `JsonNumber` → `NumericNode` (or `JsonNode` with `.numberValue()`)
- `JsonObject` → `ObjectNode`
- `JsonArray` → `ArrayNode`
- Boolean/Null — use `JsonNode.booleanValue()` / `JsonNode.isNull()`

**Preserve:** The JSON-B annotation reading in `JsonStringReader` (`@JsonbDateFormat`, `@JsonbNumberFormat`) — these are read via Java reflection, not JSON-B runtime, so the logic stays the same.

- [ ] **Step 4: Verify compilation**

Run: `mvn compile -pl client/implementation`

- [ ] **Step 5: Commit**

```bash
git add client/implementation/src/main/java/io/smallrye/graphql/client/impl/typesafe/json/
git commit -m "feat: rewrite typesafe client reader hierarchy to use Jackson JsonNode"
```

---

### Task 22: Update `TypeInfo` and `FieldInfo` — preserve JSON-B annotation reading

**Files:**
- Modify: `client/implementation/src/main/java/io/smallrye/graphql/client/impl/typesafe/reflection/TypeInfo.java`
- Modify: `client/implementation/src/main/java/io/smallrye/graphql/client/impl/typesafe/reflection/FieldInfo.java`

- [ ] **Step 1: Review TypeInfo**

`TypeInfo` reads `@JsonbTransient`, `@JsonbTypeInfo`, `@JsonbSubtype` via Java reflection. These imports stay — the annotations are still on the classpath via the compat module's transitive dependency on `jakarta.json.bind-api`. No changes needed unless there are JSON-P type references (e.g., `JsonObject` field types).

- [ ] **Step 2: Review FieldInfo**

`FieldInfo` reads `@JsonbNillable`, `@JsonbProperty`. Same as above — these stay. Check for any JSON-P type references and remove them.

- [ ] **Step 3: Remove JsonProviderHolder from client**

Delete `client/implementation/src/main/java/io/smallrye/graphql/client/impl/JsonProviderHolder.java`

- [ ] **Step 4: Verify compilation**

Run: `mvn compile -pl client/implementation`

- [ ] **Step 5: Commit**

```bash
git add client/implementation/src/main/java/io/smallrye/graphql/client/impl/
git commit -m "feat: update TypeInfo/FieldInfo for Jackson migration, remove client JsonProviderHolder"
```

---

### Task 23: Rewrite `ResultBuilder` — replace JSON Pointer and Patch

**Files:**
- Modify: `client/implementation/src/main/java/io/smallrye/graphql/client/impl/typesafe/ResultBuilder.java`

- [ ] **Step 1: Replace JSON-P types**

Replace:
- `jakarta.json.JsonPointer` → `com.fasterxml.jackson.core.JsonPointer`
- `jakarta.json.JsonPatch` → rewrite as direct Jackson tree manipulation
- `jakarta.json.JsonArrayBuilder` → `ArrayNode`
- `JsonCollectors.toJsonArray()` → collect into `ArrayNode`
- `JsonBuilderFactory` → `ObjectMapper`'s `NodeFactory`

The JSON Patch usage is for inserting error markers at specific paths in the response tree. Rewrite using Jackson's `ObjectNode.set()` / `ArrayNode.set()` with `JsonPointer`-based traversal.

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -pl client/implementation`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add client/implementation/src/main/java/io/smallrye/graphql/client/impl/typesafe/ResultBuilder.java
git commit -m "feat: rewrite ResultBuilder to use Jackson tree model instead of JSON-P Patch"
```

---

### Task 24: Migrate `client/implementation-vertx`

**Files:**
- Modify: `client/implementation-vertx/pom.xml`
- Modify: Various files in `client/implementation-vertx/src/main/java/`

- [ ] **Step 1: Update POM**

Replace `jakarta.json-api`, `jakarta.json.bind-api`, `yasson` with `jackson-databind` and the compat module.

- [ ] **Step 2: Update Vert.x dynamic client**

In `VertxDynamicGraphQLClient.java`: replace JSON-P types with Jackson.

- [ ] **Step 3: Update WebSocket handlers**

In `GraphQLTransportWSSubprotocolHandler.java`, `GraphQLWSSubprotocolHandler.java`, `WebSocketSubprotocolHandler.java`: replace JSON-P message construction/parsing with Jackson.

- [ ] **Step 4: Verify compilation**

Run: `mvn compile -pl client/implementation-vertx`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add client/implementation-vertx/
git commit -m "feat: migrate client/implementation-vertx to Jackson"
```

---

### Task 25: Run client tests and fix failures

**Files:**
- Modify: various test files in `client/implementation/src/test/` and `client/tck/`

- [ ] **Step 1: Run client implementation tests**

Run: `mvn test -pl client/implementation`

- [ ] **Step 2: Fix test failures**

Update tests that use JSON-P or JSON-B types directly. The TCK tests using `@JsonbProperty`, `@JsonbTransient`, `@JsonbTypeInfo`/`@JsonbSubtype` on test POJOs should still work via the compat module.

- [ ] **Step 3: Run client TCK tests**

Run: `mvn test -pl client/tck`

- [ ] **Step 4: Fix TCK failures**

Update any test that directly references JSON-P types or uses `@JsonbTypeAdapter` (migrate to `@AdaptWith`).

- [ ] **Step 5: Verify all client tests pass**

Run: `mvn test -pl client/implementation,client/implementation-vertx,client/tck`
Expected: All PASS

- [ ] **Step 6: Commit**

```bash
git add client/
git commit -m "test: fix client tests for Jackson migration"
```

---

## Phase 5: Schema Builder and Servlet Changes

### Task 26: Update `common/schema-builder` — remove JSON-B deps, add Jackson adapter support

**Files:**
- Modify: `common/schema-builder/pom.xml`
- Modify: `common/schema-builder/src/main/java/io/smallrye/graphql/schema/helper/AdaptWithHelper.java`
- Modify: `common/schema-builder/src/main/java/io/smallrye/graphql/schema/Annotations.java` (if needed for new Jackson constants)

- [ ] **Step 1: Update schema-builder POM**

Remove `jakarta.json.bind-api` and `jakarta.json-api` dependencies. The DotName constants in `Annotations.java` are just strings — they don't need the annotation classes on the classpath.

Also remove `yasson` from test scope if present.

- [ ] **Step 2: Remove @JsonbTypeAdapter from AdaptWithHelper**

In `AdaptWithHelper.java`:
- Remove the blocks that check for `JAKARTA_JSONB_TYPE_ADAPTER` and `JAVAX_JSONB_TYPE_ADAPTER`
- Add a build-time warning log when either annotation is detected:

```java
if (annotations.containsOneOfTheseAnnotations(Annotations.JAKARTA_JSONB_TYPE_ADAPTER, Annotations.JAVAX_JSONB_TYPE_ADAPTER)) {
    LOG.warn("@JsonbTypeAdapter is no longer supported. Use @AdaptWith or Jackson @JsonSerialize/@JsonDeserialize instead.");
}
```

- [ ] **Step 3: Add Jackson converter annotation support**

Add new constants to `Annotations.java`:

```java
public static final DotName JACKSON_SERIALIZE = DotName.createSimple("com.fasterxml.jackson.databind.annotation.JsonSerialize");
public static final DotName JACKSON_DESERIALIZE = DotName.createSimple("com.fasterxml.jackson.databind.annotation.JsonDeserialize");
```

In `AdaptWithHelper.getAdapterType()`, add support for `@JsonSerialize(converter=...)`:

```java
if (annotations.containsOneOfTheseAnnotations(Annotations.JACKSON_SERIALIZE)) {
    AnnotationInstance serializeAnn = annotations.getAnnotation(Annotations.JACKSON_SERIALIZE);
    AnnotationValue converterValue = serializeAnn.value("converter");
    if (converterValue != null) {
        // Extract converter type and create AdapterType
        // Jackson Converter<IN,OUT> has convert(IN) method
        AdaptWith adaptWith = new AdaptWith(
                "com.fasterxml.jackson.databind.util.StdConverter",
                "convert", "convert");
        return new AdapterType(converterValue.asClass(), adaptWith);
    }
}
```

- [ ] **Step 4: Verify compilation and tests**

Run: `mvn test -pl common/schema-builder`
Expected: BUILD SUCCESS, tests PASS

- [ ] **Step 5: Commit**

```bash
git add common/schema-builder/
git commit -m "feat: remove @JsonbTypeAdapter, add Jackson converter support in schema builder"
```

---

### Task 27: Update `server/implementation-servlet`

**Files:**
- Modify: `server/implementation-servlet/pom.xml`
- Modify: files in `server/implementation-servlet/src/main/java/`

- [ ] **Step 1: Update POM**

Replace `jakarta.json.bind-api` and `jakarta.json-api` with `jackson-databind`.

- [ ] **Step 2: Update servlet classes**

In `ExecutionServlet.java` and related:
- Replace JSON-P `JsonReader` request parsing with `ObjectMapper.readTree()`
- Replace response writing with `ObjectMapper.writeValue(outputStream, node)`

- [ ] **Step 3: Verify compilation**

Run: `mvn compile -pl server/implementation-servlet`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add server/implementation-servlet/
git commit -m "feat: migrate server/implementation-servlet to Jackson"
```

---

### Task 28: Update `server/implementation-cdi`

**Files:**
- Modify: `server/implementation-cdi/pom.xml` (if JSON-B deps exist)
- Modify: files in `server/implementation-cdi/src/main/java/` (if JSON-B usage exists)

- [ ] **Step 1: Check for JSON-B usage**

Grep for `jakarta.json` imports in `server/implementation-cdi/src/main/java/`. Update any references.

- [ ] **Step 2: Update POM if needed**

Remove JSON-B/JSON-P dependencies, add Jackson if needed.

- [ ] **Step 3: Verify compilation and tests**

Run: `mvn test -pl server/implementation-cdi`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add server/implementation-cdi/
git commit -m "chore: update server/implementation-cdi for Jackson migration"
```

---

## Phase 6: Full Build Verification

### Task 29: Full SmallRye GraphQL build

- [ ] **Step 1: Run full build**

Run: `mvn clean install -DskipTests` from the root to verify everything compiles together.
Expected: BUILD SUCCESS

- [ ] **Step 2: Run full test suite**

Run: `mvn clean install` from the root.
Expected: All tests PASS

- [ ] **Step 3: Run code quality checks**

Run: `mvn formatter:validate impsort:check javadoc:javadoc`
Expected: PASS — fix any formatting/import ordering issues

- [ ] **Step 4: Fix any remaining issues**

Address any test failures, compilation errors, or formatting issues discovered.

- [ ] **Step 5: Commit any fixes**

```bash
git add -A
git commit -m "fix: resolve remaining issues from Jackson migration"
```

---

## Phase 7: Quarkus Extension Migration

### Task 30: Update Quarkus GraphQL server extension POMs

**Files:**
- Modify: `extensions/smallrye-graphql/runtime/pom.xml` (in Quarkus repo)
- Modify: `extensions/smallrye-graphql/deployment/pom.xml` (in Quarkus repo)

- [ ] **Step 1: Update runtime POM**

Replace `quarkus-jsonb` with `quarkus-jackson`:

```xml
<!-- Remove -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-jsonb</artifactId>
</dependency>

<!-- Add -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-jackson</artifactId>
</dependency>
```

- [ ] **Step 2: Update deployment POM**

Replace `quarkus-jsonb-deployment` with `quarkus-jackson-deployment`.

- [ ] **Step 3: Commit**

```bash
git add extensions/smallrye-graphql/
git commit -m "chore: swap quarkus-jsonb for quarkus-jackson in GraphQL extension"
```

---

### Task 31: Update Quarkus GraphQL client extension POMs

**Files:**
- Modify: `extensions/smallrye-graphql-client/runtime/pom.xml`
- Modify: `extensions/smallrye-graphql-client/deployment/pom.xml`

- [ ] **Step 1: Update runtime POM**

Replace `quarkus-jsonb` with `quarkus-jackson`. Remove the `jakarta.json` exclusion from `smallrye-graphql-client-implementation-vertx` (no longer needed).

- [ ] **Step 2: Update deployment POM**

Replace `quarkus-jsonb-deployment` with `quarkus-jackson-deployment`.

- [ ] **Step 3: Commit**

```bash
git add extensions/smallrye-graphql-client/
git commit -m "chore: swap quarkus-jsonb for quarkus-jackson in GraphQL client extension"
```

---

### Task 32: Update `SmallRyeGraphQLAbstractHandler` and `SmallRyeGraphQLExecutionHandler`

**Files:**
- Modify: `extensions/smallrye-graphql/runtime/src/main/java/io/quarkus/smallrye/graphql/runtime/SmallRyeGraphQLAbstractHandler.java`
- Modify: `extensions/smallrye-graphql/runtime/src/main/java/io/quarkus/smallrye/graphql/runtime/SmallRyeGraphQLExecutionHandler.java`

- [ ] **Step 1: Update SmallRyeGraphQLAbstractHandler**

Replace:
- `import static io.quarkus.jsonp.JsonProviderHolder.jsonProvider;` → removed
- `JsonReaderFactory jsonReaderFactory = jsonProvider().createReaderFactory(null);` → `ObjectMapper objectMapper = new ObjectMapper();`
- JSON-P parsing → Jackson `objectMapper.readTree()`

- [ ] **Step 2: Update SmallRyeGraphQLExecutionHandler**

Replace:
- `jsonProvider().createMergePatch(...)` merge logic → Jackson `ObjectNode.setAll()` for merging query param and body JSON
- `VertxExecutionResponseWriter` — update to work with Jackson types from `ExecutionResponse`
- Response writing — already writes strings, should be compatible

- [ ] **Step 3: Verify compilation**

Run from Quarkus root: `mvn compile -pl extensions/smallrye-graphql/runtime`

- [ ] **Step 4: Commit**

```bash
git add extensions/smallrye-graphql/runtime/
git commit -m "feat: migrate Quarkus GraphQL handlers to Jackson"
```

---

### Task 33: Update `SmallRyeGraphQLProcessor` — adapter scanning

**Files:**
- Modify: `extensions/smallrye-graphql/deployment/src/main/java/io/quarkus/smallrye/graphql/deployment/SmallRyeGraphQLProcessor.java`

- [ ] **Step 1: Update adapter scanning**

Remove `@JsonbTypeAdapter` scanning:
```java
// Remove this line:
adapterClasses.addAll(getAdapterClasses(index, DotName.createSimple("jakarta.json.bind.annotation.JsonbTypeAdapter")));
```

Add Jackson converter scanning:
```java
adapterClasses.addAll(getAdapterClasses(index, DotName.createSimple("com.fasterxml.jackson.databind.annotation.JsonSerialize")));
```

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -pl extensions/smallrye-graphql/deployment`

- [ ] **Step 3: Commit**

```bash
git add extensions/smallrye-graphql/deployment/
git commit -m "feat: update adapter scanning — remove @JsonbTypeAdapter, add Jackson converter"
```

---

### Task 34: Update Quarkus WebSocket auth handlers

**Files:**
- Modify: `extensions/smallrye-graphql/runtime/src/main/java/io/quarkus/smallrye/graphql/runtime/SmallRyeAuthGraphQLWSHandler.java`
- Modify: `extensions/smallrye-graphql/runtime/src/main/java/io/quarkus/smallrye/graphql/runtime/SmallRyeAuthGraphQLWSSubprotocolHandler.java`
- Modify: `extensions/smallrye-graphql/runtime/src/main/java/io/quarkus/smallrye/graphql/runtime/SmallRyeAuthGraphQLTransportWSSubprotocolHandler.java`

- [ ] **Step 1: Replace JSON-P types**

In each handler, replace `jakarta.json.JsonString`/`JsonObject` usage for extracting authorization tokens with Jackson equivalents (`JsonNode.asText()`).

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -pl extensions/smallrye-graphql/runtime`

- [ ] **Step 3: Commit**

```bash
git add extensions/smallrye-graphql/runtime/
git commit -m "feat: migrate WebSocket auth handlers to Jackson"
```

---

### Task 35: Update Quarkus tests

**Files:**
- Modify: Various test files in `extensions/smallrye-graphql/deployment/src/test/`

- [ ] **Step 1: Update AbstractGraphQLTest**

Replace `jsonProvider()` usage for building test request JSON with Jackson `ObjectMapper`.

- [ ] **Step 2: Update adapter tests**

Any tests using `@JsonbTypeAdapter` should be migrated to `@AdaptWith`.

- [ ] **Step 3: Run Quarkus GraphQL extension tests**

Run: `mvn test -pl extensions/smallrye-graphql/deployment`
Expected: All tests PASS

- [ ] **Step 4: Run Quarkus GraphQL client extension tests**

Run: `mvn test -pl extensions/smallrye-graphql-client/deployment`
Expected: All tests PASS

- [ ] **Step 5: Commit**

```bash
git add extensions/smallrye-graphql/ extensions/smallrye-graphql-client/
git commit -m "test: update Quarkus GraphQL tests for Jackson migration"
```

---

### Task 36: Run MicroProfile GraphQL TCK

**Hard requirement** — the TCK must pass.

- [ ] **Step 1: Run the TCK**

Run from Quarkus root: `mvn test -pl tcks/microprofile-graphql`
Expected: All tests PASS

- [ ] **Step 2: Fix any TCK failures**

TCK failures likely indicate backward compatibility issues. Investigate and fix — these may require updates to the compat module or to the server implementation.

- [ ] **Step 3: Commit fixes**

```bash
git add .
git commit -m "fix: resolve MicroProfile GraphQL TCK failures"
```

---

## Phase 8: Final Verification

### Task 37: Full Quarkus GraphQL build verification

- [ ] **Step 1: Build all GraphQL-related Quarkus modules**

Run from Quarkus root:
```bash
mvn install -pl extensions/smallrye-graphql/runtime,extensions/smallrye-graphql/deployment,extensions/smallrye-graphql-client/runtime,extensions/smallrye-graphql-client/deployment -am
```
Expected: BUILD SUCCESS

- [ ] **Step 2: Run all related integration tests**

Check for any Quarkus integration tests that exercise the GraphQL extensions and run them.

- [ ] **Step 3: Verify SmallRye GraphQL code quality**

Back in the SmallRye repo, run:
```bash
mvn formatter:validate impsort:check javadoc:javadoc
```
Fix any issues.

- [ ] **Step 4: Final commit**

```bash
git add -A
git commit -m "chore: final cleanup for JSON-B to Jackson migration"
```

---

## Task Dependency Graph

```
Phase 1 (Foundation):     [1] → [2] → [3] → [4,5] → [6] → [7] → [8]
Phase 2 (Server API):     [9] → [10]
Phase 3 (Server Impl):    [10] → [11] → [12] → [13] → [14] → [15] → [16]
Phase 4 (Client):         [17] → [18] → [19] → [20] → [21] → [22] → [23] → [24] → [25]
Phase 5 (Schema+Servlet): [26] → [27] → [28]
Phase 6 (Full Build):     [16,25,28] → [29]
Phase 7 (Quarkus):        [29] → [30,31] → [32] → [33] → [34] → [35] → [36]
Phase 8 (Final):          [36] → [37]
```

Tasks within the same phase are mostly sequential (each builds on the previous), but Phase 2 can start in parallel with Phase 1 tasks 4-8, and Phase 5 can run in parallel with Phase 4.
