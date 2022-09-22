package tck.graphql.typesafe;

import static java.time.ZoneOffset.UTC;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.NonNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

class ScalarBehavior {
    private final TypesafeGraphQLClientFixture fixture = TypesafeGraphQLClientFixture.load();

    @GraphQLClientApi
    interface BoolApi {
        boolean bool(boolean in);
    }

    @GraphQLClientApi
    interface BooleanApi {
        Boolean bool(Boolean in);
    }

    @Nested
    class BooleanBehavior {
        @Test
        void shouldCallBoolQuery() {
            fixture.returnsData("'bool':true");
            BoolApi api = fixture.build(BoolApi.class);

            boolean bool = api.bool(true);

            then(fixture.query()).isEqualTo("query bool($in: Boolean!) { bool(in: $in) }");
            then(bool).isTrue();
        }

        @Test
        void shouldFailToAssignNullToBool() {
            fixture.returnsData("'bool':null");
            BoolApi api = fixture.build(BoolApi.class);

            InvalidResponseException thrown = catchThrowableOfType(() -> api.bool(true), InvalidResponseException.class);

            then(thrown).hasMessage("invalid boolean value for " + BoolApi.class.getName() + "#bool: null");
        }

        @Test
        void shouldFailToAssignStringToBool() {
            fixture.returnsData("'bool':'xxx'");
            BoolApi api = fixture.build(BoolApi.class);

            InvalidResponseException thrown = catchThrowableOfType(() -> api.bool(true), InvalidResponseException.class);

            then(thrown).hasMessage("invalid boolean value for " + BoolApi.class.getName() + "#bool: \"xxx\"");
        }

        @Test
        void shouldFailToAssignNumberToBool() {
            fixture.returnsData("'bool':123");
            BoolApi api = fixture.build(BoolApi.class);

            InvalidResponseException thrown = catchThrowableOfType(() -> api.bool(true), InvalidResponseException.class);

            then(thrown).hasMessage("invalid boolean value for " + BoolApi.class.getName() + "#bool: 123");
        }

        @Test
        void shouldFailToAssignListToBool() {
            fixture.returnsData("'bool':[123]");
            BoolApi api = fixture.build(BoolApi.class);

            InvalidResponseException thrown = catchThrowableOfType(() -> api.bool(true), InvalidResponseException.class);

            then(thrown).hasMessage("invalid boolean value for " + BoolApi.class.getName() + "#bool: [123]");
        }

        @Test
        void shouldFailToAssignObjectToBool() {
            fixture.returnsData("'bool':{'foo':'bar'}");
            BoolApi api = fixture.build(BoolApi.class);

            InvalidResponseException thrown = catchThrowableOfType(() -> api.bool(true), InvalidResponseException.class);

            then(thrown).hasMessage("invalid boolean value for " + BoolApi.class.getName() + "#bool: {\"foo\":\"bar\"}");
        }

        @Test
        void shouldCallBooleanQuery() {
            fixture.returnsData("'bool':true");
            BooleanApi api = fixture.build(BooleanApi.class);

            Boolean bool = api.bool(true);

            then(fixture.query()).isEqualTo("query bool($in: Boolean) { bool(in: $in) }");
            then(bool).isTrue();
        }
    }

    @GraphQLClientApi
    interface ByteApi {
        Byte code(Byte in);
    }

    @GraphQLClientApi
    interface PrimitiveByteApi {
        byte code(byte in);
    }

    @Nested
    class ByteBehavior {
        @Test
        void shouldCallByteQuery() {
            fixture.returnsData("'code':5");
            ByteApi api = fixture.build(ByteApi.class);

            Byte code = api.code((byte) 1);

            then(fixture.query()).isEqualTo("query code($in: Int) { code(in: $in) }");
            then(code).isEqualTo((byte) 5);
        }

        @Test
        void shouldCallPrimitiveByteQuery() {
            fixture.returnsData("'code':5");
            PrimitiveByteApi api = fixture.build(PrimitiveByteApi.class);

            byte code = api.code((byte) 1);

            then(fixture.query()).isEqualTo("query code($in: Int!) { code(in: $in) }");
            then(code).isEqualTo((byte) 5);
        }

