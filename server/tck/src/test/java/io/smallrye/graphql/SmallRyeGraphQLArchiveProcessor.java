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
import io.smallrye.graphql.test.apps.context.api.ContextApi;
import io.smallrye.graphql.test.apps.defaultvalue.api.DefaultValueParrotAPI;
import io.smallrye.graphql.test.apps.error.api.ErrorApi;
import io.smallrye.graphql.test.apps.generics.api.ControllerWithGenerics;
import io.smallrye.graphql.test.apps.grouping.api.BookGraphQLApi;
import io.smallrye.graphql.test.apps.jsonp.api.JsonPApi;
import io.smallrye.graphql.test.apps.mutiny.api.MutinyApi;
import io.smallrye.graphql.test.apps.optional.api.OptionalTestingApi;
import io.smallrye.graphql.test.apps.profile.api.ProfileGraphQLApi;
import io.smallrye.graphql.test.apps.scalars.api.AdditionalScalarsApi;
import io.smallrye.graphql.test.apps.variables.api.VariablesTestingApi;

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
                    .resolve("io.smallrye:smallrye-graphql-servlet",
                            "io.smallrye.reactive:mutiny",
                            "io.smallrye:smallrye-context-propagation")
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
            testDeployment.addPackage(BookGraphQLApi.class.getPackage());
            testDeployment.addPackage(DefaultValueParrotAPI.class.getPackage());
            testDeployment.addPackage(ControllerWithGenerics.class.getPackage());
            testDeployment.addPackage(VariablesTestingApi.class.getPackage());
            testDeployment.addPackage(OptionalTestingApi.class.getPackage());
            testDeployment.addPackage(MutinyApi.class.getPackage());
            testDeployment.addPackage(ContextApi.class.getPackage());
            testDeployment.addPackage(JsonPApi.class.getPackage());
        }
    }
}
