package io.smallrye.graphql.execution.datafetcher.decorator;

import static graphql.ErrorType.ValidationError;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.validation.ConstraintViolation;
import javax.validation.Path.Node;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.execution.DataFetcherResult.Builder;
import graphql.language.Argument;
import graphql.language.Field;
import graphql.language.NamedNode;
import graphql.language.SourceLocation;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.execution.datafetcher.ExecutionContext;
import io.smallrye.graphql.transformation.TransformException;

public class ValidationDecorator extends AbstractDataFetcherDecorator {
    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

    @Override
    protected void before(ExecutionContext executionContext) throws BeanValidationException {
        Set<ConstraintViolation<Object>> violations = VALIDATOR_FACTORY.getValidator()
                .forExecutables().validateParameters(
                        executionContext.target(),
                        executionContext.method(),
                        executionContext.arguments());

        if (!violations.isEmpty()) {
            throw new BeanValidationException(violations, executionContext.method());
        }
    }

    private static class BeanValidationException extends TransformException {
        private final Set<ConstraintViolation<Object>> violations;
        private final Method method;

        public BeanValidationException(Set<ConstraintViolation<Object>> violations, Method method) {
            super(null, null, null);
            this.violations = violations;
            this.method = method;
        }

        @Override
        public Builder<Object> appendDataFetcherResult(Builder<Object> builder, DataFetchingEnvironment dfe) {
            RequestNodeBuilder requestNodeBuilder = new RequestNodeBuilder(method, dfe);
            violations.stream()
                    .map(violation -> new ValidationFailedGraphQLError(violation, requestNodeBuilder.build(violation)))
                    .forEach(builder::error);
            return builder;
        }

        private static class RequestNodeBuilder {
            private final Method method;
            private final DataFetchingEnvironment dfe;

            RequestNodeBuilder(Method method, DataFetchingEnvironment dfe) {
                this.method = method;
                this.dfe = dfe;
            }

            List<NamedNode<?>> build(ConstraintViolation<Object> violation) {
                Iterator<Node> violationNodes = violation.getPropertyPath().iterator();

                return asList(
                        methodNode(violationNodes.next()),
                        requestedArgument(violationNodes.next()));
                // we could try to locate all other request nodes from the violation path, too
            }

            private Field methodNode(Node methodNode) {
                assert dfe.getFieldDefinition().getName().equals(methodNode.getName()) : "expected first violation path item "
                        + methodNode.getName() + " to be the method name field definition "
                        + dfe.getFieldDefinition().getName();
                return dfe.getField();
            }

            private Argument requestedArgument(Node node) {
                String graphQLArgumentName = dfe.getFieldDefinition().getArguments().get(parameterIndex(node.getName()))
                        .getName();

                Field requestedField = dfe.getMergedField().getSingleField();

                return requestedField.getArguments().stream()
                        .filter(argument -> argument.getName().equals(graphQLArgumentName))
                        .findFirst()
                        .orElseThrow(() -> new AssertionError(
                                "expected field " + graphQLArgumentName + " in " + requestedField.getArguments()));
            }

            private int parameterIndex(String name) {
                Parameter[] parameters = method.getParameters();
                for (int i = 0; i < parameters.length; i++)
                    if (name.equals(parameters[i].getName()))
                        return i;
                throw new AssertionError("expected parameter " + name + " in " + method);
            }
        }
    }

    private static class ValidationFailedGraphQLError implements GraphQLError {
        private final ConstraintViolation<Object> violation;
        private final List<NamedNode<?>> requestedPath;

        public ValidationFailedGraphQLError(ConstraintViolation<Object> violation, List<NamedNode<?>> requestedPath) {
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

        private Stream<String> items(Node node) {
            if (node.getIndex() == null)
                return Stream.of(node.getName());
            return Stream.of(node.getIndex().toString(), node.getName());
        }

        private Map<String, Object> getConstraintAttributes() {
            Map<String, Object> attributes = new HashMap<>(violation.getConstraintDescriptor().getAttributes());
            attributes.computeIfPresent("groups", ValidationFailedGraphQLError::classNames);
            attributes.computeIfPresent("payload", ValidationFailedGraphQLError::classNames);
            return attributes;
        }

        private static Object classNames(String key, Object oldValue) {
            return Stream.of((Class<?>[]) oldValue).map(Class::getName).collect(toList());
        }
    }

    static <T> Stream<T> toStream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }
}
