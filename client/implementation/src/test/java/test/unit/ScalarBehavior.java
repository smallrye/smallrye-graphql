package test.unit;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientException;

class ScalarBehavior {
    private final GraphQlClientFixture fixture = new GraphQlClientFixture();

    @GraphQlClientApi
    interface BoolApi {
        boolean bool();
    }

    @GraphQlClientApi
    interface BooleanApi {
        Boolean bool();
    }

    @Nested
    class BooleanBehavior {
        @Test
        void shouldCallBoolQuery() {
            fixture.returnsData("'bool':true");
            BoolApi api = fixture.builder().build(BoolApi.class);

            boolean bool = api.bool();

            then(fixture.query()).isEqualTo("bool");
            then(bool).isTrue();
        }

        @Test
        void shouldFailToAssignNullToBool() {
            fixture.returnsData("'bool':null");
            BoolApi api = fixture.builder().build(BoolApi.class);

            GraphQlClientException thrown = catchThrowableOfType(api::bool, GraphQlClientException.class);

            then(thrown).hasMessage("invalid boolean value for " + BoolApi.class.getName() + "#bool: null");
        }

        @Test
        void shouldFailToAssignStringToBool() {
            fixture.returnsData("'bool':'xxx'");
            BoolApi api = fixture.builder().build(BoolApi.class);

            GraphQlClientException thrown = catchThrowableOfType(api::bool, GraphQlClientException.class);

            then(thrown).hasMessage("invalid boolean value for " + BoolApi.class.getName() + "#bool: \"xxx\"");
        }

        @Test
        void shouldFailToAssignNumberToBool() {
            fixture.returnsData("'bool':123");
            BoolApi api = fixture.builder().build(BoolApi.class);

            GraphQlClientException thrown = catchThrowableOfType(api::bool, GraphQlClientException.class);

            then(thrown).hasMessage("invalid boolean value for " + BoolApi.class.getName() + "#bool: 123");
        }

        @Test
        void shouldFailToAssignListToBool() {
            fixture.returnsData("'bool':[123]");
            BoolApi api = fixture.builder().build(BoolApi.class);

            GraphQlClientException thrown = catchThrowableOfType(api::bool, GraphQlClientException.class);

            then(thrown).hasMessage("invalid boolean value for " + BoolApi.class.getName() + "#bool: [123]");
        }

        @Test
        void shouldFailToAssignObjectToBool() {
            fixture.returnsData("'bool':{'foo':'bar'}");
            BoolApi api = fixture.builder().build(BoolApi.class);

            GraphQlClientException thrown = catchThrowableOfType(api::bool, GraphQlClientException.class);

            then(thrown).hasMessage("invalid boolean value for " + BoolApi.class.getName() + "#bool: {\"foo\":\"bar\"}");
        }

        @Test
        void shouldCallBooleanQuery() {
            fixture.returnsData("'bool':true");
            BooleanApi api = fixture.builder().build(BooleanApi.class);

            Boolean bool = api.bool();

            then(fixture.query()).isEqualTo("bool");
            then(bool).isTrue();
        }
    }

    @GraphQlClientApi
    interface ByteApi {
        Byte code();
    }

    @GraphQlClientApi
    interface PrimitiveByteApi {
        byte code();
    }

    @Nested
    class ByteBehavior {
        @Test
        void shouldCallByteQuery() {
            fixture.returnsData("'code':5");
            ByteApi api = fixture.builder().build(ByteApi.class);

            Byte code = api.code();

            then(fixture.query()).isEqualTo("code");
            then(code).isEqualTo((byte) 5);
        }

        @Test
        void shouldCallPrimitiveByteQuery() {
            fixture.returnsData("'code':5");
            PrimitiveByteApi api = fixture.builder().build(PrimitiveByteApi.class);

            byte code = api.code();

            then(fixture.query()).isEqualTo("code");
            then(code).isEqualTo((byte) 5);
        }

        @Test
        void shouldFailByteFromTooBigNumberQuery() {
            int tooBig = (int) Byte.MAX_VALUE + 1;
            fixture.returnsData("'code':" + tooBig);
            ByteApi api = fixture.builder().build(ByteApi.class);

            GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

            then(thrown).hasMessage("invalid java.lang.Byte value for " + ByteApi.class.getName() + "#code: " + tooBig);
        }

