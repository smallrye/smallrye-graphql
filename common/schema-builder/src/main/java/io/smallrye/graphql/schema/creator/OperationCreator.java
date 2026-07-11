package io.smallrye.graphql.schema.creator;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.Classes;
import io.smallrye.graphql.schema.SchemaBuilderException;
import io.smallrye.graphql.schema.helper.DeprecatedDirectivesHelper;
import io.smallrye.graphql.schema.helper.Direction;
import io.smallrye.graphql.schema.helper.MethodHelper;
import io.smallrye.graphql.schema.helper.RolesAllowedDirectivesHelper;
import io.smallrye.graphql.schema.model.Argument;
import io.smallrye.graphql.schema.model.Execute;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.OperationType;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.Wrapper;
import kotlin.metadata.Attributes;
import kotlin.metadata.KmClassifier;
import kotlin.metadata.KmFunction;
import kotlin.metadata.KmType;
import kotlin.metadata.KmTypeProjection;
import kotlin.metadata.KmValueParameter;
import kotlin.metadata.jvm.KotlinClassHeader;
import kotlin.metadata.jvm.KotlinClassMetadata;

/**
 * Creates a Operation object
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class OperationCreator extends ModelCreator {

    private final ArgumentCreator argumentCreator;

    private final RolesAllowedDirectivesHelper rolesAllowedHelper;
    private final DeprecatedDirectivesHelper deprecatedHelper;

    private final Logger logger = Logger.getLogger(OperationCreator.class.getName());

    public OperationCreator(ReferenceCreator referenceCreator, ArgumentCreator argumentCreator) {
        super(referenceCreator);
        this.argumentCreator = argumentCreator;
        this.rolesAllowedHelper = new RolesAllowedDirectivesHelper();
        this.deprecatedHelper = new DeprecatedDirectivesHelper();
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
        addDirectiveForDeprecated(annotationsForMethod, operation);
        populateField(Direction.OUT, operation, fieldType, annotationsForMethod);

        checkWrappedTypeKotlinNullability(methodInfo, annotationsForClass, operation);
        return operation;
    }

    // If the operation return type is a wrapper and is written in Kotlin,
    // this checks whether the wrapped type is nullable.
    // Nullability metadata is stored in the kotlin.Metadata annotation
    // on the class that contains the operation.
    private void checkWrappedTypeKotlinNullability(MethodInfo methodInfo,
            Annotations annotationsForClass,
            Operation operation) {
        Optional<AnnotationInstance> kotlinMetadataAnnotation = annotationsForClass
                .getOneOfTheseAnnotations(Annotations.KOTLIN_METADATA);
        if (kotlinMetadataAnnotation.isPresent()) {
            KotlinClassMetadata.Class kotlinClass = toKotlinClassMetadata(kotlinMetadataAnnotation.get());
            // We need to find the corresponding function inside
            // the KotlinClassMetadata to check its IS_NULLABLE flag.
            Optional<KmFunction> function = kotlinClass.getKmClass().getFunctions()
                    .stream()
                    .filter(f -> f.getName().equals(methodInfo.name()))
                    .filter(f -> compareParameterLists(f.getValueParameters(), methodInfo.parameterTypes()))
                    .findAny();
            if (function.isPresent()) {
                // handle return type
                if (operation.hasWrapper()) {
                    applyKotlinWrapperNullability(operation, operation.getWrapper(),
                            function.get().getReturnType(), true);
                }

                // handle arguments
                List<Argument> arguments = operation.getArguments();
                List<KmValueParameter> valueParameters = function.get().getValueParameters();
                if (arguments.size() == valueParameters.size()) {
                    for (int i = 0; i < arguments.size(); i++) {
                        Argument argument = arguments.get(i);
                        if (argument.hasWrapper()) {
                            // Arguments cannot be async wrappers (Uni etc.), so only collection-like
                            // wrappers may control the argument's own nullability.
                            applyKotlinWrapperNullability(argument, argument.getWrapper(),
                                    valueParameters.get(i).getType(),
                                    argument.getWrapper().isCollectionOrArrayOrMap());
                        }
                    }
                }
            }
        }
    }

    /**
     * Apply Kotlin nullability metadata to the whole wrapper chain of a field.
     * <p>
     * For a plain {@code List<String>}, the collection wrapper's wrapped-type nullability comes from
     * {@code String}. For {@code Uni<List<String>>}, the async wrapper's type-argument nullability
     * controls the field/operation nullability, and the nested collection wrapper still needs the
     * list-item nullability from {@code String}.
     *
     * @param field the field/operation/argument being configured
     * @param wrapper the current wrapper in the chain
     * @param kotlinType the Kotlin type corresponding to that wrapper
     * @param controlsFieldNullability whether this level should update {@code field.notNull}
     */
    private static void applyKotlinWrapperNullability(Field field,
            Wrapper wrapper,
            KmType kotlinType,
            boolean controlsFieldNullability) {
        if (wrapper == null || kotlinType == null || kotlinType.getArguments().isEmpty()) {
            return;
        }
        KmTypeProjection projection = kotlinType.getArguments().get(0);
        if (projection.getType() == null) {
            // star projection carries no nullability information
            return;
        }
        KmType argType = projection.getType();
        boolean argNullable = Attributes.isNullable(argType);

        if (wrapper.isCollectionOrArrayOrMap()) {
            wrapper.setWrappedTypeNotNull(!argNullable);
            // workaround: consistent behavior to java: if wrapped type is non null, collection will be marked as non null
            if (controlsFieldNullability && !argNullable && !field.isNotNull()) {
                field.setNotNull(true);
            }
        } else if (controlsFieldNullability && argNullable) {
            // Uni, CompletionStage, etc.: nullability of the immediate wrapped type maps to the GraphQL field
            field.setNotNull(false);
        }

        if (wrapper.hasWrapper()) {
            // Nested wrappers (e.g. List inside Uni) must still receive item nullability, but must not
            // override field nullability already decided by an outer async wrapper.
            applyKotlinWrapperNullability(field, wrapper.getWrapper(), argType, false);
        }
    }

    private boolean compareParameterLists(List<KmValueParameter> kotlinParameters,
            List<Type> jandexParameters) {
        if (kotlinParameters.size() != jandexParameters.size()) {
            return false;
        }
        for (int i = 0; i < kotlinParameters.size(); i++) {
            KmType kotlinType = kotlinParameters.get(i).getType();
            Type jandexType = jandexParameters.get(i);
            if (!compareJavaAndKotlinType(jandexType, kotlinType)) {
                return false;
            }
        }
        return true;
    }

    private boolean compareJavaAndKotlinType(Type javaType, kotlin.metadata.KmType kotlinType) {
        if (kotlinType == null) {
            return false;
        }
        String kotlinTypeName = ((KmClassifier.Class) kotlinType.classifier).getName().replace("/", ".");

        boolean signatureIsEqual = false;
        if (kotlinTypeName.equals(javaType.name().toString())) {
            signatureIsEqual = true;
        } else if (Classes.isCollection(javaType)) {
            signatureIsEqual = compareKotlinCollectionType(javaType, kotlinType);
        }
        // TODO: the matching of parameter types could use some improvements
        // TODO: only some cases are handled, there are much more: see https://kotlinlang.org/docs/java-interop.html#mapped-types

        boolean parametersAreEqual = true;
        if (javaType instanceof ParameterizedType) {
            List<Type> arguments = javaType.asParameterizedType().arguments();
            for (int i = 0; i < arguments.size(); i++) {
                Type javaArg = arguments.get(i);
                KmType kotlinArg = kotlinType.getArguments().get(i).getType();

                boolean argTypeEqual = compareJavaAndKotlinType(javaArg, kotlinArg);
                if (!argTypeEqual) {
                    parametersAreEqual = false;
                    break;
                }
            }
        }

        return signatureIsEqual && parametersAreEqual;
    }

    private boolean compareKotlinCollectionType(Type javaType, KmType kotlinType) {
        String javaCollectionType = javaType.name().toString();
        String kotlinCollectionType = ((KmClassifier.Class) kotlinType.classifier).getName().replace("/", ".");
        // TODO: naive implementation.
        if (Collection.class.getName().equals(javaCollectionType)) {
            return kotlinCollectionType.equals("kotlin.collections.Collection") ||
                    kotlinCollectionType.equals("kotlin.collections.MutableCollection");
        }
        if (List.class.getName().equals(javaCollectionType)) {
            return kotlinCollectionType.equals("kotlin.collections.List") ||
                    kotlinCollectionType.equals("kotlin.collections.MutableList");
        }
        if (Set.class.getName().equals(javaCollectionType)) {
            return kotlinCollectionType.equals("kotlin.collections.Set") ||
                    kotlinCollectionType.equals("kotlin.collections.MutableSet");
        }
        if (Iterable.class.getName().equals(javaCollectionType)) {
            return kotlinCollectionType.equals("kotlin.collections.Iterable") ||
                    kotlinCollectionType.equals("kotlin.collections.MutableIterable");
        }
        return false;
    }

    private KotlinClassMetadata.Class toKotlinClassMetadata(AnnotationInstance metadata) {
        KotlinClassHeader classHeader = new KotlinClassHeader(
                metadata.value("k").asInt(),
                metadata.value("mv").asIntArray(),
                metadata.value("d1").asStringArray(),
                metadata.value("d2").asStringArray(),
                metadata.value("xs") != null ? metadata.value("xs").asString() : null,
                metadata.value("pn") != null ? metadata.value("pn").asString() : null,
                metadata.value("xi").asInt());
        return (KotlinClassMetadata.Class) KotlinClassMetadata.readStrict(classHeader);
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
            case RESOLVER:
                return Annotations.RESOLVER;
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
        } else if (annotationsForMethod.containsOneOfTheseAnnotations(Annotations.RUN_ON_VIRTUAL_THREAD)) {
            return Execute.RUN_ON_VIRTUAL_THREAD;
        }

        // then check annotation on class
        if (annotationsForClass.containsOneOfTheseAnnotations(Annotations.BLOCKING)) {
            return Execute.BLOCKING;
        } else if (annotationsForClass.containsOneOfTheseAnnotations(Annotations.NON_BLOCKING)) {
            return Execute.NON_BLOCKING;
        } else if (annotationsForClass.containsOneOfTheseAnnotations(Annotations.RUN_ON_VIRTUAL_THREAD)) {
            return Execute.RUN_ON_VIRTUAL_THREAD;
        }

        // lastly use default based on return type
        return Execute.DEFAULT;
    }

    private void addDirectivesForRolesAllowed(Annotations annotationsForOperation, Annotations classAnnotations,
            Operation operation,
            Reference parentObjectReference) {
        rolesAllowedHelper
                .transformRolesAllowedToDirectives(annotationsForOperation, classAnnotations)
                .ifPresent(rolesAllowedDirective -> {
                    logger.debug("Adding rolesAllowed directive " + rolesAllowedDirective + " to method '" + operation.getName()
                            + "'");
                    operation.addDirectiveInstance(rolesAllowedDirective);
                });
    }

    private void addDirectiveForDeprecated(Annotations annotationsForOperation, Operation operation) {
        if (deprecatedHelper != null && directives != null) {
            deprecatedHelper
                    .transformDeprecatedToDirective(annotationsForOperation,
                            directives.getDirectiveTypes().get(DotName.createSimple("io.smallrye.graphql.api.Deprecated")))
                    .ifPresent(deprecatedDirective -> {
                        logger.debug("Adding deprecated directive " + deprecatedDirective + " to method '" + operation.getName()
                                + "'");
                        operation.addDirectiveInstance(deprecatedDirective);
                    });
        }
    }

    @Override
    public String getDirectiveLocation() {
        return "FIELD_DEFINITION";
    }
}
