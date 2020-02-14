/*
 * Copyright 2020 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.smallrye.graphql.index;

import java.io.File;
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
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.enterprise.context.ApplicationScoped;

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
@ApplicationScoped
public class IndexInitializer {
    private static final Logger LOG = Logger.getLogger(IndexInitializer.class.getName());

    public IndexView createIndex() {
        Indexer indexer = new Indexer();
        List<URL> urls = new ArrayList<>();

        ClassLoader cl = ClassLoader.getSystemClassLoader();
        if (cl instanceof URLClassLoader) {
            urls.addAll(Arrays.asList(((URLClassLoader) cl).getURLs()));
        } else {
            urls.addAll(collectURLsFromClassPath());
        }

        for (URL url : urls) {
            try {
                if (url.toString().endsWith(DOT_JAR)) {
                    processJar(url.openStream(), indexer);
                } else if (url.toString().endsWith(SLASH)) {
                    processFolder(url, indexer);
                }
            } catch (IOException ex) {
                LOG.warn("Cannot process file " + url.toString(), ex);
            }
        }

        return indexer.complete();
    }

    private List<URL> collectURLsFromClassPath() {
        List<URL> urls = new ArrayList<>();

        for (String s : System.getProperty(JAVA_CLASS_PATH).split(System.getProperty(PATH_SEPARATOR))) {
            if (s.endsWith(DOT_JAR)) {
                try {
                    urls.add(new File(s).toURI().toURL());
                } catch (MalformedURLException e) {
                    LOG.warn("Cannot create URL from a JAR file included in the classpath", e);
                }
            }
        }

        return urls;
    }

    private void processFolder(URL url, Indexer indexer) throws IOException {
        try {
            Path folderPath = Paths.get(url.toURI());

            List<Path> collected = Files.walk(folderPath)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());

            for (Path c : collected) {
                String entryName = c.getFileName().toString();
                processFile(entryName, Files.newInputStream(c), indexer);
            }

        } catch (URISyntaxException ex) {
            LOG.error("Could not process url [" + url + "] while indexing files", ex);
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
            LOG.debug("Indexing [" + fileName + "]");
            indexer.index(is);
        } else if (fileName.endsWith(DOT_WAR)) {
            // necessary because of the thorntail arquillian adapter
            processJar(is, indexer);
        }
    }

    private static final String DOT_JAR = ".jar";
    private static final String DOT_WAR = ".war";
    private static final String DOT_CLASS = ".class";
    private static final String SLASH = "/";

    private static final String JAVA_CLASS_PATH = "java.class.path";
    private static final String PATH_SEPARATOR = "path.separator";
}