        @Test
        void shouldFailByteFromNegativeNumberQuery() {
            int tooSmall = (int) Byte.MIN_VALUE - 1;
            fixture.returnsData("'code':" + tooSmall);
            ByteApi api = fixture.builder().build(ByteApi.class);

            GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

            then(thrown).hasMessage("invalid java.lang.Byte value for " + ByteApi.class.getName() + "#code: " + tooSmall);
        }
    }

    @GraphQlClientApi
    interface CharacterApi {
        Character code();
    }

    @GraphQlClientApi
    interface PrimitiveCharApi {
        char code();
    }

    @Nested
    class CharacterBehavior {
        @Test
        void shouldCallCharacterFromStringQuery() {
            fixture.returnsData("'code':'a'");
            CharacterApi api = fixture.builder().build(CharacterApi.class);

            Character c = api.code();

            then(fixture.query()).isEqualTo("code");
            then(c).isEqualTo('a');
        }

        @Test
        void shouldFailCharacterFromStringQueryWithMoreThanOneCharacter() {
            fixture.returnsData("'code':'ab'");
            CharacterApi api = fixture.builder().build(CharacterApi.class);

            GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

            then(thrown).hasMessage("invalid java.lang.Character value for " + CharacterApi.class.getName() + "#code: \"ab\"");
        }

        @Test
        void shouldCallCharacterFromNumberQuery() {
            fixture.returnsData("'code':97");
            CharacterApi api = fixture.builder().build(CharacterApi.class);

            Character c = api.code();

            then(fixture.query()).isEqualTo("code");
            then(c).isEqualTo('a');
        }

        @Test
        void shouldFailCharacterFromTooBigNumberQuery() {
            int tooBig = (int) Character.MAX_VALUE + 1;
            fixture.returnsData("'code':" + tooBig);
            CharacterApi api = fixture.builder().build(CharacterApi.class);

            GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

            then(thrown)
                    .hasMessage("invalid java.lang.Character value for " + CharacterApi.class.getName() + "#code: " + tooBig);
        }

        @Test
        void shouldFailCharacterFromNegativeNumberQuery() {
            fixture.returnsData("'code':-15");
            CharacterApi api = fixture.builder().build(CharacterApi.class);

            GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

            then(thrown).hasMessage("invalid java.lang.Character value for " + CharacterApi.class.getName() + "#code: -15");
        }

        @Test
        void shouldCallPrimitiveCharQuery() {
            fixture.returnsData("'code':'a'");
            PrimitiveCharApi api = fixture.builder().build(PrimitiveCharApi.class);

            char c = api.code();

            then(fixture.query()).isEqualTo("code");
            then(c).isEqualTo('a');
        }

        @Test
        void shouldFailPrimitiveCharQueryWithMoreThanOneCharacter() {
            fixture.returnsData("'code':'ab'");
            PrimitiveCharApi api = fixture.builder().build(PrimitiveCharApi.class);

            GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

            then(thrown).hasMessage("invalid char value for " + PrimitiveCharApi.class.getName() + "#code: \"ab\"");
        }
    }

    @GraphQlClientApi
    interface ShortApi {
        Short code();
    }

    @GraphQlClientApi
    interface PrimitiveShortApi {
        short code();
    }

    @Nested
    class ShortBehavior {
        @Test
        void shouldCallShortQuery() {
            fixture.returnsData("'code':5");
            ShortApi api = fixture.builder().build(ShortApi.class);

            Short code = api.code();

            then(fixture.query()).isEqualTo("code");
            then(code).isEqualTo((short) 5);
        }

        @Test
        void shouldFailToCallTooSmallShortQuery() {
            int tooSmall = (int) Short.MIN_VALUE - 1;
            fixture.returnsData("'code':" + tooSmall);
            ShortApi api = fixture.builder().build(ShortApi.class);

            GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

            then(thrown).hasMessage("invalid java.lang.Short value for " + ShortApi.class.getName() + "#code: " + tooSmall);
        }

        @Test
        void shouldFailToCallTooBigShortQuery() {
            int tooBig = (int) Short.MAX_VALUE + 1;
            fixture.returnsData("'code':" + tooBig);
            ShortApi api = fixture.builder().build(ShortApi.class);

            GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

            then(thrown).hasMessage("invalid java.lang.Short value for " + ShortApi.class.getName() + "#code: " + tooBig);
        }

