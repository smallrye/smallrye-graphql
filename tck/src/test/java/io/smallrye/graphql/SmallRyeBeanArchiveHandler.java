/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
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
package io.smallrye.graphql;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.Priority;

import org.jboss.weld.environment.deployment.discovery.BeanArchiveBuilder;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveHandler;
import org.jboss.weld.environment.util.Files;

/**
 * Special handler for WildFly VFS urls.
 *
 * @author Martin Kouba
 */
@Priority(1)
public class SmallRyeBeanArchiveHandler implements BeanArchiveHandler {

    @Override
    public BeanArchiveBuilder handle(String beanArchiveReference) {

        // An external form of a wildfly beans.xml URL will be something like:
        // vfs:/content/_DEFAULT___DEFAULT__1f4a3572-fc97-41f5-8fed-0e7e7f7895df.war/WEB-INF/lib/4e0a1b50-4a74-429d-b519-9eedcac11046.jar/META-INF/beans.xml
        beanArchiveReference = beanArchiveReference.substring(0, beanArchiveReference.lastIndexOf("/META-INF/beans.xml"));

        if (beanArchiveReference.endsWith(".war")) {
            // We only use this handler for libraries - WEB-INF classes are handled by ServletContextBeanArchiveHandler
            return null;
        }

        try {
            URL url = new URL(beanArchiveReference);
            try (ZipInputStream in = openStream(url)) {
                BeanArchiveBuilder builder = new BeanArchiveBuilder();
                handleLibrary(url, in, builder);
                return builder;
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    ZipInputStream openStream(URL url) throws IOException {
        InputStream in = url.openStream();
        return (in instanceof ZipInputStream) ? (ZipInputStream) in : new ZipInputStream(in);
    }

    private void handleLibrary(URL url, ZipInputStream zip, BeanArchiveBuilder builder) throws IOException {
        ZipEntry entry = null;
        while ((entry = zip.getNextEntry()) != null) {
            if (Files.isClass(entry.getName())) {
                builder.addClass(Files.filenameToClassname(entry.getName()));
            }
        }
    }
}
