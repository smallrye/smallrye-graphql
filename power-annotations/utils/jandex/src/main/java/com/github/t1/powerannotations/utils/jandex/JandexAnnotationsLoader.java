package com.github.t1.powerannotations.utils.jandex;

import static com.github.t1.powerannotations.common.CommonUtils.signature;
import static com.github.t1.powerannotations.common.CommonUtils.toDotName;
import static org.jboss.jandex.AnnotationTarget.Kind.METHOD;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;

import com.github.t1.annotations.AmbiguousAnnotationResolutionException;
import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.AnnotationsLoader;
import com.github.t1.powerannotations.common.Jandex;
import com.github.t1.powerannotations.scanner.IndexBuilder;

public class JandexAnnotationsLoader extends AnnotationsLoader {
    static final Index jandex = IndexBuilder.loadOrScan();

    @Override
    public Annotations onType(Class<?> type) {
        ClassInfo classInfo = getClassInfo(type);

        return new Annotations() {
            @Override
            public Stream<Annotation> all() {
                return classInfo.classAnnotations().stream()
                        .flatMap(JandexAnnotationsLoader::resolveRepeatableAnnotations)
                        .map(JandexAnnotationsLoader::proxy);
            }

            @Override
            public <T extends Annotation> Optional<T> get(Class<T> type) {
                return all(type)
                        .collect(toOptionalOrThrow());
            }

            @Override
            public <T extends Annotation> Stream<T> all(Class<T> type) {
                DotName typeName = toDotName(type);
                return classInfo.classAnnotations().stream()
                        .flatMap(JandexAnnotationsLoader::resolveRepeatableAnnotations)
                        .filter(annotationInstance -> annotationInstance.name().equals(typeName))
                        .map(JandexAnnotationsLoader::proxy);
            }
        };
    }

    @Override
    public Annotations onField(Class<?> type, String fieldName) {
        ClassInfo classInfo = getClassInfo(type);
        FieldInfo fieldInfo = classInfo.field(fieldName);
        if (fieldInfo == null)
            throw new RuntimeException("no field '" + fieldName + "' in class " + classInfo.name());

        // Looks like a code duplicate, but ClassInfo, FieldInfo, and MethodInfo don't have a common interface
        //noinspection DuplicatedCode
        return new Annotations() {
            @Override
            public Stream<Annotation> all() {
                return fieldInfo.annotations().stream()
                        .flatMap(JandexAnnotationsLoader::resolveRepeatableAnnotations)
                        .map(JandexAnnotationsLoader::proxy);
            }

            @Override
            public <T extends Annotation> Optional<T> get(Class<T> type) {
                return all(type).collect(toOptionalOrThrow());
            }

            @Override
            public <T extends Annotation> Stream<T> all(Class<T> type) {
                DotName typeName = toDotName(type);
                return fieldInfo.annotations().stream()
                        .flatMap(JandexAnnotationsLoader::resolveRepeatableAnnotations)
                        .filter(annotationInstance -> annotationInstance.name().equals(typeName))
                        .map(JandexAnnotationsLoader::proxy);
            }
        };
    }

    @Override
    public Annotations onMethod(Class<?> type, String methodName, Class<?>... argTypes) {
        ClassInfo classInfo = getClassInfo(type);
        Type[] jandexArgTypes = Stream.of(argTypes).map(Class::getName).map(JandexAnnotationsLoader::createClassType)
                .toArray(Type[]::new);
        MethodInfo methodInfo = classInfo.method(methodName, jandexArgTypes);
        if (methodInfo == null) {
            String[] argTypeNames = Stream.of(argTypes).map(Class::getName).toArray(String[]::new);
            throw new RuntimeException("no method " + signature(methodName, argTypeNames) + " in class " + classInfo.name());
        }

        // Looks like a code duplicate, but ClassInfo, FieldInfo, and MethodInfo don't have a common interface
        //noinspection DuplicatedCode
        return new Annotations() {
            @Override
            public Stream<Annotation> all() {
                return methodInfo.annotations().stream()
                        .filter(annotationInstance -> annotationInstance.target().kind() == METHOD) // not params
                        .flatMap(JandexAnnotationsLoader::resolveRepeatableAnnotations)
                        .map(JandexAnnotationsLoader::proxy);
            }

            @Override
            public <T extends Annotation> Optional<T> get(Class<T> type) {
                return all(type).collect(toOptionalOrThrow());
            }

            @Override
            public <T extends Annotation> Stream<T> all(Class<T> type) {
                DotName typeName = toDotName(type);
                return methodInfo.annotations().stream()
                        .flatMap(JandexAnnotationsLoader::resolveRepeatableAnnotations)
                        .filter(annotationInstance -> annotationInstance.name().equals(typeName))
                        .map(JandexAnnotationsLoader::proxy);
            }
        };
    }

    private static Stream<AnnotationInstance> resolveRepeatableAnnotations(AnnotationInstance annotationInstance) {
        return new Jandex(jandex).isRepeatedAnnotation(annotationInstance)
                ? resolveRepeatableAnnotationsDo(annotationInstance)
                : Stream.of(annotationInstance);
    }

    private static Stream<AnnotationInstance> resolveRepeatableAnnotationsDo(AnnotationInstance annotationInstance) {
        return Stream.of(annotationInstance.value().asNestedArray());
    }

    private ClassInfo getClassInfo(Class<?> type) {
        ClassInfo classInfo = jandex.getClassByName(toDotName(type));
        if (classInfo == null)
            throw new RuntimeException("class not found in index: " + type.getName());
        return classInfo;
    }

    private static Type createClassType(String name) {
        return Type.create(DotName.createSimple(name), Kind.CLASS);
    }

    private static <T> T proxy(AnnotationInstance annotationInstance) {
        //noinspection unchecked
        return (T) AnnotationProxy.proxy(annotationInstance);
    }

    private static <T> Collector<T, ?, Optional<T>> toOptionalOrThrow() {
        return Collector.of(
                (Supplier<List<T>>) ArrayList::new,
                List::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                },
                list -> {
                    switch (list.size()) {
                        case 0:
                            return Optional.empty();
                        case 1:
                            return Optional.of(list.get(0));
                        default:
                            throw new AmbiguousAnnotationResolutionException("expected one element maximum, but found " + list);
                    }
                });
    }
}
