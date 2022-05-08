package io.smallrye.graphql.cdi.validation;

import static java.util.Arrays.asList;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;

import graphql.execution.DataFetcherResult;
import graphql.language.Argument;
import graphql.language.Field;
import graphql.language.NamedNode;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.transformation.AbstractDataFetcherException;

public class BeanValidationException extends AbstractDataFetcherException {
    private final Set<ConstraintViolation<Object>> violations;
    private final Method method;

    public BeanValidationException(Set<ConstraintViolation<Object>> violations, Method method) {
        this.violations = violations;
        this.method = method;
    }

    @Override
    public DataFetcherResult.Builder<Object> appendDataFetcherResult(DataFetcherResult.Builder<Object> builder,
            DataFetchingEnvironment dfe) {
        RequestNodeBuilder requestNodeBuilder = new RequestNodeBuilder(method, dfe);
        violations.stream()
                .map(violation -> new BeanValidationError(violation, requestNodeBuilder.build(violation)))
                .forEach(builder::error);
        return builder;
    }

    class RequestNodeBuilder {
        private final Method method;
        private final DataFetchingEnvironment dfe;

        RequestNodeBuilder(Method method, DataFetchingEnvironment dfe) {
            this.method = method;
            this.dfe = dfe;
        }

        List<NamedNode<?>> build(ConstraintViolation<Object> violation) {
            Iterator<Path.Node> violationNodes = violation.getPropertyPath().iterator();

            return asList(
                    methodNode(violationNodes.next()),
                    requestedArgument(violationNodes.next()));
            // we could try to locate all other request nodes from the violation path, too
        }

        private Field methodNode(Path.Node methodNode) {
            assert dfe.getFieldDefinition().getName().equals(methodNode.getName()) : "expected first violation path item "
                    + methodNode.getName() + " to be the method name field definition "
                    + dfe.getFieldDefinition().getName();
            return dfe.getField();
        }

        private Argument requestedArgument(Path.Node node) {
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
            int index = 0;
            for (int i = 0; i < parameters.length; i++) {
                if (name.equals(parameters[i].getName())) {
                    return index;
                }
                // parameters of type Context are not stored as arguments in the FieldDefinition, so don't increment the index on them
                if (!parameters[i].getType().isAssignableFrom(Context.class)) {
                    index++;
                }
            }
            throw new AssertionError("expected parameter " + name + " in " + method);
        }
    }

}