        @Test
        void shouldFailByteFromTooBigNumberQuery() {
            int tooBig = (int) Byte.MAX_VALUE + 1;
            fixture.returnsData("'code':" + tooBig);
            ByteApi api = fixture.build(ByteApi.class);

            InvalidResponseException thrown = catchThrowableOfType(() -> api.code((byte) 1), InvalidResponseException.class);

            then(thrown).hasMessage("invalid java.lang.Byte value for " + ByteApi.class.getName() + "#code: " + tooBig);
        }

        @Test
        void shouldFailByteFromNegativeNumberQuery() {
            int tooSmall = (int) Byte.MIN_VALUE - 1;
            fixture.returnsData("'code':" + tooSmall);
            ByteApi api = fixture.build(ByteApi.class);

            InvalidResponseException thrown = catchThrowableOfType(() -> api.code((byte) 1), InvalidResponseException.class);

            then(thrown).hasMessage("invalid java.lang.Byte value for " + ByteApi.class.getName() + "#code: " + tooSmall);
        }
    }

    @GraphQLClientApi
    interface CharacterApi {
        Character code(Character in);
    }

    @GraphQLClientApi
    interface PrimitiveCharApi {
        char code(char in);
    }

    @Nested
    class CharacterBehavior {
        @Test
        void shouldCallCharacterFromStringQuery() {
            fixture.returnsData("'code':'a'");
            CharacterApi api = fixture.build(CharacterApi.class);

            Character c = api.code('c');

            then(fixture.query()).isEqualTo("query code($in: String) { code(in: $in) }");
            then(c).isEqualTo('a');
        }

        @Test
        void shouldFailCharacterFromStringQueryWithMoreThanOneCharacter() {
            fixture.returnsData("'code':'ab'");
            CharacterApi api = fixture.build(CharacterApi.class);

            InvalidResponseException thrown = catchThrowableOfType(() -> api.code('c'), InvalidResponseException.class);

            then(thrown).hasMessage("invalid java.lang.Character value for " + CharacterApi.class.getName() + "#code: \"ab\"");
        }

        @Test
        void shouldCallCharacterFromNumberQuery() {
            fixture.returnsData("'code':97");
            CharacterApi api = fixture.build(CharacterApi.class);

            Character c = api.code('c');

            then(fixture.query()).isEqualTo("query code($in: String) { code(in: $in) }");
            then(c).isEqualTo('a');
        }

        @Test
        void shouldFailCharacterFromTooBigNumberQuery() {
            int tooBig = (int) Character.MAX_VALUE + 1;
            fixture.returnsData("'code':" + tooBig);
            CharacterApi api = fixture.build(CharacterApi.class);

            InvalidResponseException thrown = catchThrowableOfType(() -> api.code('c'), InvalidResponseException.class);

            then(thrown)
                    .hasMessage("invalid java.lang.Character value for " + CharacterApi.class.getName() + "#code: " + tooBig);
        }

        @Test
        void shouldFailCharacterFromNegativeNumberQuery() {
            fixture.returnsData("'code':-15");
            CharacterApi api = fixture.build(CharacterApi.class);

            InvalidResponseException thrown = catchThrowableOfType(() -> api.code('c'), InvalidResponseException.class);

            then(thrown).hasMessage("invalid java.lang.Character value for " + CharacterApi.class.getName() + "#code: -15");
        }

        @Test
        void shouldCallPrimitiveCharQuery() {
            fixture.returnsData("'code':'a'");
            PrimitiveCharApi api = fixture.build(PrimitiveCharApi.class);

            char c = api.code('c');

            then(fixture.query()).isEqualTo("query code($in: String!) { code(in: $in) }");
            then(c).isEqualTo('a');
        }

        @Test
        void shouldFailPrimitiveCharQueryWithMoreThanOneCharacter() {
            fixture.returnsData("'code':'ab'");
            PrimitiveCharApi api = fixture.build(PrimitiveCharApi.class);

            InvalidResponseException thrown = catchThrowableOfType(() -> api.code('c'), InvalidResponseException.class);

            then(thrown).hasMessage("invalid char value for " + PrimitiveCharApi.class.getName() + "#code: \"ab\"");
        }
    }

    @GraphQLClientApi
    interface ShortApi {
        Short code(Short in);
    }

    @GraphQLClientApi
    interface PrimitiveShortApi {
        short code(short in);
    }

    @Nested
    class ShortBehavior {
        @Test
        void shouldCallShortQuery() {
            fixture.returnsData("'code':5");
            ShortApi api = fixture.build(ShortApi.class);

            Short code = api.code((short) 2);

            then(fixture.query()).isEqualTo("query code($in: Int) { code(in: $in) }");
            then(code).isEqualTo((short) 5);
        }

