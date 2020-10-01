package io.smallrye.graphql.schema.creator.type;

import java.util.ArrayList;
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
import io.smallrye.graphql.schema.helper.TypeNameHelper;
import io.smallrye.graphql.schema.model.InterfaceType;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.TypeAutoNameStrategy;

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
        // Fields
        List<MethodInfo> allMethods = new ArrayList<>();

        // Find all methods up the tree
        for (ClassInfo c = classInfo; c != null; c = ScanningContext.getIndex().getClassByName(c.superName())) {
            if (!c.toString().startsWith(JAVA_DOT)) { // Not java interfaces (like Serializable)
                allMethods.addAll(c.methods());
            }
        }

        for (MethodInfo methodInfo : allMethods) {
            if (MethodHelper.isPropertyMethod(Direction.OUT, methodInfo.name())) {
                fieldCreator.createFieldForInterface(methodInfo, reference)
                        .ifPresent(interfaceType::addField);
            }
        }
    }

    private void addInterfaces(InterfaceType interfaceType, ClassInfo classInfo) {
        List<DotName> interfaceNames = classInfo.interfaceNames();
        for (DotName interfaceName : interfaceNames) {
            // Ignore java interfaces (like Serializable)
            if (!interfaceName.toString().startsWith(JAVA_DOT)) {
                ClassInfo c = ScanningContext.getIndex().getClassByName(interfaceName);
                if (c != null) {
                    Reference interfaceRef = referenceCreator.createReference(Direction.OUT, classInfo);
                    interfaceType.addInterface(interfaceRef);
                }
            }
        }
    }

    private static final String JAVA_DOT = "java.";
}
