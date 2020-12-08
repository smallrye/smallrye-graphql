package com.github.t1.powerannotations.common;

import static java.lang.reflect.Modifier.PUBLIC;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.jboss.jandex.AnnotationValue.createNestedAnnotationValue;
import static org.jboss.jandex.JandexBackdoor.annotations;
import static org.jboss.jandex.JandexBackdoor.classes;
import static org.jboss.jandex.Type.Kind.ARRAY;
import static org.jboss.jandex.Type.Kind.CLASS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.JandexBackdoor;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

public class Jandex {
    final Index index;

    private final Map<DotName, List<AnnotationInstance>> annotations;
    private final Map<DotName, ClassInfo> classes;

    public Jandex(Index index) {
        this.index = index;

        this.annotations = annotations(index);
        this.classes = classes(index);
    }

    List<AnnotationInstance> getAnnotationInstances(DotName annotationName) {
        return index.getAnnotations(annotationName);
    }

    Set<DotName> allAnnotationNames() {
        return annotations.keySet();
    }

    ClassInfo getClassInfo(DotName className) {
        ClassInfo classInfo = classes.get(className);
        return (classInfo == null) ? mock(className) : classInfo;
    }

    @SuppressWarnings("deprecation")
    private static org.jboss.jandex.ClassInfo mock(DotName name) {
        return org.jboss.jandex.ClassInfo.create(
                name, null, (short) PUBLIC, new DotName[0], emptyMap(), true);
    }

    boolean isNotTargetable(DotName annotationName, AnnotationTarget annotationTarget) {
        return !isTargetable(annotationName, annotationTarget);
    }

    private boolean isTargetable(DotName annotationName, AnnotationTarget annotationTarget) {
        AnnotationInstance target = getClassInfo(annotationName).classAnnotation(TARGET);
        if (target == null)
            return true;
        List<String> allowedTypes = asList(target.value().asEnumArray());
        switch (annotationTarget.kind()) {
            case CLASS:
                return allowedTypes.contains("TYPE");
            case FIELD:
                return allowedTypes.contains("FIELD");
            case METHOD:
                return allowedTypes.contains("METHOD");
        }
        throw new UnsupportedOperationException("unexpected annotation kind " + annotationTarget.kind());
    }

    AnnotationInstance copyAnnotationInstance(AnnotationInstance original, AnnotationTarget annotationTarget) {
        AnnotationValue[] values = original.values().toArray(new AnnotationValue[0]);
        return AnnotationInstance.create(original.name(), annotationTarget, values);
    }

    void addOrSkip(ClassInfo targetClass, AnnotationInstance annotation) {
        List<AnnotationInstance> annotationInstances = get(annotations(targetClass), annotation.name());
        if (isRepeatable(annotation) || isRepeatedAnnotation(annotation) || indexOfType(annotationInstances, annotation) < 0)
            add(annotationInstances, annotation);
    }

    boolean addOrReplace(ClassInfo targetClass, AnnotationInstance annotation) {
        List<AnnotationInstance> annotationInstances = get(annotations(targetClass), annotation.name());
        if (isRepeatable(annotation)) {
            add(annotationInstances, annotation);
            return false;
        } else {
            boolean replaced = replace(annotationInstances, annotation);
            if (!replaced)
                add(annotationInstances, annotation);
            return replaced;
        }
    }

    void addOrSkip(FieldInfo targetField, AnnotationInstance annotation) {
        List<AnnotationInstance> annotationInstances = get(annotations(targetField.declaringClass()), annotation.name());
        if (isRepeatable(annotation) || indexOfType(annotationInstances, annotation) < 0) {
            add(annotationInstances, annotation);
            JandexBackdoor.add(targetField, annotation);
        }
    }

    void addOrReplace(FieldInfo targetField, AnnotationInstance copy) {
        boolean replaced = addOrReplace(targetField.declaringClass(), copy);
        if (replaced)
            JandexBackdoor.replace(targetField, copy);
        else
            JandexBackdoor.add(targetField, copy);
    }

