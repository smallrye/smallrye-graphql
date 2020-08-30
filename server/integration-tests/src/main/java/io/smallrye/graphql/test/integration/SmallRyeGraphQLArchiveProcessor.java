package io.smallrye.graphql.test.integration;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * Creates the deployable unit with all the needed dependencies.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SmallRyeGraphQLArchiveProcessor implements ApplicationArchiveProcessor {

    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {

        if (applicationArchive instanceof WebArchive) {
            WebArchive testDeployment = (WebArchive) applicationArchive;

            final File[] dependencies = Maven.resolver()
                    .loadPomFromFile("pom.xml")
                    .resolve("io.smallrye:smallrye-graphql-servlet")
                    .withTransitivity()
                    .asFile();
            // Make sure it's unique
            Set<File> dependenciesSet = new LinkedHashSet<>(Arrays.asList(dependencies));
            testDeployment.addAsLibraries(dependenciesSet.toArray(new File[] {}));
        }
    }

}
