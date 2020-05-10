package io.smallrye.graphql.schema.creator;

import static org.junit.Assert.*;

import io.smallrye.graphql.schema.Classes;
import org.jboss.jandex.DotName;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.junit.Test;

//TODO: Junit 5: use parametrized tests to merge methods
public class ArrayCreatorTest {

    @Test
    public void shouldNotCreateArrayForCompletableFuture() {
        Type stringType = Type.create(DotName.createSimple(String.class.getName()), Type.Kind.CLASS);
        final ParameterizedType completableFuture = ParameterizedType.create(Classes.COMPLETABLE_FUTURE,
                new Type[] { stringType }, null);

        assertFalse(ArrayCreator.createArray(completableFuture).isPresent());
    }

    @Test
    public void shouldNotCreateArrayForCompletionStage() {
        Type stringType = Type.create(DotName.createSimple(String.class.getName()), Type.Kind.CLASS);
        final ParameterizedType completionStage = ParameterizedType.create(Classes.COMPLETION_STAGE, new Type[] { stringType },
                null);

        assertFalse(ArrayCreator.createArray(completionStage).isPresent());
    }

}
