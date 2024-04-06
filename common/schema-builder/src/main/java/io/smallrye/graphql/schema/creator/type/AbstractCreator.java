package io.smallrye.graphql.schema.creator.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.ScanningContext;
import io.smallrye.graphql.schema.SchemaBuilderException;
import io.smallrye.graphql.schema.creator.OperationCreator;
import io.smallrye.graphql.schema.creator.ReferenceCreator;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.Directives;
import io.smallrye.graphql.schema.helper.SourceOperationHelper;
import io.smallrye.graphql.schema.helper.TypeNameHelper;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.OperationType;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.Type;

/**
 * Abstract superclass for {@link TypeCreator} and {@link InterfaceCreator}, since both are almost equal.
 */
abstract class AbstractCreator implements Creator<Type> {
    private static final Logger LOG = Logger.getLogger(AbstractCreator.class.getName());

    private final OperationCreator operationCreator;
    private final ReferenceCreator referenceCreator;
    private Directives directives;

    protected AbstractCreator(OperationCreator operationCreator, ReferenceCreator referenceCreator) {
        this.operationCreator = operationCreator;
        this.referenceCreator = referenceCreator;
    }

    public void setDirectives(Directives directives) {
        this.directives = directives;
    }

    protected abstract ReferenceType referenceType();

    @Override
    public Type create(ClassInfo classInfo, Reference reference) {
        LOG.debug("Creating from " + classInfo.name().toString());

        Annotations annotations = Annotations.getAnnotationsForClass(classInfo);

        // Name
        String name = TypeNameHelper.getAnyTypeName(classInfo,
                annotations,
                referenceCreator.getTypeAutoNameStrategy(),
                referenceType(),
                reference.getClassParametrizedTypes());

        // Description
        String description = DescriptionHelper.getDescriptionForType(annotations).orElse(null);

        Type type = new Type(classInfo.name().toString(), name, description);
        type.setIsInterface(referenceType() == ReferenceType.INTERFACE);

        // Fields
        addFields(type, classInfo, reference);

        // Interfaces/Unions
        addPolymorphicTypes(type, classInfo, reference);

        // Operations
        addOperations(type, classInfo);

        // Directives
        addDirectives(type, annotations, getDirectiveLocation(), type.getClassName());
        return type;
    }

    protected abstract void addFields(Type type, ClassInfo classInfo, Reference reference);

    private void addDirectives(Type type, Annotations annotations, String directiveLocation, String referenceName) {
        type.setDirectiveInstances(directives.buildDirectiveInstances(annotations, directiveLocation, referenceName));
    }

    private void addPolymorphicTypes(Type type, ClassInfo classInfo, Reference reference) {
        List<org.jboss.jandex.Type> interfaceNames = classInfo.interfaceTypes();
        for (org.jboss.jandex.Type interfaceType : interfaceNames) {
            String interfaceFullName = interfaceType.name().toString();
            // Ignore java interfaces (like Serializable)
            // TODO: should this check be renamed now that it is used for both union and interface checks?
            if (InterfaceCreator.canAddInterfaceIntoScheme(interfaceFullName)) {
                ClassInfo interfaceInfo = ScanningContext.getIndex().getClassByName(interfaceType.name());
                if (interfaceInfo != null) {
                    Annotations annotationsForInterface = Annotations.getAnnotationsForClass(interfaceInfo);
                    Reference interfaceRef = referenceCreator.createReferenceForInterfaceField(interfaceType,
                            annotationsForInterface, reference);
                    if (annotationsForInterface.containsOneOfTheseAnnotations(Annotations.UNION)) {
                        type.addUnion(interfaceRef);
                    } else {
                        type.addInterface(interfaceRef);
                        // add all parent interfaces recursively as GraphQL schema requires it
                        addPolymorphicTypes(type, interfaceInfo, reference);
                    }
                }
            }
        }
    }

    protected void addOperations(Type type, ClassInfo classInfo) {
        SourceOperationHelper sourceOperationHelper = new SourceOperationHelper();
        Map<DotName, List<MethodParameterInfo>> sourceFields = sourceOperationHelper.getSourceAnnotations();
        Map<DotName, List<MethodParameterInfo>> batchedFields = sourceOperationHelper.getSourceListAnnotations();
        type.setOperations(validateOperationNames(toOperations(sourceFields, type, classInfo), type));
        type.setBatchOperations(validateOperationNames(toOperations(batchedFields, type, classInfo), type));
    }

    protected Map<String, Operation> toOperations(Map<DotName, List<MethodParameterInfo>> sourceFields, Type type,
            ClassInfo classInfo) {
        // See if there is source operations for this class
        Map<String, Operation> operations = new HashMap<>();
        if (sourceFields.containsKey(classInfo.name())) {
            List<MethodParameterInfo> methodParameterInfos = sourceFields.get(classInfo.name());
            for (MethodParameterInfo methodParameterInfo : methodParameterInfos) {
                MethodInfo methodInfo = methodParameterInfo.method();
                Operation o = operationCreator.createOperation(methodInfo, OperationType.QUERY, type);
                operations.put(o.getName(), o);
            }
        }
        for (Reference anInterface : type.getInterfaces()) {
            final String className = anInterface.getClassName();
            if (sourceFields.containsKey(DotName.createSimple(className))) {
                List<MethodParameterInfo> methodParameterInfos = sourceFields.get(DotName.createSimple(className));
                for (MethodParameterInfo methodParameterInfo : methodParameterInfos) {
                    MethodInfo methodInfo = methodParameterInfo.method();
                    Operation o = operationCreator.createOperation(methodInfo, OperationType.QUERY, type);
                    operations.put(o.getName(), o);
                }
            }
        }
        for (Reference u : type.getUnionMemberships()) {
            String className = u.getClassName();
            if (sourceFields.containsKey(DotName.createSimple(className))) {
                List<MethodParameterInfo> methodParameterInfos = sourceFields.get(DotName.createSimple(className));
                for (MethodParameterInfo methodParameterInfo : methodParameterInfos) {
                    MethodInfo methodInfo = methodParameterInfo.method();
                    Operation o = operationCreator.createOperation(methodInfo, OperationType.QUERY, type);
                    operations.put(o.getName(), o);
                }
            }
        }
        return operations;
    }

    /**
     * Validates if the source field names are not already present as a type's fields.
     * Make sure that the type's fields are already scanned.
     *
     */
    private Map<String, Operation> validateOperationNames(Map<String, Operation> operations, Type type) {
        operations.keySet().forEach(fieldName -> {
            if (type.getFields().keySet().contains(fieldName)) {
                throw new SchemaBuilderException(String.format("Type '%s' already contains field named '%s'" +
                        " so source field, with the same name, cannot be applied", type.getName(), fieldName));
            }
        });
        return operations;
    }
}
