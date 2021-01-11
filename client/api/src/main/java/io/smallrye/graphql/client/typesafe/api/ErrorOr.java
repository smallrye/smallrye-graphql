package io.smallrye.graphql.client.typesafe.api;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Like an {@link java.util.Optional}, but if a value is not present, there is a List of {@link GraphQlClientError}s instead.
 * There can be the paradox situation that there is a <code>value</code> <em>as well as</em> errors,
 * but this is what a GraphQL service could theoretically return!
 */
public final class ErrorOr<T> {
    private final T value;
    private final List<GraphQlClientError> errors;

    public static <T> ErrorOr<T> of(T value) {
        return new ErrorOr<>(requireNonNull(value), null);
    }

    public static <T> ErrorOr<T> ofErrors(List<GraphQlClientError> errors) {
        return new ErrorOr<>(null, unmodifiableList(new ArrayList<>(requireNonNull(errors))));
    }

    private ErrorOr(T value, List<GraphQlClientError> errors) {
        this.value = value;
        this.errors = errors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ErrorOr<?> that = (ErrorOr<?>) o;
        return Objects.equals(value, that.value) && Objects.equals(errors, that.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, errors);
    }

    @Override
    public String toString() {
        return "ErrorOr(" + (isPresent() ? "value=" + value : "errors=" + errors) + ")";
    }

    public boolean isPresent() {
        return value != null;
    }

    public boolean isError() {
        return errors != null && !errors.isEmpty();
    }

    public T get() {
        if (isError())
            throw new NoSuchElementException("No value present, but " + errors);
        return value;
    }

    public List<GraphQlClientError> getErrors() {
        if (isPresent())
            throw new NoSuchElementException("No error present, but value " + value);
        return errors;
    }

    // TODO public void ifPresent(Consumer<? super T> action)
    // TODO public void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction)
    // TODO public Optional<T> filter(Predicate<? super T> predicate)
    // TODO public <U> Optional<U> map(Function<? super T, ? extends U> mapper)
    // TODO public <U> Optional<U> flatMap(Function<? super T, ? extends Optional<? extends U>> mapper)
    // TODO public Optional<T> or(Supplier<? extends Optional<? extends T>> supplier)
    // TODO public Stream<T> stream()
    // TODO public T orElse(T other)
    // TODO public T orElseGet(Supplier<? extends T> supplier)
    // TODO public T orElseThrow()
    // TODO public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X
}
