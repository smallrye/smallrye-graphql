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
import io.smallrye.graphql.schema.model.Complex;
import io.smallrye.graphql.schema.model.Method;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.Return;

/**
 * Create a complex type (input/type/interface) in the schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class ComplexCreator {
    private static final Logger LOG = Logger.getLogger(ComplexCreator.class.getName());

    private final IndexView index;
    private final ReferenceType referenceType;

    public ComplexCreator(ReferenceType referenceType, IndexView index) {
        this.index = index;
        this.referenceType = referenceType;
    }

    public Complex create(ClassInfo classInfo) {
        LOG.debug("Creating " + referenceType.name() + " from " + classInfo.name().toString());
        Complex complex = new Complex(classInfo.name().toString());

        Annotations annotations = AnnotationsHelper.getAnnotationsForClass(classInfo);

        // Name
        complex.setName(NameHelper.getAnyTypeName(referenceType, classInfo, annotations));

        // Description
        complex.setDescription(DescriptionHelper.getDescriptionForType(annotations).orElse(null));

        // Fields
        addFields(complex, classInfo);

        // Interfaces
        addInterfaces(complex, classInfo);

        return complex;
    }

    private void addFields(Complex complex, ClassInfo classInfo) {
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
            if (NameHelper.isPojoMethod(referenceType, methodInfo.name())) {
                String fieldName = NameHelper.toNameFromPojoMethod(referenceType, methodInfo.name());
                FieldInfo fieldInfo = allFields.get(fieldName);
                addField(complex, fieldInfo, methodInfo);
            }
        }
    }

    private void addField(Complex complex, FieldInfo fieldInfo, MethodInfo methodInfo) {
        // Annotations on the field and setter
        Annotations annotations = AnnotationsHelper.getAnnotationsForAnyField(referenceType, fieldInfo, methodInfo);
        if (!IgnoreHelper.shouldIgnore(annotations)) {
            Type methodType = getMethodType(methodInfo);
            Type fieldType = getFieldType(fieldInfo);

            Method method = new Method();

            // Name
            String fieldName = NameHelper.getAnyNameForField(referenceType, annotations, methodInfo.name());
            method.setName(fieldName);

            // Description
            Optional<String> maybeFieldDescription = DescriptionHelper.getDescriptionForField(annotations, methodType);
            method.setDescription(maybeFieldDescription.orElse(null));

            // Type
            method.setReturn(getReturnField(fieldType, methodType, annotations));

            complex.addMethod(method);
        }
    }

    private Return getReturnField(Type fieldType, Type methodType, Annotations annotations) {
        Return returnField = new Return();
        if (CreatorHelper.isParameterized(methodType)) {
            returnField.setCollection(true);
        }
        Reference returnTypeRef = CreatorHelper.getReference(index, ReferenceType.TYPE, fieldType,
                methodType, annotations);
        returnField.setReturnType(returnTypeRef);

        // NotNull
        if (NonNullHelper.markAsNonNull(methodType, annotations)) {
            returnField.setMandatory(true);
        }
        return returnField;
    }

    private Type getFieldType(FieldInfo fieldInfo) {
        if (fieldInfo == null) {
            return null;
        }
        return fieldInfo.type();
    }

    private void addInterfaces(Complex complex, ClassInfo classInfo) {
        List<DotName> interfaceNames = classInfo.interfaceNames();
        for (DotName interfaceName : interfaceNames) {
            // Ignore java interfaces (like Serializable)
            if (!interfaceName.toString().startsWith(JAVA_DOT)) {
                ClassInfo c = index.getClassByName(interfaceName);
                if (c != null) {
                    Annotations annotationsForClass = AnnotationsHelper.getAnnotationsForClass(c);
                    String iname = NameHelper.getAnyTypeName(ReferenceType.TYPE, c, annotationsForClass);
                    Reference interfaceRef = CreatorHelper.toBeScanned(ReferenceType.INTERFACE, c, iname);
                    complex.addInterface(interfaceRef);
                }
            }
        }
    }

    private Type getMethodType(MethodInfo method) {
        if (this.referenceType.equals(ReferenceType.INPUT)) {
            return method.parameters().get(0);
        }
        return method.returnType();
    }

    private static final String JAVA_DOT = "java.";
}
