package io.smallrye.graphql.schema.creator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jboss.jandex.ArrayType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.schema.Classes;
import io.smallrye.graphql.schema.ScanningContext;

//TODO: Junit 5: use parametrized tests to merge methods
public class ArrayCreatorTest {

    public static final Type STRING_TYPE = Type.create(DotName.createSimple(String.class.getName()), Type.Kind.CLASS);

    public static final DotName SET = DotName.createSimple(Set.class.getName());
    public static final DotName LINKED_LIST = DotName.createSimple(LinkedList.class.getName());

    @BeforeAll
    public static void intitJandexIndex() {
        Indexer indexer = new Indexer();
        // SchemaBuilderTest.indexBasicJavaClasses(indexer);

        IndexView index = indexer.complete();
        ScanningContext.register(index);
    }

    @AfterAll
    public static void clearJandexIndex() {
        ScanningContext.remove();
    }

    @Test
    public void shouldCreateArrayForJavaArray() {
        final ArrayType type = ArrayType.create(STRING_TYPE, 1);

        assertTrue(ArrayCreator.createArray(type).isPresent());
    }

    @Test
    public void shouldCreateArrayForCollection() {
        final ParameterizedType type = ParameterizedType.create(Classes.COLLECTION,
                new Type[] { STRING_TYPE }, null);

        assertTrue(ArrayCreator.createArray(type).isPresent());
    }

    @Test
    public void shouldCreateArrayForSet() {
        final ParameterizedType type = ParameterizedType.create(SET,
                new Type[] { STRING_TYPE }, null);

        assertTrue(ArrayCreator.createArray(type).isPresent());
    }

    @Test
    public void shouldCreateArrayForLinkedList() {
        final ParameterizedType type = ParameterizedType.create(LINKED_LIST,
                new Type[] { STRING_TYPE }, null);

        assertTrue(ArrayCreator.createArray(type).isPresent());
    }

    @Test
    public void shouldNotCreateArrayForOptional() {
        final ParameterizedType type = ParameterizedType.create(Classes.OPTIONAL,
                new Type[] { STRING_TYPE }, null);

        assertFalse(ArrayCreator.createArray(type).isPresent());
    }

    @Test
    public void shouldNotCreateArrayForCompletableFuture() {
        final ParameterizedType type = ParameterizedType.create(Classes.COMPLETABLE_FUTURE,
                new Type[] { STRING_TYPE }, null);

        assertFalse(ArrayCreator.createArray(type).isPresent());
    }

    @Test
    public void shouldNotCreateArrayForCompletionStage() {
        final ParameterizedType type = ParameterizedType.create(Classes.COMPLETION_STAGE, new Type[] { STRING_TYPE },
                null);

        assertFalse(ArrayCreator.createArray(type).isPresent());
    }

    @Test
    public void shouldCreateArrayForCompletionStageOfList() {
        final ParameterizedType stringList = ParameterizedType.create(DotName.createSimple(List.class.getName()),
                new Type[] { STRING_TYPE }, null);
        final ParameterizedType type = ParameterizedType.create(Classes.COMPLETION_STAGE, new Type[] { stringList },
                null);

        assertTrue(ArrayCreator.createArray(type).isPresent());
    }

    @Test
    public void shouldCreateArrayForCompletionStageOfArray() {
        final ArrayType stringArray = ArrayType.create(STRING_TYPE, 1);
        final ParameterizedType type = ParameterizedType.create(Classes.COMPLETION_STAGE, new Type[] { stringArray },
                null);

        assertTrue(ArrayCreator.createArray(type).isPresent());
    }

}
