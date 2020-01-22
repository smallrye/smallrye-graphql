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
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.jandex.Index;
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

    public Index createIndex() {
        Indexer indexer = new Indexer();
        List<URL> urls;

        ClassLoader cl = ClassLoader.getSystemClassLoader();
        if (cl instanceof URLClassLoader) {
            urls = Arrays.asList(((URLClassLoader) cl).getURLs());
        } else {
            urls = collectURLsFromClassPath();
        }

        for (URL url : urls) {
            try {
                processFile(url.openStream(), indexer);
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

    private void processFile(InputStream inputStream, Indexer indexer) throws IOException {
        ZipInputStream zis = new ZipInputStream(inputStream, StandardCharsets.UTF_8);
        ZipEntry ze;

        while ((ze = zis.getNextEntry()) != null) {
            String entryName = ze.getName();
            if (entryName.endsWith(DOT_CLASS)) {
                LOG.debug("Indexing [" + entryName + "]");
                indexer.index(zis);
            } else if (entryName.endsWith(DOT_WAR)) {
                // necessary because of the thorntail arquillian adapter
                processFile(zis, indexer);
            }
        }
    }

    private static final String DOT_JAR = ".jar";
    private static final String DOT_WAR = ".war";
    private static final String DOT_CLASS = ".class";

    private static final String JAVA_CLASS_PATH = "java.class.path";
    private static final String PATH_SEPARATOR = "path.separator";
}
