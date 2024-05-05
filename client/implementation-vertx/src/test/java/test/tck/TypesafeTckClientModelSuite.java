package test.tck;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
 */
@ExcludeClassNamePatterns("^tck.graphql.typesafe.RecursionBehavior$")
class TypesafeTckClientModelSuite extends TypesafeTCK {
    public final static ClientModels CLIENT_MODELS;

    static {
        try {
            CLIENT_MODELS = ClientModelBuilder.build(createIndexExcludingClasses(
                    new File(Animal.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()
                            + "tck/graphql/typesafe"),
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

    private static Index createIndexExcludingClasses(File directory, String classNameToExclude) throws IOException {
        Indexer indexer = new Indexer();

        // Get all files in the directory
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                // Check if the file is a .class file and does not contain the excluded class name
                if (file.isFile() && file.getName().endsWith(".class") && !file.getName().contains(classNameToExclude)) {
                    try (FileInputStream fileInputStream = new FileInputStream(file)) {
                        // Index the class in the new index
                        indexer.index(fileInputStream);
                    }
                } else if (file.isFile() && file.getName().endsWith(".jar")) {
                    try (JarFile jarFile = new JarFile(file)) {
                        Enumeration<JarEntry> entries = jarFile.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            if (entry.getName().endsWith(".class")) {
                                try (InputStream in = jarFile.getInputStream(entry)) {
                                    indexer.index(in);
                                }
                            }
                        }
                    }
                }
            }
            // SOME OTHER CLASSES TO BE ADDED TO INDEX
            try {
                indexer.indexClass(Input.class);
                indexer.indexClass(Closeable.class);
                indexer.indexClass(AutoCloseable.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        // Build the new index
        return indexer.complete();
    }
}
