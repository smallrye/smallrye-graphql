package io.smallrye.graphql.schema;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.schema.helper.package_nonnull.PackageNonNullTestApi;

class AnnotationsTest {

    @AfterEach
    void tearDown() {
        ScanningContext.remove();
    }

    @Test
    void name() {
        final Index index = IndexCreator.indexWithPackage(PackageNonNullTestApi.class);
        ScanningContext.register(index);

        ClassInfo classByName = index.getClassByName(DotName.createSimple(PackageNonNullTestApi.class.getName()));
        MethodInfo optionalString = classByName.method("optionalString");

        final Annotations annotationsForMethod = Annotations.getAnnotationsForMethod(optionalString);
        assertFalse(annotationsForMethod.parentAnnotations.isEmpty());
    }
}
