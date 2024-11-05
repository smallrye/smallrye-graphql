package io.smallrye.graphql.validation;

import static graphql.ErrorType.ValidationError;
import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.execution.ResultPath;
import graphql.language.SourceLocation;

public class BeanValidationError implements GraphQLError {
    private final Set<ConstraintViolation<?>> violations;
    private final ResultPath resultPath;
    private final List<SourceLocation> sourceLocations;

    public BeanValidationError(
            Set<ConstraintViolation<?>> violations,
            ResultPath resultPath,
            List<SourceLocation> sourceLocations) {
        this.violations = violations;
        this.resultPath = resultPath;
        this.sourceLocations = sourceLocations;
    }

    @Override
    public ErrorClassification getErrorType() {
        return ValidationError;
    }

    @Override
    public String getMessage() {
        String joinedMessage = violations.stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .collect(Collectors.joining(", "));
        return "validation failed: " + joinedMessage;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return sourceLocations;
    }

    @Override
    public List<Object> getPath() {
        return resultPath.toList();
    }

    @Override
    public Map<String, Object> getExtensions() {
        return Map.of("violations", violations.stream().map(this::getViolationAttributes).collect(toList()));
    }

    private Map<String, Object> getViolationAttributes(ConstraintViolation<?> violation) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("message", violation.getMessage());
        attributes.put("propertyPath",
                toStream(violation.getPropertyPath()).flatMap(this::items).collect(toList()));
        attributes.put("invalidValue", violation.getInvalidValue());
        attributes.put("constraint", getConstraintAttributes(violation));
        return attributes;
    }

    private Stream<String> items(Path.Node node) {
        if (node.getIndex() == null)
            return Stream.of(node.getName());
        return Stream.of(node.getIndex().toString(), node.getName());
    }

    private Map<String, Object> getConstraintAttributes(ConstraintViolation<?> violation) {
        Map<String, Object> attributes = new HashMap<>(violation.getConstraintDescriptor().getAttributes());
        attributes.computeIfPresent("groups", BeanValidationError::classNames);
        attributes.computeIfPresent("payload", BeanValidationError::classNames);
        return attributes;
    }

    private static Object classNames(String key, Object oldValue) {
        return Stream.of((Class<?>[]) oldValue).map(Class::getName).collect(toList());
    }

    static <T> Stream<T> toStream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }
}
