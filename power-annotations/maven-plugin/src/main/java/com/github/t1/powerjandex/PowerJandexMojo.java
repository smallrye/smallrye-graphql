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

    private final MojoLogger logger = new MojoLogger();

    @Override
    public void execute() {
        Index index = scanIndex(baseDir());

        new PowerAnnotations(index, logger).resolveAnnotations();

        new JandexPrinter(index, logger).run();

        write(index);
    }

    private Path baseDir() {
        return project.getBasedir().toPath().resolve("target/classes");
    }

    private class MojoLogger implements Logger {
        @Override
        public void info(String message) {
            getLog().info(message);
        }
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

    private void write(Index index) {
        Path filePath = baseDir().resolve("META-INF/jandex.idx");
        logger.info("write index to " + filePath);
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
