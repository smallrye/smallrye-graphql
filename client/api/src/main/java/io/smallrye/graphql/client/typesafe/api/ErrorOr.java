package io.smallrye.graphql.client.typesafe.api;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import io.smallrye.graphql.client.GraphQLError;

/**
 * Like an {@link java.util.Optional}, but if a value is not present, there is a List of
 * {@link GraphQLError}s instead.
 * There can be the paradox situation that there is a <code>value</code> <em>as well as</em> errors,
 * but this is what a GraphQL service could theoretically return!
 */
public class ErrorOr<T> {
    private final T value;
    private final List<GraphQLError> errors;

    public static <T> ErrorOr<T> of(T value) {
        return new ErrorOr<>(requireNonNull(value, "value must not be null"), null);
    }

    public static <T> ErrorOr<T> ofErrors(List<GraphQLError> errors) {
        if (errors == null)
            throw new NullPointerException("errors must not be null");
        if (errors.isEmpty())
            throw new IllegalArgumentException("errors must not be empty");
        return new ErrorOr<>(null, unmodifiableList(new ArrayList<>(errors)));
    }

    protected ErrorOr(T value, List<GraphQLError> errors) {
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
        return !hasErrors();
    }

    public boolean hasErrors() {
        return errors != null;
    }

    public T get() {
        if (hasErrors())
            throw new NoSuchElementException("No value present, but " + errors);
        return value;
    }

    public List<GraphQLError> getErrors() {
        if (isPresent())
            throw new NoSuchElementException("No error present, but value " + value);
        return errors;
    }

    public void ifPresent(Consumer<? super T> action) {
        Objects.requireNonNull(action, "ifPresent action must not be null");
        if (isPresent())
            action.accept(value);
    }

    public void handle(Consumer<? super T> dataAction, Consumer<List<GraphQLError>> errorsAction) {
        Objects.requireNonNull(dataAction, "handle dataAction must not be null");
        Objects.requireNonNull(errorsAction, "handle errorsAction must not be null");
        if (isPresent())
            dataAction.accept(value);
        else
            errorsAction.accept(errors);
    }

    public <U> ErrorOr<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "map function must not be null");
        if (isPresent())
            return ErrorOr.of(mapper.apply(value));
        //noinspection unchecked
        return (ErrorOr<U>) this;
    }

    public <U> ErrorOr<U> flatMap(Function<? super T, ErrorOr<U>> mapper) {
        Objects.requireNonNull(mapper, "flatMap function must not be null");
        if (isPresent())
            return mapper.apply(value);
        //noinspection unchecked
        return (ErrorOr<U>) this;
    }

    public Optional<T> optional() {
        return isPresent() ? Optional.of(value) : Optional.empty();
    }

    public Stream<T> stream() {
        return isPresent() ? Stream.of(value) : Stream.empty();
    }
}
