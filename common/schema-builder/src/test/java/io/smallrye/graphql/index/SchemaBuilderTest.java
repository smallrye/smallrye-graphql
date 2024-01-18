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
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

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
import io.smallrye.graphql.schema.model.Operation;
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
        assertEquals(new HashSet<>(asList("INTERFACE", "FIELD_DEFINITION", "OBJECT")), someDirective.getLocations());

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

    @Test
    public void testGenericSchemaBuilding() {
        Indexer indexer = new Indexer();
        indexDirectory(indexer, "io/smallrye/graphql/index/generic");
        IndexView index = indexer.complete();

        Schema schema = SchemaBuilder.build(index);

        assertNotNull(schema);
        Set<Operation> queries = schema.getQueries();
        Set<Operation> mutations = schema.getMutations();
        Map<String, Type> outputTypes = schema.getTypes();

        assertEquals(queries.size(), 3);
        assertEquals(mutations.size(), 5);

        Operation firstQuery = queries.stream()
                .filter(q -> q.getName().equals("heroes"))
                .findFirst()
                .orElseThrow(AssertionError::new);

        // return type
        assertEquals(firstQuery.getReference().getName(), "Hero");

        Operation secondQuery = queries.stream()
                .filter(q -> q.getName().equals("sayHello")).findFirst().orElseThrow(AssertionError::new);
        assertEquals(secondQuery.getReference().getName(), "ResponseComposite");
        assertEquals(secondQuery.getDescription(), "Say hello");

        Type responseCompositeType = outputTypes.get("ResponseComposite");
        assertNotNull(responseCompositeType);

        Type greetingType = outputTypes.get("Greet");
        assertNotNull(greetingType);

        Operation thirdQuery = queries.stream()
                .filter(q -> q.getName().equals("saySome")).findFirst().orElseThrow(AssertionError::new);
        assertEquals(thirdQuery.getArguments().size(), 1);
        assertEquals(thirdQuery.getArguments().get(0).getReference().getName(), "SomeInput");
        assertEquals(thirdQuery.getReference().getName(), "Some");

        // ------------------------------------------------------------------
        // MUTATIONS
        Operation firstMutation = mutations.stream()
                .filter(q -> q.getName().equals("addHero"))
                .findFirst()
                .orElseThrow(AssertionError::new);

        // arguments
        assertEquals(firstMutation
                .getArguments()
                .stream()
                .filter(a -> a.getName().equals("hero"))
                .findFirst()
                .orElseThrow(AssertionError::new).getName(), "hero");

        // return type
        assertEquals(firstMutation.getReference().getName(), "Hero");
        // ------------------------------------------------------------------
        Operation secondMutation = mutations.stream()
                .filter(q -> q.getName().equals("removeHero"))
                .findFirst()
                .orElseThrow(AssertionError::new);

        // arguments
        assertEquals(secondMutation
                .getArguments()
                .stream()
                .filter(a -> a.getName().equals("hero"))
                .findFirst()
                .orElseThrow(AssertionError::new).getName(), "hero");

        // return type
        assertEquals(secondMutation.getReference().getName(), "Hero");
        // ------------------------------------------------------------------
        Operation thirdMutation = mutations.stream()
                .filter(q -> q.getName().equals("updateHero"))
                .findFirst()
                .orElseThrow(AssertionError::new);

        // arguments
        assertEquals(thirdMutation
                .getArguments()
                .stream()
                .filter(a -> a.getName().equals("hero"))
                .findFirst()
                .orElseThrow(AssertionError::new).getName(), "hero");

        // return type
        assertEquals(thirdMutation.getReference().getName(), "Hero");
        // ------------------------------------------------------------------
        Operation fourthMutation = mutations.stream()
                .filter(q -> q.getName().equals("doSomething"))
                .findFirst()
                .orElseThrow(AssertionError::new);

        // arguments
        assertEquals(fourthMutation.getArguments().size(), 0);
        // return type
        assertEquals(fourthMutation.getReference().getName(), "Hero");

        Operation fifthMutation = mutations.stream()
                .filter(q -> q.getName().equals("updateSome"))
                .findFirst()
                .orElseThrow(AssertionError::new);

        // arguments
        assertEquals(fifthMutation.getArguments().size(), 1);
        assertEquals(fifthMutation.getArguments().get(0).getReference().getName(), "SomeInput");
        // return type
        assertEquals(fifthMutation.getReference().getName(), "Some");

    }

    @Test
    public void testKotlinTypeNullability() {
        Indexer indexer = new Indexer();
        indexDirectory(indexer, "io/smallrye/graphql/kotlin");
        IndexView index = indexer.complete();
        Schema schema = SchemaBuilder.build(index);

        assertTrue(getQueryByName(schema, "notNullable").isNotNull());
        assertFalse(getQueryByName(schema, "nullable").isNotNull());
        assertTrue(getQueryByName(schema, "notNullableItemInUni").isNotNull());
        assertFalse(getQueryByName(schema, "nullableItemInUni").isNotNull());

        Map<String, Operation> fooSubfields = schema.getTypes().get("Foo").getOperations();
        assertTrue(fooSubfields.get("notNullableNestedItem").isNotNull());
        assertTrue(fooSubfields.get("notNullableNestedItemInUni").isNotNull());
        assertFalse(fooSubfields.get("nullableNestedItem").isNotNull());
        assertFalse(fooSubfields.get("nullableNestedItemInUni").isNotNull());

        assertFalse(getQueryByName(schema, "zzz1").isNotNull());
        assertFalse(getQueryByName(schema, "zzz2").isNotNull());
        assertTrue(getQueryByName(schema, "zzz3").isNotNull());
        assertTrue(getQueryByName(schema, "zzz4").isNotNull());
    }

    @Test
    public void testKotlinTypeWrappedInCollectionNullability() {
        Indexer indexer = new Indexer();
        indexDirectory(indexer, "io/smallrye/graphql/kotlin");
        IndexView index = indexer.complete();
        Schema schema = SchemaBuilder.build(index);

        var yyy1 = getQueryByName(schema, "yyy1");
        assertTrue(yyy1.isNotNull());
        assertTrue(yyy1.getWrapper().isNotEmpty());

        var yyy2 = getQueryByName(schema, "yyy2");
        assertTrue(yyy2.isNotNull());
        assertFalse(yyy2.getWrapper().isNotEmpty());

        var yyy3 = getQueryByName(schema, "yyy3");
        assertTrue(yyy3.isNotNull());
        assertTrue(yyy3.getWrapper().isNotEmpty());

        var yyy4 = getQueryByName(schema, "yyy4");
        assertFalse(yyy4.isNotNull());
        var arguments = yyy4.getArguments();
        assertTrue(arguments.get(0).isNotNull()); // i0

        assertFalse(arguments.get(1).isNotNull()); // i1

        assertTrue(arguments.get(2).isNotNull()); // i2
        assertTrue(arguments.get(2).getWrapper().isNotEmpty()); // i2

        assertTrue(arguments.get(3).isNotNull()); // i3
        assertFalse(arguments.get(3).getWrapper().isNotEmpty()); // i3

        assertFalse(arguments.get(4).isNotNull()); // i4
        assertFalse(arguments.get(4).getWrapper().isNotEmpty()); // i4

        // TODO: actually it is not consistent with java: if in java the wrapped list element is not null, the list will be marked as non-null in any case. Is this a problem?
        assertFalse(arguments.get(5).isNotNull()); // i5
        assertTrue(arguments.get(5).getWrapper().isNotEmpty()); // i5
    }

    private Operation getQueryByName(Schema schema, String name) {
        return schema.getQueries()
                .stream().filter(q -> q.getName().equals(name))
                .findFirst().orElseThrow();
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