        @Test
        void shouldCallPrimitiveShortQuery() {
            fixture.returnsData("'code':5");
            PrimitiveShortApi api = fixture.builder().build(PrimitiveShortApi.class);

            short code = api.code();

            then(fixture.query()).isEqualTo("code");
            then(code).isEqualTo((short) 5);
        }
    }

    @GraphQlClientApi
    interface IntegerApi {
        Integer code();
    }

    @GraphQlClientApi
    interface IntApi {
        int code();
    }

    @Nested
    class IntegerBehavior {
        @Test
        void shouldCallIntegerQuery() {
            fixture.returnsData("'code':5");
            IntegerApi api = fixture.builder().build(IntegerApi.class);

            Integer code = api.code();

            then(fixture.query()).isEqualTo("code");
            then(code).isEqualTo(5);
        }

        @Test
        void shouldFailToCallDoubleQuery() {
            double number = 123.456d;
            fixture.returnsData("'code':" + number);
            IntegerApi api = fixture.builder().build(IntegerApi.class);

            GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

            then(thrown).hasMessage("invalid java.lang.Integer value for " + IntegerApi.class.getName() + "#code: " + number);
        }

        @Test
        void shouldFailToCallTooSmallIntegerQuery() {
            long tooSmall = (long) Integer.MIN_VALUE - 1;
            fixture.returnsData("'code':" + tooSmall);
            IntegerApi api = fixture.builder().build(IntegerApi.class);

            GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

            then(thrown).hasMessage("invalid java.lang.Integer value for " + IntegerApi.class.getName() + "#code: " + tooSmall);
        }

        @Test
        void shouldFailToCallTooBigIntegerQuery() {
            long tooBig = (long) Integer.MAX_VALUE + 1;
            fixture.returnsData("'code':" + tooBig);
            IntegerApi api = fixture.builder().build(IntegerApi.class);

            GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

            then(thrown).hasMessage("invalid java.lang.Integer value for " + IntegerApi.class.getName() + "#code: " + tooBig);
        }

        @Test
        void shouldCallIntQuery() {
            fixture.returnsData("'code':5");
            IntApi api = fixture.builder().build(IntApi.class);

            int code = api.code();

            then(fixture.query()).isEqualTo("code");
            then(code).isEqualTo(5);
        }
    }

    @GraphQlClientApi
    interface LongApi {
        Long code();
    }

    @GraphQlClientApi
    interface PrimitiveLongApi {
        long code();
    }

    @Nested
    class LongBehavior {
        @Test
        void shouldCallLongQuery() {
            fixture.returnsData("'code':5");
            LongApi api = fixture.builder().build(LongApi.class);

            Long code = api.code();

            then(fixture.query()).isEqualTo("code");
            then(code).isEqualTo(5L);
        }

        @Test
        void shouldFailToCallTooSmallLongQuery() {
            String tooSmall = "-9223372036854775809";
            fixture.returnsData("'code':" + tooSmall);
            LongApi api = fixture.builder().build(LongApi.class);

            GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

            then(thrown).hasMessage("invalid java.lang.Long value for " + LongApi.class.getName() + "#code: " + tooSmall);
        }

        @Test
        void shouldFailToCallTooBigLongQuery() {
            String tooBig = "9223372036854775808";
            fixture.returnsData("'code':" + tooBig);
            LongApi api = fixture.builder().build(LongApi.class);

            GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

            then(thrown).hasMessage("invalid java.lang.Long value for " + LongApi.class.getName() + "#code: " + tooBig);
        }

        @Test
        void shouldCallPrimitiveLongQuery() {
            fixture.returnsData("'code':5");
            PrimitiveLongApi api = fixture.builder().build(PrimitiveLongApi.class);

            long code = api.code();

            then(fixture.query()).isEqualTo("code");
            then(code).isEqualTo(5L);
        }
    }

    @GraphQlClientApi
    interface FloatApi {
        Float number();
    }

    @GraphQlClientApi
    interface PrimitiveFloatApi {
        float number();
    }

    @Nested
    class FloatBehavior {
        @Test
        void shouldCallFloatQuery() {
            fixture.returnsData("'number':123.456");
            FloatApi api = fixture.builder().build(FloatApi.class);

            Float number = api.number();

            then(fixture.query()).isEqualTo("number");
            then(number).isEqualTo(123.456f);
        }

