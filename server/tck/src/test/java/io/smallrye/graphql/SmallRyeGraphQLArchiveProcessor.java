package io.smallrye.graphql;

import java.io.File;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import io.smallrye.graphql.test.apps.async.api.AsyncApi;
import io.smallrye.graphql.test.apps.batch.api.BatchApi;
import io.smallrye.graphql.test.apps.context.api.ContextApi;
import io.smallrye.graphql.test.apps.defaultvalue.api.DefaultValueParrotAPI;
import io.smallrye.graphql.test.apps.error.api.ErrorApi;
import io.smallrye.graphql.test.apps.fieldexistence.api.FieldExistenceApi;
import io.smallrye.graphql.test.apps.generics.api.ControllerWithGenerics;
import io.smallrye.graphql.test.apps.grouping.api.BookGraphQLApi;
import io.smallrye.graphql.test.apps.jsonp.api.JsonPApi;
import io.smallrye.graphql.test.apps.mapping.api.MappingResource;
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

            WebArchive war = (WebArchive) applicationArchive;

            // Exclude the TCK beans in the deployed app. The TCK jar also has a beans.xml which causes duplicated beans
            war.addAsWebInfResource(new StringAsset(
                    "<beans bean-discovery-mode=\"all\">\n" +
                            "    <scan>\n" +
                            "        <exclude name=\"org.eclipse.microprofile.graphql.tck.**\"/>\n" +
                            "    </scan>\n" +
                            "</beans>"),
                    "beans.xml");

            // The Jetty classloader only reads resources from classes
            Node config = war.get("/META-INF/microprofile-config.properties");
            if (config != null) {
                war.addAsWebInfResource(config.getAsset(), "classes/META-INF/microprofile-config.properties");
            }

            // Add OpenTracing Producer
            war.addClass(TracerProducer.class);

            // Add GraphQL
            String[] deps = {
                    "io.smallrye:smallrye-graphql-servlet",
            };
            File[] dependencies = Maven.configureResolver()
                    .workOffline()
                    .loadPomFromFile(new File("pom.xml"))
                    .resolve(deps)
                    .withoutTransitivity()
                    .asFile();
            war.addAsLibraries(dependencies);

            // Add our own test app
            war.addPackage(ProfileGraphQLApi.class.getPackage());
            war.addPackage(AdditionalScalarsApi.class.getPackage());
            war.addPackage(AsyncApi.class.getPackage());
            war.addPackage(ErrorApi.class.getPackage());
            war.addPackage(BookGraphQLApi.class.getPackage());
            war.addPackage(DefaultValueParrotAPI.class.getPackage());
            war.addPackage(ControllerWithGenerics.class.getPackage());
            war.addPackage(VariablesTestingApi.class.getPackage());
            war.addPackage(OptionalTestingApi.class.getPackage());
            war.addPackage(MutinyApi.class.getPackage());
            war.addPackage(ContextApi.class.getPackage());
            war.addPackage(JsonPApi.class.getPackage());
            war.addPackage(BatchApi.class.getPackage());
            war.addPackage(MappingResource.class.getPackage());
            war.addPackage(FieldExistenceApi.class.getPackage());

        }
    }
}
