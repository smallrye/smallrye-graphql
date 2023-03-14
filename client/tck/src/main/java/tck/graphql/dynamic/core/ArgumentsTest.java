package tck.graphql.dynamic.core;

import static io.smallrye.graphql.client.core.Argument.arg;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * This class tests creating arguments (via DSL).
 */

public class ArgumentsTest {

    @Test
    public void argumentShouldNotThrowExceptionForValidNameTest() {
        float value = 3.14f;
        assertDoesNotThrow(() -> arg("myArg", value));
        assertDoesNotThrow(() -> arg("_myArg", value));
        assertDoesNotThrow(() -> arg("my_arg", value));
        assertDoesNotThrow(() -> arg("my123Arg", value));
        assertDoesNotThrow(() -> arg("a", value));
        assertDoesNotThrow(() -> arg("_", value));
        assertDoesNotThrow(() -> arg("ARGUMENT_", value));
        assertDoesNotThrow(() -> arg("arg_ument", value));
    }

    @Test
    public void argumentShouldThrowExceptionForInvalidNameTest() {
        int value = 1;
        assertThrows(IllegalArgumentException.class, () -> arg("", value));
        assertThrows(IllegalArgumentException.class, () -> arg(" ", value));
        assertThrows(IllegalArgumentException.class, () -> arg(null, value));
        assertThrows(IllegalArgumentException.class, () -> arg("invalid name", value));
        assertThrows(IllegalArgumentException.class, () -> arg("1myArg", value));
        assertThrows(IllegalArgumentException.class, () -> arg(":myArg", value));
        assertThrows(IllegalArgumentException.class, () -> arg("myArg:", value));
        assertThrows(IllegalArgumentException.class, () -> arg("myArg::invalid", value));
        assertThrows(IllegalArgumentException.class, () -> arg("@myArg", value));
    }
}
