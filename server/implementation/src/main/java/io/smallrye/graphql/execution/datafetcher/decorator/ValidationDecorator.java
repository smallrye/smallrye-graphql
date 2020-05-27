package io.smallrye.graphql.execution.datafetcher.decorator;

import static graphql.ErrorType.ValidationError;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.validation.ConstraintViolation;
import javax.validation.Path.Node;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.execution.DataFetcherResult.Builder;
import graphql.language.Argument;
import graphql.language.SourceLocation;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import io.smallrye.graphql.execution.datafetcher.ExecutionContext;
import io.smallrye.graphql.transformation.TransformException;

public class ValidationDecorator extends AbstractDataFetcherDecorator {
    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

    @Override
    protected void before(ExecutionContext executionContext) throws TransformException {
        Annotation[][] parametersAnnotations = executionContext.method().getParameterAnnotations();
        for (int i = 0; i < parametersAnnotations.length; i++) {
            Annotation[] annotations = parametersAnnotations[i];
            for (Annotation annotation : annotations) {
                if (Valid.class.equals(annotation.annotationType())) {
                    Object argument = executionContext.arguments()[i];

                    // TODO validate annotations on the parameter itself
                    Set<ConstraintViolation<Object>> violations = VALIDATOR_FACTORY.getValidator().validate(argument,
                            groups(annotations));

                    if (!violations.isEmpty()) {
                        int parameterIndex = i;
                        throw new TransformException(null, null, null) {
                            @Override
                            public Builder<Object> appendDataFetcherResult(Builder<Object> builder,
                                    DataFetchingEnvironment dfe) {
                                violations.forEach(violation -> builder
                                        .error(new ValidationFailedGraphQLError(dfe, violation, parameterIndex)));
                                return builder;
                            }
                        };
                    }
                }
            }
        }
    }

    private Class<?>[] groups(Annotation[] annotations) {
        ConvertGroup found = null;
        for (Annotation annotation : annotations) {
            if (ConvertGroup.class.equals(annotation.annotationType())) {
                if (found != null)
                    throw new IllegalArgumentException("can't have multiple `ConvertGroup` parameter annotations");
                found = (ConvertGroup) annotation;
                if (!Default.class.equals(found.from()))
                    throw new IllegalArgumentException(
                            "don't use a `ConvertGroup` parameter annotation with a `from` other than `Default`");
            }
        }
        return (found == null) ? new Class[0] : new Class<?>[] { found.to() };
    }

    private static class ValidationFailedGraphQLError implements GraphQLError {
        private final GraphQLArgument graphQLArgument;
        private final DataFetchingEnvironment dfe;
        private final ConstraintViolation<Object> violation;

        public ValidationFailedGraphQLError(DataFetchingEnvironment dfe, ConstraintViolation<Object> violation,
                int parameterIndex) {
            this.dfe = dfe;
            this.violation = violation;
            this.graphQLArgument = dfe.getFieldDefinition().getArguments().get(parameterIndex);
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
            for (Argument a : dfe.getField().getArguments()) {
                if (a.getName().equals(graphQLArgument.getName())) {
                    // we could also try to locate the exact violation path
                    return singletonList(a.getSourceLocation());
                }
            }
            return singletonList(dfe.getMergedField().getSingleField().getSourceLocation());
        }

        @Override
        public List<Object> getPath() {
            List<Object> path = new ArrayList<>();
            for (Object o : dfe.getExecutionStepInfo().getPath().toList()) {
                path.add(o.toString());
            }
            path.add(graphQLArgument.getName());
            violation.getPropertyPath().forEach(pathItem -> path.add(pathItem.getName()));
            return path;
        }

        @Override
        public Map<String, Object> getExtensions() {
            Map<String, Object> extensions = new HashMap<>();
            extensions.put("violation.message", violation.getMessage());
            extensions.put("violation.propertyPath", StreamSupport.stream(violation.getPropertyPath().spliterator(), false)
                    .flatMap(this::items)
                    .collect(toList()));
            extensions.put("violation.invalidValue", violation.getInvalidValue());
            extensions.put("violation.constraint", getAttributes());
            return extensions;
        }

        private Stream<String> items(Node node) {
            if (node.getIndex() == null)
                return Stream.of(node.getName());
            return Stream.of(node.getIndex().toString(), node.getName());
        }

        private Map<String, Object> getAttributes() {
            Map<String, Object> attributes = new HashMap<>(violation.getConstraintDescriptor().getAttributes());
            attributes.computeIfPresent("groups", ValidationFailedGraphQLError::classNames);
            attributes.computeIfPresent("payload", ValidationFailedGraphQLError::classNames);
            return attributes;
        }

        private static Object classNames(String key, Object oldValue) {
            return Stream.of((Class<?>[]) oldValue).map(Class::getName).collect(toList());
        }
    }
}
