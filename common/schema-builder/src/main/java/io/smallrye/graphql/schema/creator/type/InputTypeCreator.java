package io.smallrye.graphql.schema.creator.type;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.ScanningContext;
import io.smallrye.graphql.schema.creator.FieldCreator;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.Direction;
import io.smallrye.graphql.schema.helper.MethodHelper;
import io.smallrye.graphql.schema.helper.TypeAutoNameStrategy;
import io.smallrye.graphql.schema.helper.TypeNameHelper;
import io.smallrye.graphql.schema.model.InputType;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;

/**
 * This creates an input type object.
 * 
 * The input object has fields that might reference other types
 * that should still be created.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class InputTypeCreator implements Creator<InputType> {
    private static final Logger LOG = Logger.getLogger(InputTypeCreator.class.getName());

    private final FieldCreator fieldCreator;
    private final TypeAutoNameStrategy autoNameStrategy;

    public InputTypeCreator(FieldCreator fieldCreator, TypeAutoNameStrategy autoNameStrategy) {
        this.fieldCreator = fieldCreator;
        this.autoNameStrategy = autoNameStrategy;
    }

    @Override
    public InputType create(ClassInfo classInfo, Reference reference) {
        if (!classInfo.hasNoArgsConstructor() ||
                !Modifier.isPublic(classInfo.method("<init>").flags())) {
            throw new IllegalArgumentException(
                    "Class " + classInfo.name().toString()
                            + " is used as input, but does not have a public default constructor");
        }

        LOG.debug("Creating Input from " + classInfo.name().toString());

        Annotations annotations = Annotations.getAnnotationsForClass(classInfo);

        // Name
        String name = TypeNameHelper.getAnyTypeName(reference, ReferenceType.INPUT, classInfo, annotations, autoNameStrategy);

        // Description
        String description = DescriptionHelper.getDescriptionForType(annotations).orElse(null);

        InputType inputType = new InputType(classInfo.name().toString(), name, description);

        // Fields
        addFields(inputType, classInfo, reference);

        return inputType;
    }

    private void addFields(InputType inputType, ClassInfo classInfo, Reference reference) {
        // Fields
        List<MethodInfo> allMethods = new ArrayList<>();
        Map<String, FieldInfo> allFields = new HashMap<>();

        // Find all methods and properties up the tree
        for (ClassInfo c = classInfo; c != null; c = ScanningContext.getIndex().getClassByName(c.superName())) {
            if (!c.toString().startsWith(JAVA_DOT)) { // Not java objects
                allMethods.addAll(c.methods());
                if (c.fields() != null && !c.fields().isEmpty()) {
                    for (final FieldInfo fieldInfo : c.fields()) {
                        allFields.putIfAbsent(fieldInfo.name(), fieldInfo);
                    }
                }
            }
        }

        for (MethodInfo methodInfo : allMethods) {
            if (MethodHelper.isPropertyMethod(Direction.IN, methodInfo)) {
                String fieldName = MethodHelper.getPropertyName(Direction.IN, methodInfo.name());
                FieldInfo fieldInfo = allFields.remove(fieldName);
                fieldCreator.createFieldForPojo(Direction.IN, fieldInfo, methodInfo, reference)
                        .ifPresent(inputType::addField);
            }
        }

        // See what fields are left (this is fields without methods)
        if (!allFields.isEmpty()) {
            for (FieldInfo fieldInfo : allFields.values()) {
                fieldCreator.createFieldForPojo(Direction.IN, fieldInfo, reference)
                        .ifPresent(inputType::addField);
            }
        }

    }

    private static final String JAVA_DOT = "java.";
}
