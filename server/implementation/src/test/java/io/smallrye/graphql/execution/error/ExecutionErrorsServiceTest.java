package io.smallrye.graphql.execution.error;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.GraphqlErrorException;
import graphql.execution.ResultPath;
import graphql.language.SourceLocation;
import graphql.validation.ValidationError;
import graphql.validation.ValidationErrorType;
import io.smallrye.graphql.spi.config.Config;

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
        ExceptionWhileDataFetching exceptionWhileDataFetching = new ExceptionWhileDataFetching(ResultPath.rootPath(),
                graphqlErrorException, new SourceLocation(1, 1));

        // When
        ArrayNode jsonArray = executionErrorsService.toJsonErrors(singletonList(exceptionWhileDataFetching));

        // Then
        ObjectNode extensionJsonObject = (ObjectNode) jsonArray.get(0).get("extensions");
        assertThat(extensionJsonObject.get("exception").asText()).isEqualTo("graphql.GraphqlErrorException");
        assertThat(extensionJsonObject.get("classification").asText()).isEqualTo("DataFetchingException");
        assertThat(extensionJsonObject.get("code").asText()).isEqualTo("OPERATION_FAILED");
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
        ArrayNode jsonArray = executionErrorsService.toJsonErrors(singletonList(validationError));

        // Then
        ObjectNode extensionJsonObject = (ObjectNode) jsonArray.get(0).get("extensions");
        assertThat(extensionJsonObject.get("description").asText()).isEqualTo("TestDescription");
        assertThat(extensionJsonObject.get("validationErrorType").asText()).isEqualTo("UnknownDirective");
        assertThat(extensionJsonObject.get("queryPath").get(0).asText()).isEqualTo("Test-Path");
        assertThat(extensionJsonObject.get("classification").asText()).isEqualTo("ValidationError");
        assertThat(extensionJsonObject.get("code").asText()).isEqualTo("OPERATION_FAILED");
    }

    @Test
    void shouldMapExceptionNameToCode() {
        class DummyBusinessException extends RuntimeException {
            public DummyBusinessException(String message) {
                super(message);
            }
        }

        ArrayNode jsonArray = whenConverting(new DummyBusinessException("dummy-message"));

        ObjectNode extensions = (ObjectNode) jsonArray.get(0).get("extensions");
        assertThat(extensions.get("exception").asText()).isEqualTo(DummyBusinessException.class.getName());
        assertThat(extensions.get("code").asText()).isEqualTo("dummy-business");
    }

    private ArrayNode whenConverting(RuntimeException exception) {
        ResultPath path = ResultPath.parse("/foo/bar");
        SourceLocation location = new SourceLocation(12, 34);
        GraphQLError graphQLError = new GraphQLExceptionWhileDataFetching(path, exception, location);
        return executionErrorsService.toJsonErrors(singletonList(graphQLError));
    }

    private Config getGraphQLConfig() {
        return new Config() {
            @Override
            public boolean isPrintDataFetcherException() {
                return true;
            }

            @Override
            public Optional<List<String>> getErrorExtensionFields() {
                return Optional.of(Config.ERROR_EXTENSION_ALL_KNOWN);
            }

            @Override
            public String getName() {
                return "Test Execution errors config";
            }
        };
    }
}
