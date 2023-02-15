package io.smallrye.graphql.client.impl.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.Test;

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

    @Test
    public void testEnumInput() {

        String value = ValueFormatter.format(Order.ASC);

        assertEquals(Order.ASC.toString(), value);
    }

    enum Order {
        ASC,
        DESC
    }

}
