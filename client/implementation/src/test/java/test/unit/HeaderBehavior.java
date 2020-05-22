package test.unit;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.zip.Inflater;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientException;
import io.smallrye.graphql.client.typesafe.api.Header;

@SuppressWarnings({ "UnusedReturnValue", "unused" })
public class HeaderBehavior {
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
        ConstantHeadersApi api = fixture.builder().build(ConstantHeadersApi.class);

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
        MethodAndConstantHeadersApi api = fixture.builder().build(MethodAndConstantHeadersApi.class);

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
        MethodNorConstantHeadersApi api = fixture.builder().build(MethodNorConstantHeadersApi.class);

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
        MethodHeadersApi api = fixture.builder().build(MethodHeadersApi.class);

        api.greeting();

        then(fixture.sentHeader("H1")).isEqualTo("V1");
        then(fixture.sentHeader("H2")).isEqualTo("V2");
        then(fixture.sentHeader("H3")).isEqualTo("V3");
    }

    @GraphQlClientApi
    public interface DefaultMethodHeaderApi {
        @Header(name = "H", method = "f")
        String greeting();

        default String f() {
            return "V";
        }
    }

    @Test
    void shouldFailToAddMethodHeaderWithDefaultMethod() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        DefaultMethodHeaderApi api = fixture.builder().build(DefaultMethodHeaderApi.class);

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
        UnknownMethodHeadersApi api = fixture.builder().build(UnknownMethodHeadersApi.class);

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
        UnknownMethodClassHeadersApi api = fixture.builder().build(UnknownMethodClassHeadersApi.class);

        Throwable thrown = catchThrowable(api::greeting);

        then(thrown)
                .isInstanceOf(GraphQlClientException.class)
                .hasMessage("class not found for expression 'foo.bar'");
    }

    @GraphQlClientApi
    interface PublicQualifiedMethodHeadersApi {
        @Header(name = "H", method = "test.unit.HeaderBehavior.publicQualifiedMethod")
        String greeting();

        static String accessibilityCheck() {
            return publicQualifiedMethod();
        }
    }

    public static String publicQualifiedMethod() {
        return "public-method-value";
    }

    @Test
    void shouldAddPublicMethodHeader() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        PublicQualifiedMethodHeadersApi api = fixture.builder().build(PublicQualifiedMethodHeadersApi.class);

        api.greeting();

        then(fixture.sentHeader("H")).isEqualTo("public-method-value");
    }

    @GraphQlClientApi
    interface PackagePrivateQualifiedMethodHeadersApi {
        @Header(name = "H", method = "test.unit.HeaderBehavior.packagePrivateQualifiedMethod")
        String greeting();

        static String accessibilityCheck() {
            return packagePrivateQualifiedMethod();
        }
    }

    static String packagePrivateQualifiedMethod() {
        return "package-private-method-value";
    }

    @Test
    void shouldAddPackagePrivateMethodHeader() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        PackagePrivateQualifiedMethodHeadersApi api = fixture.builder().build(PackagePrivateQualifiedMethodHeadersApi.class);

        api.greeting();

        then(fixture.sentHeader("H")).isEqualTo("package-private-method-value");
    }

    @GraphQlClientApi
    interface PrivateQualifiedMethodHeadersApi {
        @Header(name = "H", method = "java.util.zip.Inflater.initIDs") // just an arbitrary private static f()
        String greeting();

        // static String accessibilityCheck() { return java.util.zip.Inflater.initIDs(); }
    }

    @Test
    void shouldFailToAddPrivateMethodHeader() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        PrivateQualifiedMethodHeadersApi api = fixture.builder().build(PrivateQualifiedMethodHeadersApi.class);

        Throwable thrown = catchThrowable(api::greeting);

        then(thrown)
                .isInstanceOf(GraphQlClientException.class)
                .hasMessage(PrivateQualifiedMethodHeadersApi.class.getName() + " can't access "
                        + Inflater.class.getName() + "#initIDs");
    }

    @GraphQlClientApi
    interface EnclosingPrivateQualifiedMethodHeadersApi {
        @Header(name = "H", method = "test.unit.HeaderBehavior.privateQualifiedMethod")
        String greeting();

        static String accessibilityCheck() {
            return privateQualifiedMethod();
        }
    }

    private static String privateQualifiedMethod() {
        return "enclosing-private-method-value";
    }

    @Test
    void shouldAddEnclosingPrivateMethodHeader() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        EnclosingPrivateQualifiedMethodHeadersApi api = fixture.builder()
                .build(EnclosingPrivateQualifiedMethodHeadersApi.class);

        api.greeting();

        then(fixture.sentHeader("H")).isEqualTo("enclosing-private-method-value");
    }

    @GraphQlClientApi
    interface EnclosingProtectedQualifiedMethodHeadersApi {
        @Header(name = "H", method = "test.unit.HeaderBehavior.protectedQualifiedMethod")
        String greeting();

        static String accessibilityCheck() {
            return protectedQualifiedMethod();
        }
    }

    protected static String protectedQualifiedMethod() {
        return "enclosing-protected-method-value";
    }

    @Test
    void shouldAddEnclosingProtectedMethodHeader() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        EnclosingProtectedQualifiedMethodHeadersApi api = fixture.builder()
                .build(EnclosingProtectedQualifiedMethodHeadersApi.class);

        api.greeting();

        then(fixture.sentHeader("H")).isEqualTo("enclosing-protected-method-value");
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
        FailingMethodHeadersApi api = fixture.builder().build(FailingMethodHeadersApi.class);

        Throwable thrown = catchThrowable(api::greeting);

        then(thrown)
                .isInstanceOf(GraphQlClientException.class)
                .hasMessage("can't resolve header method expression 'f' in " + FailingMethodHeadersApi.class.getName())
                .hasRootCauseExactlyInstanceOf(RuntimeException.class)
                .hasRootCauseMessage("dummy");
    }

    @GraphQlClientApi
    public interface ErrorFailingMethodHeadersApi {
        @Header(name = "H", method = "f")
        String greeting();

        static String f() {
            throw new AssertionError("dummy");
        }
    }

    @Test
    void shouldFailToAddMethodHeaderWithErrorFailingMethod() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        ErrorFailingMethodHeadersApi api = fixture.builder().build(ErrorFailingMethodHeadersApi.class);

        Throwable thrown = catchThrowable(api::greeting);

        then(thrown).isExactlyInstanceOf(AssertionError.class).hasMessage("dummy");
    }

    @GraphQlClientApi
    public interface ExceptionFailingMethodHeadersApi {
        @Header(name = "H", method = "f")
        String greeting();

        static String f() throws Exception {
            throw new Exception("dummy");
        }
    }

    @Test
    void shouldFailToAddMethodHeaderWithExceptionFailingMethod() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        ExceptionFailingMethodHeadersApi api = fixture.builder().build(ExceptionFailingMethodHeadersApi.class);

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
        ArgMethodHeadersApi api = fixture.builder().build(ArgMethodHeadersApi.class);

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
        HeadersParamApi api = fixture.builder().build(HeadersParamApi.class);

        api.greeting("bar");

        then(fixture.query()).isEqualTo("greeting");
        then(fixture.sentHeader("foo")).isEqualTo("bar");
    }
}
