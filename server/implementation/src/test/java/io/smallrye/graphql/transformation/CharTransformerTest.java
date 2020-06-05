package io.smallrye.graphql.transformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

public class CharTransformerTest {

    @Test
    public void shouldUseFirstCharFromString() {
        final Character actual = new CharTransformer().in("ab");

        assertThat(actual).isEqualTo('a');
    }

    @Test
    public void shouldUseFirstCharFromSingleLetterString() {
        final Character actual = new CharTransformer().in("a");

        assertThat(actual).isEqualTo('a');
    }

    @Test
    public void shouldThrowIfStringIsEmpty() {
        assertThatThrownBy(() -> new CharTransformer().in(""))
                .isInstanceOf(Exception.class);

    }

    @Test
    public void shouldFormatCharAsString() {
        final String actual = new CharTransformer().out('a');

        assertThat(actual).isEqualTo("a");
    }

}
