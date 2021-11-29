package io.smallrye.graphql.schema.creator;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;

import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.SchemaBuilderException;
import io.smallrye.graphql.schema.helper.Direction;
import io.smallrye.graphql.schema.helper.MethodHelper;
import io.smallrye.graphql.schema.model.Argument;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.OperationType;
import io.smallrye.graphql.schema.model.Reference;

/**
 * Creates a Operation object
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class OperationCreator extends ModelCreator {

    private final ArgumentCreator argumentCreator;

    public OperationCreator(ReferenceCreator referenceCreator, ArgumentCreator argumentCreator) {
        super(referenceCreator);
        this.argumentCreator = argumentCreator;
    }

    /**
     * This creates a single operation.
     * It translate to one entry under a query / mutation in the schema or
     * one method in the Java class annotated with Query or Mutation
     *
     * @param methodInfo the java method
     * @param operationType the type of operation (Query / Mutation)
     * @param type
     * @return a Operation that defines this GraphQL Operation
     */
    public Operation createOperation(MethodInfo methodInfo, OperationType operationType,
            final io.smallrye.graphql.schema.model.Type type) {

        if (!Modifier.isPublic(methodInfo.flags())) {
            throw new IllegalArgumentException(
                    "Method " + methodInfo.declaringClass().name().toString() + "#" + methodInfo.name()
                            + " is used as an operation, but is not public");
        }

        Annotations annotationsForMethod = Annotations.getAnnotationsForMethod(methodInfo);

        Type fieldType = getReturnType(methodInfo);

        // Name
        String name = getOperationName(methodInfo, operationType, annotationsForMethod);

        // Field Type
        validateFieldType(methodInfo, operationType);
        Reference reference = referenceCreator.createReferenceForOperationField(fieldType, annotationsForMethod);

        Operation operation = new Operation(methodInfo.declaringClass().name().toString(),
                methodInfo.name(),
                MethodHelper.getPropertyName(Direction.OUT, methodInfo.name()),
                name,
                reference,
                operationType);
        if (type != null) {
            operation.setSourceFieldOn(new Reference(type));
        }

        // Arguments
        List<Type> parameters = methodInfo.parameters();
        for (short i = 0; i < parameters.size(); i++) {
            Optional<Argument> maybeArgument = argumentCreator.createArgument(operation, methodInfo, i);
            maybeArgument.ifPresent(operation::addArgument);
        }

        populateField(Direction.OUT, operation, fieldType, annotationsForMethod);

        return operation;
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
                Annotations.JSONB_PROPERTY,
                Annotations.JACKSON_PROPERTY)
                .orElse(getDefaultExecutionTypeName(methodInfo, operationType));

    }

    private static DotName getOperationAnnotation(OperationType operationType) {
        switch (operationType) {
            case QUERY:
                return Annotations.QUERY;
            case MUTATION:
                return Annotations.MUTATION;
            case SUBSCRIPTION:
                return Annotations.SUBCRIPTION;
            default:
                break;
        }
        return null;
    }

    private static String getDefaultExecutionTypeName(MethodInfo methodInfo, OperationType operationType) {
        String methodName = methodInfo.name();
        if (operationType.equals(OperationType.QUERY) || operationType.equals(OperationType.SUBSCRIPTION)) {
            methodName = MethodHelper.getPropertyName(Direction.OUT, methodName);
        } else if (operationType.equals(OperationType.MUTATION)) {
            methodName = MethodHelper.getPropertyName(Direction.IN, methodName);
        }
        return methodName;
    }

}
