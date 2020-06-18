package io.smallrye.graphql.execution.error;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;

import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.GraphqlErrorException;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;
import graphql.validation.ValidationError;
import graphql.validation.ValidationErrorType;
import io.smallrye.graphql.api.GraphQlErrorCode;

/**
 * Test for {@link ExecutionErrorsService}
 *
 * @author Nandor Holozsnyak (nandor.holozsnyak@gmail.com)
 */
class ExecutionErrorsServiceTest {

    private final ExecutionErrorsService executionErrorsService = new ExecutionErrorsService();

    @Test
    void testToJsonErrors_WhenExceptionWhileDataFetchingErrorCaught_ShouldReturnJsonBodyWithCustomExtensions() {
        // Given
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("code", "OPERATION_FAILED");
        GraphqlErrorException graphqlErrorException = GraphqlErrorException.newErrorException()
                .extensions(extensions)
                .build();
        ExceptionWhileDataFetching exceptionWhileDataFetching = new ExceptionWhileDataFetching(ExecutionPath.rootPath(),
                graphqlErrorException, new SourceLocation(1, 1));

        // When
        JsonArray jsonArray = executionErrorsService.toJsonErrors(singletonList(exceptionWhileDataFetching));

        // Then
        JsonObject extensionJsonObject = jsonArray.getJsonObject(0).getJsonObject("extensions");
        assertThat(extensionJsonObject.getString("exception")).isEqualTo("graphql.GraphqlErrorException");
        assertThat(extensionJsonObject.getString("classification")).isEqualTo("DataFetchingException");
        assertThat(extensionJsonObject.getString("code")).isEqualTo("OPERATION_FAILED");
    }

    @Test
    void testToJsonErrors_WhenExceptionWhileValidationErrorCaught_ShouldReturnJsonBodyWithCustomExtensions() {
        // Given
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("code", "OPERATION_FAILED");
        ValidationError validationError = ValidationError.newValidationError()
                .validationErrorType(ValidationErrorType.UnknownDirective)
                .description("TestDescription")
                .queryPath(singletonList("Test-Path"))
                .extensions(extensions)
                .build();

        // When
        JsonArray jsonArray = executionErrorsService.toJsonErrors(singletonList(validationError));

        // Then
        JsonObject extensionJsonObject = jsonArray.getJsonObject(0).getJsonObject("extensions");
        assertThat(extensionJsonObject.getString("description")).isEqualTo("TestDescription");
        assertThat(extensionJsonObject.getString("validationErrorType")).isEqualTo("UnknownDirective");
        assertThat(extensionJsonObject.getJsonArray("queryPath").getString(0)).isEqualTo("Test-Path");
        assertThat(extensionJsonObject.getString("classification")).isEqualTo("ValidationError");
        assertThat(extensionJsonObject.getString("code")).isEqualTo("OPERATION_FAILED");
    }

    @Test
    void shouldMapExceptionNameToCode() {
        class DummyBusinessException extends RuntimeException {
            public DummyBusinessException(String message) {
                super(message);
            }
        }

        JsonArray jsonArray = whenConverting(new DummyBusinessException("dummy-message"));

        JsonObject extensions = jsonArray.getJsonObject(0).getJsonObject("extensions");
        assertThat(extensions.getString("exception")).isEqualTo(DummyBusinessException.class.getName());
        assertThat(extensions.getString("code", null)).isEqualTo("dummy-business");
    }

    @Test
    void shouldMapClassAnnotationErrorCode() {
        @GraphQlErrorCode("dummy-code")
        class DummyBusinessException extends RuntimeException {
            public DummyBusinessException(String message) {
                super(message);
            }
        }

        JsonArray jsonArray = whenConverting(new DummyBusinessException("dummy-message"));

        JsonObject extensions = jsonArray.getJsonObject(0).getJsonObject("extensions");
        assertThat(extensions.getString("exception")).isEqualTo(DummyBusinessException.class.getName());
        assertThat(extensions.getString("code", null)).isEqualTo("dummy-code");
    }

    private JsonArray whenConverting(RuntimeException exception) {
        ExecutionPath path = ExecutionPath.parse("/foo/bar");
        SourceLocation location = new SourceLocation(12, 34);
        GraphQLError graphQLError = new GraphQLExceptionWhileDataFetching(path, exception, location);
        return executionErrorsService.toJsonErrors(singletonList(graphQLError));
    }
}
