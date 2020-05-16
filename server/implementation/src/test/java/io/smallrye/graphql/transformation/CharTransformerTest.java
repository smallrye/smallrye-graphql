package io.smallrye.graphql.transformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CharTransformerTest {

    @ParameterizedTest
    @CsvSource({
            "ab,a",
            "a,a"
    })
    void shouldUseFirstCharFromString(String graphQlString, Character expected) {
        final Character actual = new CharTransformer().in(graphQlString);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldThrowIfStringIsEmpty() {
        assertThatThrownBy(() -> new CharTransformer().in(""))
                .isInstanceOf(Exception.class);

    }

    @Test
    void shouldFormatCharAsString() {
        final String actual = new CharTransformer().out('a');

        assertThat(actual).isEqualTo("a");
    }
}
