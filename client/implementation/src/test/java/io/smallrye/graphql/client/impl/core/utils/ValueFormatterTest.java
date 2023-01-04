package io.smallrye.graphql.client.impl.core.utils;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ValueFormatterTest {

    @Test
    public void testUnsupportedInput() {

        assertThrows(IllegalStateException.class, () -> {
            ValueFormatter.format(new Object());
        });

        assertThrows(IllegalStateException.class, () -> {
            ValueFormatter.format(Map.of("test", "value"));
        });

        assertThrows(IllegalStateException.class, () -> {
            ValueFormatter.format(new Date());
        });
    }

}