package test;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.GraphQLError;
import io.smallrye.graphql.client.typesafe.api.ErrorOr;

class ErrorOrTest {
    private static final GraphQLError CLIENT_ERROR = new GraphQLError() {
        @Override
        public String getMessage() {
            return "some message";
        }

        @Override
        public Map<String, Object> getOtherFields() {
            return Collections.emptyMap();
        }

        @Override
        public List<Map<String, Integer>> getLocations() {
            Map<String, Integer> location1 = new HashMap<>();
            location1.put("line", 1);
            location1.put("column", 2);
            return singletonList(location1);
        }

        @Override
        public Object[] getPath() {
            return new Object[] { "one", "two", "three" };
        }

        @Override
        public Map<String, Object> getExtensions() {
            return new HashMap<>();
        }

        @Override
        public String toString() {
            return "some toString";
        }
    };

    @Nested
    class Creation {
        @Test
        void shouldFailToCreateWithNullErrors() {
            @SuppressWarnings("ConstantConditions")
            Throwable throwable = catchThrowable(() -> ErrorOr.ofErrors(null));

            then(throwable).isInstanceOf(NullPointerException.class)
                    .hasMessage("errors must not be null");
        }

        @Test
        void shouldFailToCreateWithEmptyErrors() {
            Throwable throwable = catchThrowable(() -> ErrorOr.ofErrors(emptyList()));

            then(throwable).isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("errors must not be empty");
        }
    }

    @Nested
    class NonNullParameters {
        private final ErrorOr<String> errorOr = ErrorOr.of("some-value");

        @Test
        void shouldRequireNonNullIfPresentAction() {
            Throwable throwable = catchThrowable(() -> errorOr.ifPresent(null));

            then(throwable).isInstanceOf(NullPointerException.class)
                    .hasMessage("ifPresent action must not be null");
        }

        @Test
        void shouldRequireNonNullHandleDataAction() {
            Throwable throwable = catchThrowable(() -> errorOr.handle(null, System.out::println));

            then(throwable).isInstanceOf(NullPointerException.class)
                    .hasMessage("handle dataAction must not be null");
        }

        @Test
        void shouldRequireNonNullHandleErrorAction() {
            Throwable throwable = catchThrowable(() -> errorOr.handle(System.out::println, null));

            then(throwable).isInstanceOf(NullPointerException.class)
                    .hasMessage("handle errorsAction must not be null");
        }

        @Test
        void shouldRequireNonNullMapFunction() {
            Throwable throwable = catchThrowable(() -> errorOr.map(null));

            then(throwable).isInstanceOf(NullPointerException.class)
                    .hasMessage("map function must not be null");
        }

        @Test
        void shouldRequireNonNullFlatMapFunction() {
            Throwable throwable = catchThrowable(() -> errorOr.flatMap(null));

            then(throwable).isInstanceOf(NullPointerException.class)
                    .hasMessage("flatMap function must not be null");
        }
    }

    @Nested
    class GivenItHasAValue {
        private final ErrorOr<String> errorOr = ErrorOr.of("some-value");

        @Test
        void shouldBeEqual() {
            then(errorOr).isEqualTo(errorOr); // itself
            then(errorOr).isEqualTo(ErrorOr.of("some-value"));
            then(errorOr).isNotEqualTo(null);
            then(errorOr).isNotEqualTo("some-value");
            then(errorOr).isNotEqualTo(ErrorOr.ofErrors(singletonList(CLIENT_ERROR)));
        }

        @Test
        void shouldBePresentAndNotHaveErrors() {
            then(errorOr.isPresent()).isTrue();
            then(errorOr.hasErrors()).isFalse();
        }

        @Test
        void shouldGetValue() {
            then(errorOr.get()).isEqualTo("some-value");
        }

        @Test
        void shouldFailToGetErrors() {
            Throwable throwable = catchThrowable(errorOr::getErrors);

            then(throwable).isInstanceOf(NoSuchElementException.class)
                    .hasMessage("No error present, but value some-value");
        }

        @Test
        void shouldExecuteIfPresent() {
            StringBuilder builder = new StringBuilder("prefixed-");

            errorOr.ifPresent(builder::append);

            then(builder).hasToString("prefixed-some-value");
        }

        @Test
        void shouldHandleValue() {
            StringBuilder builder = new StringBuilder("prefixed-");

            errorOr.handle(builder::append, builder::append);

            then(builder).hasToString("prefixed-some-value");
        }

        @Test
        void shouldMapValue() {
            ErrorOr<Integer> mapped = errorOr.map(String::length);

            then(mapped.get()).isEqualTo(10);
        }

        @Test
        void shouldFlatMapValue() {
            ErrorOr<Integer> mapped = errorOr.flatMap(v -> ErrorOr.of(v.length()));

            then(mapped.get()).isEqualTo(10);
        }

