package tck.graphql.dynamic.core;

import static io.smallrye.graphql.client.core.DirectiveArgument.directiveArg;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * This class tests creating arguments used in directives (via DSL).
 */
public class DirectiveArgumentsTest {
    @Test
    public void directiveArgumentShouldNotThrowExceptionForValidNameTest() {
        String value = "3.14";
        assertDoesNotThrow(() -> directiveArg("myArg", value));
        assertDoesNotThrow(() -> directiveArg("_myArg", value));
        assertDoesNotThrow(() -> directiveArg("my_arg", value));
        assertDoesNotThrow(() -> directiveArg("my123Arg", value));
        assertDoesNotThrow(() -> directiveArg("a", value));
        assertDoesNotThrow(() -> directiveArg("_", value));
        assertDoesNotThrow(() -> directiveArg("arg_ument", value));
    }

    @Test
    public void directiveArgumentShouldThrowExceptionForInvalidNameTest() {
        String value = "3.14";
        assertThrows(IllegalArgumentException.class, () -> directiveArg(null, value));
        assertThrows(IllegalArgumentException.class, () -> directiveArg("", value));
        assertThrows(IllegalArgumentException.class, () -> directiveArg(" ", value));
        assertThrows(IllegalArgumentException.class, () -> directiveArg("Invalid-Name", value));
        assertThrows(IllegalArgumentException.class, () -> directiveArg(":InvalidName", value));
        assertThrows(IllegalArgumentException.class, () -> directiveArg("InvalidName:", value));
        assertThrows(IllegalArgumentException.class, () -> directiveArg("Invalid::Name", value));
        assertThrows(IllegalArgumentException.class, () -> directiveArg("@InvalidName", value));
    }

}
