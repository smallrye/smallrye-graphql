package io.smallrye.graphql.schema.creator;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Optional;

import org.jboss.jandex.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.smallrye.graphql.schema.IndexCreator;
import io.smallrye.graphql.schema.ScanningContext;
import io.smallrye.graphql.schema.helper.Direction;
import io.smallrye.graphql.schema.helper.TypeAutoNameStrategy;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Reference;

class FieldCreatorTest {

    private final ReferenceCreator referenceCreator = new ReferenceCreator(TypeAutoNameStrategy.Default);
    private final FieldCreator fieldCreator = new FieldCreator(referenceCreator);

    /**
     * Tests the visibility of fields with certain field- and method-modifier combinations.
     *
     * @param direction the direction of the graphql-field to test
     * @param fieldName the field name, may be null or empty if just a method is provided
     * @param methodName the method name, may be null or empty if just a method is provided
     * @param expected the expected visibility
     */
    @ParameterizedTest
    @CsvSource(value = {
            " IN, PUBLIC_STATIC_FIELD, , false",
            " IN, PUBLIC_STATIC_FIELD, setField, true",
            " IN, privateField, ,false",
            " IN, publicFinalField, , false",
            " IN, publicFinalField, setField, true",
            " IN, publicField, , true",
            " IN, publicTransientField, , false",
            " IN, publicTransientField, setField, false",
            " IN, ,setStaticField, false",

            "OUT, PUBLIC_STATIC_FIELD, , false",
            "OUT, PUBLIC_STATIC_FIELD, getField, true",
            "OUT, privateField, , false",
            "OUT, publicFinalField, , true",
            "OUT, publicField, , true",
            "OUT, publicTransientField, , false",
            "OUT, publicTransientField, getField, false",
            "OUT, ,getStaticField, false",
    })
    void testVisibility(Direction direction, String fieldName, String methodName, boolean expected) throws IOException {
        MethodInfo method = getMethod(methodName);
        FieldInfo fieldInfo = getField(fieldName);

        final boolean isVisible = FieldCreator.isGraphQlField(direction, fieldInfo, method);
        assertEquals(expected, isVisible);
    }

    @ParameterizedTest
    @CsvSource(value = {
            " IN, PUBLIC_STATIC_FIELD, false",
            " IN, privateField, false",
            " IN, publicFinalField, false",
            " IN, publicField, true",
            "OUT, PUBLIC_STATIC_FIELD, false",
            "OUT, privateField, false",
            "OUT, publicFinalField, true",
            "OUT, publicField, true",
    })
    void testFieldsWithoutMethod(Direction direction, String fieldName, boolean expected) throws IOException {
        FieldInfo field = getField(fieldName);

        final Optional<Field> fieldForPojo = fieldCreator.createFieldForPojo(direction, field, new Reference());

        assertEquals(expected, fieldForPojo.isPresent());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "OUT, getStaticField, false",
            " IN, setStaticField, false",
    })
    void testMethodsWithoutField(Direction direction, String methodName, boolean expected) throws IOException {
        MethodInfo method = getMethod(methodName);

        final Optional<Field> fieldForPojo = fieldCreator.createFieldForPojo(direction, null, method, new Reference());

        assertEquals(expected, fieldForPojo.isPresent());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "OUT, privateField, getField, true",
            " IN, privateField, setField, true",
    })
    void testMethodsWithField(Direction direction, String fieldName, String methodName, boolean expected) throws IOException {
        MethodInfo method = getMethod(methodName);
        FieldInfo fieldInfo = getField(fieldName);

        final boolean isVisible = FieldCreator.isGraphQlField(direction, fieldInfo, method);
        assertEquals(expected, isVisible);
        //final Optional<Field> fieldForPojo = fieldCreator.createFieldForPojo(direction, null, method, new Reference());
        //assertEquals(expected, fieldForPojo.isPresent());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "OUT, getStaticField, false",
    })
    void testInterfaceMethods(Direction direction, String methodName, boolean expected) throws IOException {
        MethodInfo method = getMethod(methodName);

        final Optional<Field> fieldForPojo = fieldCreator.createFieldForInterface(method, new Reference());
        assertEquals(expected, fieldForPojo.isPresent());
    }

    private static FieldInfo getField(String name) throws IOException {
        if (name == null || name.isEmpty()) {
            return null;
        }
        Index complete = IndexCreator.index(SimplePojo.class);
        ScanningContext.register(complete);

        ClassInfo classByName = complete.getClassByName(DotName.createSimple(SimplePojo.class.getName()));
        return classByName.field(name);
    }

    private static MethodInfo getMethod(String name) throws IOException {
        if (name == null || name.isEmpty()) {
            return null;
        }
        Index complete = IndexCreator.index(SimplePojo.class);
        ScanningContext.register(complete);

        ClassInfo classByName = complete.getClassByName(DotName.createSimple(SimplePojo.class.getName()));
        return classByName.firstMethod(name);
    }

    @AfterEach
    void tearDown() {
        ScanningContext.remove();
    }

}