        @Test
        void shouldStreamValue() {
            Stream<String> stream = errorOr.stream();

            then(stream).containsExactly("some-value");
        }

        @Test
        void shouldContainValue() {
            Optional<String> optional = errorOr.optional();

            then(optional).contains("some-value");
        }
    }

    @Nested
    class GivenItHasANullValue {
        private final ErrorOr<String> errorOr = ErrorOr.of(null);

        @Test
        void shouldBeEqual() {
            then(errorOr).isEqualTo(errorOr); // itself
            then(errorOr).isEqualTo(ErrorOr.of(null));
            then(errorOr).isNotEqualTo(null);
            then(errorOr).isNotEqualTo(ErrorOr.ofErrors(singletonList(CLIENT_ERROR)));
        }

        @Test
        void shouldBePresentAndNotHaveErrors() {
            then(errorOr.isPresent()).isTrue();
            then(errorOr.hasErrors()).isFalse();
        }

        @Test
        void shouldGetValue() {
            then(errorOr.get()).isEqualTo(null);
        }

        @Test
        void shouldFailToGetErrors() {
            Throwable throwable = catchThrowable(errorOr::getErrors);

            then(throwable).isInstanceOf(NoSuchElementException.class)
                    .hasMessage("No error present, but value null");
        }

        @Test
        void shouldExecuteIfPresent() {
            StringBuilder builder = new StringBuilder("prefixed-");

            errorOr.ifPresent(builder::append);

            then(builder).hasToString("prefixed-null");
        }

        @Test
        void shouldHandleValue() {
            StringBuilder builder = new StringBuilder("prefixed-");

            errorOr.handle(builder::append, builder::append);

            then(builder).hasToString("prefixed-null");
        }

        @Test
        void shouldMapValue() {
            ErrorOr<Integer> mapped = errorOr.map(value -> {
                assert value == null;
                return 10;
            });

            then(mapped.get()).isEqualTo(10);
        }

        @Test
        void shouldFlatMapValue() {
            ErrorOr<Integer> mapped = errorOr.flatMap(value -> {
                assert value == null;
                return ErrorOr.of(10);
            });

            then(mapped.get()).isEqualTo(10);
        }

        @Test
        void shouldStreamValue() {
            Stream<String> stream = errorOr.stream();

            then(stream).containsExactly((String) null);
        }

        @Test
        void shouldContainValue() {
            Optional<String> optional = errorOr.optional();

            then(optional).isEmpty();
        }
    }

    @Nested
    class GivenItHasErrors {
        private final ErrorOr<String> errorOr = ErrorOr.ofErrors(singletonList(CLIENT_ERROR));

        @Test
        void shouldBeEqual() {
            then(errorOr).isEqualTo(errorOr); // itself
            then(errorOr).isNotEqualTo(ErrorOr.of("some-value"));
            then(errorOr).isNotEqualTo(null);
            then(errorOr).isNotEqualTo("some-value");
            then(errorOr).isEqualTo(ErrorOr.ofErrors(singletonList(CLIENT_ERROR)));
        }

        @Test
        void shouldNotBePresentButHaveErrors() {
            then(errorOr.isPresent()).isFalse();
            then(errorOr.hasErrors()).isTrue();
        }

        @Test
        void shouldFailToGetValue() {
            Throwable throwable = catchThrowable(errorOr::get);

            then(throwable).isInstanceOf(NoSuchElementException.class)
                    .hasMessage("No value present, but [some toString]");
        }

        @Test
        void shouldGetErrors() {
            then(errorOr.getErrors()).containsExactly(CLIENT_ERROR);
        }

        @Test
        void shouldNotExecuteIfPresent() {
            StringBuilder builder = new StringBuilder("prefixed-");

            errorOr.ifPresent(builder::append);

            then(builder).hasToString("prefixed-");
        }

        @Test
        void shouldHandleValue() {
            StringBuilder builder = new StringBuilder("prefixed-");

            errorOr.handle(builder::append, builder::append);

            then(builder).hasToString("prefixed-[some toString]");
        }

        @Test
        void shouldNotMapValue() {
            ErrorOr<Integer> mapped = errorOr.map(v -> {
                throw new RuntimeException("dummy");
            });

            then(mapped).isEqualTo(errorOr);
        }

        @Test
        void shouldNotFlatMapValue() {
            ErrorOr<Integer> mapped = errorOr.flatMap(v -> {
                throw new RuntimeException("dummy");
            });

            then(mapped).isEqualTo(errorOr);
        }

        @Test
        void shouldNotStreamValue() {
            Stream<String> stream = errorOr.stream();

            then(stream).isEmpty();
        }

        @Test
        void shouldNotContainValue() {
            Optional<String> optional = errorOr.optional();

            then(optional).isEmpty();
        }
    }
}