        @Test
        void shouldFailToCallTooSmallShortQuery() {
            int tooSmall = (int) Short.MIN_VALUE - 1;
            fixture.returnsData("'code':" + tooSmall);
            ShortApi api = fixture.build(ShortApi.class);

            InvalidResponseException thrown = catchThrowableOfType(() -> api.code((short) 2), InvalidResponseException.class);

            then(thrown).hasMessage("invalid java.lang.Short value for " + ShortApi.class.getName() + "#code: " + tooSmall);
        }

        @Test
        void shouldFailToCallTooBigShortQuery() {
            int tooBig = (int) Short.MAX_VALUE + 1;
            fixture.returnsData("'code':" + tooBig);
            ShortApi api = fixture.build(ShortApi.class);

            InvalidResponseException thrown = catchThrowableOfType(() -> api.code((short) 2), InvalidResponseException.class);

            then(thrown).hasMessage("invalid java.lang.Short value for " + ShortApi.class.getName() + "#code: " + tooBig);
        }

        @Test
        void shouldCallPrimitiveShortQuery() {
            fixture.returnsData("'code':5");
            PrimitiveShortApi api = fixture.build(PrimitiveShortApi.class);

            short code = api.code((short) 2);

            then(fixture.query()).isEqualTo("query code($in: Int!) { code(in: $in) }");
            then(code).isEqualTo((short) 5);
        }
    }

    @GraphQLClientApi
    interface IntegerApi {
        Integer code(Integer in);
    }

    @GraphQLClientApi
    interface IntApi {
        int code(int in);
    }

    @Nested
    class IntegerBehavior {
        @Test
        void shouldCallIntegerQuery() {
            fixture.returnsData("'code':5");
            IntegerApi api = fixture.build(IntegerApi.class);

            Integer code = api.code(3);

            then(fixture.query()).isEqualTo("query code($in: Int) { code(in: $in) }");
            then(code).isEqualTo(5);
        }

        @Test
        void shouldFailToCallDoubleQuery() {
            double number = 123.456d;
            fixture.returnsData("'code':" + number);
            IntegerApi api = fixture.build(IntegerApi.class);

            InvalidResponseException thrown = catchThrowableOfType(() -> api.code(3), InvalidResponseException.class);

            then(thrown).hasMessage("invalid java.lang.Integer value for " + IntegerApi.class.getName() + "#code: " + number);
        }

        @Test
        void shouldFailToCallTooSmallIntegerQuery() {
            long tooSmall = (long) Integer.MIN_VALUE - 1;
            fixture.returnsData("'code':" + tooSmall);
            IntegerApi api = fixture.build(IntegerApi.class);

            InvalidResponseException thrown = catchThrowableOfType(() -> api.code(3), InvalidResponseException.class);

            then(thrown).hasMessage("invalid java.lang.Integer value for " + IntegerApi.class.getName() + "#code: " + tooSmall);
        }

        @Test
        void shouldFailToCallTooBigIntegerQuery() {
            long tooBig = (long) Integer.MAX_VALUE + 1;
            fixture.returnsData("'code':" + tooBig);
            IntegerApi api = fixture.build(IntegerApi.class);

            InvalidResponseException thrown = catchThrowableOfType(() -> api.code(3), InvalidResponseException.class);

            then(thrown).hasMessage("invalid java.lang.Integer value for " + IntegerApi.class.getName() + "#code: " + tooBig);
        }

        @Test
        void shouldCallIntQuery() {
            fixture.returnsData("'code':5");
            IntApi api = fixture.build(IntApi.class);

            int code = api.code(3);

            then(fixture.query()).isEqualTo("query code($in: Int!) { code(in: $in) }");
            then(code).isEqualTo(5);
        }
    }

    @GraphQLClientApi
    interface LongApi {
        Long code(Long in);
    }

    @GraphQLClientApi
    interface PrimitiveLongApi {
        long code(long in);
    }

    @Nested
    class LongBehavior {
        @Test
        void shouldCallLongQuery() {
            fixture.returnsData("'code':5");
            LongApi api = fixture.build(LongApi.class);

            Long code = api.code(7L);

            then(fixture.query()).isEqualTo("query code($in: BigInteger) { code(in: $in) }");
            then(code).isEqualTo(5L);
        }

