package com.github.t1.powerannotations.common;

import static com.github.t1.powerannotations.common.PowerAnnotations.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

class MixinResolver implements Runnable {
    private final Jandex jandex;

    MixinResolver(Jandex jandex) {
        this.jandex = jandex;
    }

    @Override
    public void run() {
        for (AnnotationInstance mixin : jandex.getAnnotationInstances(MIXIN_FOR)) {
            ClassInfo mixinTarget = jandex.getClassInfo(mixin.value().asClass().name());
            ClassInfo mixinClass = mixin.target().asClass();
            log.info("mix " + mixinClass + " into " + mixinTarget);
            if (Jandex.isAnnotationType(mixinTarget))
                resolveAnnotationMixin(mixinTarget, mixinClass);
            else
                resolveMixinAnnotations(mixinTarget, mixinClass);
        }
    }

    private void resolveAnnotationMixin(ClassInfo mixinTarget, ClassInfo mixinClass) {
        for (AnnotationInstance annotationInstance : new ArrayList<>(jandex.getAnnotationInstances(mixinTarget.name()))) {
            resolveMixinAnnotations(annotationInstance.target(), mixinClass);
        }
    }

    private void resolveMixinAnnotations(AnnotationTarget mixinTarget, ClassInfo mixinClass) {
        Map<DotName, List<AnnotationInstance>> annotations = mixinClass.annotations();
        for (DotName annotationName : annotations.keySet()) {
            if (MIXIN_FOR.equals(annotationName))
                continue;
            for (AnnotationInstance annotationInstance : annotations.get(annotationName)) {
                AnnotationTarget annotation = annotationInstance.target();
                log.info("- " + annotationInstance + " -> " + annotation.kind().name().toLowerCase() + " " + annotation);
                switch (annotation.kind()) {
                    case CLASS:
                        switch (mixinTarget.kind()) {
                            case CLASS:
                                resolveClassMixin(annotationInstance, mixinTarget.asClass());
                                continue;
                            case FIELD:
                                resolveFieldMixin(annotationInstance, mixinTarget.asField());
                                continue;
                            case METHOD:
                                try {
                                    MethodInfo targetMethod = getTargetMethod(mixinTarget.asMethod().declaringClass(),
                                            mixinTarget.asMethod());
                                    resolveMethodMixin(annotationInstance, targetMethod);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                continue;
                            default:
                                throw new RuntimeException(
                                        "can't resolve an annotation mixin for the " + mixinTarget.kind() + ": " + mixinTarget);
                        }
                    case FIELD:
                        resolveFieldMixin(annotationInstance, mixinTarget.asClass().field(annotation.asField().name()));
                        continue;
                    case METHOD:
                        resolveMethodMixin(annotationInstance, getTargetMethod(mixinTarget.asClass(), annotation.asMethod()));
                        continue;
                }
                throw new UnsupportedOperationException("don't know how to resolve a " + annotation.kind() + " mixin: "
                        + annotationInstance);
            }
        }
    }

    private MethodInfo getTargetMethod(ClassInfo classInfo, MethodInfo method) {
        return classInfo.method(method.name(), method.parameters().toArray(new Type[0]));
    }

    private void resolveClassMixin(AnnotationInstance annotationInstance, ClassInfo mixinTarget) {
        AnnotationInstance copy = jandex.copyAnnotationInstance(annotationInstance, mixinTarget);
        jandex.addOrReplace(mixinTarget, copy);
    }

    private void resolveFieldMixin(AnnotationInstance annotationInstance, FieldInfo targetField) {
        AnnotationInstance copy = jandex.copyAnnotationInstance(annotationInstance, targetField);
        jandex.addOrReplace(targetField, copy);
    }

    private void resolveMethodMixin(AnnotationInstance annotationInstance, MethodInfo targetMethod) {
        AnnotationInstance copy = jandex.copyAnnotationInstance(annotationInstance, targetMethod);
        jandex.addOrReplace(targetMethod, copy);
    }

    private static final DotName MIXIN_FOR = DotName.createSimple("com.github.t1.annotations.MixinFor");
}
