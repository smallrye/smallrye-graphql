package io.smallrye.graphql.schema.helper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.Type;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.model.DefinitionType;

/**
 * Getting all the annotations for something.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class AnnotationsHelper {

    private AnnotationsHelper() {
    }

    public static Annotations getAnnotationsForAnyField(DefinitionType definitionType, FieldInfo fieldInfo,
            MethodInfo methodInfo) {

        if (definitionType.equals(DefinitionType.INTERFACE)) {
            return getAnnotationsForInterfaceField(methodInfo);
        } else if (definitionType.equals(DefinitionType.TYPE)) {
            return getAnnotationsForOutputField(fieldInfo, methodInfo);
        } else if (definitionType.equals(DefinitionType.INPUT)) {
            return getAnnotationsForInputField(fieldInfo, methodInfo);
        } else {
            return null;
        }
    }

    public static Annotations getAnnotationsForInputField(FieldInfo fieldInfo, MethodInfo methodInfo) {
        Map<DotName, AnnotationInstance> annotationsForField = getAnnotationsForField(fieldInfo, methodInfo);

        List<Type> parameters = methodInfo.parameters();
        if (parameters != null && !parameters.isEmpty()) {
            Type param = parameters.get(ZERO);
            Map<DotName, AnnotationInstance> parameterAnnotations = getAnnotationsWithFilter(param, Annotations.DATE_FORMAT,
                    Annotations.NUMBER_FORMAT);
            if (!parameterAnnotations.isEmpty()) {
                annotationsForField.putAll(parameterAnnotations);
            }
        }
        return new Annotations(annotationsForField);
    }

    public static Annotations getAnnotationsForInterfaceField(MethodInfo methodInfo) {
        return getAnnotationsForOutputField(null, methodInfo);
    }

    public static Annotations getAnnotationsForOutputField(FieldInfo fieldInfo, MethodInfo methodInfo) {
        Map<DotName, AnnotationInstance> annotationsForField = getAnnotationsForField(fieldInfo, methodInfo);
        Type returnType = methodInfo.returnType();
        if (returnType != null) {
            Map<DotName, AnnotationInstance> returnTypeAnnotations = getAnnotationsWithFilter(returnType,
                    Annotations.DATE_FORMAT,
                    Annotations.NUMBER_FORMAT);
            if (!returnTypeAnnotations.isEmpty()) {
                annotationsForField.putAll(returnTypeAnnotations);
            }
        }
        return new Annotations(annotationsForField);
    }

    public static Annotations getAnnotationsForMethod(MethodInfo methodInfo) {
        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        for (AnnotationInstance annotationInstance : methodInfo.annotations()) {
            DotName name = annotationInstance.name();
            AnnotationTarget.Kind kind = annotationInstance.target().kind();
            if (kind.equals(AnnotationTarget.Kind.METHOD)) {
                annotationMap.put(name, annotationInstance);
            }
        }
        return new Annotations(annotationMap);
    }

    public static Annotations getAnnotationsForClass(ClassInfo classInfo) {

        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        for (AnnotationInstance annotationInstance : classInfo.classAnnotations()) {
            DotName name = annotationInstance.name();
            annotationMap.put(name, annotationInstance);
        }

        return new Annotations(annotationMap);
    }

    public static Annotations getAnnotationsForType(Type type, Type methodType) {
        Map<DotName, AnnotationInstance> annotationMap = getAnnotationsForType(type);
        annotationMap.putAll(getAnnotationsForType(methodType));
        return new Annotations(annotationMap);
    }

    public static Annotations getAnnotationsForArgument(MethodInfo methodInfo) {
        return getAnnotationsForArgument(methodInfo, ZERO); // Default to ZERO for normal setters in Beans
    }

    public static Annotations getAnnotationsForArgument(MethodInfo methodInfo, short pos) {
        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        for (AnnotationInstance anno : methodInfo.annotations()) {

            List<Type> parameters = methodInfo.parameters();
            if (!parameters.isEmpty()) {
                Type type = parameters.get(pos);
                if (anno.target().kind().equals(AnnotationTarget.Kind.METHOD_PARAMETER)) {
                    MethodParameterInfo methodParameter = anno.target().asMethodParameter();
                    short position = methodParameter.position();
                    if (position == pos) {
                        annotationMap.put(anno.name(), anno);
                        Map<DotName, AnnotationInstance> annotations = getAnnotations(type);
                        annotationMap.putAll(annotations);
                    }
                }
            }
        }
        return new Annotations(annotationMap);
    }

    private static Map<DotName, AnnotationInstance> getAnnotations(Type type) {
        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        if (type.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
            Type typeInCollection = type.asParameterizedType().arguments().get(0);
            annotationMap.putAll(getAnnotations(typeInCollection));
        } else {
            List<AnnotationInstance> annotations = type.annotations();
            for (AnnotationInstance annotationInstance : annotations) {
                annotationMap.put(annotationInstance.name(), annotationInstance);
            }
        }

        return annotationMap;
    }

    private static Map<DotName, AnnotationInstance> getAnnotationsForType(Type type) {
        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();
        for (AnnotationInstance annotationInstance : type.annotations()) {
            DotName name = annotationInstance.name();
            annotationMap.put(name, annotationInstance);
        }
        return annotationMap;
    }

    private static Map<DotName, AnnotationInstance> getAnnotationsForField(FieldInfo fieldInfo, MethodInfo methodInfo) {
        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();
        if (fieldInfo != null)
            annotationMap.putAll(listToMap(fieldInfo.annotations()));
        if (methodInfo != null)
            annotationMap.putAll(listToMap(methodInfo.annotations()));
        return annotationMap;
    }

    private static Map<DotName, AnnotationInstance> listToMap(List<AnnotationInstance> annotationInstances) {
        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        for (AnnotationInstance annotationInstance : annotationInstances) {
            DotName name = annotationInstance.name();
            annotationMap.put(name, annotationInstance);
        }
        return annotationMap;
    }

    private static Map<DotName, AnnotationInstance> getAnnotationsWithFilter(Type type, DotName... filter) {
        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        if (type.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
            Type typeInCollection = type.asParameterizedType().arguments().get(0);
            annotationMap.putAll(getAnnotationsWithFilter(typeInCollection, filter));
        } else {
            List<AnnotationInstance> annotations = type.annotations();
            for (AnnotationInstance annotationInstance : annotations) {
                if (Arrays.asList(filter).contains(annotationInstance.name())) {
                    annotationMap.put(annotationInstance.name(), annotationInstance);
                }
            }
        }

        return annotationMap;
    }

    private static final short ZERO = 0;
}
