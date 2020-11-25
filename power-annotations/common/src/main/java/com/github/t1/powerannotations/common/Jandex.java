package com.github.t1.powerannotations.common;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.walkFileTree;
import static org.jboss.jandex.JandexBackdoor.annotations;
import static org.jboss.jandex.JandexBackdoor.classes;
import static org.jboss.jandex.JandexBackdoor.implementors;
import static org.jboss.jandex.JandexBackdoor.newAnnotationInstance;
import static org.jboss.jandex.JandexBackdoor.subclasses;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.JandexBackdoor;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

public class Jandex {

    public static Jandex scan(Path path) {
        return new Jandex(path, scanIndex(path));
    }

    private static Index scanIndex(Path path) {
        final Indexer indexer = new Indexer();
        try {
            walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".class")) {
                        try (InputStream inputStream = newInputStream(file)) {
                            indexer.index(inputStream);
                        }
                    }
                    return CONTINUE;
                }
            });
            return indexer.complete();
        } catch (IOException e) {
            throw new RuntimeException("failed to index", e);
        }
    }

    private final Path path;
    final Index index;

    private final Map<DotName, List<AnnotationInstance>> annotations;
    private final Map<DotName, List<ClassInfo>> subclasses;
    private final Map<DotName, List<ClassInfo>> implementors;
    private final Map<DotName, ClassInfo> classes;

    public Jandex(Path path, Index index) {
        this.path = path;
        this.index = index;

        this.annotations = annotations(index);
        this.subclasses = subclasses(index);
        this.implementors = implementors(index);
        this.classes = classes(index);
    }

    public Set<DotName> allAnnotationNames() {
        return annotations.keySet();
    }

    public void copyClassAnnotation(AnnotationInstance original, DotName className) {
        ClassInfo classInfo = classes.get(className);
        AnnotationInstance copy = copyAnnotationInstance(original, classInfo);
        add(copy, annotations(classInfo));
    }

    public void copyFieldAnnotation(AnnotationInstance original, DotName className, String fieldName) {
        ClassInfo classInfo = classes.get(className);
        FieldInfo field = classInfo.field(fieldName);
        AnnotationInstance annotationInstance = copyAnnotationInstance(original, field);
        JandexBackdoor.add(annotationInstance, field);
        add(annotationInstance, annotations(classInfo));
    }

    public void copyMethodAnnotation(AnnotationInstance original, DotName className, String methodName, Type... parameters) {
        ClassInfo classInfo = classes.get(className);
        MethodInfo method = classInfo.method(methodName, parameters);
        AnnotationInstance annotationInstance = copyAnnotationInstance(original, method);
        JandexBackdoor.add(annotationInstance, method);
        add(annotationInstance, annotations(classInfo));
    }

    private AnnotationInstance copyAnnotationInstance(AnnotationInstance original, AnnotationTarget annotationTarget) {
        return createAnnotationInstance(original.name(), annotationTarget, original.values().toArray(new AnnotationValue[0]));
    }

    private AnnotationInstance createAnnotationInstance(DotName annotationName, AnnotationTarget target,
            AnnotationValue... values) {
        AnnotationInstance annotation = newAnnotationInstance(annotationName, target, values);
        add(annotation, annotations);
        return annotation;
    }

    private void add(AnnotationInstance instance, Map<DotName, List<AnnotationInstance>> map) {
        if (!map.containsKey(instance.name()))
            map.put(instance.name(), new ArrayList<>());
        map.get(instance.name()).add(instance);
    }

    public void write() {
        Path filePath = path.resolve("META-INF/jandex.idx");
        try {
            createDirectories(filePath.getParent());
            OutputStream outputStream = newOutputStream(filePath);
            new IndexWriter(outputStream).write(index);
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("can't write jandex to " + filePath, e);
        }
    }

    public void print() {
        new JandexPrinter(index).run();
    }
}
