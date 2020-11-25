package com.github.t1.powerannotations.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

public class PowerAnnotations {
    private static final DotName MIXIN = DotName.createSimple("com.github.t1.annotations.MixinFor");
    private static final DotName RETENTION = DotName.createSimple("java.lang.annotation.Retention");

    private final Jandex jandex;
    private final Logger log;

    public PowerAnnotations(Jandex jandex, Logger log) {
        this.jandex = jandex;
        this.log = log;
    }

    public void resolveAnnotations() {
        resolveMixins();
        resolveStereotypes();
    }

    private void resolveMixins() {
        for (AnnotationInstance mixin : getAnnotationInstances(MIXIN)) {
            Type mixinTarget = mixin.value().asClass();
            log.info("mix " + mixin.target() + " into " + mixinTarget);
            Map<DotName, List<AnnotationInstance>> annotations = mixin.target().asClass().annotations();
            for (DotName annotationName : annotations.keySet()) {
                if (MIXIN.equals(annotationName))
                    continue;
                for (AnnotationInstance annotationInstance : annotations.get(annotationName)) {
                    AnnotationTarget annotationTarget = annotationInstance.target();
                    log.info("- " + annotationInstance + " -> " + annotationTarget.kind().name().toLowerCase()
                            + " " + annotationInstance.target());
                    switch (annotationTarget.kind()) {
                        case CLASS:
                            jandex.copyClassAnnotation(annotationInstance, mixinTarget.name());
                            continue;
                        case FIELD:
                            jandex.copyFieldAnnotation(annotationInstance, mixinTarget.name(),
                                    annotationInstance.target().asField().name());
                            continue;
                        case METHOD:
                            jandex.copyMethodAnnotation(annotationInstance, mixinTarget.name(),
                                    annotationInstance.target().asMethod().name(),
                                    annotationInstance.target().asMethod().parameters().toArray(new Type[0]));
                            continue;
                        case TYPE:
                        case METHOD_PARAMETER:
                            break;
                    }
                    throw new RuntimeException("don't know how to handle a " + annotationTarget.kind() + ": "
                            + annotationInstance);
                }
            }
        }
    }

    private void resolveStereotypes() {
        for (DotName stereotypeTypeName : jandex.allAnnotationNames()) {
            if (!stereotypeTypeName.withoutPackagePrefix().equals("Stereotype"))
                continue;
            log.info("stereotype type " + stereotypeTypeName);
            for (AnnotationInstance stereotypeAnnotationInstance : new ArrayList<>(
                    getAnnotationInstances(stereotypeTypeName))) {
                ClassInfo stereotype = stereotypeAnnotationInstance.target().asClass();
                log.info("stereotype " + stereotype);
                for (AnnotationInstance stereotypeTargetAnnotationInstance : new ArrayList<>(
                        getAnnotationInstances(stereotype.asClass().name()))) {
                    AnnotationTarget annotationTarget = stereotypeTargetAnnotationInstance.target();
                    log.info("-> " + annotationTarget.kind().name().toLowerCase() + " " + annotationTarget);
                    Map<DotName, List<AnnotationInstance>> annotations = stereotype.annotations();
                    for (DotName annotationName : annotations.keySet()) {
                        if (stereotypeTypeName.equals(annotationName) || RETENTION.equals(annotationName))
                            continue;
                        for (AnnotationInstance annotationInstance : annotations.get(annotationName)) {
                            log.info(" - " + annotationInstance);
                            switch (annotationTarget.kind()) {
                                case CLASS:
                                    jandex.copyClassAnnotation(annotationInstance,
                                            annotationTarget.asClass().name());
                                    continue;
                                case FIELD:
                                    jandex.copyFieldAnnotation(annotationInstance,
                                            annotationTarget.asField().declaringClass().name(),
                                            annotationTarget.asField().name());
                                    continue;
                                case METHOD:
                                    jandex.copyMethodAnnotation(annotationInstance,
                                            annotationTarget.asMethod().declaringClass().name(),
                                            annotationTarget.asMethod().name(),
                                            annotationTarget.asMethod().parameters().toArray(new Type[0]));
                                    continue;
                                case TYPE:
                                case METHOD_PARAMETER:
                                    break;
                            }
                            throw new RuntimeException("don't know how to copy to a " + annotationTarget.kind() + ": "
                                    + stereotypeTargetAnnotationInstance);
                        }
                    }
                }
            }
        }
    }

    private List<AnnotationInstance> getAnnotationInstances(DotName annotationName) {
        return jandex.index.getAnnotations(annotationName);
    }
}
