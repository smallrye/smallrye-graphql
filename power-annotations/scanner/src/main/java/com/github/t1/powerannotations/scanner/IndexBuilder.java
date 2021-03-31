package com.github.t1.powerannotations.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;

import com.github.t1.powerannotations.common.PowerAnnotations;

public class IndexBuilder {
    public static Index loadOrScan() {
        return load().orElseGet(IndexBuilder::scan);
    }

    @SuppressWarnings("UnusedReturnValue")
    private static Optional<Index> load() {
        try (InputStream inputStream = getClassLoader().getResourceAsStream("META-INF/jandex.idx")) {
            return Optional.ofNullable(inputStream).map(IndexBuilder::load);
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException("can't read index file", e);
        }
    }

    private static Index load(InputStream inputStream) {
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

    private static Index scan() {
        Index index = new Scanner().scanClassPath();
        new PowerAnnotations(index).resolveAnnotations();
        return index;
    }
}
