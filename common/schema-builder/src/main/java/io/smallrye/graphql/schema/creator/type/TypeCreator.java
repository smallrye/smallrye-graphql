package io.smallrye.graphql.schema.creator.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;

import io.smallrye.graphql.schema.Classes;
import io.smallrye.graphql.schema.ScanningContext;
import io.smallrye.graphql.schema.creator.FieldCreator;
import io.smallrye.graphql.schema.creator.OperationCreator;
import io.smallrye.graphql.schema.creator.ReferenceCreator;
import io.smallrye.graphql.schema.helper.Direction;
import io.smallrye.graphql.schema.helper.MethodHelper;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.Type;

/**
 * This creates a type object.
 * <p>
 * The type object has fields that might reference other types that should still be created. It might also implement
 * some interfaces that should be created. It might also have some operations that reference other types that should
 * still be created.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class TypeCreator extends AbstractCreator {

    private final FieldCreator fieldCreator;

    public TypeCreator(ReferenceCreator referenceCreator, FieldCreator fieldCreator, OperationCreator operationCreator) {
        super(operationCreator, referenceCreator);
        this.fieldCreator = fieldCreator;
    }

    @Override
    protected void addFields(Type type, ClassInfo classInfo, Reference reference) {
        // Fields
        List<MethodInfo> allMethods = new ArrayList<>();
        Map<String, FieldInfo> allFields = new HashMap<>();

        // Find all methods and properties up the tree
        for (ClassInfo c = classInfo; c != null; c = ScanningContext.getIndex().getClassByName(c.superName())) {
            if (InterfaceCreator.canAddInterfaceIntoScheme(c.toString())) { // Not java objects
                List<MethodInfo> classMethods = c.methods();
                allMethods.addAll(classMethods);
                allMethods.addAll(getAllInterfaceMethods(c, classMethods
                        .stream()
                        .map(MethodInfo::toString)
                        .collect(Collectors.toSet())));
                for (FieldInfo fieldInfo : c.fields()) {
                    allFields.putIfAbsent(fieldInfo.name(), fieldInfo);
                }
            }
        }
        for (MethodInfo methodInfo : allMethods) {
            if (MethodHelper.isPropertyMethod(Direction.OUT, methodInfo)) {
                String fieldName = MethodHelper.getPropertyName(Direction.OUT, methodInfo.name());
                FieldInfo fieldInfo = allFields.remove(fieldName);
                fieldCreator.createFieldForPojo(Direction.OUT, fieldInfo, methodInfo, reference).ifPresent(type::addField);
            }
        }

        if (Objects.equals(classInfo.superName(), Classes.RECORD)) {
            // Each record component has an accessor method
            // We check these after getters, so that getters are preferred, e.g. if they have been inherited by an interface
            for (FieldInfo fieldInfo : allFields.values()) {
                MethodInfo methodInfo = classInfo.method(fieldInfo.name());
                fieldCreator.createFieldForPojo(Direction.OUT, fieldInfo, methodInfo, reference).ifPresent(type::addField);
            }
        } else {
            // See what fields are left (this is fields without methods)
            for (FieldInfo fieldInfo : allFields.values()) {
                fieldCreator.createFieldForPojo(Direction.OUT, fieldInfo, reference).ifPresent(type::addField);
            }
        }
    }

    @Override
    protected ReferenceType referenceType() {
        return ReferenceType.TYPE;
    }

    private List<MethodInfo> getAllInterfaceMethods(ClassInfo classInfo, Set<String> methodMemory) {
        return classInfo
                .interfaceNames()
                .stream()
                .map(ScanningContext.getIndex()::getClassByName)
                .filter(Objects::nonNull)
                .flatMap(parentInterfaceInfo -> Stream.concat(
                        parentInterfaceInfo
                                .methods()
                                .stream()
                                .filter(method -> isNotGenericType(method) && methodMemory.add(method.toString())),
                        getAllInterfaceMethods(parentInterfaceInfo, methodMemory).stream()))
                .collect(Collectors.toList());
    }

    private boolean isNotGenericType(MethodInfo method) {
        return method.returnType().kind() != org.jboss.jandex.Type.Kind.TYPE_VARIABLE &&
                method.returnType().kind() != org.jboss.jandex.Type.Kind.PARAMETERIZED_TYPE &&
                method.parameterTypes().stream().allMatch(type -> type.kind() != org.jboss.jandex.Type.Kind.TYPE_VARIABLE &&
                        type.kind() != org.jboss.jandex.Type.Kind.PARAMETERIZED_TYPE);
    }

    @Override
    public String getDirectiveLocation() {
        return "OBJECT";
    }
}
