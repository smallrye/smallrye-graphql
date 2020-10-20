package io.smallrye.graphql.schema;

import static org.junit.jupiter.api.Assertions.*;

import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;

public class ClassesTest {

    enum TestEnum {
        A,
        B,
        C;
    }

    @Test
    public void isEnumAcceptsNullParameter() {
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
