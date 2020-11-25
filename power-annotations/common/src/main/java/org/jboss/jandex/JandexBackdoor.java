package org.jboss.jandex;

import static org.jboss.jandex.ReflectionUtils.get;
import static org.jboss.jandex.ReflectionUtils.isArraysArrayList;
import static org.jboss.jandex.ReflectionUtils.isUnmodifiable;
import static org.jboss.jandex.ReflectionUtils.modifiable;
import static org.jboss.jandex.ReflectionUtils.set;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A lot of methods or fields in Jandex are package private. This is a jump host,
 * which is much better than using reflection or putting all of our own stuff in the same package.
 * Sometimes even that is not enough and we need reflection.
 */
public class JandexBackdoor {
    private JandexBackdoor() {
    }

    public static Map<DotName, List<AnnotationInstance>> annotations(Index index) {
        return modifiable(index.annotations);
    }

    public static Map<DotName, List<ClassInfo>> subclasses(Index index) {
        return index.subclasses;
    }

    public static Map<DotName, List<ClassInfo>> implementors(Index index) {
        return index.implementors;
    }

    public static Map<DotName, ClassInfo> classes(Index index) {
        return modifiable(index.classes);
    }

    public static Map<DotName, List<AnnotationInstance>> annotations(ClassInfo classInfo) {
        Map<DotName, List<AnnotationInstance>> map = get(classInfo, "annotations");
        if (isUnmodifiable(map))
            map = modifiable(map);
        return map;
    }

    public static void add(AnnotationInstance annotationInstance, MethodInfo method) {
        add(annotationInstance, method.methodInternal());
    }

    private static void add(AnnotationInstance instance, MethodInternal method) {
        ArrayList<AnnotationInstance> instances = new ArrayList<>(method.annotations());
        instances.add(instance);
        method.setAnnotations(instances);
    }

    public static void add(AnnotationInstance instance, FieldInfo field) {
        ArrayList<AnnotationInstance> instances = new ArrayList<>(field.annotations());
        instances.add(instance);
        field.setAnnotations(instances);
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

    public static AnnotationInstance newAnnotationInstance(DotName name, AnnotationTarget target, AnnotationValue... values) {
        return new AnnotationInstance(name, target, values);
    }
}
