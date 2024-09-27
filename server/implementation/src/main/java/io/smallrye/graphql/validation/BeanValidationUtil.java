package io.smallrye.graphql.validation;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;

import org.eclipse.microprofile.graphql.Source;

import graphql.execution.DataFetcherResult;
import graphql.execution.ResultPath;
import graphql.language.Argument;
import graphql.language.Field;
import graphql.language.NamedNode;
import graphql.language.SourceLocation;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.api.Context;

public class BeanValidationUtil {

    public static DataFetcherResult.Builder<Object> addConstraintViolationsToDataFetcherResult(
            Set<ConstraintViolation<?>> violations,
            Method method,
            DataFetcherResult.Builder<Object> builder,
            DataFetchingEnvironment dfe) {
        ResultPath resultPath = dfe.getExecutionStepInfo().getPath();
        RequestNodeBuilder requestNodeBuilder = new RequestNodeBuilder(method, dfe);
        List<SourceLocation> sourceLocations = violations.stream()
                .map(requestNodeBuilder::build)
                .flatMap(List::stream)
                .distinct()
                .map(NamedNode::getSourceLocation)
                .collect(toList());
        return builder.error(new BeanValidationError(violations, resultPath, sourceLocations));
    }

    static class RequestNodeBuilder {
        private final Method method;
        private final DataFetchingEnvironment dfe;

        RequestNodeBuilder(Method method, DataFetchingEnvironment dfe) {
            this.method = method;
            this.dfe = dfe;
        }

        List<NamedNode<?>> build(ConstraintViolation<?> violation) {
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
            for (Parameter parameter : parameters) {
                if (name.equals(parameter.getName())) {
                    return index;
                }
                // parameters of type Context are not stored as arguments in the FieldDefinition, so don't increment the index on them
                boolean isContext = parameter.getType().isAssignableFrom(Context.class);
                // similarly, parameters annotated with @Source are also not stored as arguments in the FieldDefinition
                boolean isAnnotatedWithSource = parameter.getAnnotation(Source.class) != null;
                if (!isContext && !isAnnotatedWithSource) {
                    index++;
                }
            }
            throw new AssertionError("expected parameter " + name + " in " + method);
        }
    }

}
