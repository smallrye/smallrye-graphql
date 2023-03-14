package tck.graphql.dynamic.core;

import static io.smallrye.graphql.client.core.InputObjectField.prop;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class InputObjectFieldsTest {
    @Test
    public void inputObjectFieldsShouldNotThrowExceptionForValidNameTest() {
        assertDoesNotThrow(() -> prop("myProp", "value"));
        assertDoesNotThrow(() -> prop("_myProp", 1));
        assertDoesNotThrow(() -> prop("my_prop", 3.14));
        assertDoesNotThrow(() -> prop("my123Prop", true));
        assertDoesNotThrow(() -> prop("p", "string"));
        assertDoesNotThrow(() -> prop("_", "string"));
        assertDoesNotThrow(() -> prop("pro_p", "string"));
    }

    @Test
    public void inputObjectFieldsShouldThrowExceptionForInvalidNameTest() {
        assertThrows(IllegalArgumentException.class, () -> prop("", "value"));
        assertThrows(IllegalArgumentException.class, () -> prop(null, "string"));
        assertThrows(IllegalArgumentException.class, () -> prop("my:prop", 1));
        assertThrows(IllegalArgumentException.class, () -> prop("@myProp", 3.14));
        assertThrows(IllegalArgumentException.class, () -> prop("myProp!", true));
        assertThrows(IllegalArgumentException.class, () -> prop(" ", "string"));
    }
}
