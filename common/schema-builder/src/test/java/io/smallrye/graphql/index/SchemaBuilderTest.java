package io.smallrye.graphql.index;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.index.app.SomeDirective;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.SchemaBuilderException;
import io.smallrye.graphql.schema.model.DirectiveInstance;
import io.smallrye.graphql.schema.model.DirectiveType;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.schema.model.Type;

/**
 * Test the model creation
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SchemaBuilderTest {
    private static final Jsonb JSONB = JsonbBuilder.create(new JsonbConfig().withFormatting(true));

    @Test
    public void testSchemaModelCreation() {

        IndexView index = getTCKIndex();
        Schema schema = SchemaBuilder.build(index);
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

        String basicSchemaString = JSONB.toJson(basicSchema);
        assertTrue(basicSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.basic.api.BasicType"));
        assertTrue(basicSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.basic.api.BasicInput"));
        assertTrue(basicSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.basic.api.BasicInterface"));
        assertTrue(basicSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.basic.api.BasicEnum"));
        assertFalse(basicSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.superhero"));
        assertFalse(basicSchemaString.contains("io.smallrye.graphql"));

        String heroSchemaString = JSONB.toJson(heroSchema);
        assertTrue(heroSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.superhero.model.SuperHero"));
        assertTrue(heroSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.superhero.model.Sidekick"));
        assertTrue(heroSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.superhero.model.Team"));
        assertTrue(heroSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.superhero.model.Character"));
        assertFalse(heroSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.basic"));
        assertFalse(heroSchemaString.contains("io.smallrye.graphql"));

        String movieSchemaString = JSONB.toJson(movieSchema);
        assertTrue(movieSchemaString.contains("io.smallrye.graphql.index.app.Movie"));
        assertTrue(movieSchemaString.contains("io.smallrye.graphql.index.app.Person"));
        assertFalse(movieSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.basic"));
        assertFalse(movieSchemaString.contains("org.eclipse.microprofile.graphql.tck.apps.superhero"));
    }

    /**
     * Test a schema where two Java classes map to the same GraphQL type. Such schema should not be allowed to create.
     */
    @Test
    public void testSchemaWithDuplicates() {
        try {
            Indexer indexer = new Indexer();
            indexDirectory(indexer, "io/smallrye/graphql/index/duplicates");
            indexDirectory(indexer, "io/smallrye/graphql/index/duplicates/a");
            indexDirectory(indexer, "io/smallrye/graphql/index/duplicates/b");
            IndexView index = indexer.complete();
            SchemaBuilder.build(index);
            Assertions.fail("Schema should not build when there are multiple classes mapped to the same type");
        } catch (SchemaBuilderException e) {
            // ok
        }
    }

    @Test
    public void testSchemaWithDirectives() throws IOException {
        Indexer indexer = new Indexer();
        Path apiDir = Paths.get(System.getProperty("user.dir"), "../../server/api/target/classes/io/smallrye/graphql/api")
                .normalize();
        indexer.index(Files.newInputStream(apiDir.resolve("Directive.class")));
        indexer.index(Files.newInputStream(apiDir.resolve("DirectiveLocation.class")));
        indexer.index(getResourceAsStream("io/smallrye/graphql/index/app/SomeDirective.class"));
        indexer.index(getResourceAsStream("io/smallrye/graphql/index/app/Movie.class"));
        indexer.index(getResourceAsStream("io/smallrye/graphql/index/app/MovieTriviaController.class"));
        Index index = indexer.complete();

        Schema schema = SchemaBuilder.build(index);

        // check directive types
        assertTrue(schema.hasDirectiveTypes());
        DirectiveType someDirective = schema.getDirectiveTypes().stream()
                .filter(d -> d.getName().equals("someDirective"))
                .findFirst().orElseThrow(NoSuchElementException::new);
        assertNotNull(someDirective);
        assertEquals("someDirective", someDirective.getName());
        assertEquals(SomeDirective.class.getName(), someDirective.getClassName());
        assertEquals(singleton("value"), someDirective.argumentNames());
        assertEquals(new HashSet<>(asList("INTERFACE", "FIELD", "OBJECT")), someDirective.getLocations());

        // check directive instances on type
        Type movie = schema.getTypes().get("Movie");
        List<DirectiveInstance> movieDirectives = movie.getDirectiveInstances();
        assertNotNull(movieDirectives);
        assertEquals(1, movieDirectives.size());
        DirectiveInstance typeDirectiveInstance = movieDirectives.get(0);
        assertNotNull(typeDirectiveInstance);
        assertEquals(someDirective, typeDirectiveInstance.getType());
        assertArrayEquals(new String[] { "foo", "bar" }, (Object[]) typeDirectiveInstance.getValue("value"));

        // check directive instances on field
        Field releaseDate = movie.getFields().get("releaseDate");
        List<DirectiveInstance> releaseDateDirectiveInstances = releaseDate.getDirectiveInstances();
        assertNotNull(releaseDateDirectiveInstances);
        assertEquals(1, releaseDateDirectiveInstances.size());
        DirectiveInstance releaseDateDirectiveInstance = releaseDateDirectiveInstances.get(0);
        assertNotNull(releaseDateDirectiveInstance);
        assertEquals(someDirective, releaseDateDirectiveInstance.getType());
        assertArrayEquals(new String[] { "field" }, (Object[]) releaseDateDirectiveInstance.getValue("value"));

        // check directive instances on getter
        Field title = movie.getFields().get("title");
        List<DirectiveInstance> titleDirectiveInstances = title.getDirectiveInstances();
        assertNotNull(titleDirectiveInstances);
        assertEquals(1, titleDirectiveInstances.size());
        DirectiveInstance titleDirectiveInstance = titleDirectiveInstances.get(0);
        assertNotNull(titleDirectiveInstance);
        assertEquals(someDirective, titleDirectiveInstance.getType());
        assertArrayEquals(new String[] { "getter" }, (Object[]) titleDirectiveInstance.getValue("value"));
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
