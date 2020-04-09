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
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.helper.AnnotationsHelper;
import io.smallrye.graphql.schema.helper.CreatorHelper;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.Direction;
import io.smallrye.graphql.schema.helper.IgnoreHelper;
import io.smallrye.graphql.schema.helper.NameHelper;
import io.smallrye.graphql.schema.helper.SourceFieldHelper;
import io.smallrye.graphql.schema.model.Complex;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Method;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;

/**
 * Create a complex type (input/type/interface) in the schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class ComplexCreator implements Creator<Complex> {
    private static final Logger LOG = Logger.getLogger(ComplexCreator.class.getName());

    private final IndexView index;
    private final ReferenceType referenceType;
    private final Direction direction;

    public ComplexCreator(ReferenceType referenceType, IndexView index) {
        this.index = index;
        this.referenceType = referenceType;
        if (this.referenceType.equals(referenceType.INPUT)) {
            this.direction = Direction.IN;
        } else {
            this.direction = Direction.OUT;
        }
    }

    @Override
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

        // Add Source fields
        addSourceFields(complex, classInfo);

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
            Method method = createMethod(annotations, fieldInfo, methodInfo);
            complex.addMethod(method);
        }
    }

    private void addSourceFields(Complex complex, ClassInfo classInfo) {
        Map<DotName, List<MethodParameterInfo>> sourceFields = SourceFieldHelper.getAllSourceAnnotations(index);
        if (sourceFields.containsKey(classInfo.name())) {
            List<MethodParameterInfo> methodParameterInfos = sourceFields.get(classInfo.name());
            for (MethodParameterInfo methodParameterInfo : methodParameterInfos) {
                MethodInfo methodInfo = methodParameterInfo.method();
                addSourceField(complex, methodInfo);
            }
        }
    }

    private void addSourceField(Complex complex, MethodInfo methodInfo) {
        // Annotations on the method
        Annotations annotations = AnnotationsHelper.getAnnotationsForMethod(methodInfo);
        if (!IgnoreHelper.shouldIgnore(annotations)) {
            Method method = createMethod(annotations, null, methodInfo);

            // Arguments (input)
            List<Type> parameters = methodInfo.parameters();
            for (short i = 0; i < parameters.size(); i++) {
                // ReferenceType
                Field parameter = CreatorHelper.getParameter(index, ReferenceType.INPUT, parameters.get(i), methodInfo, i);
                if (!parameter.getTypeReference().getClassName().equals(complex.getClassName())) { // Do not add the @Source field
                    method.addParameter(parameter);
                }
            }

            complex.addSource(method);
        }
    }

    private Method createMethod(Annotations annotations, FieldInfo fieldInfo, MethodInfo methodInfo) {

        Type methodType = getMethodType(methodInfo);
        Type fieldType = getFieldType(fieldInfo);

        // Name
        String fieldName = NameHelper.getAnyNameForField(direction, annotations, methodInfo.name());

        // Description
        Optional<String> maybeFieldDescription = DescriptionHelper.getDescriptionForField(annotations, methodType);

        Method method = new Method(fieldName, maybeFieldDescription.orElse(null));

        // Type
        method.setReturn(CreatorHelper.getReturnField(index, referenceType, fieldType, methodType, annotations));

        // TODO: Arguments ? For now we assume setters (so not arguments as it's just fields)

        return method;
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
                    Reference interfaceRef = CreatorHelper.toBeScanned(index, ReferenceType.INTERFACE, c);
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
