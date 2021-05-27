package io.smallrye.graphql.schema.creator.type;

import java.util.List;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;

import io.smallrye.graphql.schema.ScanningContext;
import io.smallrye.graphql.schema.creator.FieldCreator;
import io.smallrye.graphql.schema.creator.OperationCreator;
import io.smallrye.graphql.schema.creator.ReferenceCreator;
import io.smallrye.graphql.schema.helper.Direction;
import io.smallrye.graphql.schema.helper.MethodHelper;
import io.smallrye.graphql.schema.helper.TypeAutoNameStrategy;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.Type;

/**
 * This creates an interface object.
 * <p>
 * The interface object has fields that might reference other types that should still be created.
 * It might also implement some interfaces that should be created.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class InterfaceCreator extends AbstractCreator {

    private final FieldCreator fieldCreator;

    public InterfaceCreator(ReferenceCreator referenceCreator, FieldCreator fieldCreator,
            TypeAutoNameStrategy autoNameStrategy, OperationCreator operationCreator) {
        super(operationCreator, referenceCreator, autoNameStrategy);
        this.fieldCreator = fieldCreator;
    }

    protected void addFields(Type interfaceType, ClassInfo classInfo, Reference reference) {

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

    @Override
    protected ReferenceType referenceType() {
        return ReferenceType.INTERFACE;
    }

}