        @Test
        void shouldCallPrimitiveFloatQuery() {
            fixture.returnsData("'number':123.456");
            PrimitiveFloatApi api = fixture.builder().build(PrimitiveFloatApi.class);

            float number = api.number();

            then(fixture.query()).isEqualTo("number");
            then(number).isEqualTo(123.456f);
        }
    }

    @GraphQlClientApi
    interface DoubleApi {
        Double number();
    }

    @GraphQlClientApi
    interface PrimitiveDoubleApi {
        double number();
    }

    @Nested
    class DoubleBehavior {
        @Test
        void shouldCallDoubleQuery() {
            fixture.returnsData("'number':123.456");
            DoubleApi api = fixture.builder().build(DoubleApi.class);

            Double number = api.number();

            then(fixture.query()).isEqualTo("number");
            then(number).isEqualTo(123.456D);
        }

        @Test
        void shouldCallPrimitiveDoubleQuery() {
            fixture.returnsData("'number':123.456");
            PrimitiveDoubleApi api = fixture.builder().build(PrimitiveDoubleApi.class);

            double number = api.number();

            then(fixture.query()).isEqualTo("number");
            then(number).isEqualTo(123.456D);
        }
    }

    @GraphQlClientApi
    interface BigIntegerApi {
        BigInteger number();
    }

    @Nested
    class BigIntegerBehavior {
        @Test
        void shouldCallReallyLongIntegerQuery() {
            String reallyLongInteger = "12345678901234567890123456789012345678901234567890123456789012345678901234567890";
            fixture.returnsData("'number':" + reallyLongInteger);
            BigIntegerApi api = fixture.builder().build(BigIntegerApi.class);

            BigInteger number = api.number();

            then(fixture.query()).isEqualTo("number");
            then(number).isEqualTo(reallyLongInteger);
        }

        @Test
        void shouldCallNotSoLongIntegerQuery() {
            String notSoLongInteger = "123456";
            fixture.returnsData("'number':" + notSoLongInteger);
            BigIntegerApi api = fixture.builder().build(BigIntegerApi.class);

            BigInteger number = api.number();

            then(fixture.query()).isEqualTo("number");
            then(number).isEqualTo(notSoLongInteger);
        }
    }

    @GraphQlClientApi
    interface BigDecimalApi {
        BigDecimal number();
    }

    @Nested
    class BigDecimalBehavior {
        @Test
        void shouldCallReallyLongDecimalQuery() {
            String reallyLongDecimal = "123.45678901234567890123456789012345678901234567890123456789012345678901234567890";
            fixture.returnsData("'number':" + reallyLongDecimal);
            BigDecimalApi api = fixture.builder().build(BigDecimalApi.class);

            BigDecimal number = api.number();

            then(fixture.query()).isEqualTo("number");
            then(number).isEqualTo(reallyLongDecimal);
        }

        @Test
        void shouldCallNotSoLongDecimalQuery() {
            String notSoLongDecimal = "123.456";
            fixture.returnsData("'number':" + notSoLongDecimal);
            BigDecimalApi api = fixture.builder().build(BigDecimalApi.class);

            BigDecimal number = api.number();

            then(fixture.query()).isEqualTo("number");
            then(number).isEqualTo(notSoLongDecimal);
        }
    }

    @GraphQlClientApi
    interface StringApi {
        String greeting();
    }

    @GraphQlClientApi
    interface ScalarWithValueOfApi {
        Integer foo();
    }

    @GraphQlClientApi
    interface ScalarWithParseApi {
        LocalDate now();
    }

    @GraphQlClientApi
    interface NonScalarWithStringConstructorApi {
        NonScalarWithStringConstructor foo();
    }

    public static class NonScalarWithStringConstructor {
        private final String value;

        @SuppressWarnings("unused")
        public NonScalarWithStringConstructor() {
            this(null);
        }

        public NonScalarWithStringConstructor(String value) {
            this.value = value;
        }
    }

    @GraphQlClientApi
    interface FailingScalarApi {
        @SuppressWarnings("UnusedReturnValue")
        FailingScalar foo();
    }

    public static class FailingScalar {
        @SuppressWarnings("unused")
        public static FailingScalar valueOf(String text) {
            return new FailingScalar(text);
        }

        @SuppressWarnings("unused")
        private final String text;

        public FailingScalar(String text) {
            throw new RuntimeException("dummy exception: " + text);
        }
    }

