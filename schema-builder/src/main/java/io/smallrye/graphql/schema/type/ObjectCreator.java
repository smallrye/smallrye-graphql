package io.smallrye.graphql.schema.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.helper.AnnotationsHelper;
import io.smallrye.graphql.schema.helper.CreatorHelper;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.IgnoreHelper;
import io.smallrye.graphql.schema.helper.NameHelper;
import io.smallrye.graphql.schema.helper.NonNullHelper;
import io.smallrye.graphql.schema.model.Definition;
import io.smallrye.graphql.schema.model.DefinitionType;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Reference;

/**
 * Create a object (input/type/interface) in the schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class ObjectCreator {
    private static final Logger LOG = Logger.getLogger(ObjectCreator.class.getName());

    private final IndexView index;
    private final DefinitionType definitionType;

    public ObjectCreator(DefinitionType definitionType, IndexView index) {
        this.index = index;
        this.definitionType = definitionType;
    }

    public Definition create(ClassInfo classInfo) {
        LOG.debug("Creating " + definitionType.name() + " from " + classInfo.name().toString());
        Definition definition = new Definition(classInfo.name().toString());

        Annotations annotations = AnnotationsHelper.getAnnotationsForClass(classInfo);

        // Name
        definition.setName(NameHelper.getAnyTypeName(definitionType, classInfo, annotations));

        // Description
        definition.setDescription(DescriptionHelper.getDescriptionForType(annotations).orElse(null));

        // Fields
        addFields(definition, classInfo);

        // Interfaces
        addInterfaces(definition, classInfo);

        return definition;
    }

    private void addFields(Definition definition, ClassInfo classInfo) {
        // Fields
        List<MethodInfo> allMethods = new ArrayList<>();
        Map<String, FieldInfo> allFields = new HashMap<>();

        for (ClassInfo c = classInfo; c != null; c = index.getClassByName(c.superName())) {
            allMethods.addAll(c.methods());
            if (c.fields() != null && !c.fields().isEmpty()) {
                for (final FieldInfo fieldInfo : c.fields()) {
                    allFields.putIfAbsent(fieldInfo.name(), fieldInfo);
                }
            }
        }

        for (MethodInfo methodInfo : allMethods) {
            if (NameHelper.isPojoMethod(definitionType, methodInfo.name())) {
                String fieldName = NameHelper.toNameFromPojoMethod(definitionType, methodInfo.name());
                FieldInfo fieldInfo = allFields.get(fieldName);
                addField(definition, fieldInfo, methodInfo);
            }
        }
    }

    private void addField(Definition definition, FieldInfo fieldInfo, MethodInfo methodInfo) {
        // Annotations on the field and setter
        Annotations annotations = AnnotationsHelper.getAnnotationsForAnyField(definitionType, fieldInfo, methodInfo);
        if (!IgnoreHelper.shouldIgnore(annotations)) {
            Type methodType = getMethodType(methodInfo);
            Type fieldType = getFieldType(fieldInfo);

            Field field = new Field();

            // Name
            String fieldName = NameHelper.getAnyNameForField(definitionType, annotations, methodInfo.name());
            field.setName(fieldName);

            // Description
            Optional<String> maybeFieldDescription = DescriptionHelper.getDescriptionForField(annotations, methodType);
            field.setDescription(maybeFieldDescription.orElse(null));

            // Type
            if (CreatorHelper.isParameterized(methodType)) {
                field.setCollection(true);
            }
            Reference returnTypeRef = CreatorHelper.getReference(index, DefinitionType.TYPE, fieldType,
                    methodType, annotations);
            field.setReturnType(returnTypeRef);

            // NotNull
            if (NonNullHelper.markAsNonNull(methodType, annotations)) {
                field.setMandatory(true);
            }

            definition.addField(field);
        }
    }

    private Type getFieldType(FieldInfo fieldInfo) {
        if (fieldInfo == null) {
            return null;
        }
        return fieldInfo.type();
    }

    private void addInterfaces(Definition definition, ClassInfo classInfo) {
        List<DotName> interfaceNames = classInfo.interfaceNames();
        for (DotName interfaceName : interfaceNames) {
            // Ignore java interfaces (like Serializable)
            if (!interfaceName.toString().startsWith(JAVA_DOT)) {
                ClassInfo c = index.getClassByName(interfaceName);
                if (c != null) {
                    Annotations annotationsForClass = AnnotationsHelper.getAnnotationsForClass(c);
                    String iname = NameHelper.getAnyTypeName(DefinitionType.TYPE, c, annotationsForClass);
                    Reference interfaceRef = CreatorHelper.toBeScanned(DefinitionType.INTERFACE, c, iname);
                    definition.addInterface(interfaceRef);
                }
            }
        }
    }

    private Type getMethodType(MethodInfo method) {
        if (this.definitionType.equals(DefinitionType.INPUT)) {
            return method.parameters().get(0);
        }
        return method.returnType();
    }

    private static final String JAVA_DOT = "java.";
}
