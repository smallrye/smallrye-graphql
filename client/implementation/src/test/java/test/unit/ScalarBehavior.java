package test.unit;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

import javax.ws.rs.core.Response;

import org.junit.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientException;

public class ScalarBehavior {
    private final GraphQlClientFixture fixture = new GraphQlClientFixture();

    @GraphQlClientApi
    interface BoolApi {
        boolean bool();
    }

    @GraphQlClientApi
    interface BooleanApi {
        Boolean bool();
    }

    //region BooleanBehavior
    @Test
    public void shouldCallBoolQuery() {
        fixture.returnsData("'bool':true");
        BoolApi api = fixture.builder().build(BoolApi.class);

        boolean bool = api.bool();

        then(fixture.query()).isEqualTo("bool");
        then(bool).isTrue();
    }

    @Test
    public void shouldFailToAssignNullToBool() {
        fixture.returnsData("'bool':null");
        BoolApi api = fixture.builder().build(BoolApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::bool, GraphQlClientException.class);

        then(thrown).hasMessage("invalid boolean value for " + BoolApi.class.getName() + "#bool: null");
    }

    @Test
    public void shouldFailToAssignStringToBool() {
        fixture.returnsData("'bool':'xxx'");
        BoolApi api = fixture.builder().build(BoolApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::bool, GraphQlClientException.class);

        then(thrown).hasMessage("invalid boolean value for " + BoolApi.class.getName() + "#bool: \"xxx\"");
    }

    @Test
    public void shouldFailToAssignNumberToBool() {
        fixture.returnsData("'bool':123");
        BoolApi api = fixture.builder().build(BoolApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::bool, GraphQlClientException.class);

        then(thrown).hasMessage("invalid boolean value for " + BoolApi.class.getName() + "#bool: 123");
    }

    @Test
    public void shouldFailToAssignListToBool() {
        fixture.returnsData("'bool':[123]");
        BoolApi api = fixture.builder().build(BoolApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::bool, GraphQlClientException.class);

        then(thrown).hasMessage("invalid boolean value for " + BoolApi.class.getName() + "#bool: [123]");
    }

    @Test
    public void shouldFailToAssignObjectToBool() {
        fixture.returnsData("'bool':{'foo':'bar'}");
        BoolApi api = fixture.builder().build(BoolApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::bool, GraphQlClientException.class);

        then(thrown).hasMessage("invalid boolean value for " + BoolApi.class.getName() + "#bool: {\"foo\":\"bar\"}");
    }

    @Test
    public void shouldCallBooleanQuery() {
        fixture.returnsData("'bool':true");
        BooleanApi api = fixture.builder().build(BooleanApi.class);

        Boolean bool = api.bool();

        then(fixture.query()).isEqualTo("bool");
        then(bool).isTrue();
    }
    //endregion

    @GraphQlClientApi
    interface ByteApi {
        Byte code();
    }

    @GraphQlClientApi
    interface PrimitiveByteApi {
        byte code();
    }

    //region ByteBehavior
    @Test
    public void shouldCallByteQuery() {
        fixture.returnsData("'code':5");
        ByteApi api = fixture.builder().build(ByteApi.class);

        Byte code = api.code();

        then(fixture.query()).isEqualTo("code");
        then(code).isEqualTo((byte) 5);
    }

    @Test
    public void shouldCallPrimitiveByteQuery() {
        fixture.returnsData("'code':5");
        PrimitiveByteApi api = fixture.builder().build(PrimitiveByteApi.class);

        byte code = api.code();

        then(fixture.query()).isEqualTo("code");
        then(code).isEqualTo((byte) 5);
    }

