package com.github.t1.powerannotations.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;

class StereotypeResolver implements Runnable {
    private static final Map<DotName, Integer> STEREOTYPE_NESTING_LEVELS = new TreeMap<>();

    private final Jandex jandex;
    private final Logger log;

    StereotypeResolver(Jandex jandex, Logger log) {
        this.jandex = jandex;
        this.log = log;
    }

    @Override
    public void run() {
        for (DotName stereotypeTypeName : jandex.allAnnotationNames()) {
            if (!stereotypeTypeName.withoutPackagePrefix().equals("Stereotype"))
                continue;
            log.info("stereotype type " + stereotypeTypeName);
            for (ClassInfo stereotype : stereotypes(stereotypeTypeName)) {
                log.info("stereotype " + stereotype);
                for (AnnotationInstance stereotypeTargetAnnotationInstance : new ArrayList<>(
                        jandex.getAnnotationInstances(stereotype.asClass().name()))) {
                    AnnotationTarget annotationTarget = stereotypeTargetAnnotationInstance.target();
                    log.info("-> " + annotationTarget.kind().name().toLowerCase() + " " + annotationTarget);
                    Map<DotName, List<AnnotationInstance>> annotations = stereotype.annotations();
                    for (DotName annotationName : annotations.keySet()) {
                        if (stereotypeTypeName.equals(annotationName) || RETENTION.equals(annotationName)
                                || jandex.isNotTargetable(annotationName, annotationTarget))
                            continue;
                        for (AnnotationInstance annotationInstance : annotations.get(annotationName)) {
                            log.info(" - " + annotationInstance);
                            switch (annotationTarget.kind()) {
                                case CLASS:
                                    resolveClassStereotype(annotationInstance, annotationTarget.asClass());
                                    continue;
                                case FIELD:
                                    resolveFieldStereotype(annotationInstance, annotationTarget.asField());
                                    continue;
                                case METHOD:
                                    resolveMethodStereotype(annotationInstance, annotationTarget.asMethod());
                                    continue;
                            }
                            throw new UnsupportedOperationException("don't know how to resolve a " + annotationTarget.kind()
                                    + " stereotype: " + stereotypeTargetAnnotationInstance);
                        }
                    }
                }
            }
        }
    }

    private List<ClassInfo> stereotypes(DotName stereotypeTypeName) {
        // we can't use streams, because Maven plugins can't run with lambdas and fail with IndexOutOfBounds
        List<ClassInfo> list = new ArrayList<>();
        for (AnnotationInstance annotationInstance : jandex.getAnnotationInstances(stereotypeTypeName)) {
            ClassInfo classInfo = annotationInstance.target().asClass();
            list.add(classInfo);
        }
        list.sort(new StereotypeLevel()); // resolve indirect stereotypes first
        return list;
    }

    private class StereotypeLevel implements Comparator<ClassInfo> {
        @Override
        public int compare(ClassInfo left, ClassInfo right) {
            int leftLevel = stereotypeLevel(left);
            int rightLevel = stereotypeLevel(right);
            return (leftLevel == rightLevel)
                    ? left.name().compareTo(right.name())
                    : (leftLevel - rightLevel);
        }
    }

    private int stereotypeLevel(ClassInfo stereotypeType) {
        // we can't use streams, because Maven plugins can't run with lambdas and fail with IndexOutOfBounds
        int max = 0;
        for (AnnotationInstance annotationInstance : stereotypeType.classAnnotations()) {
            int annotationLevel = stereotypeLevel(annotationInstance);
            if (annotationLevel > max) {
                max = annotationLevel;
            }
        }
        return max + 1;
    }

    private int stereotypeLevel(AnnotationInstance annotationInstance) {
        if (STEREOTYPE_NESTING_LEVELS.containsKey(annotationInstance.name()))
            return STEREOTYPE_NESTING_LEVELS.get(annotationInstance.name());
        STEREOTYPE_NESTING_LEVELS.put(annotationInstance.name(), 0); // prevent recursion
        ClassInfo type = jandex.getClassInfo(annotationInstance.name());
        int annotationLevel = stereotypeLevel(type);
        STEREOTYPE_NESTING_LEVELS.put(annotationInstance.name(), annotationLevel);
        return annotationLevel;
    }

    private void resolveClassStereotype(AnnotationInstance annotationInstance, ClassInfo targetClass) {
        AnnotationInstance copy = jandex.copyAnnotationInstance(annotationInstance, targetClass);
        jandex.addOrSkip(targetClass, copy);
    }

    private void resolveFieldStereotype(AnnotationInstance annotationInstance, FieldInfo targetField) {
        AnnotationInstance copy = jandex.copyAnnotationInstance(annotationInstance, targetField);
        jandex.addOrSkip(targetField, copy);
    }

    private void resolveMethodStereotype(AnnotationInstance annotationInstance, MethodInfo targetMethod) {
        AnnotationInstance copy = jandex.copyAnnotationInstance(annotationInstance, targetMethod);
        jandex.addOrSkip(targetMethod, copy);
    }

    private static final DotName RETENTION = DotName.createSimple("java.lang.annotation.Retention");
}
