package io.smallrye.graphql.execution;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;

import org.jboss.jandex.IndexView;

/**
 * Index the classes we want to test against
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Indexer {

    public static IndexView getAllTestIndex() {
        return getTestIndex("io/smallrye/graphql/test");
    }

    public static IndexView getTestIndex(String packageName) {
        org.jboss.jandex.Indexer indexer = new org.jboss.jandex.Indexer();
        indexDirectory(indexer, packageName);
        return indexer.complete();
    }

    private static void indexDirectory(org.jboss.jandex.Indexer indexer, String baseDir) {
        InputStream directoryStream = getResourceAsStream(baseDir);
        BufferedReader reader = new BufferedReader(new InputStreamReader(directoryStream));
        reader.lines()
                .filter(resName -> resName.endsWith(".class"))
                .map(resName -> Paths.get(baseDir, resName))
                .forEach(path -> index(indexer, path.toString()));
    }

    private static InputStream getResourceAsStream(String path) {
        return Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(path);
    }

    private static void index(org.jboss.jandex.Indexer indexer, String resName) {
        try {
            InputStream stream = getResourceAsStream(resName);
            indexer.index(stream);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
