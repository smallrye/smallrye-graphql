package io.smallrye.graphql.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.logging.Logger;

/**
 * This creates an index from the classpath.
 * Based on similar class in LRA
 * (https://github.com/jbosstm/narayana/blob/master/rts/lra/lra-proxy/api/src/main/java/io/narayana/lra/client/internal/proxy/nonjaxrs/ClassPathIndexer.java)
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class IndexInitializer {
    private static final Logger LOG = Logger.getLogger(IndexInitializer.class.getName());

    public IndexView createIndex(Set<URL> urls) {
        List<IndexView> indexes = new ArrayList<>();

        // TODO: Read all jandex.idx in the classpath: 
        // something like Enumeration<URL> systemResources = ClassLoader.getSystemResources(JANDEX_IDX);

        // Check in this war
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(JANDEX_IDX)) {
            IndexReader reader = new IndexReader(stream);
            IndexView i = reader.read();
            LOG.info("Loaded index from [" + JANDEX_IDX + "]");
            indexes.add(i);
        } catch (IOException ex) {
            LOG.info("No jandex index available, let's generate one...");
        }

        // Classes in this artifact
        IndexView i = createIndexView(urls);
        indexes.add(i);

        return merge(indexes);
    }

    public IndexView createIndex() {
        Set<URL> urls = getUrlFromClassPath();
        return createIndexView(urls);
    }

    private IndexView createIndexView(Set<URL> urls) {
        Indexer indexer = new Indexer();
        for (URL url : urls) {
            try {
                if (url.toString().endsWith(DOT_JAR) || url.toString().endsWith(DOT_WAR)) {
                    LOG.debug("processing archive [" + url.toString() + "]");
                    processJar(url.openStream(), indexer);
                } else {
                    processFolder(url, indexer);
                }
            } catch (IOException ex) {
                LOG.warn("Cannot process file " + url.toString(), ex);
            }
        }

        return indexer.complete();
    }

    private Set<URL> collectURLsFromClassPath() {
        Set<URL> urls = new HashSet<>();
        for (String s : System.getProperty(JAVA_CLASS_PATH).split(System.getProperty(PATH_SEPARATOR))) {
            try {
                urls.add(Paths.get(s).toUri().toURL());
            } catch (MalformedURLException e) {
                LOG.warn("Cannot create URL from a JAR/WAR file included in the classpath", e);
            }
        }

        return urls;
    }

    private void processFolder(URL url, Indexer indexer) throws IOException {
        try {
            Path folderPath = Paths.get(url.toURI());
            if (Files.isDirectory(folderPath)) {
                try (Stream<Path> walk = Files.walk(folderPath)) {

                    List<Path> collected = walk
                            .filter(Files::isRegularFile)
                            .collect(Collectors.toList());

                    for (Path c : collected) {
                        String entryName = c.getFileName().toString();
                        processFile(entryName, Files.newInputStream(c), indexer);
                    }
                }
            } else {
                LOG.warn("Ignoring url [" + url + "] as it's not a jar, war or folder");
            }

        } catch (URISyntaxException ex) {
            LOG.warn("Could not process url [" + url + "] while indexing files", ex);
        }
    }

    private void processJar(InputStream inputStream, Indexer indexer) throws IOException {

        ZipInputStream zis = new ZipInputStream(inputStream, StandardCharsets.UTF_8);
        ZipEntry ze;

        while ((ze = zis.getNextEntry()) != null) {
            String entryName = ze.getName();
            processFile(entryName, zis, indexer);
        }
    }

    private void processFile(String fileName, InputStream is, Indexer indexer) throws IOException {
        if (fileName.endsWith(DOT_CLASS)) {
            LOG.debug("\t indexing [" + fileName + "]");
            indexer.index(is);
        } else if (fileName.endsWith(DOT_WAR)) {
            // necessary because of the thorntail arquillian adapter
            processJar(is, indexer);
        }
    }

    private IndexView merge(Collection<IndexView> indexes) {
        return CompositeIndex.create(indexes);
    }

    private Set<URL> getUrlFromClassPath() {
        Set<URL> urls = new HashSet<>();

        ClassLoader cl = ClassLoader.getSystemClassLoader();
        if (cl instanceof URLClassLoader) {
            urls.addAll(Arrays.asList(((URLClassLoader) cl).getURLs()));
        } else {
            urls.addAll(collectURLsFromClassPath());
        }
        return urls;
    }

    private static final String DOT_JAR = ".jar";
    private static final String DOT_WAR = ".war";
    private static final String DOT_CLASS = ".class";
    private static final String JAVA_CLASS_PATH = "java.class.path";
    private static final String PATH_SEPARATOR = "path.separator";
    private static final String JANDEX_IDX = "META-INF/jandex.idx";
}
