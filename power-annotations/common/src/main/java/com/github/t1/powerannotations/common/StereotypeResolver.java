package com.github.t1.powerannotations.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;

class StereotypeResolver implements Runnable {
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
                                    resolveClassStereotype(annotationTarget, annotationInstance);
                                    continue;
                                case FIELD:
                                    resolveFieldStereotype(annotationTarget, annotationInstance);
                                    continue;
                                case METHOD:
                                    resolveMethodStereotype(annotationTarget, annotationInstance);
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
        int max = 0;
        Set<ClassInfo> uniqueValues = new HashSet<>();
        for (AnnotationInstance annotationInstance : stereotypeType.classAnnotations()) {
            ClassInfo type = jandex.getClassInfo(annotationInstance.name());
            if (uniqueValues.add(type)) {
                int stereotypeLevel = stereotypeLevel(type);
                if (stereotypeLevel > max) {
                    max = stereotypeLevel;
                }
            }
        }
        return max + 1;
    }

    private void resolveClassStereotype(AnnotationTarget annotationTarget, AnnotationInstance annotationInstance) {
        ClassInfo targetClass = jandex.getClassInfo(annotationTarget.asClass().name());
        AnnotationInstance copy = jandex.copyAnnotationInstance(annotationInstance, targetClass);
        jandex.addOrSkip(targetClass, copy);
    }

    private void resolveFieldStereotype(AnnotationTarget annotationTarget, AnnotationInstance annotationInstance) {
        ClassInfo targetClass = jandex.getClassInfo(annotationTarget.asField().declaringClass().name());
        FieldInfo targetField = targetClass.field(annotationTarget.asField().name());
        AnnotationInstance copy = jandex.copyAnnotationInstance(annotationInstance, targetField.declaringClass());
        jandex.addOrSkip(targetField, copy);
    }

    private void resolveMethodStereotype(AnnotationTarget annotationTarget, AnnotationInstance annotationInstance) {
        MethodInfo targetMethod = annotationTarget.asMethod();
        AnnotationInstance copy = jandex.copyAnnotationInstance(annotationInstance, targetMethod);
        jandex.addOrSkip(targetMethod, copy);
    }

    private static final DotName RETENTION = DotName.createSimple("java.lang.annotation.Retention");
}
