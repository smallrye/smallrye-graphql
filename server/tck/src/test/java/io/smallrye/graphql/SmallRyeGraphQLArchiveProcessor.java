/*
 * Copyright 2020 Red Hat, Inc
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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import io.smallrye.graphql.test.apps.async.api.AsyncApi;
import io.smallrye.graphql.test.apps.defaultvalue.api.DefaultValueParrotAPI;
import io.smallrye.graphql.test.apps.error.api.ErrorApi;
import io.smallrye.graphql.test.apps.profile.api.ProfileGraphQLApi;
import io.smallrye.graphql.test.apps.scalars.api.AdditionalScalarsApi;

/**
 * Creates the deployable unit with all the needed dependencies.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SmallRyeGraphQLArchiveProcessor implements ApplicationArchiveProcessor {
    private static final Logger LOG = Logger.getLogger(SmallRyeGraphQLArchiveProcessor.class.getName());

    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {

        if (applicationArchive instanceof WebArchive) {
            LOG.info("\n ================================================================================"
                    + "\n Testing [" + testClass.getName() + "]"
                    + "\n ================================================================================"
                    + "\n");

            WebArchive testDeployment = (WebArchive) applicationArchive;

            final File[] dependencies = Maven.resolver()
                    .loadPomFromFile("pom.xml")
                    .resolve("io.smallrye:smallrye-graphql-servlet")
                    .withTransitivity()
                    .asFile();

            // Make sure it's unique
            Set<File> dependenciesSet = new LinkedHashSet<>(Arrays.asList(dependencies));
            testDeployment.addAsLibraries(dependenciesSet.toArray(new File[] {}));

            // MicroProfile properties
            testDeployment.addAsResource(
                    SmallRyeGraphQLArchiveProcessor.class.getClassLoader()
                            .getResource("META-INF/microprofile-config.properties"),
                    "META-INF/microprofile-config.properties");

            // Add our own test app
            testDeployment.addPackage(ProfileGraphQLApi.class.getPackage());
            testDeployment.addPackage(AdditionalScalarsApi.class.getPackage());
            testDeployment.addPackage(AsyncApi.class.getPackage());
            testDeployment.addPackage(ErrorApi.class.getPackage());
            testDeployment.addPackage(DefaultValueParrotAPI.class.getPackage());
        }
    }
}
