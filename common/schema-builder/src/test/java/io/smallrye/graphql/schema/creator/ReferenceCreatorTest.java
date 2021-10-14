package io.smallrye.graphql.schema.creator;

import static org.junit.jupiter.api.Assertions.*;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.IndexCreator;
import io.smallrye.graphql.schema.ScanningContext;
import io.smallrye.graphql.schema.helper.TypeAutoNameStrategy;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ReferenceCreatorTest {

    interface GenericInterface<T> {
    }

    public static class TestOps {
        public GenericInterface<String> getGenericInterface() {
            return null;
        }

        public GenericInterface<? super String> getWildcardInterfaceSuper() {
            return null;
        }

        public GenericInterface<? extends String> getWildcardInterfaceExtends() {
            return null;
        }
    }

    public static class SpecializedImplementorOfGenericInterface implements GenericInterface<String> {
    }

    @ParameterizedTest
    @ValueSource(strings = { "getGenericInterface", "getWildcardInterfaceSuper", "getWildcardInterfaceExtends" })
    public void shouldCreateReferenceDespiteSpecializedImplementationOfInterface(String methodName) throws Exception {
        try {
            ReferenceCreator referenceCreator = new ReferenceCreator(TypeAutoNameStrategy.Full);

            Index index = IndexCreator.index(TestOps.class, GenericInterface.class,
                    SpecializedImplementorOfGenericInterface.class, java.lang.String.class);
            ScanningContext.register(index);

            ClassInfo testOps = index.getClassByName(DotName.createSimple(TestOps.class.getName()));
            MethodInfo method = testOps.method(methodName);
            Reference reference = referenceCreator.createReferenceForOperationField(method.returnType(),
                    Annotations.getAnnotationsForMethod(method));

            assertEquals("io_smallrye_graphql_schema_creator_ReferenceCreatorTestGenericInterface_String", reference.getName());
            assertEquals("io.smallrye.graphql.schema.creator.ReferenceCreatorTest$GenericInterface", reference.getClassName());
            assertEquals("io.smallrye.graphql.schema.creator.ReferenceCreatorTest$GenericInterface",
                    reference.getGraphQlClassName());
            assertEquals(ReferenceType.INTERFACE, reference.getType());
            assertNull(reference.getMapping());
            assertFalse(reference.getParametrizedTypeArguments().isEmpty());
            assertTrue(reference.isAddParametrizedTypeNameExtension());
        } finally {
            ScanningContext.remove();
        }
    }
}