        @Test
        void shouldFailToCallTooSmallLongQuery() {
            String tooSmall = "-9223372036854775809";
            fixture.returnsData("'code':" + tooSmall);
            LongApi api = fixture.build(LongApi.class);

            InvalidResponseException thrown = catchThrowableOfType(() -> api.code(7L), InvalidResponseException.class);

            then(thrown).hasMessage("invalid java.lang.Long value for " + LongApi.class.getName() + "#code: " + tooSmall);
        }

        @Test
        void shouldFailToCallTooBigLongQuery() {
            String tooBig = "9223372036854775808";
            fixture.returnsData("'code':" + tooBig);
            LongApi api = fixture.build(LongApi.class);

            InvalidResponseException thrown = catchThrowableOfType(() -> api.code(7L), InvalidResponseException.class);

            then(thrown).hasMessage("invalid java.lang.Long value for " + LongApi.class.getName() + "#code: " + tooBig);
        }

        @Test
        void shouldCallPrimitiveLongQuery() {
            fixture.returnsData("'code':5");
            PrimitiveLongApi api = fixture.build(PrimitiveLongApi.class);

            long code = api.code(7L);

            then(fixture.query()).isEqualTo("query code($in: BigInteger!) { code(in: $in) }");
            then(code).isEqualTo(5L);
        }
    }

    @GraphQLClientApi
    interface FloatApi {
        Float number(Float in);
    }

    @GraphQLClientApi
    interface PrimitiveFloatApi {
        float number(float in);
    }

    @Nested
    class FloatBehavior {
        @Test
        void shouldCallFloatQuery() {
            fixture.returnsData("'number':123.456");
            FloatApi api = fixture.build(FloatApi.class);

            Float number = api.number(7.8f);

            then(fixture.query()).isEqualTo("query number($in: Float) { number(in: $in) }");
            then(number).isEqualTo(123.456f);
        }

        @Test
        void shouldCallPrimitiveFloatQuery() {
            fixture.returnsData("'number':123.456");
            PrimitiveFloatApi api = fixture.build(PrimitiveFloatApi.class);

            float number = api.number(7.8f);

            then(fixture.query()).isEqualTo("query number($in: Float!) { number(in: $in) }");
            then(number).isEqualTo(123.456f);
        }
    }

    @GraphQLClientApi
    interface DoubleApi {
        Double number(Double in);
    }

    @GraphQLClientApi
    interface PrimitiveDoubleApi {
        double number(double in);
    }

    @Nested
    class DoubleBehavior {
        @Test
        void shouldCallDoubleQuery() {
            fixture.returnsData("'number':123.456");
            DoubleApi api = fixture.build(DoubleApi.class);

            Double number = api.number(4.5);

            then(fixture.query()).isEqualTo("query number($in: Float) { number(in: $in) }");
            then(number).isEqualTo(123.456D);
        }

        @Test
        void shouldCallPrimitiveDoubleQuery() {
            fixture.returnsData("'number':123.456");
            PrimitiveDoubleApi api = fixture.build(PrimitiveDoubleApi.class);

            double number = api.number(4.5);

            then(fixture.query()).isEqualTo("query number($in: Float!) { number(in: $in) }");
            then(number).isEqualTo(123.456D);
        }
    }

    @GraphQLClientApi
    interface BigIntegerApi {
        BigInteger number(BigInteger in);
    }

    @Nested
    class BigIntegerBehavior {
        @Test
        void shouldCallReallyLongIntegerQuery() {
            String reallyLongInteger = "12345678901234567890123456789012345678901234567890123456789012345678901234567890";
            fixture.returnsData("'number':" + reallyLongInteger);
            BigIntegerApi api = fixture.build(BigIntegerApi.class);

            BigInteger number = api.number(BigInteger.TEN);

            then(fixture.query()).isEqualTo("query number($in: BigInteger) { number(in: $in) }");
            then(number).isEqualTo(reallyLongInteger);
        }

        @Test
        void shouldCallNotSoLongIntegerQuery() {
            String notSoLongInteger = "123456";
            fixture.returnsData("'number':" + notSoLongInteger);
            BigIntegerApi api = fixture.build(BigIntegerApi.class);

            BigInteger number = api.number(BigInteger.TEN);

            then(fixture.query()).isEqualTo("query number($in: BigInteger) { number(in: $in) }");
            then(number).isEqualTo(notSoLongInteger);
        }
    }

