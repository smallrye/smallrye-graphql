package com.github.t1.powerannotations.scanner;

import static java.util.logging.Level.FINE;
import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;

public class IndexBuilder {
    public static IndexView loadOrScan() {
        IndexView jandex = load().orElseGet(Scanner::scan);
        if (LOG.isLoggable(LEVEL)) {
            LOG.log(LEVEL, "------------------------------------------------------------");
            jandex.getKnownClasses()
                    .forEach(classInfo -> LOG.log(LEVEL, classInfo.name() + " :: " + classInfo.classAnnotations().stream()
                            .filter(instance -> !instance.name().toString().equals("kotlin.Metadata")) // contains binary
                            .map(Object::toString).collect(joining(", "))));
            LOG.log(LEVEL, "------------------------------------------------------------");
        }
        return jandex;
    }

    @SuppressWarnings("UnusedReturnValue")
    private static Optional<IndexView> load() {
        try (InputStream inputStream = getClassLoader().getResourceAsStream("META-INF/jandex.idx")) {
            return Optional.ofNullable(inputStream).map(IndexBuilder::load);
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException("can't read index file", e);
        }
    }

    private static IndexView load(InputStream inputStream) {
        try {
            return new IndexReader(inputStream).read();
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException("can't read Jandex input stream", e);
        }
    }

    private static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return (classLoader == null) ? ClassLoader.getSystemClassLoader() : classLoader;
    }

    private static final Logger LOG = Logger.getLogger(IndexBuilder.class.getName());
    private static final Level LEVEL = FINE;
}
