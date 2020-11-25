package com.github.t1.powerannotations.common;

import static java.nio.file.Files.newInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;

public class JandexPrinter {

    private final IndexView index;

    public JandexPrinter(Path indexFile) {
        this(load(indexFile));
    }

    public JandexPrinter(IndexView index) {
        this.index = index;
    }

    private static IndexView load(Path indexFile) {
        System.out.println("load from " + indexFile);
        try (InputStream inputStream = new BufferedInputStream(newInputStream(indexFile))) {
            return new IndexReader(inputStream).read();
        } catch (IOException e) {
            throw new RuntimeException("can't load Jandex index file", e);
        }
    }

    public void run() {
        System.out.println("------------------------------------------------------------");
        ((Index) index).printAnnotations();
        System.out.println("------------------------------------------------------------");
        ((Index) index).printSubclasses();
        System.out.println("------------------------------------------------------------");
        index.getKnownClasses().forEach(new Consumer<ClassInfo>() {
            @Override
            public void accept(ClassInfo classInfo) {
                if (!classInfo.name().toString().startsWith("test."))
                    return;
                System.out.println(classInfo.name() + ":");
                classInfo.methods().forEach(new Consumer<MethodInfo>() {
                    @Override
                    public void accept(MethodInfo method) {
                        System.out.println("    " + method.name() + " [" + method.defaultValue() + "]");
                    }
                });
            }
        });
        System.out.println("------------------------------------------------------------");
    }
}