    @GraphQlClientApi
    interface ScalarWithStringConstructorMethodApi {
        ScalarWithStringConstructorMethod foo();
    }

    @SuppressWarnings("unused")
    public static class ScalarWithStringConstructorMethod {
        public static ScalarWithStringConstructorMethod other(String text) {
            return null;
        }

        public static ScalarWithStringConstructorMethod valueOf() {
            return null;
        }

        public static ScalarWithStringConstructorMethod valueOf(int i) {
            return null;
        }

        public static void valueOf(String text) {
        }

        public static ScalarWithStringConstructorMethod parse(String text) {
            ScalarWithStringConstructorMethod result = new ScalarWithStringConstructorMethod();
            result.text = text;
            return result;
        }

        String text;
    }

    @GraphQlClientApi
    interface ScalarWithOfConstructorMethodApi {
        ScalarWithOfConstructorMethod foo();
    }

    public static class ScalarWithOfConstructorMethod {
        @SuppressWarnings("unused")
        public static ScalarWithOfConstructorMethod of(String text) {
            return new ScalarWithOfConstructorMethod("x-" + text);
        }

        public ScalarWithOfConstructorMethod(String text) {
            this.text = text;
        }

        String text;
    }

    @Nested
    class StringBehavior {
        @Test
        void shouldCallStringQuery() {
            fixture.returnsData("'greeting':'dummy-greeting'");
            StringApi api = fixture.builder().build(StringApi.class);

            String greeting = api.greeting();

            then(fixture.query()).isEqualTo("greeting");
            then(greeting).isEqualTo("dummy-greeting");
        }

        @Test
        void shouldCallScalarWithValueOfQuery() {
            fixture.returnsData("'foo':123456");
            ScalarWithValueOfApi api = fixture.builder().build(ScalarWithValueOfApi.class);

            Integer value = api.foo();

            then(fixture.query()).isEqualTo("foo");
            then(value).isEqualTo(123456);
        }

        @Test
        void shouldCallScalarWithParseQuery() {
            LocalDate now = LocalDate.now();
            fixture.returnsData("'now':'" + now + "'");
            ScalarWithParseApi api = fixture.builder().build(ScalarWithParseApi.class);

            LocalDate value = api.now();

            then(fixture.query()).isEqualTo("now");
            then(value).isEqualTo(now);
        }

        @Test
        void shouldCallNonScalarWithStringConstructorApiQuery() {
            fixture.returnsData("'foo':{'value':'1234'}");
            NonScalarWithStringConstructorApi api = fixture.builder().build(NonScalarWithStringConstructorApi.class);

            NonScalarWithStringConstructor result = api.foo();

            then(fixture.query()).isEqualTo("foo {value}");
            then(result.value).isEqualTo("1234");
        }

        @Test
        void shouldFailToCreateFailingScalar() {
            fixture.returnsData("'foo':'a'");
            FailingScalarApi api = fixture.builder().build(FailingScalarApi.class);

            GraphQlClientException thrown = catchThrowableOfType(api::foo, GraphQlClientException.class);

            then(thrown).hasMessage("can't create scalar " + FailingScalar.class.getName() + " value " +
                    "for " + FailingScalarApi.class.getName() + "#foo");
        }

        @Test
        void shouldCallScalarWithStringConstructorMethodQuery() {
            fixture.returnsData("'foo':'bar'");
            ScalarWithStringConstructorMethodApi api = fixture.builder().build(ScalarWithStringConstructorMethodApi.class);

            ScalarWithStringConstructorMethod value = api.foo();

            then(fixture.query()).isEqualTo("foo");
            then(value.text).isEqualTo("bar");
        }

        @Test
        void shouldCallScalarWithOfConstructorMethodQuery() {
            fixture.returnsData("'foo':'bar'");
            ScalarWithOfConstructorMethodApi api = fixture.builder().build(ScalarWithOfConstructorMethodApi.class);

            ScalarWithOfConstructorMethod value = api.foo();

            then(fixture.query()).isEqualTo("foo");
            then(value.text).isEqualTo("x-bar");
        }
    }

    interface StringGettersApi {
        String getGreeting();

        String get();

        String getG();

        String gets();

        String getting();
    }

    @Test
    void shouldCallStringGetterQuery() {
        fixture.returnsData("'greeting':'foo'");
        StringGettersApi api = fixture.builder().build(StringGettersApi.class);

        String value = api.getGreeting();

        then(fixture.query()).isEqualTo("greeting");
        then(value).isEqualTo("foo");
    }

