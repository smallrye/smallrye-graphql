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
import io.smallrye.graphql.schema.creator.OperationCreator;
import io.smallrye.graphql.schema.creator.ReferenceCreator;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.Direction;
import io.smallrye.graphql.schema.helper.Directives;
import io.smallrye.graphql.schema.helper.SourceOperationHelper;
import io.smallrye.graphql.schema.helper.TypeAutoNameStrategy;
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
    private final TypeAutoNameStrategy autoNameStrategy;
    private Directives directives;

    protected AbstractCreator(OperationCreator operationCreator, ReferenceCreator referenceCreator,
            TypeAutoNameStrategy autoNameStrategy) {
        this.operationCreator = operationCreator;
        this.referenceCreator = referenceCreator;
        this.autoNameStrategy = autoNameStrategy;
    }

    public void setDirectives(Directives directives) {
        this.directives = directives;
    }

    protected abstract ReferenceType referenceType();

    @Override
    public Type create(ClassInfo classInfo, Reference reference) {
        LOG.debug("Creating Interface from " + classInfo.name().toString());

        Annotations annotations = Annotations.getAnnotationsForClass(classInfo);

        // Name
        String name = TypeNameHelper.getAnyTypeName(reference, referenceType(), classInfo, annotations,
                autoNameStrategy);

        // Description
        String description = DescriptionHelper.getDescriptionForType(annotations).orElse(null);

        Type type = new Type(classInfo.name().toString(), name, description);
        type.setIsInterface(referenceType() == ReferenceType.INTERFACE);

        // Fields
        addFields(type, classInfo, reference);

        // Interfaces
        addInterfaces(type, classInfo, reference);

        // Operations
        addOperations(type, classInfo);

        // Directives
        addDirectives(type, classInfo);

        return type;
    }

    protected abstract void addFields(Type type, ClassInfo classInfo, Reference reference);

    private void addDirectives(Type type, ClassInfo classInfo) {
        type.setDirectiveInstances(directives.buildDirectiveInstances(classInfo::classAnnotation));
    }

    private void addInterfaces(Type type, ClassInfo classInfo, Reference reference) {
        List<org.jboss.jandex.Type> interfaceNames = classInfo.interfaceTypes();
        for (org.jboss.jandex.Type interfaceType : interfaceNames) {
            // Ignore java interfaces (like Serializable)
            if (InterfaceCreator.canAddInterfaceIntoScheme(interfaceType.name().toString())) {
                ClassInfo interfaceInfo = ScanningContext.getIndex().getClassByName(interfaceType.name());
                if (interfaceInfo != null) {

                    Map<String, Reference> parametrizedTypeArgumentsReferences = null;

                    if (interfaceType.kind().equals(org.jboss.jandex.Type.Kind.PARAMETERIZED_TYPE))
                        parametrizedTypeArgumentsReferences = referenceCreator.collectParametrizedTypes(interfaceInfo,
                                interfaceType.asParameterizedType().arguments(), Direction.OUT, reference);

                    Reference interfaceRef = referenceCreator.createReference(Direction.OUT, interfaceInfo, true, reference,
                            parametrizedTypeArgumentsReferences, true);
                    type.addInterface(interfaceRef);
                    // add all parent interfaces recursively as GraphQL schema requires it
                    addInterfaces(type, interfaceInfo, reference);
                }
            }
        }
    }

    protected void addOperations(Type type, ClassInfo classInfo) {
        SourceOperationHelper sourceOperationHelper = new SourceOperationHelper();
        Map<DotName, List<MethodParameterInfo>> sourceFields = sourceOperationHelper.getSourceAnnotations();
        Map<DotName, List<MethodParameterInfo>> batchedFields = sourceOperationHelper.getSourceListAnnotations();
        type.setOperations(toOperations(sourceFields, type, classInfo));
        type.setBatchOperations(toOperations(batchedFields, type, classInfo));
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
        return operations;
    }
}
