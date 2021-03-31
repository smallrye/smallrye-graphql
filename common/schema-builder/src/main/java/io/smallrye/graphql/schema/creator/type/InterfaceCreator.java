package io.smallrye.graphql.schema.creator.type;

import java.util.List;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.ScanningContext;
import io.smallrye.graphql.schema.creator.FieldCreator;
import io.smallrye.graphql.schema.creator.ReferenceCreator;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.Direction;
import io.smallrye.graphql.schema.helper.MethodHelper;
import io.smallrye.graphql.schema.helper.TypeAutoNameStrategy;
import io.smallrye.graphql.schema.helper.TypeNameHelper;
import io.smallrye.graphql.schema.model.InterfaceType;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;

/**
 * This creates an interface object.
 * 
 * The interface object has fields that might reference other types that should still be created.
 * It might also implement some interfaces that should be created.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class InterfaceCreator implements Creator<InterfaceType> {
    private static final Logger LOG = Logger.getLogger(InputTypeCreator.class.getName());

    private final ReferenceCreator referenceCreator;
    private final FieldCreator fieldCreator;
    private final TypeAutoNameStrategy autoNameStrategy;

    public InterfaceCreator(ReferenceCreator referenceCreator, FieldCreator fieldCreator,
            TypeAutoNameStrategy autoNameStrategy) {
        this.referenceCreator = referenceCreator;
        this.fieldCreator = fieldCreator;
        this.autoNameStrategy = autoNameStrategy;
    }

    @Override
    public InterfaceType create(ClassInfo classInfo, Reference reference) {
        LOG.debug("Creating Interface from " + classInfo.name().toString());

        Annotations annotations = Annotations.getAnnotationsForClass(classInfo);

        // Name
        String name = TypeNameHelper.getAnyTypeName(reference, ReferenceType.INTERFACE, classInfo, annotations,
                autoNameStrategy);

        // Description
        String description = DescriptionHelper.getDescriptionForType(annotations).orElse(null);

        InterfaceType interfaceType = new InterfaceType(classInfo.name().toString(), name, description);

        // Fields
        addFields(interfaceType, classInfo, reference);

        // Interfaces
        addInterfaces(interfaceType, classInfo);

        return interfaceType;
    }

    private void addFields(InterfaceType interfaceType, ClassInfo classInfo, Reference reference) {

        // Add all fields from interface itself
        for (MethodInfo methodInfo : classInfo.methods()) {
            if (MethodHelper.isPropertyMethod(Direction.OUT, methodInfo)) {
                fieldCreator.createFieldForInterface(methodInfo, reference)
                        .ifPresent(interfaceType::addField);
            }
        }

        // Also add all fields from all parent interfaces as GraphQL schema requires it
        List<DotName> interfaceNames = classInfo.interfaceNames();
        for (DotName interfaceName : interfaceNames) {
            // Ignore java interfaces (like Serializable)
            if (canAddInterfaceIntoScheme(interfaceName.toString())) {
                ClassInfo c = ScanningContext.getIndex().getClassByName(interfaceName);
                if (c != null) {
                    addFields(interfaceType, c, reference);
                }
            }
        }
    }

    private void addInterfaces(InterfaceType interfaceType, ClassInfo classInfo) {
        List<DotName> interfaceNames = classInfo.interfaceNames();
        for (DotName interfaceName : interfaceNames) {
            // Ignore java interfaces (like Serializable)
            if (canAddInterfaceIntoScheme(interfaceName.toString())) {
                ClassInfo c = ScanningContext.getIndex().getClassByName(interfaceName);
                if (c != null) {
                    Reference interfaceRef = referenceCreator.createReference(Direction.OUT, c);
                    interfaceType.addInterface(interfaceRef);
                    // add all parent interfaces recursively as GraphQL schema requires it
                    addInterfaces(interfaceType, c);
                }
            }
        }
    }

    private static final String JAVA_DOT = "java.";

    /**
     * Check if interface can be added into GraphQL schema, eg. ignore java interfaces (like Serializable)
     * 
     * @param interfaceFullName full name of the interface, including package name
     * @return true if interface can be added
     */
    public static boolean canAddInterfaceIntoScheme(String interfaceFullName) {
        return interfaceFullName != null && !interfaceFullName.startsWith(JAVA_DOT);
    }
}
