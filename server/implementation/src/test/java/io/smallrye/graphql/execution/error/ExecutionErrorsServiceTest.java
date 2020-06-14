package io.smallrye.graphql.execution.error;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import graphql.ExceptionWhileDataFetching;
import graphql.GraphqlErrorException;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;
import graphql.validation.ValidationError;
import graphql.validation.ValidationErrorType;

/**
 * Test for {@link ExecutionErrorsService}
 *
 * @author Nandor Holozsnyak (nandor.holozsnyak@gmail.com)
 */
class ExecutionErrorsServiceTest {

    private ExecutionErrorsService executionErrorsService;

    @BeforeEach
    void init() {
        executionErrorsService = new ExecutionErrorsService();
    }

    @Test
    void testToJsonErrors_WhenExceptionWhileDataFetchingErrorCaught_ShouldReturnJsonBodyWithCustomExtensions() {
        // Given
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("errorCode", "OPERATION_FAILED");
        GraphqlErrorException graphqlErrorException = GraphqlErrorException.newErrorException()
                .extensions(extensions)
                .build();
        ExceptionWhileDataFetching exceptionWhileDataFetching = new ExceptionWhileDataFetching(ExecutionPath.rootPath(),
                graphqlErrorException, new SourceLocation(1, 1));

        // When
        JsonArray jsonArray = executionErrorsService.toJsonErrors(Collections.singletonList(exceptionWhileDataFetching));

        // Then
        JsonObject extensionJsonObject = jsonArray.getJsonObject(0).getJsonObject("extensions");
        assertThat(extensionJsonObject.getString("exception")).isEqualTo("graphql.GraphqlErrorException");
        assertThat(extensionJsonObject.getString("classification")).isEqualTo("DataFetchingException");
        assertThat(extensionJsonObject.getString("errorCode")).isEqualTo("OPERATION_FAILED");
    }

    @Test
    void testToJsonErrors_WhenExceptionWhileValidationErrorCaught_ShouldReturnJsonBodyWithCustomExtensions() {
        // Given
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("errorCode", "OPERATION_FAILED");
        ValidationError validationError = ValidationError.newValidationError()
                .validationErrorType(ValidationErrorType.UnknownDirective)
                .description("TestDescription")
                .queryPath(Collections.singletonList("Test-Path"))
                .extensions(extensions)
                .build();

        // When
        JsonArray jsonArray = executionErrorsService.toJsonErrors(Collections.singletonList(validationError));

        // Then
        JsonObject extensionJsonObject = jsonArray.getJsonObject(0).getJsonObject("extensions");
        assertThat(extensionJsonObject.getString("description")).isEqualTo("TestDescription");
        assertThat(extensionJsonObject.getString("validationErrorType")).isEqualTo("UnknownDirective");
        assertThat(extensionJsonObject.getJsonArray("queryPath").getString(0)).isEqualTo("Test-Path");
        assertThat(extensionJsonObject.getString("classification")).isEqualTo("ValidationError");
        assertThat(extensionJsonObject.getString("errorCode")).isEqualTo("OPERATION_FAILED");
    }
}
