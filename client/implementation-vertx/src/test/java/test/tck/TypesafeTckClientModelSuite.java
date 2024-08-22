package test.tck;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import org.eclipse.microprofile.graphql.Input;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.ExcludeClassNamePatterns;

import io.smallrye.graphql.client.model.ClientModelBuilder;
import io.smallrye.graphql.client.model.ClientModels;
import tck.graphql.typesafe.Animal;
import tck.graphql.typesafe.TypesafeTCK;

/**
 * This test suite is used only for the new client-model typesafe implementation using the Jandex scanning.
 *
 */
@ExcludeClassNamePatterns({ "^tck.graphql.typesafe.RecursionBehavior$" })
class TypesafeTckClientModelSuite extends TypesafeTCK {
    public final static ClientModels CLIENT_MODELS;

    static {
        try {
            CLIENT_MODELS = ClientModelBuilder.build(createIndexExcludingClasses(
                    new File(Animal.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()),
                    "RecursionBehavior"));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    static void beforeAll() {
        // These properties are for the ‘TypesafeGraphQLClientBuilder#builder’ to differentiate between the typesafe-client
        // using the client model and the one that does not.

        System.clearProperty("clientModelCase");
        System.setProperty("clientModelCase", "true");
    }

    // This test aims to initialize system properties.
    // With it, the *beforeAll* method will be executed (before all the suite tests).
    @Test
    void minimalTest() {
    }

    private static Index createIndexExcludingClasses(File file, String... classNamesToExclude) throws IOException {
        Indexer indexer = new Indexer();
        if (!file.getName().endsWith(".jar")) {
            file = new File(file.getPath()
                    + File.separator + "tck"
                    + File.separator + "graphql"
                    + File.separator + "typesafe");
            // Get all files in the directory
            Stream.of(Objects.requireNonNull(file.listFiles()))
                    .filter(File::isFile)
                    .filter(f -> f.getName().endsWith(".class"))
                    .filter(f -> Arrays.stream(classNamesToExclude)
                            .noneMatch(classNameToExclude -> f.getName().contains(classNameToExclude)))
                    .forEach(f -> {
                        try (InputStream inputStream = new FileInputStream(f)) {
                            // Index the class in the new index
                            indexer.index(inputStream);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } else { // .jar
            try (JarFile jarFile = new JarFile(file)) {
                jarFile.stream()
                        .filter(jarEntry -> jarEntry.getName().endsWith(".class"))
                        .filter(jarEntry -> Arrays.stream(classNamesToExclude)
                                .noneMatch(classNameToExclude -> jarEntry.getName().contains(classNameToExclude)))
                        .forEach(jarEntry -> {
                            try (InputStream inputStream = jarFile.getInputStream(jarEntry)) {
                                // Index the class in the new index
                                indexer.index(inputStream);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }
        // SOME OTHER CLASSES TO BE ADDED TO INDEX
        indexer.indexClass(Input.class);
        indexer.indexClass(Closeable.class);
        indexer.indexClass(AutoCloseable.class);

        // Build the new index
        return indexer.complete();
    }
}
