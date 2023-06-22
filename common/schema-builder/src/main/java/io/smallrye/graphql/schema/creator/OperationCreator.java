package io.smallrye.graphql.schema.creator;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.SchemaBuilderException;
import io.smallrye.graphql.schema.helper.Direction;
import io.smallrye.graphql.schema.helper.MethodHelper;
import io.smallrye.graphql.schema.helper.RolesAllowedDirectivesHelper;
import io.smallrye.graphql.schema.model.Argument;
import io.smallrye.graphql.schema.model.DirectiveInstance;
import io.smallrye.graphql.schema.model.Execute;
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

    private final RolesAllowedDirectivesHelper rolesAllowedHelper;

    private final Logger logger = Logger.getLogger(OperationCreator.class.getName());

    public OperationCreator(ReferenceCreator referenceCreator, ArgumentCreator argumentCreator) {
        super(referenceCreator);
        this.argumentCreator = argumentCreator;
        this.rolesAllowedHelper = new RolesAllowedDirectivesHelper();
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
        Annotations annotationsForClass = Annotations.getAnnotationsForClass(methodInfo.declaringClass());

        Type fieldType = getReturnType(methodInfo);

        // Name
        String name = getOperationName(methodInfo, operationType, annotationsForMethod);

        // Field Type
        validateFieldType(methodInfo, operationType);

        // Execution
        Execute execute = getExecution(annotationsForMethod, annotationsForClass);

        Reference reference = referenceCreator.createReferenceForOperationField(fieldType, annotationsForMethod);
        Operation operation = new Operation(methodInfo.declaringClass().name().toString(),
                methodInfo.name(),
                MethodHelper.getPropertyName(Direction.OUT, methodInfo.name()),
                name,
                reference,
                operationType,
                execute);
        if (type != null) {
            operation.setSourceFieldOn(new Reference.Builder().reference(type).build());
        }

        // Arguments
        List<Type> parameters = methodInfo.parameterTypes();
        for (short i = 0; i < parameters.size(); i++) {
            Optional<Argument> maybeArgument = argumentCreator.createArgument(operation, methodInfo, i);
            maybeArgument.ifPresent(operation::addArgument);
        }
        addDirectivesForRolesAllowed(annotationsForMethod, annotationsForClass, operation, reference);
        populateField(Direction.OUT, operation, fieldType, annotationsForMethod);

        return operation;
    }

    private static void validateFieldType(MethodInfo methodInfo, OperationType operationType) {
        Type returnType = methodInfo.returnType();
        if (!operationType.equals(OperationType.MUTATION) && returnType.kind().equals(Type.Kind.VOID)) {
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
                Annotations.JAKARTA_JSONB_PROPERTY,
                Annotations.JAVAX_JSONB_PROPERTY,
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

    private Execute getExecution(Annotations annotationsForMethod, Annotations annotationsForClass) {
        // first check annotation on method
        if (annotationsForMethod.containsOneOfTheseAnnotations(Annotations.BLOCKING)) {
            return Execute.BLOCKING;
        } else if (annotationsForMethod.containsOneOfTheseAnnotations(Annotations.NON_BLOCKING)) {
            return Execute.NON_BLOCKING;
        }

        // then check annotation on class
        if (annotationsForClass.containsOneOfTheseAnnotations(Annotations.BLOCKING)) {
            return Execute.BLOCKING;
        } else if (annotationsForClass.containsOneOfTheseAnnotations(Annotations.NON_BLOCKING)) {
            return Execute.NON_BLOCKING;
        }

        // lastly use default based on return type
        return Execute.DEFAULT;
    }

    private void addDirectivesForRolesAllowed(Annotations annotationsForPojo, Annotations classAnnotations, Operation operation,
            Reference parentObjectReference) {
        DirectiveInstance rolesAllowedDirectives = rolesAllowedHelper
                .transformRolesAllowedToDirectives(annotationsForPojo, classAnnotations);
        if (!Objects.isNull(rolesAllowedDirectives)) {
            logger.debug("Adding rolesAllowed directive " + rolesAllowedDirectives + " to method '" + operation.getName()
                    + "' of parent type '" + parentObjectReference.getName() + "'");
            operation.addDirectiveInstance(rolesAllowedDirectives);
        }
    }
}
