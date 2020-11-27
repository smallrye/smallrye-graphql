package com.github.t1.powerannotations.utils.jandex;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.stream.Stream;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.AnnotationsLoader;
import com.github.t1.powerannotations.scanner.IndexBuilder;

public class JandexAnnotationsLoader extends AnnotationsLoader {
    private static final IndexView jandex = IndexBuilder.loadOrScan();

    @Override
    public Annotations onType(Class<?> type) {
        ClassInfo classInfo = jandex.getClassByName(toDotName(type));

        return new Annotations() {
            @Override
            public Stream<Annotation> all() {
                return classInfo.classAnnotations().stream().map(JandexAnnotationsLoader::proxy);
            }

            @Override
            public <T extends Annotation> Optional<T> get(Class<T> type) {
                return Optional.ofNullable(classInfo.classAnnotation(toDotName(type))).map(JandexAnnotationsLoader::proxy);
            }

            @Override
            public <T extends Annotation> Stream<T> all(Class<T> type) {
                throw new UnsupportedOperationException(); // TODO resolve repeated annotations
            }
        };
    }

    @Override
    public Annotations onField(Class<?> type, String fieldName) {
        ClassInfo classInfo = jandex.getClassByName(toDotName(type));
        FieldInfo fieldInfo = classInfo.field(fieldName);

        // Looks like a code duplicate, but ClassInfo, FieldInfo, and MethodInfo don't have a common interface
        //noinspection DuplicatedCode
        return new Annotations() {
            @Override
            public Stream<Annotation> all() {
                return fieldInfo.annotations().stream().map(JandexAnnotationsLoader::proxy);
            }

            @Override
            public <T extends Annotation> Optional<T> get(Class<T> type) {
                return Optional.ofNullable(fieldInfo.annotation(toDotName(type))).map(JandexAnnotationsLoader::proxy);
            }

            @Override
            public <T extends Annotation> Stream<T> all(Class<T> type) {
                throw new UnsupportedOperationException(); // TODO resolve repeated annotations
            }
        };
    }

    @Override
    public Annotations onMethod(Class<?> type, String methodName, Class<?>... argTypes) {
        ClassInfo classInfo = jandex.getClassByName(toDotName(type));
        MethodInfo methodInfo = classInfo.method(methodName);// TODO arg types

        // Looks like a code duplicate, but ClassInfo, FieldInfo, and MethodInfo don't have a common interface
        //noinspection DuplicatedCode
        return new Annotations() {
            @Override
            public Stream<Annotation> all() {
                return methodInfo.annotations().stream().map(JandexAnnotationsLoader::proxy);
            }

            @Override
            public <T extends Annotation> Optional<T> get(Class<T> type) {
                return Optional.ofNullable(methodInfo.annotation(toDotName(type))).map(JandexAnnotationsLoader::proxy);
            }

            @Override
            public <T extends Annotation> Stream<T> all(Class<T> type) {
                throw new UnsupportedOperationException(); // TODO resolve repeated annotations
            }
        };
    }

    private static <T> T proxy(AnnotationInstance annotationInstance) {
        //noinspection unchecked
        return (T) AnnotationProxy.proxy(annotationInstance);
    }

    private static DotName toDotName(Class<?> annotationType) {
        return DotName.createSimple(annotationType.getName());
    }
}
