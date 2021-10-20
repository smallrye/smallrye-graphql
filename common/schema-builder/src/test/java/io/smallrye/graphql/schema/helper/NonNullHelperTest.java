package io.smallrye.graphql.schema.helper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.AsyncApi;
import io.smallrye.graphql.schema.IndexCreator;

public class NonNullHelperTest {

    @Test
    public void testNonNullString() throws Exception {
        Index complete = IndexCreator.index(AsyncApi.class);

        ClassInfo classByName = complete.getClassByName(DotName.createSimple(AsyncApi.class.getName()));
        MethodInfo nonNullString = classByName.method("nonNullString");
        Type type = nonNullString.returnType();

        Annotations annotationsForMethod = Annotations.getAnnotationsForMethod(nonNullString);

        assertTrue(NonNullHelper.markAsNonNull(type, annotationsForMethod));
    }
    //todo after jandex 3 is used this should work
    /*
     * @Test
     * public void testNonNullStringKotlin() throws Exception {
     * Index complete = IndexCreator.index(AsyncApi.class);
     * 
     * ClassInfo classByName = complete.getClassByName(DotName.createSimple(AsyncApi.class.getName()));
     * MethodInfo nonNullString = classByName.method("nonNullStringKotlin");
     * Type type = nonNullString.returnType();
     * 
     * Annotations annotationsForMethod = Annotations.getAnnotationsForMethod(nonNullString);
     * 
     * assertTrue(NonNullHelper.markAsNonNull(type, annotationsForMethod));
     * }
     */

    @Test
    public void testNullableString() throws Exception {
        Index complete = IndexCreator.index(AsyncApi.class);

        ClassInfo classByName = complete.getClassByName(DotName.createSimple(AsyncApi.class.getName()));
        MethodInfo nonNullString = classByName.method("string");
        Type type = nonNullString.returnType();

        Annotations annotationsForMethod = Annotations.getAnnotationsForMethod(nonNullString);

        assertFalse(NonNullHelper.markAsNonNull(type, annotationsForMethod));
    }

    @Test
    public void testNonNullCompletionStage() throws Exception {
        Index complete = IndexCreator.index(AsyncApi.class);

        ClassInfo classByName = complete.getClassByName(DotName.createSimple(AsyncApi.class.getName()));
        MethodInfo nonNullString = classByName.method("nonNullCompletionStage");
        Type type = nonNullString.returnType();

        Annotations annotationsForMethod = Annotations.getAnnotationsForMethod(nonNullString);

        assertTrue(NonNullHelper.markAsNonNull(type, annotationsForMethod));
    }

}
