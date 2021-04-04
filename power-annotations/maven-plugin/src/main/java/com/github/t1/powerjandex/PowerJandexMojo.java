package com.github.t1.powerjandex;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.walkFileTree;
import static org.apache.maven.plugins.annotations.LifecyclePhase.PROCESS_CLASSES;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javax.inject.Inject;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;

import com.github.t1.powerannotations.common.JandexPrinter;
import com.github.t1.powerannotations.common.Logger;
import com.github.t1.powerannotations.common.PowerAnnotations;

@Mojo(name = "power-jandex", defaultPhase = PROCESS_CLASSES, threadSafe = true)
public class PowerJandexMojo extends AbstractMojo {

    @Inject
    @SuppressWarnings("CdiInjectionPointsInspection")
    MavenProject project;

    private final MojoLogger log = new MojoLogger();

    private class MojoLogger implements Logger {
        @Override
        public void info(String message) {
            getLog().info(message);
        }
    }

    @Override
    public void execute() {
        PowerAnnotations.log = log;

        Index index = new Scanner().createIndex();

        new PowerAnnotations(index).resolveAnnotations();

        new JandexPrinter(index).run();

        write(index);
    }

    private Path baseDir() {
        return project.getBasedir().toPath().resolve("target/classes");
    }

    private class Scanner {
        private final Indexer indexer = new Indexer();

        public Index createIndex() {
            scanDirectory(baseDir());
            return indexer.complete();
        }

        public void scanDirectory(Path path) {
            try {
                walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (isClassFile(file))
                            scanClassFile(file);
                        return CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException("failed to index " + path, e);
            }
        }

        private boolean isClassFile(Path file) {
            return file.toString().endsWith(".class");
        }

        private void scanClassFile(Path path) {
            try (InputStream inputStream = newInputStream(path)) {
                indexer.index(inputStream);
            } catch (IOException e) {
                throw new RuntimeException("failed to index " + path, e);
            }
        }
    }

    private void write(Index index) {
        Path filePath = baseDir().resolve("META-INF/jandex.idx");
        log.info("write index to " + filePath);
        try {
            createDirectories(filePath.getParent());
            try (OutputStream outputStream = newOutputStream(filePath)) {
                new IndexWriter(outputStream).write(index);
            }
        } catch (IOException e) {
            throw new RuntimeException("can't write jandex to " + filePath, e);
        }
    }
}
