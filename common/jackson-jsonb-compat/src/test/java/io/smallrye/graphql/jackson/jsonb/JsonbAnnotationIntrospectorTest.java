package io.smallrye.graphql.jackson.jsonb;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class JsonbAnnotationIntrospectorTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JsonbCompatModule());

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

        @jakarta.json.bind.annotation.JsonbCreator
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
}
