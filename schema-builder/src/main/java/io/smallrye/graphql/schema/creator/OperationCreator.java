package io.smallrye.graphql.schema.creator;

import java.util.List;
import java.util.Optional;

import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.OperationType;
import io.smallrye.graphql.schema.SchemaBuilderException;
import io.smallrye.graphql.schema.helper.DefaultValueHelper;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.Direction;
import io.smallrye.graphql.schema.helper.FormatHelper;
import io.smallrye.graphql.schema.helper.MethodHelper;
import io.smallrye.graphql.schema.helper.NonNullHelper;
import io.smallrye.graphql.schema.model.Argument;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Reference;

/**
 * Creates a Operation object
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class OperationCreator {

    private OperationCreator() {
    }

    /**
     * This creates a single operation.
     * It translate to one entry under a query / mutation in the schema or
     * one method in the Java class annotated with Query or Mutation
     * 
     * @param methodInfo the java method
     * @param operationType the type of operation (Query / Mutation)
     * @return a Operation that defines this GraphQL Operation
     */
    public static Operation createOperation(MethodInfo methodInfo, OperationType operationType) {
        Annotations annotationsForMethod = Annotations.getAnnotationsForMethod(methodInfo);
        Type fieldType = methodInfo.returnType();

        // Name
        String name = getOperationName(methodInfo, operationType, annotationsForMethod);

        // Description
        Optional<String> maybeDescription = DescriptionHelper.getDescriptionForField(annotationsForMethod, fieldType);

        // Field Type
        validateFieldType(methodInfo, operationType);
        Reference reference = ReferenceCreator.createReferenceForOperationField(fieldType, annotationsForMethod);

        Operation operation = new Operation(methodInfo.declaringClass().name().toString(),
                methodInfo.name(),
                MethodHelper.getPropertyName(Direction.OUT, methodInfo.name()),
                name,
                maybeDescription.orElse(null),
                reference);

        // NotNull
        if (NonNullHelper.markAsNonNull(fieldType, annotationsForMethod)) {
            operation.markNotNull();
        }

        // Array
        operation.setArray(ArrayCreator.createArray(fieldType));

        // TransformInfo
        operation.setTransformInfo(FormatHelper.getFormat(fieldType, annotationsForMethod));

        // Default Value
        Optional maybeDefaultValue = DefaultValueHelper.getDefaultValue(annotationsForMethod);
        operation.setDefaultValue(maybeDefaultValue);

        // Arguments
        List<Type> parameters = methodInfo.parameters();
        for (short i = 0; i < parameters.size(); i++) {
            Optional<Argument> maybeArgument = ArgumentCreator.createArgument(fieldType, methodInfo, i);
            if (maybeArgument.isPresent()) {
                Argument argument = maybeArgument.get();

                // See if this is a @Source
                Annotations annotationsForThisArgument = Annotations.getAnnotationsForArgument(methodInfo, i);
                if (isSourceAnnotationOnSourceOperation(annotationsForThisArgument, operationType)) {
                    argument.markAsSourceArgument();
                }

                operation.addArgument(argument);
            }

        }

        return operation;
    }

    /**
     * Source operation on types should remove the Source argument
     * 
     * @param annotationsForArgument
     * @param operationType
     * @return
     */
    private static boolean isSourceAnnotationOnSourceOperation(Annotations annotationsForArgument,
            OperationType operationType) {
        return operationType.equals(OperationType.Source) &&
                annotationsForArgument.containsOneOfTheseAnnotations(Annotations.SOURCE);
    }

    private static void validateFieldType(MethodInfo methodInfo, OperationType operationType) {
        Type returnType = methodInfo.returnType();
        if (returnType.kind().equals(Type.Kind.VOID)) {
            throw new SchemaBuilderException(
                    "Can not have a void return for [" + operationType.name()
                            + "] on method [" + methodInfo.name() + "]");
        }
    }

    /**
     * Get the name from annotation(s) or default.
     * This is for operations (query, mutation and source)
     * 
     * @param methodInfo the java method
     * @param operationType the type (query, mutation)
     * @param annotations the annotations on this method
     * @return the operation name
     */
    private static String getOperationName(MethodInfo methodInfo, OperationType operationType, Annotations annotations) {
        DotName operationAnnotation = getOperationAnnotation(operationType);

        // If the @Query or @Mutation annotation has a value, use that, else use name or jsonb property
        return annotations.getOneOfTheseMethodAnnotationsValue(
                operationAnnotation,
                Annotations.NAME,
                Annotations.JSONB_PROPERTY)
                .orElse(getDefaultExecutionTypeName(methodInfo, operationType));

    }

    private static DotName getOperationAnnotation(OperationType operationType) {
        if (operationType.equals(OperationType.Query)) {
            return Annotations.QUERY;
        } else if (operationType.equals(OperationType.Mutation)) {
            return Annotations.MUTATION;
        }
        return null;
    }

    private static String getDefaultExecutionTypeName(MethodInfo methodInfo, OperationType operationType) {
        String methodName = methodInfo.name();
        if (operationType.equals(OperationType.Query) || operationType.equals(OperationType.Source)) {
            methodName = MethodHelper.getPropertyName(Direction.OUT, methodName);
        } else if (operationType.equals(OperationType.Mutation)) {
            methodName = MethodHelper.getPropertyName(Direction.IN, methodName);
        }
        return methodName;
    }

}