    void addOrReplace(MethodInfo targetMethod, AnnotationInstance copy) {
        boolean replaced = addOrReplace(targetMethod.declaringClass(), copy);
        if (replaced)
            JandexBackdoor.replace(copy, targetMethod);
        else
            JandexBackdoor.add(targetMethod, copy);
    }

    void addOrSkip(MethodInfo targetMethod, AnnotationInstance annotation) {
        List<AnnotationInstance> annotationInstances = get(annotations(targetMethod.declaringClass()), annotation.name());
        if (isRepeatable(annotation) || indexOfType(annotationInstances, annotation) < 0) {
            add(annotationInstances, annotation);
            JandexBackdoor.add(targetMethod, annotation);
        }
    }

    private void add(List<AnnotationInstance> existingAnnotations, AnnotationInstance newAnnotation) {
        if (isRepeatable(newAnnotation)) {
            for (int i = 0; i < existingAnnotations.size(); i++) {
                AnnotationInstance existingAnnotation = existingAnnotations.get(i);
                if (existingAnnotation.name().equals(newAnnotation.name())) {
                    existingAnnotations.remove(i);
                    AnnotationValue values = AnnotationValue.createArrayValue("value", new AnnotationValue[] {
                            createNestedAnnotationValue("", existingAnnotation),
                            createNestedAnnotationValue("", newAnnotation)
                    });
                    newAnnotation = AnnotationInstance.create(
                            repeaterTypeName(newAnnotation.name()),
                            newAnnotation.target(),
                            new AnnotationValue[] { values });
                    break;
                }
            }
        }
        existingAnnotations.add(newAnnotation);
    }

    private DotName repeaterTypeName(DotName repeatedType) {
        ClassInfo repeatedClass = getClassInfo(repeatedType);
        AnnotationInstance repeatableAnnotation = repeatedClass.classAnnotation(REPEATABLE);
        return repeatableAnnotation.value().asClass().name();
    }

    public boolean isRepeatedAnnotation(AnnotationInstance annotationInstance) {
        ClassInfo annotationType = getClassInfo(annotationInstance.name());
        if (annotationType.methods().size() == 1 && annotationType.method("value") != null) {
            Type returnType = annotationType.method("value").returnType();
            if (returnType.kind() == ARRAY && returnType.asArrayType().component().kind() == CLASS) {
                ClassInfo returnClassInfo = getClassInfo(returnType.asArrayType().component().name());
                return returnClassInfo != null && isRepeatable(returnClassInfo);
            }
        }
        return false;
    }

    private boolean isRepeatable(AnnotationInstance annotationInstance) {
        ClassInfo annotationType = getClassInfo(annotationInstance.name());
        return annotationType != null && isRepeatable(annotationType);
    }

    private boolean isRepeatable(ClassInfo annotationType) {
        return annotationType.classAnnotation(REPEATABLE) != null;
    }

    private static boolean replace(List<AnnotationInstance> instances, AnnotationInstance newInstance) {
        // we can't use `instances.replaceAll()`, because Maven plugins can't run with lambdas and fail with IndexOutOfBounds
        int index = indexOfType(instances, newInstance);
        if (index < 0)
            return false;
        instances.set(index, newInstance);
        return true;
    }

    private static <K, V> List<V> get(Map<K, List<V>> map, K name) {
        // we can't use `map.computeIfAbsent()`, because Maven plugins can't run with lambdas and fail with IndexOutOfBounds
        if (!map.containsKey(name))
            map.put(name, new ArrayList<>());
        return map.get(name);
    }

    private static int indexOfType(List<AnnotationInstance> instances, AnnotationInstance annotation) {
        for (int i = 0; i < instances.size(); i++)
            if (instances.get(i).name().equals(annotation.name()))
                return i;
        return -1;
    }

    static boolean isAnnotationType(ClassInfo classInfo) {
        return classInfo.interfaceNames().contains(ANNOTATION);
    }

    private static final DotName REPEATABLE = DotName.createSimple("java.lang.annotation.Repeatable");
    private static final DotName ANNOTATION = DotName.createSimple("java.lang.annotation.Annotation");
    private static final DotName TARGET = DotName.createSimple("java.lang.annotation.Target");
}
