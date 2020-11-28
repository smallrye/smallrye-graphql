package org.jboss.jandex;

import static com.github.t1.powerannotations.common.CommonUtils.with;
import static org.jboss.jandex.ReflectionUtils.get;
import static org.jboss.jandex.ReflectionUtils.isArraysArrayList;
import static org.jboss.jandex.ReflectionUtils.isUnmodifiable;
import static org.jboss.jandex.ReflectionUtils.modifiable;
import static org.jboss.jandex.ReflectionUtils.set;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.t1.powerannotations.common.CommonUtils;

/**
 * A lot of methods or fields in Jandex are package private. The methods in here "add functionality to Jandex"
 * that needs access to the internals of Jandex,
 * which is much better than using reflection or putting all of our own stuff in the same package.
 * Sometimes even that is not enough and we need reflection.
 */
public class JandexBackdoor {
    private JandexBackdoor() {
    }

    public static Map<DotName, List<AnnotationInstance>> annotations(Index index) {
        return modifiable(index.annotations);
    }

    public static Map<DotName, ClassInfo> classes(Index index) {
        return modifiable(index.classes);
    }

    public static Map<DotName, List<AnnotationInstance>> annotations(ClassInfo classInfo) {
        Map<DotName, List<AnnotationInstance>> map = get(classInfo, "annotations");
        if (map.isEmpty()) { // EMPTY_MAP is immutable
            map = new HashMap<>();
            set(classInfo, "annotations", map);
        } else if (isUnmodifiable(map))
            map = modifiable(map);
        return map;
    }

    public static void add(MethodInfo targetMethod, AnnotationInstance newInstance) {
        AnnotationInstance[] copy = with(targetMethod.methodInternal().annotationArray(), newInstance);
        replaceAnnotations(targetMethod, copy);
    }

    public static void replace(AnnotationInstance newInstance, MethodInfo targetMethod) {
        AnnotationInstance[] copy = replace(targetMethod.methodInternal().annotationArray(), newInstance);
        replaceAnnotations(targetMethod, copy);
    }

    private static void replaceAnnotations(MethodInfo targetMethod, AnnotationInstance[] copy) {
        MethodInternal originalMethodInternal = targetMethod.methodInternal();
        MethodInternal newMethodInternal = withAnnotations(originalMethodInternal, copy);
        targetMethod.setMethodInternal(newMethodInternal);
        CommonUtils.replace(targetMethod.declaringClass().methodArray(), originalMethodInternal, newMethodInternal);
    }

    private static MethodInternal withAnnotations(MethodInternal methodInternal, AnnotationInstance[] annotations) {
        return new MethodInternal(
                methodInternal.nameBytes(),
                methodInternal.parameterNamesBytes(),
                methodInternal.parameterArray(),
                methodInternal.returnType(),
                methodInternal.flags(),
                methodInternal.receiverTypeField(),
                methodInternal.typeParameterArray(),
                methodInternal.exceptionArray(),
                annotations,
                methodInternal.defaultValue());
    }

    public static void add(FieldInfo targetField, AnnotationInstance newInstance) {
        AnnotationInstance[] copy = with(targetField.fieldInternal().annotationArray(), newInstance);
        replaceAnnotations(targetField, copy);
    }

    public static void replace(FieldInfo targetField, AnnotationInstance newInstance) {
        AnnotationInstance[] copy = replace(targetField.fieldInternal().annotationArray(), newInstance);
        replaceAnnotations(targetField, copy);
    }

    private static void replaceAnnotations(FieldInfo targetField, AnnotationInstance[] copy) {
        FieldInternal originalFieldInternal = targetField.fieldInternal();
        FieldInternal newFieldInternal = new FieldInternal(originalFieldInternal.nameBytes(), originalFieldInternal.type(),
                originalFieldInternal.flags(), copy);
        targetField.setFieldInternal(newFieldInternal);
        CommonUtils.replace(targetField.declaringClass().fieldArray(), originalFieldInternal, newFieldInternal);
    }

    private static AnnotationInstance[] replace(AnnotationInstance[] original, AnnotationInstance newInstance) {
        AnnotationInstance[] copy = Arrays.copyOf(original, original.length);
        for (int i = 0; i < original.length; i++) {
            if (copy[i].name().equals(newInstance.name())) {
                copy[i] = newInstance;
                return copy;
            }
        }
        throw new RuntimeException("original element " + newInstance.name() + " not in array " + Arrays.toString(original));
    }

    public static List<AnnotationInstance> annotations(MethodInfo methodInfo) {
        List<AnnotationInstance> list = methodInfo.methodInternal().annotations();
        if (isUnmodifiable(list))
            list = modifiable(list);
        if (isArraysArrayList(list)) {
            list = new ArrayList<>(list);
            set(methodInfo.methodInternal(), "annotations", list);
        }
        return list;
    }
}