    @Test
    void shouldCallJustGetQuery() {
        fixture.returnsData("'get':'foo'");
        StringGettersApi api = fixture.builder().build(StringGettersApi.class);

        String value = api.get();

        then(fixture.query()).isEqualTo("get");
        then(value).isEqualTo("foo");
    }

    @Test
    void shouldCallOneCharGetterQuery() {
        fixture.returnsData("'g':'foo'");
        StringGettersApi api = fixture.builder().build(StringGettersApi.class);

        String value = api.getG();

        then(fixture.query()).isEqualTo("g");
        then(value).isEqualTo("foo");
    }

    @Test
    void shouldCallGetAndOneLowerCharQuery() {
        fixture.returnsData("'gets':'foo'");
        StringGettersApi api = fixture.builder().build(StringGettersApi.class);

        String value = api.gets();

        then(fixture.query()).isEqualTo("gets");
        then(value).isEqualTo("foo");
    }

    @Test
    void shouldCallGetAndLowerCharsQuery() {
        fixture.returnsData("'getting':'foo'");
        StringGettersApi api = fixture.builder().build(StringGettersApi.class);

        String value = api.getting();

        then(fixture.query()).isEqualTo("getting");
        then(value).isEqualTo("foo");
    }

    @GraphQlClientApi
    interface LocalDateTimeApi {
        LocalDateTime foo(LocalDateTime date);
    }

    @Test
    void shouldCallLocalDateTimeQuery() {
        LocalDateTime in = LocalDateTime.ofEpochSecond(123456789, 987654321, UTC);
        LocalDateTime out = LocalDateTime.ofEpochSecond(987654321, 123456789, UTC);
        fixture.returnsData("'foo':'" + out + "'");
        LocalDateTimeApi api = fixture.builder().build(LocalDateTimeApi.class);

        LocalDateTime value = api.foo(in);

        then(fixture.query()).isEqualTo("foo(date: $date)");
        then(fixture.variables()).isEqualTo("{'date':'" + in + "'}");
        then(value).isEqualTo(out);
    }

    @GraphQlClientApi
    interface ZonedDateTimeApi {
        ZonedDateTime foo(ZonedDateTime date);
    }

    @Test
    void shouldCallZonedDateTimeQuery() {
        ZonedDateTime in = ZonedDateTime.of(2020, 10, 9, 15, 58, 21, 0, ZoneOffset.ofHours(2));
        ZonedDateTime out = ZonedDateTime.of(2018, 1, 9, 1, 2, 3, 4, ZoneOffset.ofHours(-2));
        fixture.returnsData("'foo':'" + out + "'");
        ZonedDateTimeApi api = fixture.builder().build(ZonedDateTimeApi.class);

        ZonedDateTime value = api.foo(in);

        then(fixture.query()).isEqualTo("foo(date: $date)");
        then(fixture.variables()).isEqualTo("{'date':'" + in + "'}");
        then(value).isEqualTo(out);
    }

    @GraphQlClientApi
    interface InstantApi {
        Instant foo(Instant instant);
    }

    @Test
    void shouldCallInstantQuery() {
        Instant in = Instant.ofEpochMilli(123456789);
        Instant out = Instant.ofEpochMilli(987654321);
        fixture.returnsData("'foo':'" + out + "'");
        InstantApi api = fixture.builder().build(InstantApi.class);

        Instant value = api.foo(in);

        then(fixture.query()).isEqualTo("foo(instant: $instant)");
        then(fixture.variables()).isEqualTo("{'instant':'" + in + "'}");
        then(value).isEqualTo(out);
    }

    @GraphQlClientApi
    interface DateApi {
        Date foo(Date date);
    }

    @Test
    void shouldCallDateQuery() {
        Instant in = Instant.ofEpochMilli(123456789);
        Instant out = Instant.ofEpochMilli(987654321);
        fixture.returnsData("'foo':'" + out + "'");
        DateApi api = fixture.builder().build(DateApi.class);

        Date value = api.foo(Date.from(in));

        then(fixture.query()).isEqualTo("foo(date: $date)");
        then(fixture.variables()).isEqualTo("{'date':'" + in + "'}");
        then(value).isEqualTo(Date.from(out));
    }

    // there's only special code for Date; java.time works out of the box, so we dont' test them all
}
