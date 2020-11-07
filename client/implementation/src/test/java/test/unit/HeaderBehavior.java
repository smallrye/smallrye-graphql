package test.unit;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.lang.annotation.Retention;

import javax.enterprise.inject.Stereotype;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientException;
import io.smallrye.graphql.client.typesafe.api.Header;
import test.unit.HeaderBehavior.EnclosingClass.EnclosingEnclosingPublicMethodHeadersApi;

@SuppressWarnings({ "UnusedReturnValue", "unused" })
class HeaderBehavior {
    private final GraphQlClientFixture fixture = new GraphQlClientFixture();

    @GraphQlClientApi
    @Header(name = "H1", constant = "V1")
    @Header(name = "H3", constant = "will-be-overwritten")
    interface ConstantHeadersApi {
        @Header(name = "H2", constant = "V2")
        @Header(name = "H3", constant = "V3")
        String greeting();
    }

    @Test
    void shouldAddConstantHeaders() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        ConstantHeadersApi api = fixture.build(ConstantHeadersApi.class);

        api.greeting();

        then(fixture.sentHeader("H1")).isEqualTo("V1");
        then(fixture.sentHeader("H2")).isEqualTo("V2");
        then(fixture.sentHeader("H3")).isEqualTo("V3");
    }

    @GraphQlClientApi
    interface MethodAndConstantHeadersApi {
        @Header(name = "H", method = "M", constant = "C")
        void greeting();
    }

    @Test
    void shouldFailToCollectHeadersWithMethodAndConstantHeaders() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        MethodAndConstantHeadersApi api = fixture.build(MethodAndConstantHeadersApi.class);

        Throwable thrown = catchThrowable(api::greeting);

        then(thrown).isInstanceOf(GraphQlClientException.class);
        then(thrown.getMessage().replace("\"", "")) // JDK 11 prints the quotes, JDK 8 doesn't
                .isEqualTo("Header with 'method' AND 'constant' not allowed: @" + Header.class.getName()
                        + "(method=M, constant=C, name=H)");
    }

    @GraphQlClientApi
    interface MethodNorConstantHeadersApi {
        @Header(name = "H")
        void greeting();
    }

    @Test
    void shouldFailToCollectHeadersWithMethodNorConstantHeaders() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        MethodNorConstantHeadersApi api = fixture.build(MethodNorConstantHeadersApi.class);

        Throwable thrown = catchThrowable(api::greeting);

        then(thrown).isInstanceOf(GraphQlClientException.class);
        then(thrown.getMessage().replace("\"", "")) // JDK 11 prints the quotes, JDK 8 doesn't
                .isEqualTo("Header must have either 'method' XOR 'constant': @" + Header.class.getName()
                        + "(method=, constant=, name=H)");
    }

    @GraphQlClientApi
    @Header(name = "H1", method = "f1")
    @Header(name = "H3", method = "f4") // overwritten
    interface MethodHeadersApi {
        @Header(name = "H2", method = "f2")
        @Header(name = "H3", method = "f3")
        String greeting();

        static String f1() {
            return "V1";
        }

        static String f2() {
            return "V2";
        }

        static String f3() {
            return "V3";
        }

        static String f4() {
            return "V4";
        }

        static String f4(int i) {
            return "x" + i;
        }
    }

    @Test
    void shouldAddMethodHeaders() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        MethodHeadersApi api = fixture.build(MethodHeadersApi.class);

        api.greeting();

        then(fixture.sentHeader("H1")).isEqualTo("V1");
        then(fixture.sentHeader("H2")).isEqualTo("V2");
        then(fixture.sentHeader("H3")).isEqualTo("V3");
    }

    @GraphQlClientApi
    interface DefaultMethodHeaderApi {
        @Header(name = "H", method = "f")
        String greeting();

        default String f() {
            return "V";
        }
    }

    @Test
    void shouldFailToAddMethodHeaderWithDefaultMethod() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        DefaultMethodHeaderApi api = fixture.build(DefaultMethodHeaderApi.class);

        Throwable thrown = catchThrowable(api::greeting);

        then(thrown)
                .isInstanceOf(GraphQlClientException.class)
                .hasMessage("referenced header method 'f' in " + DefaultMethodHeaderApi.class.getName() + " is not static");
    }

    @GraphQlClientApi
    interface UnknownMethodHeadersApi {
        @Header(name = "H", method = "f")
        String greeting();
    }

    @Test
    void shouldFailToAddMethodHeaderWithUnknownMethod() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        UnknownMethodHeadersApi api = fixture.build(UnknownMethodHeadersApi.class);

        Throwable thrown = catchThrowable(api::greeting);

        then(thrown)
                .isInstanceOf(GraphQlClientException.class)
                .hasMessage("no no-arg method 'f' found in " + UnknownMethodHeadersApi.class.getName());
    }

    @GraphQlClientApi
    interface UnknownMethodClassHeadersApi {
        @Header(name = "H", method = "foo.bar")
        String greeting();
    }

    @Test
    void shouldFailToAddMethodHeaderWithUnknownMethodClass() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        UnknownMethodClassHeadersApi api = fixture.build(UnknownMethodClassHeadersApi.class);

        Throwable thrown = catchThrowable(api::greeting);

        then(thrown)
                .isInstanceOf(GraphQlClientException.class)
                .hasMessage("class not found for expression 'foo.bar'");
    }

    @GraphQlClientApi
    interface QualifiedEnclosingPublicMethodHeadersApi {
        @Header(name = "H", method = "test.unit.HeaderBehavior.publicMethod")
        String greeting();

        static String accessibilityCheck() {
            return publicMethod();
        }
    }

    static String publicMethod() {
        return "public-method-value";
    }

    @Test
    void shouldAddQualifiedEnclosingPublicMethodHeader() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        QualifiedEnclosingPublicMethodHeadersApi api = fixture.build(QualifiedEnclosingPublicMethodHeadersApi.class);

        api.greeting();

        then(fixture.sentHeader("H")).isEqualTo("public-method-value");
    }

    @GraphQlClientApi
    interface EnclosingPublicMethodHeadersApi {
        @Header(name = "H", method = "publicMethod")
        String greeting();

        static String accessibilityCheck() {
            return publicMethod();
        }
    }

    @Test
    void shouldAddEnclosingPublicMethodHeader() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        EnclosingPublicMethodHeadersApi api = fixture.build(EnclosingPublicMethodHeadersApi.class);

        api.greeting();

        then(fixture.sentHeader("H")).isEqualTo("public-method-value");
    }

    static class EnclosingClass {
        @GraphQlClientApi
        interface EnclosingEnclosingPublicMethodHeadersApi {
            @Header(name = "H", method = "publicMethod")
            String greeting();

            static String accessibilityCheck() {
                return publicMethod();
            }
        }
    }

    @Test
    void shouldAddEnclosingEnclosingPublicMethodHeader() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        EnclosingEnclosingPublicMethodHeadersApi api = fixture.build(EnclosingEnclosingPublicMethodHeadersApi.class);

        api.greeting();

        then(fixture.sentHeader("H")).isEqualTo("public-method-value");
    }

    @GraphQlClientApi
    interface QualifiedPackagePrivateMethodHeadersApi {
        @Header(name = "H", method = "test.unit.Outside.packagePrivateMethod")
        String greeting();

        static String accessibilityCheck() {
            return Outside.packagePrivateMethod();
        }
    }

    @Test
    void shouldAddPackagePrivateMethodHeader() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        QualifiedPackagePrivateMethodHeadersApi api = fixture.build(QualifiedPackagePrivateMethodHeadersApi.class);

        api.greeting();

        then(fixture.sentHeader("H")).isEqualTo("package-private-method-value");
    }

    @GraphQlClientApi
    interface QualifiedPrivateMethodHeadersApi {
        @Header(name = "H", method = "test.unit.Outside.privateMethod")
        String greeting();

        // static String accessibilityCheck() { return Outside.privateMethod(); }
    }

    @Test
    void shouldFailToAddPrivateMethodHeader() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        QualifiedPrivateMethodHeadersApi api = fixture.build(QualifiedPrivateMethodHeadersApi.class);

        Throwable thrown = catchThrowable(api::greeting);

        then(thrown)
                .isInstanceOf(GraphQlClientException.class)
                .hasMessage(QualifiedPrivateMethodHeadersApi.class.getName() + " can't access "
                        + Outside.class.getName() + "#privateMethod");
    }

    @GraphQlClientApi
    interface QualifiedEnclosingPrivateMethodHeadersApi {
        @Header(name = "H", method = "test.unit.HeaderBehavior.privateMethod")
        String greeting();

        static String accessibilityCheck() {
            return privateMethod();
        }
    }

    private static String privateMethod() {
        return "enclosing-private-method-value";
    }

    @Test
    void shouldAddEnclosingPrivateMethodHeader() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        QualifiedEnclosingPrivateMethodHeadersApi api = fixture.builder()
                .build(QualifiedEnclosingPrivateMethodHeadersApi.class);

        api.greeting();

        then(fixture.sentHeader("H")).isEqualTo("enclosing-private-method-value");
    }

    @GraphQlClientApi
    interface QualifiedEnclosingProtectedMethodHeadersApi {
        @Header(name = "H", method = "test.unit.HeaderBehavior.protectedMethod")
        String greeting();

        static String accessibilityCheck() {
            return protectedMethod();
        }
    }

    protected static String protectedMethod() {
        return "enclosing-protected-method-value";
    }

    @Test
    void shouldAddEnclosingProtectedMethodHeader() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        QualifiedEnclosingProtectedMethodHeadersApi api = fixture.builder()
                .build(QualifiedEnclosingProtectedMethodHeadersApi.class);

        api.greeting();

        then(fixture.sentHeader("H")).isEqualTo("enclosing-protected-method-value");
    }

    @GraphQlClientApi
    interface QualifiedNonStaticEnclosingProtectedMethodHeadersApi {
        @Header(name = "H", method = "test.unit.HeaderBehavior.nonStaticProtectedMethod")
        String greeting();

        // static String accessibilityCheck() { return nonStaticProtectedMethod(); }
    }

    protected String nonStaticProtectedMethod() {
        throw new UnsupportedOperationException();
    }

    @Test
    void shouldFailToAddNonStaticEnclosingProtectedMethodHeader() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        QualifiedNonStaticEnclosingProtectedMethodHeadersApi api = fixture.builder()
                .build(QualifiedNonStaticEnclosingProtectedMethodHeadersApi.class);

        Throwable thrown = catchThrowable(api::greeting);

        then(thrown)
                .isInstanceOf(GraphQlClientException.class)
                .hasMessage("referenced header method '" + HeaderBehavior.class.getName() + ".nonStaticProtectedMethod' in "
                        + QualifiedNonStaticEnclosingProtectedMethodHeadersApi.class.getName() + " is not static");
    }

    @GraphQlClientApi
    interface FailingMethodHeadersApi {
        @Header(name = "H", method = "f")
        String greeting();

        static String f() {
            throw new RuntimeException("dummy");
        }
    }

    @Test
    void shouldFailToAddMethodHeaderWithFailingMethod() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        FailingMethodHeadersApi api = fixture.build(FailingMethodHeadersApi.class);

        Throwable thrown = catchThrowable(api::greeting);

        then(thrown)
                .isInstanceOf(GraphQlClientException.class)
                .hasMessage("can't resolve header method expression 'f' in " + FailingMethodHeadersApi.class.getName())
                .hasRootCauseExactlyInstanceOf(RuntimeException.class)
                .hasRootCauseMessage("dummy");
    }

    @GraphQlClientApi
    interface ErrorFailingMethodHeadersApi {
        @Header(name = "H", method = "f")
        String greeting();

        static String f() {
            throw new AssertionError("dummy");
        }
    }

    @Test
    void shouldFailToAddMethodHeaderWithErrorFailingMethod() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        ErrorFailingMethodHeadersApi api = fixture.build(ErrorFailingMethodHeadersApi.class);

        Throwable thrown = catchThrowable(api::greeting);

        then(thrown).isExactlyInstanceOf(AssertionError.class).hasMessage("dummy");
    }

    @GraphQlClientApi
    interface ExceptionFailingMethodHeadersApi {
        @Header(name = "H", method = "f")
        String greeting();

        static String f() throws Exception {
            throw new Exception("dummy");
        }
    }

    @Test
    void shouldFailToAddMethodHeaderWithExceptionFailingMethod() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        ExceptionFailingMethodHeadersApi api = fixture.build(ExceptionFailingMethodHeadersApi.class);

        Throwable thrown = catchThrowable(api::greeting);

        then(thrown)
                .isExactlyInstanceOf(GraphQlClientException.class)
                .hasMessage("can't invoke " + ExceptionFailingMethodHeadersApi.class.getName() + "#f")
                .hasRootCauseExactlyInstanceOf(Exception.class)
                .hasRootCauseMessage("dummy");
    }

    @GraphQlClientApi
    interface ArgMethodHeadersApi {
        @Header(name = "H", method = "f")
        String greeting();

        static String f(int i) {
            return "" + i;
        }
    }

    @Test
    void shouldFailToAddMethodHeaderWithMethodWithArgs() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        ArgMethodHeadersApi api = fixture.build(ArgMethodHeadersApi.class);

        Throwable thrown = catchThrowable(api::greeting);

        then(thrown)
                .isInstanceOf(GraphQlClientException.class)
                .hasMessage("no no-arg method 'f' found in " + ArgMethodHeadersApi.class.getName());
    }

    @GraphQlClientApi
    interface HeadersParamApi {
        String greeting(@Header(name = "foo") String value);
    }

    @Test
    void shouldAddParamHeader() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        HeadersParamApi api = fixture.build(HeadersParamApi.class);

        api.greeting("bar");

        then(fixture.query()).isEqualTo("query greeting { greeting }");
        then(fixture.sentHeader("foo")).isEqualTo("bar");
    }

    @Retention(RUNTIME)
    @Stereotype
    @Header(name = "H", constant = "V")
    @interface StereotypedHeader {
    }

    @GraphQlClientApi
    interface StereotypedMethodHeaderApi {
        @StereotypedHeader
        String greeting();
    }

    @Test
    void shouldAddStereotypedMethodHeaders() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        StereotypedMethodHeaderApi api = fixture.build(StereotypedMethodHeaderApi.class);

        api.greeting();

        then(fixture.sentHeader("H")).isEqualTo("V");
    }

    @GraphQlClientApi
    @StereotypedHeader
    interface StereotypedTypeHeaderApi {
        String greeting();
    }

    @Test
    void shouldAddStereotypedTypeHeader() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        StereotypedTypeHeaderApi api = fixture.build(StereotypedTypeHeaderApi.class);

        api.greeting();

        then(fixture.sentHeader("H")).isEqualTo("V");
    }

    @Retention(RUNTIME)
    @Stereotype
    @StereotypedHeader
    @interface MetaStereotypedHeader {
    }

    @GraphQlClientApi
    interface MetaStereotypedMethodHeaderApi {
        @MetaStereotypedHeader
        String greeting();
    }

    @Test
    void shouldAddMetaStereotypedMethodHeaders() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        MetaStereotypedMethodHeaderApi api = fixture.build(MetaStereotypedMethodHeaderApi.class);

        api.greeting();

        then(fixture.sentHeader("H")).isEqualTo("V");
    }

    @GraphQlClientApi
    @Header(name = "H1", constant = "V1")
    @Header(name = "overwrite", constant = "sub")
    interface InheritingHeadersApi extends Base, SideBase {
    }

    @Header(name = "H2", constant = "V2")
    @Header(name = "overwrite", constant = "super")
    interface Base extends SuperBase {
        @Header(name = "H3", constant = "V3")
        String greeting();
    }

    @Header(name = "H4", constant = "V4")
    interface SideBase {
    }

    @Header(name = "H5", constant = "V5")
    interface SuperBase {
    }

    @Test
    void shouldAddExtendedHeaders() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        InheritingHeadersApi api = fixture.build(InheritingHeadersApi.class);

        api.greeting();

        then(fixture.sentHeader("H1")).isEqualTo("V1");
        then(fixture.sentHeader("H2")).isEqualTo("V2");
        then(fixture.sentHeader("H3")).isEqualTo("V3");
        then(fixture.sentHeader("H4")).isEqualTo("V4");
        then(fixture.sentHeader("H5")).isEqualTo("V5");
        then(fixture.sentHeader("overwrite")).isEqualTo("sub");
    }

    @GraphQlClientApi
    interface SimpleApi {
        String greeting(String target);
    }

    @Test
    void shouldAddCharsetRequestAndResponseHeaders() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        SimpleApi api = fixture.build(SimpleApi.class);

        api.greeting("foo");

        then(fixture.query()).isEqualTo("query greeting($target: String) { greeting(target: $target) }");
        then(fixture.variables()).isEqualTo("{'target':'foo'}");
        then(fixture.sentHeader("Content-Type")).hasToString("application/json;charset=utf-8");
        then(fixture.sentHeader("Accept")).hasToString("application/json;charset=utf-8");
    }
}
