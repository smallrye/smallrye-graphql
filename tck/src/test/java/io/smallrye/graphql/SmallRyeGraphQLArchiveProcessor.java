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

import java.io.File;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveHandler;

public class SmallRyeGraphQLArchiveProcessor implements ApplicationArchiveProcessor {

    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        if (applicationArchive instanceof WebArchive) {
            WebArchive testDeployment = (WebArchive) applicationArchive;
            // Register SmallRyeBeanArchiveHandler using the ServiceLoader mechanism
            testDeployment.addClass(SmallRyeBeanArchiveHandler.class);
            testDeployment.addAsServiceProvider(BeanArchiveHandler.class, SmallRyeBeanArchiveHandler.class);

            String[] deps = {
                    "io.smallrye:smallrye-graphql-1.0",
                    "io.smallrye:smallrye-config-1.3",
                    "io.smallrye:smallrye-graphql-tck-1.0",
                    "org.eclipse.microprofile.graphql:microprofile-graphql-tck",
                    "org.jboss.weld.servlet:weld-servlet-core" };

            File[] dependencies = Maven.resolver().loadPomFromFile(new File("pom.xml")).resolve(deps).withTransitivity()
                    .asFile();

            testDeployment.addAsLibraries(dependencies);
        }
    }

}