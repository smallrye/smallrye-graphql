package io.smallrye.graphql.schema;

import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ClassesTest {

    enum TestEnum {
        A, B, C;
    }

    @Test
    public void isEnumAcceptsNullParameter() throws Exception {
        assertFalse(Classes.isEnum(null));
    }

    @Test
    public void objectIsNotEnum() throws Exception {
        Index index = IndexCreator.index(Object.class);
        assertFalse(Classes.isEnum(index.getClassByName(DotName.createSimple(Object.class.getName()))));
    }

    @Test
    public void testEnumIsEnum() throws Exception {
        Index index = IndexCreator.index(TestEnum.class);
        assertTrue(Classes.isEnum(index.getClassByName(DotName.createSimple(TestEnum.class.getName()))));
    }

}