    @GraphQLClientApi
    interface BigDecimalApi {
        BigDecimal number(BigDecimal in);
    }

    @Nested
    class BigDecimalBehavior {
        @Test
        void shouldCallReallyLongDecimalQuery() {
            String reallyLongDecimal = "123.45678901234567890123456789012345678901234567890123456789012345678901234567890";
            fixture.returnsData("'number':" + reallyLongDecimal);
            BigDecimalApi api = fixture.build(BigDecimalApi.class);

            BigDecimal number = api.number(BigDecimal.valueOf(12.34));

            then(fixture.query()).isEqualTo("query number($in: BigDecimal) { number(in: $in) }");
            then(number).isEqualTo(reallyLongDecimal);
        }

        @Test
        void shouldCallNotSoLongDecimalQuery() {
            String notSoLongDecimal = "123.456";
            fixture.returnsData("'number':" + notSoLongDecimal);
            BigDecimalApi api = fixture.build(BigDecimalApi.class);

            BigDecimal number = api.number(BigDecimal.valueOf(12.34));

            then(fixture.query()).isEqualTo("query number($in: BigDecimal) { number(in: $in) }");
            then(number).isEqualTo(notSoLongDecimal);
        }
    }

    @GraphQLClientApi
    interface StringApi {
        String greeting(String in);
    }

    @GraphQLClientApi
    interface IdApi {
        @Id
        String idea(
                @Id String stringId,
                @Id List<String> stringListId,
                @Id List<@NonNull String> stringListInnerNonNullId,
                @Id @NonNull List<String> stringListOuterNonNullId,
                @Id @NonNull List<@NonNull String> stringListBothNonNullId,
                @Id long primitiveLongId,
                @Id int primitiveIntId,
                @Id Long longId,
                @Id List<Long> longListId,
                @Id Integer intId,
                @Id UUID uuidId);
    }

    @GraphQLClientApi
    interface NonNullIdApi {
        @NonNull
        @Id
        String idea(
                @NonNull @Id String stringId,
                @NonNull @Id long primitiveLongId,
                @NonNull @Id int primitiveIntId,
                @NonNull @Id Long longId,
                @NonNull @Id Integer intId,
                @NonNull @Id UUID uuidId);
    }

    @GraphQLClientApi
    interface ScalarWithValueOfApi {
        Integer foo();
    }

    @GraphQLClientApi
    interface ScalarWithParseApi {
        LocalDate now();
    }

    @GraphQLClientApi
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

    @GraphQLClientApi
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

    @GraphQLClientApi
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

    @GraphQLClientApi
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
            StringApi api = fixture.build(StringApi.class);

            String greeting = api.greeting("in");

