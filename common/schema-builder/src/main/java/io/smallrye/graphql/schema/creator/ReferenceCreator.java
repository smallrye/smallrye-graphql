package io.smallrye.graphql.schema.creator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.Classes;
import io.smallrye.graphql.schema.ScanningContext;
import io.smallrye.graphql.schema.SchemaBuilderException;
import io.smallrye.graphql.schema.helper.AdaptToHelper;
import io.smallrye.graphql.schema.helper.AdaptWithHelper;
import io.smallrye.graphql.schema.helper.Direction;
import io.smallrye.graphql.schema.helper.FormatHelper;
import io.smallrye.graphql.schema.helper.TypeAutoNameStrategy;
import io.smallrye.graphql.schema.helper.TypeNameHelper;
import io.smallrye.graphql.schema.model.AdaptTo;
import io.smallrye.graphql.schema.model.AdaptWith;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.Scalars;

/**
 * Here we create references to things that might not yet exist.
 * 
 * We store all references to be created later.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ReferenceCreator {
    private static final Logger LOG = Logger.getLogger(ReferenceCreator.class.getName());

    private final Queue<Reference> inputReferenceQueue = new ArrayDeque<>();
    private final Queue<Reference> typeReferenceQueue = new ArrayDeque<>();
    private final Queue<Reference> enumReferenceQueue = new ArrayDeque<>();
    private final Queue<Reference> interfaceReferenceQueue = new ArrayDeque<>();

    // Some maps we populate during scanning
    private final Map<String, Reference> inputReferenceMap = new HashMap<>();
    private final Map<String, Reference> typeReferenceMap = new HashMap<>();
    private final Map<String, Reference> enumReferenceMap = new HashMap<>();
    private final Map<String, Reference> interfaceReferenceMap = new HashMap<>();

    private final TypeAutoNameStrategy autoNameStrategy;

    public ReferenceCreator(TypeAutoNameStrategy autoNameStrategy) {
        this.autoNameStrategy = autoNameStrategy;
    }

    /**
     * Clear the scanned references. This is done when we created all references and do not need to remember what to
     * scan.
     */
    public void clear() {
        inputReferenceMap.clear();
        typeReferenceMap.clear();
        enumReferenceMap.clear();
        interfaceReferenceMap.clear();

        inputReferenceQueue.clear();
        typeReferenceQueue.clear();
        enumReferenceQueue.clear();
        interfaceReferenceQueue.clear();
    }

    /**
     * Get the values for a certain type
     * 
     * @param referenceType the type
     * @return the references
     */
    public Queue<Reference> values(ReferenceType referenceType) {
        return getReferenceQueue(referenceType);
    }

    /**
     * Get a reference to a field type for an adapter on a field
     * 
     * @param direction the direction
     * @param fieldType the java type
     * @param annotations annotation on this operations method
     * @return a reference to the type
     */
    public Reference createReferenceForAdapter(Direction direction, Type fieldType, Annotations annotations) {
        return getReference(direction, null, fieldType, annotations);
    }

    /**
     * Get a reference to a field type for an operation Direction is OUT on a field (and IN on an argument) In the case
     * of operations, there is no fields (only methods)
     * 
     * @param fieldType the java type
     * @param annotationsForMethod annotation on this operations method
     * @return a reference to the type
     */
    public Reference createReferenceForOperationField(Type fieldType, Annotations annotationsForMethod) {
        return getReference(Direction.OUT, null, fieldType, annotationsForMethod);
    }

    /**
     * Get a reference to a argument type for an operation Direction is IN on an argument (and OUT on a field) In the
     * case of operation, there is no field (only methods)
     * 
     * @param argumentType the java type
     * @param annotationsForThisArgument annotations on this argument
     * @return a reference to the argument
     */
    public Reference createReferenceForOperationArgument(Type argumentType, Annotations annotationsForThisArgument) {
        return getReference(Direction.IN, null, argumentType, annotationsForThisArgument);
    }

    /**
     * Get a reference to a source argument type for an operation Direction is OUT on an argument.
     *
     * @param argumentType the java type
     * @param annotationsForThisArgument annotations on this argument
     * @return a reference to the argument
     */
    public Reference createReferenceForSourceArgument(Type argumentType, Annotations annotationsForThisArgument) {
        return getReference(Direction.OUT, null, argumentType, annotationsForThisArgument);
    }

    /**
     * Get a reference to a field (method response) on an interface
     * 
     * Interfaces is only usable on Type, so the direction in OUT.
     * 
     * @param methodType the method response type
     * @param annotationsForThisMethod annotations on this method
     * @return a reference to the type
     */
    public Reference createReferenceForInterfaceField(Type methodType, Annotations annotationsForThisMethod,
            Reference parentObjectReference) {
        return getReference(Direction.OUT, null, methodType, annotationsForThisMethod, parentObjectReference);
    }

    /**
     * Get a reference to a Field Type for a InputType or Type.
     * 
     * We need both the type and the getter/setter method as both is applicable.
     * 
     * @param direction in or out
     * @param fieldType the field type
     * @param methodType the method type
     * @param annotations the annotations on the field and method
     * @param parentObjectReference Reference of the parent PoJo use so we can evaluate generics types
     * @return a reference to the type
     */
    public Reference createReferenceForPojoField(Direction direction, Type fieldType, Type methodType, Annotations annotations,
            Reference parentObjectReference) {
        return getReference(direction, fieldType, methodType, annotations, parentObjectReference);
    }

    /**
     * This method create a reference to type that might not yet exist. It also store to be created later, if we do not
     * already know about it.
     * 
     * @param direction the direction (in or out)
     * @param classInfo the Java class
     * @return a reference
     */
    public Reference createReference(Direction direction, ClassInfo classInfo) {
        return createReference(direction, classInfo, true, true, null, null, true);
    }

    /**
     * This method create a reference to type that might not yet exist. It also store to be created later, if we do not
     * already know about it.
     * 
     * @param direction the direction (in or out)
     * @param classInfo the Java class
     * @param createAdapedToType create the type in the schema
     * @return a reference
     */
    public Reference createReference(Direction direction, ClassInfo classInfo, boolean createAdapedToType,
            boolean createAdapedWithType,
            Reference parentObjectReference, Map<String, Reference> parametrizedTypeArgumentsReferences,
            boolean addParametrizedTypeNameExtension) {
        // Get the initial reference type. It's either Type or Input depending on the direction. This might change as
        // we figure out this is actually an enum or interface
        ReferenceType referenceType = getCorrectReferenceType(direction);

        Annotations annotationsForClass = Annotations.getAnnotationsForClass(classInfo);

        // Now check if this is an interface or enum
        if (isInterface(classInfo, annotationsForClass)) {
            // Also check that we create all implementations
            Collection<ClassInfo> knownDirectImplementors = ScanningContext.getIndex()
                    .getAllKnownImplementors(classInfo.name());
            for (ClassInfo impl : knownDirectImplementors) {
                // TODO: First check the class annotations for @Type, if we get one that has that, use it, else any/all
                // ?

                // translate parametrizedTypeArgumentsReferences to match class implementing interface
                Map<String, Reference> parametrizedTypeArgumentsReferencesImpl = null;
                if (!classInfo.typeParameters().isEmpty()) {
                    ParameterizedType interfaceType = null;
                    for (Type it : impl.interfaceTypes()) {
                        if (it.name().equals(classInfo.name())) {
                            interfaceType = it.asParameterizedType();
                        }
                    }
                    parametrizedTypeArgumentsReferencesImpl = new HashMap<>();
                    int i = 0;
                    for (TypeVariable tp : classInfo.typeParameters()) {
                        Type type = interfaceType.arguments().get(i++);
                        if (type.kind() == Type.Kind.TYPE_VARIABLE) {
                            parametrizedTypeArgumentsReferencesImpl.put(
                                    type.asTypeVariable().identifier(),
                                    parametrizedTypeArgumentsReferences.get(tp.identifier()));
                        }
                    }

                }

                createReference(direction, impl, createAdapedToType, createAdapedWithType, parentObjectReference,
                        parametrizedTypeArgumentsReferencesImpl,
                        true);
            }
            referenceType = ReferenceType.INTERFACE;
        } else if (Classes.isEnum(classInfo)) {
            referenceType = ReferenceType.ENUM;
        }

        // Now we should have the correct reference type.
        String className = classInfo.name().toString();

        String name = TypeNameHelper.getAnyTypeName(
                addParametrizedTypeNameExtension
                        ? TypeNameHelper.createParametrizedTypeNameExtension(parametrizedTypeArgumentsReferences)
                        : null,
                referenceType, classInfo, annotationsForClass,
                this.autoNameStrategy);

        Reference reference = new Reference(className, name, referenceType, parametrizedTypeArgumentsReferences,
                addParametrizedTypeNameExtension);

        // Adaptation
        Optional<AdaptTo> adaptTo = AdaptToHelper.getAdaptTo(reference, annotationsForClass);
        reference.setAdaptTo(adaptTo.orElse(null));

        Optional<AdaptWith> adaptWith = AdaptWithHelper.getAdaptWith(direction, this, reference, annotationsForClass);
        reference.setAdaptWith(adaptWith.orElse(null));

        // Now add it to the correct map
        boolean shouldCreateAdapedToType = AdaptToHelper.shouldCreateTypeInSchema(annotationsForClass);
        boolean shouldCreateAdapedWithType = AdaptWithHelper.shouldCreateTypeInSchema(annotationsForClass);

        // We ignore the field that is being adapted
        if (shouldCreateAdapedToType && createAdapedToType && shouldCreateAdapedWithType && createAdapedWithType) {
            putIfAbsent(name, reference, referenceType);
        }
        return reference;
    }

    public TypeAutoNameStrategy getTypeAutoNameStrategy() {
        return this.autoNameStrategy;
    }

    private boolean isInterface(ClassInfo classInfo, Annotations annotationsForClass) {
        boolean isJavaInterface = Classes.isInterface(classInfo);
        if (isJavaInterface) {
            if (annotationsForClass.containsOneOfTheseAnnotations(Annotations.TYPE, Annotations.INPUT)) {
                // This should be mapped to a type/input and not an interface
                return false;
            }
            return true;
        }
        return false;
    }

    private Reference getReference(Direction direction,
            Type fieldType,
            Type methodType,
            Annotations annotations) {
        return getReference(direction, fieldType, methodType, annotations, null);
    }

    private Reference getReference(Direction direction,
            Type fieldType,
            Type methodType,
            Annotations annotations,
            Reference parentObjectReference) {

        // In some case, like operations and interfaces, there is no fieldType
        if (fieldType == null) {
            fieldType = methodType;
        }

        String fieldTypeName = fieldType.name().toString();

        if (annotations != null && annotations.containsOneOfTheseAnnotations(Annotations.ID)) {
            // ID
            return Scalars.getIDScalar(fieldTypeName);
        } else if (Scalars.isScalar(fieldTypeName)) {
            // Scalar
            if (FormatHelper.hasAnyFormatting(annotations)) {
                return Scalars.getFormattedScalar(fieldTypeName);
            }
            return Scalars.getScalar(fieldTypeName);
        } else if (fieldType.kind().equals(Type.Kind.ARRAY)) {
            // Java Array
            Type typeInArray = fieldType.asArrayType().component();
            Type typeInMethodArray = methodType.asArrayType().component();
            return getReference(direction, typeInArray, typeInMethodArray, annotations, parentObjectReference);
        } else if (Classes.isCollection(fieldType) || Classes.isUnwrappedType(fieldType)) {
            // Collections and unwrapped types
            Type typeInCollection = fieldType.asParameterizedType().arguments().get(0);
            Type typeInMethodCollection = methodType.asParameterizedType().arguments().get(0);
            return getReference(direction, typeInCollection, typeInMethodCollection, annotations, parentObjectReference);
        } else if (Classes.isMap(fieldType)) {
            ParameterizedType parameterizedFieldType = fieldType.asParameterizedType();
            List<Type> fieldArguments = parameterizedFieldType.arguments();
            ParameterizedType entryType = ParameterizedType.create(Classes.ENTRY, fieldArguments.toArray(new Type[] {}), null);
            return getReference(direction, entryType, entryType, annotations, parentObjectReference);
        } else if (fieldType.kind().equals(Type.Kind.CLASS)) {
            ClassInfo classInfo = ScanningContext.getIndex().getClassByName(fieldType.name());
            if (classInfo != null) {

                Map<String, Reference> parametrizedTypeArgumentsReferences = null;

                ParameterizedType parametrizedParentType = findParametrizedParentType(classInfo);
                if (parametrizedParentType != null) {
                    ClassInfo ci = ScanningContext.getIndex().getClassByName(parametrizedParentType.name());
                    if (ci == null) {
                        throw new SchemaBuilderException(
                                "No class info found for parametrizedParentType name [" + parametrizedParentType.name() + "]");
                    }

                    parametrizedTypeArgumentsReferences = collectParametrizedTypes(ci, parametrizedParentType.arguments(),
                            direction, parentObjectReference);
                }

                boolean shouldCreateAdapedToType = AdaptToHelper.shouldCreateTypeInSchema(annotations);
                boolean shouldCreateAdapedWithType = AdaptWithHelper.shouldCreateTypeInSchema(annotations);
                return createReference(direction, classInfo, shouldCreateAdapedToType, shouldCreateAdapedWithType,
                        parentObjectReference,
                        parametrizedTypeArgumentsReferences, false);
            } else {
                return getNonIndexedReference(direction, fieldType);
            }
        } else if (fieldType.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
            // Type.Kind.PARAMETERIZED_TYPE handles generics PoJos here, collections and unwrapped types are catched
            // before.
            // We have to add parametrized types into returned reference object name, and also store parameter types
            // into reference so they can be used for later processing of types
            ClassInfo classInfo = ScanningContext.getIndex().getClassByName(fieldType.name());
            if (classInfo != null) {

                List<Type> parametrizedTypeArguments = fieldType.asParameterizedType().arguments();
                Map<String, Reference> parametrizedTypeArgumentsReferences = collectParametrizedTypes(classInfo,
                        parametrizedTypeArguments, direction, parentObjectReference);

                boolean shouldCreateAdapedToType = AdaptToHelper.shouldCreateTypeInSchema(annotations);
                boolean shouldCreateAdapedWithType = AdaptWithHelper.shouldCreateTypeInSchema(annotations);
                return createReference(direction, classInfo, shouldCreateAdapedToType, shouldCreateAdapedWithType,
                        parentObjectReference,
                        parametrizedTypeArgumentsReferences, true);
            } else {
                return getNonIndexedReference(direction, fieldType);
            }
        } else if (fieldType.kind().equals(Type.Kind.TYPE_VARIABLE)) {
            if (parentObjectReference == null || parentObjectReference.getParametrizedTypeArguments() == null) {
                throw new SchemaBuilderException("Don't know what to do with [" + fieldType + "] of kind [" + fieldType.kind()
                        + "] as parent object reference is missing or incomplete: " + parentObjectReference);
            }

            LOG.debug("Type variable: " + fieldType.asTypeVariable().name() + " identifier: "
                    + fieldType.asTypeVariable().identifier());

            Reference ret = parentObjectReference.getParametrizedTypeArguments().get(fieldType.asTypeVariable().identifier());

            if (ret == null) {
                throw new SchemaBuilderException("Don't know what to do with [" + fieldType + "] of kind [" + fieldType.kind()
                        + "] as parent object reference doesn't contain necessary info: " + parentObjectReference);
            }

            return ret;
        } else {
            throw new SchemaBuilderException(
                    "Don't know what to do with [" + fieldType + "] of kind [" + fieldType.kind() + "]");
        }
    }

    public Map<String, Reference> collectParametrizedTypes(ClassInfo classInfo, List<? extends Type> parametrizedTypeArguments,
            Direction direction, Reference parentObjectReference) {
        Map<String, Reference> parametrizedTypeArgumentsReferences = null;
        if (parametrizedTypeArguments != null) {
            List<TypeVariable> tvl = new ArrayList<>();
            collectTypeVariables(tvl, classInfo);
            parametrizedTypeArgumentsReferences = new LinkedHashMap<>();
            int i = 0;
            for (Type pat : parametrizedTypeArguments) {
                if (i >= tvl.size()) {
                    throw new SchemaBuilderException(
                            "List of type variables is not correct for class " + classInfo + " and generics argument " + pat);
                } else {
                    parametrizedTypeArgumentsReferences.put(tvl.get(i++).identifier(),
                            getReference(direction, pat, null, null, parentObjectReference));
                }
            }
        }
        return parametrizedTypeArgumentsReferences;
    }

    private void collectTypeVariables(List<TypeVariable> tvl, ClassInfo classInfo) {
        if (classInfo == null)
            return;
        if (classInfo.typeParameters() != null) {
            tvl.addAll(classInfo.typeParameters());
        }
        if (classInfo.superClassType() != null) {
            collectTypeVariables(tvl, ScanningContext.getIndex().getClassByName(classInfo.superName()));
        }
    }

    public ParameterizedType findParametrizedParentType(ClassInfo classInfo) {
        if (classInfo != null && classInfo.superClassType() != null && !Classes.isEnum(classInfo)) {
            if (classInfo.superClassType().kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
                return classInfo.superClassType().asParameterizedType();
            }
            return findParametrizedParentType(ScanningContext.getIndex().getClassByName(classInfo.superName()));
        }
        return null;
    }

    private void putIfAbsent(String key, Reference reference, ReferenceType referenceType) {
        Map<String, Reference> map = getReferenceMap(referenceType);
        Queue<Reference> queue = getReferenceQueue(referenceType);
        if (map != null && queue != null) {
            if (!map.containsKey(key)) {
                map.put(key, reference);
                queue.add(reference);
            } else {
                String existingClass = map.get(key).getClassName();
                String newClass = reference.getClassName();
                if (!existingClass.equals(newClass)) {
                    throw new SchemaBuilderException(
                            "Classes " + existingClass + " and " + newClass + " map to the same GraphQL type '" + key + "', "
                                    + "consider using the @Name annotation or a different naming strategy to distinguish between them");
                }
            }
        }
    }

    private Map<String, Reference> getReferenceMap(ReferenceType referenceType) {
        switch (referenceType) {
            case ENUM:
                return enumReferenceMap;
            case INPUT:
                return inputReferenceMap;
            case INTERFACE:
                return interfaceReferenceMap;
            case TYPE:
                return typeReferenceMap;
            default:
                return null;
        }
    }

    private Queue<Reference> getReferenceQueue(ReferenceType referenceType) {
        switch (referenceType) {
            case ENUM:
                return enumReferenceQueue;
            case INPUT:
                return inputReferenceQueue;
            case INTERFACE:
                return interfaceReferenceQueue;
            case TYPE:
                return typeReferenceQueue;
            default:
                return null;
        }
    }

    private static ReferenceType getCorrectReferenceType(Direction direction) {
        if (direction.equals(Direction.IN)) {
            return ReferenceType.INPUT;
        } else {
            return ReferenceType.TYPE;
        }
    }

    private Reference getNonIndexedReference(Direction direction, Type fieldType) {

        // If this is an unknown Wrapper, throw an exception
        if (fieldType.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {

            if (direction.equals(Direction.IN)) {
                throw new IllegalArgumentException(
                        "Invalid parameter type [" + fieldType.name().toString() + "]");
            } else {
                throw new IllegalArgumentException(
                        "Invalid return type [" + fieldType.name().toString() + "]");
            }
        }

        LOG.warn("Class [" + fieldType.name()
                + "] is not indexed in Jandex. Can not scan Object Type, might not be mapped correctly. Kind = ["
                + fieldType.kind() + "]");

        Reference r = new Reference();
        r.setClassName(fieldType.name().toString());
        r.setGraphQlClassName(fieldType.name().toString());
        r.setName(fieldType.name().local());

        boolean isNumber = Classes.isNumberLikeTypeOrContainedIn(fieldType);
        boolean isDate = Classes.isDateLikeTypeOrContainedIn(fieldType);
        if (isNumber || isDate) {
            r.setType(ReferenceType.SCALAR);
        } else if (direction.equals(Direction.IN)) {
            r.setType(ReferenceType.INPUT);
        } else {
            r.setType(ReferenceType.TYPE);
        }
        return r;
    }
}
