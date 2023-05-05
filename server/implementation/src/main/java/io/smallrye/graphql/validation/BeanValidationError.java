package io.smallrye.graphql.validation;

import static graphql.ErrorType.ValidationError;
import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.language.NamedNode;
import graphql.language.SourceLocation;

public class BeanValidationError implements GraphQLError {
    private final ConstraintViolation<?> violation;
    private final List<NamedNode<?>> requestedPath;

    public BeanValidationError(ConstraintViolation<?> violation, List<NamedNode<?>> requestedPath) {
        this.violation = violation;
        this.requestedPath = requestedPath;
    }

    @Override
    public ErrorClassification getErrorType() {
        return ValidationError;
    }

    @Override
    public String getMessage() {
        return "validation failed: " + violation.getPropertyPath() + " " + violation.getMessage();
    }

    @Override
    public List<SourceLocation> getLocations() {
        return requestedPath.stream().map(NamedNode::getSourceLocation).collect(toList());
    }

    @Override
    public List<Object> getPath() {
        return requestedPath.stream().map(argument -> (Object) argument.getName()).collect(toList());
    }

    @Override
    public Map<String, Object> getExtensions() {
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("violation.message", violation.getMessage());
        extensions.put("violation.propertyPath",
                toStream(violation.getPropertyPath()).flatMap(this::items).collect(toList()));
        extensions.put("violation.invalidValue", violation.getInvalidValue());
        extensions.put("violation.constraint", getConstraintAttributes());
        return extensions;
    }

    private Stream<String> items(Path.Node node) {
        if (node.getIndex() == null)
            return Stream.of(node.getName());
        return Stream.of(node.getIndex().toString(), node.getName());
    }

    private Map<String, Object> getConstraintAttributes() {
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
