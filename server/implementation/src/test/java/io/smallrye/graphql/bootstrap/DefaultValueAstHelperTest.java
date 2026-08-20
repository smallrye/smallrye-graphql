package io.smallrye.graphql.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.NullValue;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;

class DefaultValueAstHelperTest {

    @Test
    void nullProducesNullValue() {
        assertInstanceOf(NullValue.class, DefaultValueAstHelper.toAstValue(null));
    }

    @Test
    void stringProducesStringValue() {
        Value<?> result = DefaultValueAstHelper.toAstValue("hello");
        assertInstanceOf(StringValue.class, result);
        assertEquals("hello", ((StringValue) result).getValue());
    }

    @Test
    void booleanProducesBooleanValue() {
        Value<?> t = DefaultValueAstHelper.toAstValue(true);
        assertInstanceOf(BooleanValue.class, t);
        assertEquals(true, ((BooleanValue) t).isValue());

        Value<?> f = DefaultValueAstHelper.toAstValue(false);
        assertEquals(false, ((BooleanValue) f).isValue());
    }

    @Test
    void integerProducesIntValue() {
        Value<?> result = DefaultValueAstHelper.toAstValue(42);
        assertInstanceOf(IntValue.class, result);
        assertEquals(BigInteger.valueOf(42), ((IntValue) result).getValue());
    }

    @Test
    void longProducesIntValue() {
        Value<?> result = DefaultValueAstHelper.toAstValue(100L);
        assertInstanceOf(IntValue.class, result);
        assertEquals(BigInteger.valueOf(100), ((IntValue) result).getValue());
    }

    @Test
    void bigIntegerProducesIntValue() {
        BigInteger large = new BigInteger("99999999999999999999");
        Value<?> result = DefaultValueAstHelper.toAstValue(large);
        assertInstanceOf(IntValue.class, result);
        assertEquals(large, ((IntValue) result).getValue());
    }

    @Test
    void bigDecimalWholeNumberProducesIntValue() {
        BigDecimal bd = new BigDecimal("1000");
        Value<?> result = DefaultValueAstHelper.toAstValue(bd);
        assertInstanceOf(IntValue.class, result);
        assertEquals(BigInteger.valueOf(1000), ((IntValue) result).getValue());
    }

    @Test
    void bigDecimalFractionalProducesFloatValue() {
        BigDecimal bd = new BigDecimal("3.14");
        Value<?> result = DefaultValueAstHelper.toAstValue(bd);
        assertInstanceOf(FloatValue.class, result);
        assertEquals(bd, ((FloatValue) result).getValue());
    }

    @Test
    void bigDecimalHighPrecisionPreserved() {
        BigDecimal bd = new BigDecimal("1.00000000000000001");
        Value<?> result = DefaultValueAstHelper.toAstValue(bd);
        assertInstanceOf(FloatValue.class, result);
        assertEquals(bd, ((FloatValue) result).getValue());
    }

    @Test
    void doubleProducesFloatValue() {
        Value<?> result = DefaultValueAstHelper.toAstValue(2.5);
        assertInstanceOf(FloatValue.class, result);
    }

    @Test
    void enumProducesEnumValue() {
        Value<?> result = DefaultValueAstHelper.toAstValue(Thread.State.RUNNABLE);
        assertInstanceOf(EnumValue.class, result);
        assertEquals("RUNNABLE", ((EnumValue) result).getName());
    }

    @Test
    void listProducesArrayValue() {
        Value<?> result = DefaultValueAstHelper.toAstValue(List.of("a", "b"));
        assertInstanceOf(ArrayValue.class, result);
        ArrayValue arr = (ArrayValue) result;
        assertEquals(2, arr.getValues().size());
        assertEquals("a", ((StringValue) arr.getValues().get(0)).getValue());
        assertEquals("b", ((StringValue) arr.getValues().get(1)).getValue());
    }

    @Test
    void setProducesArrayValue() {
        LinkedHashSet<String> set = new LinkedHashSet<>(Arrays.asList("x", "y"));
        Value<?> result = DefaultValueAstHelper.toAstValue(set);
        assertInstanceOf(ArrayValue.class, result);
        assertEquals(2, ((ArrayValue) result).getValues().size());
    }

    @Test
    void javaArrayProducesArrayValue() {
        Value<?> result = DefaultValueAstHelper.toAstValue(new int[] { 1, 2, 3 });
        assertInstanceOf(ArrayValue.class, result);
        ArrayValue arr = (ArrayValue) result;
        assertEquals(3, arr.getValues().size());
        assertEquals(BigInteger.valueOf(1), ((IntValue) arr.getValues().get(0)).getValue());
    }

    @Test
    void mapProducesObjectValue() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", new BigDecimal("1000"));
        map.put("name", "Cape");
        Value<?> result = DefaultValueAstHelper.toAstValue(map);
        assertInstanceOf(ObjectValue.class, result);
        ObjectValue obj = (ObjectValue) result;
        assertEquals(2, obj.getObjectFields().size());
        assertEquals("id", obj.getObjectFields().get(0).getName());
        assertInstanceOf(IntValue.class, obj.getObjectFields().get(0).getValue());
        assertEquals("name", obj.getObjectFields().get(1).getName());
        assertInstanceOf(StringValue.class, obj.getObjectFields().get(1).getValue());
    }

    @Test
    void nestedMapInList() {
        Map<String, Object> inner = new LinkedHashMap<>();
        inner.put("key", "val");
        Value<?> result = DefaultValueAstHelper.toAstValue(List.of(inner));
        assertInstanceOf(ArrayValue.class, result);
        ArrayValue arr = (ArrayValue) result;
        assertInstanceOf(ObjectValue.class, arr.getValues().get(0));
    }
}