    @Test
    public void shouldFailByteFromTooBigNumberQuery() {
        int tooBig = (int) Byte.MAX_VALUE + 1;
        fixture.returnsData("'code':" + tooBig);
        ByteApi api = fixture.builder().build(ByteApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

        then(thrown).hasMessage("invalid java.lang.Byte value for " + ByteApi.class.getName() + "#code: " + tooBig);
    }

    @Test
    public void shouldFailByteFromNegativeNumberQuery() {
        int tooSmall = (int) Byte.MIN_VALUE - 1;
        fixture.returnsData("'code':" + tooSmall);
        ByteApi api = fixture.builder().build(ByteApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

        then(thrown).hasMessage("invalid java.lang.Byte value for " + ByteApi.class.getName() + "#code: " + tooSmall);
    }
    //endregion

    @GraphQlClientApi
    interface CharacterApi {
        Character code();
    }

    @GraphQlClientApi
    interface PrimitiveCharApi {
        char code();
    }

    //region CharacterBehavior
    @Test
    public void shouldCallCharacterFromStringQuery() {
        fixture.returnsData("'code':'a'");
        CharacterApi api = fixture.builder().build(CharacterApi.class);

        Character c = api.code();

        then(fixture.query()).isEqualTo("code");
        then(c).isEqualTo('a');
    }

    @Test
    public void shouldFailCharacterFromStringQueryWithMoreThanOneCharacter() {
        fixture.returnsData("'code':'ab'");
        CharacterApi api = fixture.builder().build(CharacterApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

        then(thrown).hasMessage("invalid java.lang.Character value for " + CharacterApi.class.getName() + "#code: \"ab\"");
    }

    @Test
    public void shouldCallCharacterFromNumberQuery() {
        fixture.returnsData("'code':97");
        CharacterApi api = fixture.builder().build(CharacterApi.class);

        Character c = api.code();

        then(fixture.query()).isEqualTo("code");
        then(c).isEqualTo('a');
    }

    @Test
    public void shouldFailCharacterFromTooBigNumberQuery() {
        int tooBig = (int) Character.MAX_VALUE + 1;
        fixture.returnsData("'code':" + tooBig);
        CharacterApi api = fixture.builder().build(CharacterApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

        then(thrown)
                .hasMessage("invalid java.lang.Character value for " + CharacterApi.class.getName() + "#code: " + tooBig);
    }

    @Test
    public void shouldFailCharacterFromNegativeNumberQuery() {
        fixture.returnsData("'code':-15");
        CharacterApi api = fixture.builder().build(CharacterApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

        then(thrown).hasMessage("invalid java.lang.Character value for " + CharacterApi.class.getName() + "#code: -15");
    }

    @Test
    public void shouldCallPrimitiveCharQuery() {
        fixture.returnsData("'code':'a'");
        PrimitiveCharApi api = fixture.builder().build(PrimitiveCharApi.class);

        char c = api.code();

        then(fixture.query()).isEqualTo("code");
        then(c).isEqualTo('a');
    }

    @Test
    public void shouldFailPrimitiveCharQueryWithMoreThanOneCharacter() {
        fixture.returnsData("'code':'ab'");
        PrimitiveCharApi api = fixture.builder().build(PrimitiveCharApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

        then(thrown).hasMessage("invalid char value for " + PrimitiveCharApi.class.getName() + "#code: \"ab\"");
    }
    //endregion

    @GraphQlClientApi
    interface ShortApi {
        Short code();
    }

    @GraphQlClientApi
    interface PrimitiveShortApi {
        short code();
    }

    //region ShortBehavior
    @Test
    public void shouldCallShortQuery() {
        fixture.returnsData("'code':5");
        ShortApi api = fixture.builder().build(ShortApi.class);

        Short code = api.code();

        then(fixture.query()).isEqualTo("code");
        then(code).isEqualTo((short) 5);
    }

    @Test
    public void shouldFailToCallTooSmallShortQuery() {
        int tooSmall = (int) Short.MIN_VALUE - 1;
        fixture.returnsData("'code':" + tooSmall);
        ShortApi api = fixture.builder().build(ShortApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

        then(thrown).hasMessage("invalid java.lang.Short value for " + ShortApi.class.getName() + "#code: " + tooSmall);
    }

    @Test
    public void shouldFailToCallTooBigShortQuery() {
        int tooBig = (int) Short.MAX_VALUE + 1;
        fixture.returnsData("'code':" + tooBig);
        ShortApi api = fixture.builder().build(ShortApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

        then(thrown).hasMessage("invalid java.lang.Short value for " + ShortApi.class.getName() + "#code: " + tooBig);
    }

    @Test
    public void shouldCallPrimitiveShortQuery() {
        fixture.returnsData("'code':5");
        PrimitiveShortApi api = fixture.builder().build(PrimitiveShortApi.class);

        short code = api.code();

        then(fixture.query()).isEqualTo("code");
        then(code).isEqualTo((short) 5);
    }
    //endregion

    @GraphQlClientApi
    interface IntegerApi {
        Integer code();
    }

    @GraphQlClientApi
    interface IntApi {
        int code();
    }

    //region IntegerBehavior
    @Test
    public void shouldCallIntegerQuery() {
        fixture.returnsData("'code':5");
        IntegerApi api = fixture.builder().build(IntegerApi.class);

        Integer code = api.code();

        then(fixture.query()).isEqualTo("code");
        then(code).isEqualTo(5);
    }

    @Test
    public void shouldFailToCallDoubleQuery() {
        double number = 123.456d;
        fixture.returnsData("'code':" + number);
        IntegerApi api = fixture.builder().build(IntegerApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

        then(thrown).hasMessage("invalid java.lang.Integer value for " + IntegerApi.class.getName() + "#code: " + number);
    }

    @Test
    public void shouldFailToCallTooSmallIntegerQuery() {
        long tooSmall = (long) Integer.MIN_VALUE - 1;
        fixture.returnsData("'code':" + tooSmall);
        IntegerApi api = fixture.builder().build(IntegerApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

        then(thrown).hasMessage("invalid java.lang.Integer value for " + IntegerApi.class.getName() + "#code: " + tooSmall);
    }

    @Test
    public void shouldFailToCallTooBigIntegerQuery() {
        long tooBig = (long) Integer.MAX_VALUE + 1;
        fixture.returnsData("'code':" + tooBig);
        IntegerApi api = fixture.builder().build(IntegerApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

        then(thrown).hasMessage("invalid java.lang.Integer value for " + IntegerApi.class.getName() + "#code: " + tooBig);
    }

    @Test
    public void shouldCallIntQuery() {
        fixture.returnsData("'code':5");
        IntApi api = fixture.builder().build(IntApi.class);

        int code = api.code();

        then(fixture.query()).isEqualTo("code");
        then(code).isEqualTo(5);
    }
    //endregion

    @GraphQlClientApi
    interface LongApi {
        Long code();
    }

    @GraphQlClientApi
    interface PrimitiveLongApi {
        long code();
    }

    //region LongBehavior
    @Test
    public void shouldCallLongQuery() {
        fixture.returnsData("'code':5");
        LongApi api = fixture.builder().build(LongApi.class);

        Long code = api.code();

        then(fixture.query()).isEqualTo("code");
        then(code).isEqualTo(5L);
    }

    @Test
    public void shouldFailToCallTooSmallLongQuery() {
        String tooSmall = "-9223372036854775809";
        fixture.returnsData("'code':" + tooSmall);
        LongApi api = fixture.builder().build(LongApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

        then(thrown).hasMessage("invalid java.lang.Long value for " + LongApi.class.getName() + "#code: " + tooSmall);
    }

    @Test
    public void shouldFailToCallTooBigLongQuery() {
        String tooBig = "9223372036854775808";
        fixture.returnsData("'code':" + tooBig);
        LongApi api = fixture.builder().build(LongApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::code, GraphQlClientException.class);

        then(thrown).hasMessage("invalid java.lang.Long value for " + LongApi.class.getName() + "#code: " + tooBig);
    }

    @Test
    public void shouldCallPrimitiveLongQuery() {
        fixture.returnsData("'code':5");
        PrimitiveLongApi api = fixture.builder().build(PrimitiveLongApi.class);

        long code = api.code();

        then(fixture.query()).isEqualTo("code");
        then(code).isEqualTo(5L);
    }
    //endregion

    @GraphQlClientApi
    interface FloatApi {
        Float number();
    }

    @GraphQlClientApi
    interface PrimitiveFloatApi {
        float number();
    }

    //region FloatBehavior
    @Test
    public void shouldCallFloatQuery() {
        fixture.returnsData("'number':123.456");
        FloatApi api = fixture.builder().build(FloatApi.class);

        Float number = api.number();

        then(fixture.query()).isEqualTo("number");
        then(number).isEqualTo(123.456f);
    }

    @Test
    public void shouldCallPrimitiveFloatQuery() {
        fixture.returnsData("'number':123.456");
        PrimitiveFloatApi api = fixture.builder().build(PrimitiveFloatApi.class);

        float number = api.number();

        then(fixture.query()).isEqualTo("number");
        then(number).isEqualTo(123.456f);
    }
    //endregion

    @GraphQlClientApi
    interface DoubleApi {
        Double number();
    }

    @GraphQlClientApi
    interface PrimitiveDoubleApi {
        double number();
    }

    //region DoubleBehavior
    @Test
    public void shouldCallDoubleQuery() {
        fixture.returnsData("'number':123.456");
        DoubleApi api = fixture.builder().build(DoubleApi.class);

        Double number = api.number();

        then(fixture.query()).isEqualTo("number");
        then(number).isEqualTo(123.456D);
    }

    @Test
    public void shouldCallPrimitiveDoubleQuery() {
        fixture.returnsData("'number':123.456");
        PrimitiveDoubleApi api = fixture.builder().build(PrimitiveDoubleApi.class);

        double number = api.number();

        then(fixture.query()).isEqualTo("number");
        then(number).isEqualTo(123.456D);
    }
    //endregion

    @GraphQlClientApi
    interface BigDecimalApi {
        BigDecimal number();
    }

    //region BigDecimalBehavior
    @Test
    public void shouldCallReallyLongDecimalQuery() {
        String reallyLongDecimal = "123.45678901234567890123456789012345678901234567890123456789012345678901234567890";
        fixture.returnsData("'number':" + reallyLongDecimal);
        BigDecimalApi api = fixture.builder().build(BigDecimalApi.class);

        BigDecimal number = api.number();

        then(fixture.query()).isEqualTo("number");
        then(number).isEqualTo(reallyLongDecimal);
    }

    @Test
    public void shouldCallNotSoLongDecimalQuery() {
        String notSoLongDecimal = "123.456";
        fixture.returnsData("'number':'" + notSoLongDecimal + "'");
        BigDecimalApi api = fixture.builder().build(BigDecimalApi.class);

        BigDecimal number = api.number();

        then(fixture.query()).isEqualTo("number");
        then(number).isEqualTo(notSoLongDecimal);
    }
    //endregion

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
    interface ScalarWithStringConstructorApi {
        BigInteger foo();
    }

    @GraphQlClientApi
    interface FailingScalarApi {
        @SuppressWarnings("UnusedReturnValue")
        FailingScalar foo();
    }

    public static class FailingScalar {
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
        public ScalarWithOfConstructorMethod(String text) {
            this.text = text;
        }

        @SuppressWarnings("unused")
        public static ScalarWithOfConstructorMethod of(String text) {
            return new ScalarWithOfConstructorMethod("x-" + text);
        }

        String text;
    }

    //region StringBehavior
    @Test
    public void shouldCallStringQuery() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        StringApi api = fixture.builder().build(StringApi.class);

        String greeting = api.greeting();

        then(fixture.query()).isEqualTo("greeting");
        then(greeting).isEqualTo("dummy-greeting");
    }

    @Test
    public void shouldFailStringQueryNotFound() {
        fixture.returns(Response.serverError().type(TEXT_PLAIN_TYPE).entity("failed").build());
        StringApi api = fixture.builder().build(StringApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greeting, GraphQlClientException.class);

        then(thrown).hasMessage("expected successful status code but got 500 Internal Server Error:\nfailed");
    }

    @Test
    public void shouldFailOnQueryError() {
        fixture.returns(Response.ok("{\"errors\":[{\"message\":\"failed\"}]}").build());
        StringApi api = fixture.builder().build(StringApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greeting, GraphQlClientException.class);

        then(thrown).hasMessage("errors from service: [{\"message\":\"failed\"}]:\n" +
                "  {\"query\":\"query { greeting }\"}");
    }

    @Test
    public void shouldFailOnMissingQueryResponse() {
        fixture.returnsData("");
        StringApi api = fixture.builder().build(StringApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greeting, GraphQlClientException.class);

        then(thrown).hasMessage("no data for 'greeting':\n  {}");
    }

    @Test
    public void shouldCallScalarWithValueOfQuery() {
        fixture.returnsData("'foo':123456");
        ScalarWithValueOfApi api = fixture.builder().build(ScalarWithValueOfApi.class);

        Integer value = api.foo();

        then(fixture.query()).isEqualTo("foo");
        then(value).isEqualTo(123456);
    }

    @Test
    public void shouldCallScalarWithParseQuery() {
        LocalDate now = LocalDate.now();
        fixture.returnsData("'now':'" + now + "'");
        ScalarWithParseApi api = fixture.builder().build(ScalarWithParseApi.class);

        LocalDate value = api.now();

        then(fixture.query()).isEqualTo("now");
        then(value).isEqualTo(now);
    }

    @Test
    public void shouldCallNotSoBigIntegerScalarWithStringConstructorApiQuery() {
        String bigNumber = "1234";
        fixture.returnsData("'foo':" + bigNumber);
        ScalarWithStringConstructorApi api = fixture.builder().build(ScalarWithStringConstructorApi.class);

        BigInteger value = api.foo();

        then(fixture.query()).isEqualTo("foo");
        then(value).isEqualTo(bigNumber);
    }

    @Test
    public void shouldCallVeryBigIntegerScalarWithStringConstructorApiQuery() {
        String bigNumber = "1234567890123456789012345678901234567890123456789012345678901234567890";
        fixture.returnsData("'foo':" + bigNumber);
        ScalarWithStringConstructorApi api = fixture.builder().build(ScalarWithStringConstructorApi.class);

        BigInteger value = api.foo();

        then(fixture.query()).isEqualTo("foo");
        then(value).isEqualTo(bigNumber);
    }

    @Test
    public void shouldFailToCreateFailingScalar() {
        fixture.returnsData("'foo':'a'");
        FailingScalarApi api = fixture.builder().build(FailingScalarApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::foo, GraphQlClientException.class);

        then(thrown).hasMessage("can't create scalar " + FailingScalar.class.getName() + " value " +
                "for " + FailingScalarApi.class.getName() + "#foo");
    }

    @Test
    public void shouldCallScalarWithStringConstructorMethodQuery() {
        fixture.returnsData("'foo':'bar'");
        ScalarWithStringConstructorMethodApi api = fixture.builder().build(ScalarWithStringConstructorMethodApi.class);

        ScalarWithStringConstructorMethod value = api.foo();

        then(fixture.query()).isEqualTo("foo");
        then(value.text).isEqualTo("bar");
    }

    @Test
    public void shouldCallScalarWithOfConstructorMethodQuery() {
        fixture.returnsData("'foo':'bar'");
        ScalarWithOfConstructorMethodApi api = fixture.builder().build(ScalarWithOfConstructorMethodApi.class);

        ScalarWithOfConstructorMethod value = api.foo();

        then(fixture.query()).isEqualTo("foo");
        then(value.text).isEqualTo("x-bar");
    }
    //endregion

    interface StringGettersApi {
        String getGreeting();

        String get();

        String getG();

        String gets();

        String getting();
    }

    @Test
    public void shouldCallStringGetterQuery() {
        fixture.returnsData("'greeting':'foo'");
        StringGettersApi api = fixture.builder().build(StringGettersApi.class);

        String value = api.getGreeting();

        then(fixture.query()).isEqualTo("greeting");
        then(value).isEqualTo("foo");
    }

    @Test
    public void shouldCallJustGetQuery() {
        fixture.returnsData("'get':'foo'");
        StringGettersApi api = fixture.builder().build(StringGettersApi.class);

        String value = api.get();

        then(fixture.query()).isEqualTo("get");
        then(value).isEqualTo("foo");
    }

    @Test
    public void shouldCallOneCharGetterQuery() {
        fixture.returnsData("'g':'foo'");
        StringGettersApi api = fixture.builder().build(StringGettersApi.class);

        String value = api.getG();

        then(fixture.query()).isEqualTo("g");
        then(value).isEqualTo("foo");
    }

    @Test
    public void shouldCallGetAndOneLowerCharQuery() {
        fixture.returnsData("'gets':'foo'");
        StringGettersApi api = fixture.builder().build(StringGettersApi.class);

        String value = api.gets();

        then(fixture.query()).isEqualTo("gets");
        then(value).isEqualTo("foo");
    }

    @Test
    public void shouldCallGetAndLowerCharsQuery() {
        fixture.returnsData("'getting':'foo'");
        StringGettersApi api = fixture.builder().build(StringGettersApi.class);

        String value = api.getting();

        then(fixture.query()).isEqualTo("getting");
        then(value).isEqualTo("foo");
    }
}
