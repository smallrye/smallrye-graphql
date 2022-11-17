package io.smallrye.graphql.execution.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.api.ErrorExtensionProvider;

public class ErrorExtensionProvidersTest {
    private final ErrorExtensionProviders providers = new ErrorExtensionProviders();

    @Test
    void should_get_same_instance() {
        List<ErrorExtensionProvider> result1 = providers.get().collect(Collectors.toList());
        List<ErrorExtensionProvider> result2 = providers.get().collect(Collectors.toList());

        Assertions.assertThat(result1.size()).isEqualTo(3);
        assertEquals(result1.size(), result2.size());
        assertTrue(result1.get(0) == result2.get(0));
        assertTrue(result1.get(1) == result2.get(1));
        assertTrue(result1.get(2) == result2.get(2));
    }
}
