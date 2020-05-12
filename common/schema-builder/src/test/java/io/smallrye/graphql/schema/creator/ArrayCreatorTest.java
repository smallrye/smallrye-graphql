package io.smallrye.graphql.schema.creator;

import static org.junit.Assert.*;

import java.util.List;

import org.jboss.jandex.ArrayType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.junit.Test;

import io.smallrye.graphql.schema.Classes;

//TODO: Junit 5: use parametrized tests to merge methods
public class ArrayCreatorTest {

    public static final Type STRING_TYPE = Type.create(DotName.createSimple(String.class.getName()), Type.Kind.CLASS);

    @Test
    public void shouldNotCreateArrayForCompletableFuture() {
        final ParameterizedType completableFuture = ParameterizedType.create(Classes.COMPLETABLE_FUTURE,
                new Type[] {STRING_TYPE}, null);

        assertFalse(ArrayCreator.createArray(completableFuture).isPresent());
    }

    @Test
    public void shouldNotCreateArrayForCompletionStage() {
        final ParameterizedType completionStage = ParameterizedType.create(Classes.COMPLETION_STAGE, new Type[] {STRING_TYPE},
                null);

        assertFalse(ArrayCreator.createArray(completionStage).isPresent());
    }

    @Test
    public void shouldCreateArrayForCompletionStageOfList() {
        final ParameterizedType stringList = ParameterizedType.create(DotName.createSimple(List.class.getName()),
                new Type[] {STRING_TYPE}, null);
        final ParameterizedType completionStage = ParameterizedType.create(Classes.COMPLETION_STAGE, new Type[] {stringList},
                null);

        assertTrue(ArrayCreator.createArray(completionStage).isPresent());
    }

    @Test
    public void shouldCreateArrayForCompletionStageOfArray() {
        final ArrayType stringArray = ArrayType.create(STRING_TYPE, 1);
        final ParameterizedType completionStage = ParameterizedType.create(Classes.COMPLETION_STAGE, new Type[] {stringArray},
                null);

        assertTrue(ArrayCreator.createArray(completionStage).isPresent());
    }

}
