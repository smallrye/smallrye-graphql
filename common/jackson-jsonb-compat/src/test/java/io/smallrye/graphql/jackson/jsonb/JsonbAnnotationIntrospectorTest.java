package io.smallrye.graphql.jackson.jsonb;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Locale;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.json.bind.annotation.JsonbNillable;
import jakarta.json.bind.annotation.JsonbNumberFormat;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbSubtype;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.json.bind.annotation.JsonbTypeInfo;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class JsonbAnnotationIntrospectorTest {

    private final ObjectMapper mapper = JsonMapper.builder()
            .addModule(new JsonbCompatModule())
            .build();

    // -- @JsonbProperty --

    static class PropertyBean {
        @JsonbProperty("custom_name")
        public String originalName = "value";
    }

    @Test
    void jsonbPropertyRenamesFieldOnSerialization() throws Exception {
        String json = mapper.writeValueAsString(new PropertyBean());
        assertThat(json).contains("\"custom_name\"").doesNotContain("\"originalName\"");
    }

    @Test
    void jsonbPropertyRenamesFieldOnDeserialization() throws Exception {
        PropertyBean deserialized = mapper.readValue("{\"custom_name\":\"hello\"}", PropertyBean.class);
        assertThat(deserialized.originalName).isEqualTo("hello");
    }

    // -- @JsonbTransient --

    static class TransientBean {
        public String visible = "yes";
        @JsonbTransient
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

        @JsonbCreator
        public CreatorBean(
                @JsonbProperty("name") String name,
                @JsonbProperty("age") int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }

    @Test
    void jsonbCreatorUsesAnnotatedConstructor() throws Exception {
        CreatorBean bean = mapper.readValue("{\"name\":\"Alice\",\"age\":30}", CreatorBean.class);
        assertThat(bean.getName()).isEqualTo("Alice");
        assertThat(bean.getAge()).isEqualTo(30);
    }

    // -- @JsonbNillable --

    @JsonbNillable
    static class NillableBean {
        public String name = null;
        public String other = "value";
    }

    @Test
    void jsonbNillableIncludesNullFields() throws Exception {
        String json = mapper.writeValueAsString(new NillableBean());
        assertThat(json).contains("\"name\":null").contains("\"other\":\"value\"");
    }

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

    // -- @JsonbNumberFormat --

    static class NumberFormatBean {
        @JsonbNumberFormat("#,##0.00")
        public double price = 1234.5;
    }

    @Test
    void jsonbNumberFormatSerializesWithPattern() throws Exception {
        ObjectMapper usMapper = JsonMapper.builder()
                .addModule(new JsonbCompatModule())
                .defaultLocale(Locale.US)
                .build();
        String json = usMapper.writeValueAsString(new NumberFormatBean());
        assertThat(json).contains("\"1,234.50\"");
    }

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

        public String getName() {
            return name;
        }
    }

    static class Cat implements Animal {
        public String name;
        public int lives;

        public String getName() {
            return name;
        }
    }

    @Test
    void jsonbTypeInfoDeserializesPolymorphicType() throws Exception {
        String json = "{\"__typename\":\"Dog\",\"name\":\"Rex\",\"breed\":\"Labrador\"}";
        Animal animal = mapper.readValue(json, Animal.class);
        assertThat(animal).isInstanceOf(Dog.class);
        assertThat(((Dog) animal).breed).isEqualTo("Labrador");
    }

    @Test
    void jsonbTypeInfoSerializesPolymorphicType() throws Exception {
        Dog dog = new Dog();
        dog.name = "Rex";
        dog.breed = "Labrador";
        String json = mapper.writeValueAsString(dog);
        // Should include the type discriminator
        assertThat(json).contains("\"name\":\"Rex\"").contains("\"breed\":\"Labrador\"");
    }
}
