package io.smallrye.graphql.schema.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.AsyncApi;
import io.smallrye.graphql.schema.IndexCreator;
import io.smallrye.graphql.schema.ScanningContext;
import io.smallrye.graphql.schema.helper.class_nonnull.ClassNonNullTestApi;
import io.smallrye.graphql.schema.helper.package_nonnull.PackageNonNullTestApi;

public class NonNullHelperTest {
    @AfterEach
    void tearDown() {
        ScanningContext.remove();
    }

    @Test
    public void testNonNullString() throws Exception {
        Index complete = IndexCreator.index(AsyncApi.class);
        ScanningContext.register(complete);

        ClassInfo classByName = complete.getClassByName(DotName.createSimple(AsyncApi.class.getName()));
        MethodInfo nonNullString = classByName.method("nonNullString");
        Type type = nonNullString.returnType();

        Annotations annotationsForMethod = Annotations.getAnnotationsForMethod(nonNullString);

        assertTrue(NonNullHelper.markAsNonNull(type, annotationsForMethod));
    }

    @Test
    public void testNonNullStringKotlin() throws Exception {
        Index complete = IndexCreator.index(AsyncApi.class);
        ScanningContext.register(complete);

        ClassInfo classByName = complete.getClassByName(DotName.createSimple(AsyncApi.class.getName()));
        MethodInfo nonNullString = classByName.method("nonNullStringKotlin");
        Type type = nonNullString.returnType();

        Annotations annotationsForMethod = Annotations.getAnnotationsForMethod(nonNullString);

        assertTrue(NonNullHelper.markAsNonNull(type, annotationsForMethod));
    }

    @Test
    public void testNullableString() throws Exception {
        Index complete = IndexCreator.index(AsyncApi.class);
        ScanningContext.register(complete);

        ClassInfo classByName = complete.getClassByName(DotName.createSimple(AsyncApi.class.getName()));
        MethodInfo nonNullString = classByName.method("string");
        Type type = nonNullString.returnType();

        Annotations annotationsForMethod = Annotations.getAnnotationsForMethod(nonNullString);

        assertFalse(NonNullHelper.markAsNonNull(type, annotationsForMethod));
    }

    @Test
    public void testNonNullCompletionStage() throws Exception {
        Index complete = IndexCreator.index(AsyncApi.class);
        ScanningContext.register(complete);

        ClassInfo classByName = complete.getClassByName(DotName.createSimple(AsyncApi.class.getName()));
        MethodInfo nonNullString = classByName.method("nonNullCompletionStage");
        Type type = nonNullString.returnType();

        Annotations annotationsForMethod = Annotations.getAnnotationsForMethod(nonNullString);

        assertTrue(NonNullHelper.markAsNonNull(type, annotationsForMethod));
    }

    @Nested
    class PackageNonNullTest {

        private void test(String methodName, boolean expected) {
            Index complete = IndexCreator.indexWithPackage(PackageNonNullTestApi.class);
            ScanningContext.register(complete);

            ClassInfo classByName = complete.getClassByName(DotName.createSimple(PackageNonNullTestApi.class.getName()));
            MethodInfo method = classByName.method(methodName);
            Type type = method.returnType();

            Annotations annotationsForMethod = Annotations.getAnnotationsForMethod(method);

            assertEquals(expected, NonNullHelper.markAsNonNull(type, annotationsForMethod));
        }

        @Test
        public void testNonNullPerPackageString() {
            test("string", true);
        }

        @Test
        public void testOptionalString() {
            test("optionalString", false);
        }

        @Test
        public void testNonNullOptionalString() {
            test("nullableString", false);
        }
    }

    @Nested
    class ClassNonNullTest {

        private void test(String methodName, boolean expected) {
            Index complete = IndexCreator.indexWithPackage(ClassNonNullTestApi.class);
            ScanningContext.register(complete);

            ClassInfo classByName = complete.getClassByName(DotName.createSimple(ClassNonNullTestApi.class.getName()));
            MethodInfo method = classByName.method(methodName);
            Type type = method.returnType();

            Annotations annotationsForMethod = Annotations.getAnnotationsForMethod(method);

            assertEquals(expected, NonNullHelper.markAsNonNull(type, annotationsForMethod));
        }

        @Test
        public void testNonNullPerPackageString() {
            test("string", true);
        }

        @Test
        public void testOptionalString() {
            test("optionalString", false);
        }

        @Test
        public void testNonNullOptionalString() {
            test("nullableString", false);
        }
    }

}
