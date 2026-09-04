package io.smallrye.graphql.client.impl.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.smallrye.graphql.api.CustomFloatScalar;
import io.smallrye.graphql.api.CustomIntScalar;
import io.smallrye.graphql.api.CustomStringScalar;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ValueFormatterTest {

    @Test
    public void testUnsupportedInput() {

        assertThrows(IllegalStateException.class, () -> ValueFormatter.format(new Object()));

        assertThrows(IllegalStateException.class, () -> ValueFormatter.format(Map.of("test", "value")));

        assertThrows(IllegalStateException.class, () -> ValueFormatter.format(new Date()));
    }

    @Test
    public void testEnumInput() {

        String value = ValueFormatter.format(Order.ASC);

        assertEquals(Order.ASC.toString(), value);
    }

    @Test
    public void testCustomStringScalarInput() {

        final var customString = new CustomString();
        final var value = ValueFormatter.format(customString);

        assertEquals("\"" + customString.stringValueForSerialization() + "\"", value);
    }

    @Test
    public void testCustomIntScalarInput() {

        final var customInt = new CustomInt();
        final var value = ValueFormatter.format(customInt);

        assertEquals(customInt.intValueForSerialization().toString(), value);
    }

    @Test
    public void testCustomFloatScalarInput() {

        final var customFloat = new CustomFloat();
        final var value = ValueFormatter.format(customFloat);

        assertEquals(customFloat.floatValueForSerialization().toString(), value);
    }

    enum Order {
        ASC,
        DESC
    }

    // custom string scalar
    static class CustomString implements CustomStringScalar {

        @Override
        public String stringValueForSerialization() {
            return "test value";
        }
    }

    // custom int scalar
    static class CustomInt implements CustomIntScalar {

        @Override
        public BigInteger intValueForSerialization() {
            return BigInteger.valueOf(123);
        }
    }

    // custom float scalar
    static class CustomFloat implements CustomFloatScalar {

        @Override
        public BigDecimal floatValueForSerialization() {
            return BigDecimal.valueOf(12.3f);
        }
    }

}
