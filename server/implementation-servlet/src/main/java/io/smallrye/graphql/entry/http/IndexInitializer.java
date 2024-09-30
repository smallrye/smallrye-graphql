package io.smallrye.graphql.entry.http;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Repeatable;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;

import io.smallrye.graphql.api.Entry;
import io.smallrye.graphql.api.Namespace;
import io.smallrye.graphql.api.OneOf;
import io.smallrye.graphql.api.federation.Authenticated;
import io.smallrye.graphql.api.federation.ComposeDirective;
import io.smallrye.graphql.api.federation.Extends;
import io.smallrye.graphql.api.federation.External;
import io.smallrye.graphql.api.federation.FieldSet;
import io.smallrye.graphql.api.federation.Inaccessible;
import io.smallrye.graphql.api.federation.InterfaceObject;
import io.smallrye.graphql.api.federation.Key;
import io.smallrye.graphql.api.federation.Override;
import io.smallrye.graphql.api.federation.Provides;
import io.smallrye.graphql.api.federation.Requires;
import io.smallrye.graphql.api.federation.Resolver;
import io.smallrye.graphql.api.federation.Shareable;
import io.smallrye.graphql.api.federation.Tag;
import io.smallrye.graphql.api.federation.link.Import;
import io.smallrye.graphql.api.federation.link.Link;
import io.smallrye.graphql.api.federation.link.Purpose;
import io.smallrye.graphql.api.federation.policy.Policy;
import io.smallrye.graphql.api.federation.policy.PolicyGroup;
import io.smallrye.graphql.api.federation.policy.PolicyItem;
import io.smallrye.graphql.api.federation.requiresscopes.RequiresScopes;
import io.smallrye.graphql.api.federation.requiresscopes.ScopeGroup;
import io.smallrye.graphql.api.federation.requiresscopes.ScopeItem;

/**
 * This creates an index from the classpath.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class IndexInitializer {

    public IndexView createIndex(Set<URL> urls) {
        List<IndexView> indexes = new ArrayList<>();

        // TODO: Read all jandex.idx in the classpath:
        // something like Enumeration<URL> systemResources = ClassLoader.getSystemResources(JANDEX_IDX);

        // Check in this war
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(JANDEX_IDX)) {
            IndexReader reader = new IndexReader(stream);
            IndexView i = reader.read();
            SmallRyeGraphQLServletLogging.log.loadedIndexFrom(JANDEX_IDX);
            indexes.add(i);
        } catch (IOException ex) {
            SmallRyeGraphQLServletLogging.log.generatingIndex();
        }

        // Classes in this artifact
        IndexView artifact = createIndexView(urls);
        indexes.add(artifact);

        IndexView custom = createCustomIndex();
        indexes.add(custom);

        return merge(indexes);
    }

    public IndexView createIndex() {
        Set<URL> urls = getUrlFromClassPath();
        return createIndexView(urls);
    }

    private IndexView createCustomIndex() {
        Indexer indexer = new Indexer();

        try {
            indexer.index(convertClassToInputStream(Map.class));
            indexer.index(convertClassToInputStream(Entry.class));
            indexer.index(convertClassToInputStream(Repeatable.class));

            // directives from the API module
            indexer.index(convertClassToInputStream(Authenticated.class));
            indexer.index(convertClassToInputStream(ComposeDirective.class));
            indexer.index(convertClassToInputStream(io.smallrye.graphql.api.Deprecated.class));
            indexer.index(convertClassToInputStream(java.lang.Deprecated.class));
            indexer.index(convertClassToInputStream(Extends.class));
            indexer.index(convertClassToInputStream(External.class));
            indexer.index(convertClassToInputStream(FieldSet.class));
            indexer.index(convertClassToInputStream(Import.class));
            indexer.index(convertClassToInputStream(Inaccessible.class));
            indexer.index(convertClassToInputStream(InterfaceObject.class));
            indexer.index(convertClassToInputStream(Key.class));
            indexer.index(convertClassToInputStream(Link.class));
            indexer.index(convertClassToInputStream(OneOf.class));
            indexer.index(convertClassToInputStream(Override.class));
            indexer.index(convertClassToInputStream(Policy.class));
            indexer.index(convertClassToInputStream(PolicyGroup.class));
            indexer.index(convertClassToInputStream(PolicyItem.class));
            indexer.index(convertClassToInputStream(Provides.class));
            indexer.index(convertClassToInputStream(Purpose.class));
            indexer.index(convertClassToInputStream(Requires.class));
            indexer.index(convertClassToInputStream(RequiresScopes.class));
            indexer.index(convertClassToInputStream(ScopeGroup.class));
            indexer.index(convertClassToInputStream(ScopeItem.class));
            indexer.index(convertClassToInputStream(Shareable.class));
            indexer.index(convertClassToInputStream(Tag.class));
            indexer.index(convertClassToInputStream(Namespace.class));
            indexer.index(convertClassToInputStream(Resolver.class));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return indexer.complete();
    }

    private InputStream convertClassToInputStream(Class<?> clazz) {
        String resourceName = '/' + clazz.getName().replace('.', '/') + ".class";
        return clazz.getResourceAsStream(resourceName);
    }

    private IndexView createIndexView(Set<URL> urls) {
        Indexer indexer = new Indexer();
        for (URL url : urls) {
            try {
                if (url.toString().endsWith(DOT_JAR) || url.toString().endsWith(DOT_WAR)) {
                    SmallRyeGraphQLServletLogging.log.processingFile(url.toString());
                    processJar(url.openStream(), indexer);
                } else {
                    processFolder(url, indexer);
                }
            } catch (IOException ex) {
                SmallRyeGraphQLServletLogging.log.cannotProcessFile(url.toString(), ex);
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
                SmallRyeGraphQLServletLogging.log.cannotCreateUrl(e);
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
                SmallRyeGraphQLServletLogging.log.ignoringUrl(url);
            }

        } catch (URISyntaxException ex) {
            SmallRyeGraphQLServletLogging.log.couldNotProcessUrl(url, ex);
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
            SmallRyeGraphQLServletLogging.log.processingFile(fileName);
            indexer.index(is);
        } else if (fileName.endsWith(DOT_WAR) || fileName.endsWith(DOT_JAR)) {
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
