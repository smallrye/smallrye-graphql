package tck.graphql.typesafe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.BDDAssertions;

import io.smallrye.graphql.client.GraphQLClientException;
import io.smallrye.graphql.client.GraphQLError;
import io.smallrye.graphql.client.typesafe.api.ErrorOr;

public class CustomAssertions extends BDDAssertions {
    public static GraphQLClientExceptionAssertions then(GraphQLClientException exception) {
        return new GraphQLClientExceptionAssertions(exception);
    }

    public static class GraphQLClientExceptionAssertions
            extends AbstractThrowableAssert<GraphQLClientExceptionAssertions, GraphQLClientException> {
        private final GraphQLClientException exception;

        public GraphQLClientExceptionAssertions(GraphQLClientException exception) {
            super(exception, GraphQLClientExceptionAssertions.class);
            this.exception = exception;
        }

        public GraphQLErrorAssertions hasExactlyOneErrorWhich() {
            then(exception.getErrors()).hasSize(1);
            return then(exception.getErrors().get(0));
        }
    }

    public static ErrorOrAssertions then(ErrorOr<?> errorOr) {
        return new ErrorOrAssertions(errorOr);
    }

    public static class ErrorOrAssertions {
        private final ErrorOr<?> errorOr;

        public ErrorOrAssertions(ErrorOr<?> errorOr) {
            this.errorOr = errorOr;
        }

        public GraphQLErrorAssertions hasExactlyOneErrorWhich() {
            then(errorOr.hasErrors()).isTrue();
            List<GraphQLError> errors = errorOr.getErrors();
            then(errors).hasSize(1);
            return then(errors.get(0));
        }
    }

    public static GraphQLErrorAssertions then(GraphQLError error) {
        return new GraphQLErrorAssertions(error);
    }

    @SuppressWarnings({ "UnusedReturnValue", "ResultOfMethodCallIgnored" })
    public static class GraphQLErrorAssertions {
        private final GraphQLError error;

        public GraphQLErrorAssertions(GraphQLError error) {
            this.error = error;
        }

        public GraphQLErrorAssertions hasMessage(String message) {
            then(error.getMessage()).isEqualTo(message);
            return this;
        }

        public GraphQLErrorAssertions hasNoSourceLocation() {
            then(error.getLocations()).isNull();
            return this;
        }

        public GraphQLErrorAssertions hasSourceLocation(int line, int column) {
            return hasSourceLocations(CustomAssertions.sourceLocation(line, column));
        }

        @SafeVarargs
        public final GraphQLErrorAssertions hasSourceLocations(Map<String, Integer>... sourceLocations) {
            then(error.getLocations()).containsExactly(sourceLocations);
            return this;
        }

        public GraphQLErrorAssertions hasPath(Object... path) {
            then(error.getPath()).containsExactly(path);
            return this;
        }

        public GraphQLErrorAssertions hasNoPath() {
            then(error.getPath()).isNull();
            return this;
        }

        public GraphQLErrorAssertions hasClassification(String classification) {
            return hasExtension("classification", classification);
        }

        public void hasErrorCode(String errorCode) {
            hasExtension("code", errorCode);
        }

        public GraphQLErrorAssertions hasExtension(String key, String value) {
            then(error.getExtensions().get(key)).isEqualTo(value);
            return this;
        }
    }

    public static Map<String, Integer> sourceLocation(int line, int column) {
        Map<String, Integer> sourceLocation = new HashMap<>();
        sourceLocation.put("line", line);
        sourceLocation.put("column", column);
        return sourceLocation;
    }
}
