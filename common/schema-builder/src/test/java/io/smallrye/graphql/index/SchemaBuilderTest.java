package io.smallrye.graphql.index;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;

/**
 * Test the model creation
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SchemaBuilderTest {
    private static final Logger LOG = Logger.getLogger(SchemaBuilderTest.class.getName());

    @Test
    public void testSchemaModelCreation() {

        IndexView index = getTCKIndex();
        Schema schema = SchemaBuilder.build(index);
        LOG.info(toString(schema));
        assertNotNull(schema);
    }

    @Test
    public void testConcurrentSchemaBuilding() throws Exception {
        Indexer indexer = new Indexer();
        indexDirectory(indexer, "org/eclipse/microprofile/graphql/tck/apps/basic/api");
        IndexView basicIndex = indexer.complete();

        indexer = new Indexer();
        indexDirectory(indexer, "org/eclipse/microprofile/graphql/tck/apps/superhero/api");
        indexDirectory(indexer, "org/eclipse/microprofile/graphql/tck/apps/superhero/db");
        indexDirectory(indexer, "org/eclipse/microprofile/graphql/tck/apps/superhero/model");
        IndexView heroIndex = indexer.complete();

        indexer = new Indexer();
        indexDirectory(indexer, "io/smallrye/graphql/index/app");
        IndexView movieIndex = indexer.complete();

        ExecutorService executor = Executors.newFixedThreadPool(4);
        Future<Schema> basicSchemaFuture = executor.submit(() -> SchemaBuilder.build(basicIndex));
        Future<Schema> heroSchemaFuture = executor.submit(() -> SchemaBuilder.build(heroIndex));
        Future<Schema> movieSchemaFuture = executor.submit(() -> SchemaBuilder.build(movieIndex));

        Schema basicSchema = basicSchemaFuture.get();
        Schema heroSchema = heroSchemaFuture.get();
        Schema movieSchema = movieSchemaFuture.get();

        assertNotNull(basicSchema);
        assertNotNull(heroSchema);
        assertNotNull(movieSchema);

        String basicSchemaString = toString(basicSchema);
        LOG.info(basicSchemaString);
        assertTrue(basicSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.basic.api.BasicType"));
        assertTrue(basicSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.basic.api.BasicInput"));
        assertTrue(basicSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.basic.api.BasicInterface"));
        assertTrue(basicSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.basic.api.BasicEnum"));
        assertFalse(basicSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.superhero"));
        assertFalse(basicSchemaString.contains("io.smallrye.graphql"));

        String heroSchemaString = toString(heroSchema);
        LOG.info(heroSchemaString);
        assertTrue(heroSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.superhero.model.SuperHero"));
        assertTrue(heroSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.superhero.model.Sidekick"));
        assertTrue(heroSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.superhero.model.Team"));
        assertTrue(heroSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.superhero.model.Character"));
        assertFalse(heroSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.basic"));
        assertFalse(heroSchemaString.contains("io.smallrye.graphql"));

        String movieSchemaString = toString(movieSchema);
        LOG.info(movieSchemaString);
        assertTrue(movieSchemaString.contains("io.smallrye.graphql.index.app.Movie"));
        assertTrue(movieSchemaString.contains("io.smallrye.graphql.index.app.Person"));
        assertFalse(movieSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.basic"));
        assertFalse(movieSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.superhero"));
    }

    static String toString(Schema schema) {

        JsonbConfig config = new JsonbConfig()
                .withFormatting(true);

        Jsonb jsonb = JsonbBuilder.create(config);
        return jsonb.toJson(schema);
    }

    static IndexView getTCKIndex() {
        Indexer indexer = new Indexer();
        indexDirectory(indexer, "org/eclipse/microprofile/graphql/tck/apps/basic/api");
        indexDirectory(indexer, "org/eclipse/microprofile/graphql/tck/apps/superhero/api");
        indexDirectory(indexer, "org/eclipse/microprofile/graphql/tck/apps/superhero/db");
        indexDirectory(indexer, "org/eclipse/microprofile/graphql/tck/apps/superhero/model");
        return indexer.complete();
    }

    public static void indexDirectory(Indexer indexer, String baseDir) {
        InputStream directoryStream = getResourceAsStream(baseDir);
        BufferedReader reader = new BufferedReader(new InputStreamReader(directoryStream));
        reader.lines()
                .filter(resName -> resName.endsWith(".class"))
                .map(resName -> Paths.get(baseDir, resName))
                .forEach(path -> index(indexer, path.toString()));
    }

    static InputStream getResourceAsStream(String path) {
        return Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(path);
    }

    static void index(Indexer indexer, String resName) {
        try {
            InputStream stream = getResourceAsStream(resName);
            indexer.index(stream);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