            then(fixture.query()).isEqualTo("query greeting($in: String) { greeting(in: $in) }");
            then(greeting).isEqualTo("dummy-greeting");
        }

        @Test
        void shouldCallIdQuery() {
            fixture.returnsData("'idea':'out'");
            IdApi api = fixture.builder().build(IdApi.class);

            String out = api.idea("stringId", singletonList("x"), singletonList("x"), singletonList("x"), singletonList("x"),
                    1L, 2, 3L, singletonList(5L), 4, UUID.randomUUID());

            then(fixture.query()).isEqualTo("query idea(" +
                    "$stringId: ID, " +
                    "$stringListId: [ID], " +
                    "$stringListInnerNonNullId: [ID!], " +
                    "$stringListOuterNonNullId: [ID]!, " +
                    "$stringListBothNonNullId: [ID!]!, " +
                    "$primitiveLongId: ID!, " +
                    "$primitiveIntId: ID!, " +
                    "$longId: ID, " +
                    "$longListId: [ID], " +
                    "$intId: ID, " +
                    "$uuidId: ID) " +
                    "{ idea(" +
                    "stringId: $stringId, " +
                    "stringListId: $stringListId, " +
                    "stringListInnerNonNullId: $stringListInnerNonNullId, " +
                    "stringListOuterNonNullId: $stringListOuterNonNullId, " +
                    "stringListBothNonNullId: $stringListBothNonNullId, " +
                    "primitiveLongId: $primitiveLongId, " +
                    "primitiveIntId: $primitiveIntId, " +
                    "longId: $longId, " +
                    "longListId: $longListId, " +
                    "intId: $intId, " +
                    "uuidId: $uuidId) }");
            then(out).isEqualTo("out");
        }

        @Test
        void shouldCallNonNullIdQuery() {
            fixture.returnsData("'idea':'out'");
            NonNullIdApi api = fixture.builder().build(NonNullIdApi.class);

            String out = api.idea("stringId", 1L, 2, 3L, 4, UUID.randomUUID());

            then(fixture.query()).isEqualTo("query idea(" +
                    "$stringId: ID!, " +
                    "$primitiveLongId: ID!, " +
                    "$primitiveIntId: ID!, " +
                    "$longId: ID!, " +
                    "$intId: ID!, " +
                    "$uuidId: ID!) " +
                    "{ idea(" +
                    "stringId: $stringId, " +
                    "primitiveLongId: $primitiveLongId, " +
                    "primitiveIntId: $primitiveIntId, " +
                    "longId: $longId, " +
                    "intId: $intId, " +
                    "uuidId: $uuidId) }");
            then(out).isEqualTo("out");
        }

        @Test
        void shouldCallScalarWithValueOfQuery() {
            fixture.returnsData("'foo':123456");
            ScalarWithValueOfApi api = fixture.build(ScalarWithValueOfApi.class);

            Integer value = api.foo();

            then(fixture.query()).isEqualTo("query foo { foo }");
            then(value).isEqualTo(123456);
        }

        @Test
        void shouldCallScalarWithParseQuery() {
            LocalDate now = LocalDate.now();
            fixture.returnsData("'now':'" + now + "'");
            ScalarWithParseApi api = fixture.build(ScalarWithParseApi.class);

            LocalDate value = api.now();

            then(fixture.query()).isEqualTo("query now { now }");
            then(value).isEqualTo(now);
        }

        @Test
        void shouldCallNonScalarWithStringConstructorApiQuery() {
            fixture.returnsData("'foo':{'value':'1234'}");
            NonScalarWithStringConstructorApi api = fixture.build(NonScalarWithStringConstructorApi.class);

            NonScalarWithStringConstructor result = api.foo();

            then(fixture.query()).isEqualTo("query foo { foo {value} }");
            then(result.value).isEqualTo("1234");
        }

        @Test
        void shouldFailToCreateFailingScalar() {
            fixture.returnsData("'foo':'a'");
            FailingScalarApi api = fixture.build(FailingScalarApi.class);

            RuntimeException thrown = catchThrowableOfType(api::foo, RuntimeException.class);

            then(thrown).hasMessage("can't create scalar " + FailingScalar.class.getName() + " value " +
                    "for " + FailingScalarApi.class.getName() + "#foo");
        }

        @Test
        void shouldCallScalarWithStringConstructorMethodQuery() {
            fixture.returnsData("'foo':'bar'");
            ScalarWithStringConstructorMethodApi api = fixture.build(ScalarWithStringConstructorMethodApi.class);

            ScalarWithStringConstructorMethod value = api.foo();

            then(fixture.query()).isEqualTo("query foo { foo }");
            then(value.text).isEqualTo("bar");
        }

        @Test
        void shouldCallScalarWithOfConstructorMethodQuery() {
            fixture.returnsData("'foo':'bar'");
            ScalarWithOfConstructorMethodApi api = fixture.build(ScalarWithOfConstructorMethodApi.class);

            ScalarWithOfConstructorMethod value = api.foo();

            then(fixture.query()).isEqualTo("query foo { foo }");
            then(value.text).isEqualTo("x-bar");
        }
    }

    @GraphQLClientApi
    interface StringGettersApi {
        String getGreeting();

        String get();

        String getG();

        String gets();

        String getting();
    }

    @Nested
    class GetterBehavior {
        @Test
        void shouldCallStringGetterQuery() {
            fixture.returnsData("'greeting':'foo'");
            StringGettersApi api = fixture.build(StringGettersApi.class);

            String value = api.getGreeting();

            then(fixture.query()).isEqualTo("query greeting { greeting }");
            then(value).isEqualTo("foo");
        }

        @Test
        void shouldCallJustGetQuery() {
            fixture.returnsData("'get':'foo'");
            StringGettersApi api = fixture.build(StringGettersApi.class);

            String value = api.get();

            then(fixture.query()).isEqualTo("query get { get }");
            then(value).isEqualTo("foo");
        }

        @Test
        void shouldCallOneCharGetterQuery() {
            fixture.returnsData("'g':'foo'");
            StringGettersApi api = fixture.build(StringGettersApi.class);

            String value = api.getG();

            then(fixture.query()).isEqualTo("query g { g }");
            then(value).isEqualTo("foo");
        }

        @Test
        void shouldCallGetAndOneLowerCharQuery() {
            fixture.returnsData("'gets':'foo'");
            StringGettersApi api = fixture.build(StringGettersApi.class);

            String value = api.gets();

            then(fixture.query()).isEqualTo("query gets { gets }");
            then(value).isEqualTo("foo");
        }

        @Test
        void shouldCallGetAndLowerCharsQuery() {
            fixture.returnsData("'getting':'foo'");
            StringGettersApi api = fixture.build(StringGettersApi.class);

            String value = api.getting();

            then(fixture.query()).isEqualTo("query getting { getting }");
            then(value).isEqualTo("foo");
        }
    }

    @GraphQLClientApi
    interface LocalDateApi {
        LocalDate foo(LocalDate date);
    }

    @GraphQLClientApi
    interface LocalTimeApi {
        LocalTime foo(LocalTime date);
    }

    @GraphQLClientApi
    interface OffsetTimeApi {
        OffsetTime foo(OffsetTime date);
    }

    @GraphQLClientApi
    interface LocalDateTimeApi {
        LocalDateTime foo(LocalDateTime date);
    }

    @GraphQLClientApi
    interface OffsetDateTimeApi {
        OffsetDateTime foo(OffsetDateTime date);
    }

    @GraphQLClientApi
    interface ZonedDateTimeApi {
        ZonedDateTime foo(ZonedDateTime date);
    }

    @GraphQLClientApi
    interface InstantApi {
        Instant foo(Instant instant);
    }

    @GraphQLClientApi
    interface DateApi {
        Date foo(Date date);
    }

    @Nested
    class DateAndTimeBehavior {
        @Test
        void shouldCallLocalDateQuery() {
            LocalDate in = LocalDate.of(2020, 10, 31);
            LocalDate out = LocalDate.of(3000, 1, 1);
            fixture.returnsData("'foo':'" + out + "'");
            LocalDateApi api = fixture.builder().build(LocalDateApi.class);

            LocalDate value = api.foo(in);

            then(fixture.query()).isEqualTo("query foo($date: Date) { foo(date: $date) }");
            then(fixture.variables()).isEqualTo("{'date':'" + in + "'}");
            then(value).isEqualTo(out);
        }

        @Test
        void shouldCallLocalTimeQuery() {
            LocalTime in = LocalTime.of(12, 34, 56);
            LocalTime out = LocalTime.of(21, 43, 55);
            fixture.returnsData("'foo':'" + out + "'");
            LocalTimeApi api = fixture.builder().build(LocalTimeApi.class);

            LocalTime value = api.foo(in);

            then(fixture.query()).isEqualTo("query foo($date: Time) { foo(date: $date) }");
            then(fixture.variables()).isEqualTo("{'date':'" + in + "'}");
            then(value).isEqualTo(out);
        }

        @Test
        void shouldCallOffsetTimeQuery() {
            OffsetTime in = OffsetTime.of(12, 34, 56, 789, ZoneOffset.ofHours(3));
            OffsetTime out = OffsetTime.of(21, 43, 55, 987, ZoneOffset.ofHours(5));
            fixture.returnsData("'foo':'" + out + "'");
            OffsetTimeApi api = fixture.builder().build(OffsetTimeApi.class);

            OffsetTime value = api.foo(in);

            then(fixture.query()).isEqualTo("query foo($date: Time) { foo(date: $date) }");
            then(fixture.variables()).isEqualTo("{'date':'" + in + "'}");
            then(value).isEqualTo(out);
        }

        @Test
        void shouldCallLocalDateTimeQuery() {
            LocalDateTime in = LocalDateTime.of(2020, 10, 9, 15, 58, 21, 0);
            LocalDateTime out = LocalDateTime.of(2018, 1, 9, 1, 2, 3, 4);
            fixture.returnsData("'foo':'" + out + "'");
            LocalDateTimeApi api = fixture.build(LocalDateTimeApi.class);

            LocalDateTime value = api.foo(in);

            then(fixture.query()).isEqualTo("query foo($date: DateTime) { foo(date: $date) }");
            then(fixture.variables()).isEqualTo("{'date':'" + in + "'}");
            then(value).isEqualTo(out);
        }

        @Test
        void shouldCallOffsetDateTimeQuery() {
            OffsetDateTime in = OffsetDateTime.of(2020, 10, 9, 15, 58, 21, 0, ZoneOffset.ofHours(2));
            OffsetDateTime out = OffsetDateTime.of(2018, 1, 9, 1, 2, 3, 4, ZoneOffset.ofHours(-2));
            fixture.returnsData("'foo':'" + out + "'");
            OffsetDateTimeApi api = fixture.builder().build(OffsetDateTimeApi.class);

            OffsetDateTime value = api.foo(in);

            then(fixture.query()).isEqualTo("query foo($date: DateTime) { foo(date: $date) }");
            then(fixture.variables()).isEqualTo("{'date':'" + in + "'}");
            then(value).isEqualTo(out);
        }

        @Test
        void shouldCallZonedDateTimeQuery() {
            ZonedDateTime in = ZonedDateTime.of(2020, 10, 31, 12, 34, 56, 789, ZoneOffset.ofHours(3));
            ZonedDateTime out = ZonedDateTime.of(3000, 1, 13, 21, 43, 55, 987, UTC);
            fixture.returnsData("'foo':'" + out + "'");
            ZonedDateTimeApi api = fixture.build(ZonedDateTimeApi.class);

            ZonedDateTime value = api.foo(in);

            then(fixture.query()).isEqualTo("query foo($date: DateTime) { foo(date: $date) }");
            then(fixture.variables()).isEqualTo("{'date':'" + in + "'}");
            then(value).isEqualTo(out);
        }

        @Test
        void shouldCallInstantQuery() {
            Instant in = Instant.ofEpochMilli(123456789);
            Instant out = Instant.ofEpochMilli(987654321);
            fixture.returnsData("'foo':'" + out + "'");
            InstantApi api = fixture.build(InstantApi.class);

            Instant value = api.foo(in);

            then(fixture.query()).isEqualTo("query foo($instant: DateTime) { foo(instant: $instant) }");
            then(fixture.variables()).isEqualTo("{'instant':'" + in + "'}");
            then(value).isEqualTo(out);
        }

        @Test
        void shouldCallDateQuery() {
            Instant in = Instant.ofEpochMilli(123456789);
            Instant out = Instant.ofEpochMilli(987654321);
            fixture.returnsData("'foo':'" + out + "'");
            DateApi api = fixture.build(DateApi.class);

            Date value = api.foo(Date.from(in));

            then(fixture.query()).isEqualTo("query foo($date: Date) { foo(date: $date) }");
            then(fixture.variables()).isEqualTo("{'date':'" + in + "'}");
            then(value).isEqualTo(Date.from(out));
        }
    }

    @GraphQLClientApi
    interface UuidApi {
        UUID foo(UUID uuid, @Id UUID id);
    }

    @GraphQLClientApi
    interface NestedUuidIdApi {
        NestedUuidId foo(NestedUuidId uuid);
    }

    static class NestedUuidId {
        @Id
        UUID id;
    }

    @Nested
    class UuidBehavior {
        @Test
        void shouldCallUuidQuery() {
            UUID in1 = UUID.randomUUID();
            UUID in2 = UUID.randomUUID();
            UUID out = UUID.randomUUID();
            fixture.returnsData("'foo':'" + out + "'");
            UuidApi api = fixture.build(UuidApi.class);

            UUID value = api.foo(in1, in2);

            then(fixture.query()).isEqualTo("query foo($uuid: String, $id: ID) { foo(uuid: $uuid, id: $id) }");
            then(fixture.variables()).isEqualTo("{'uuid':'" + in1 + "','id':'" + in2 + "'}");
            then(value).isEqualTo(out);
        }

        @Test
        void shouldCallNestedUuidIdQuery() {
            NestedUuidId in = new NestedUuidId();
            in.id = UUID.randomUUID();
            NestedUuidId out = new NestedUuidId();
            out.id = UUID.randomUUID();
            fixture.returnsData("'foo':{'id':'" + out.id + "'}");
            NestedUuidIdApi api = fixture.build(NestedUuidIdApi.class);

            NestedUuidId value = api.foo(in);

            then(fixture.query()).isEqualTo("query foo($uuid: NestedUuidIdInput) { foo(uuid: $uuid) {id} }");
            then(fixture.variables()).isEqualTo("{'uuid':{'id':'" + in.id + "'}}");
            then(value.id).isEqualTo(out.id);
        }
    }
}
