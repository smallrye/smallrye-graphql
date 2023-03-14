package tck.graphql.dynamic.core;

import static io.smallrye.graphql.client.core.VariableType.list;
import static io.smallrye.graphql.client.core.VariableType.nonNull;
import static io.smallrye.graphql.client.core.VariableType.varType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class VariableTypesTest {
    @Test
    public void varTypesShouldNotThrowExceptionForValidName() {
        assertDoesNotThrow(() -> varType("ValidName"));
        assertDoesNotThrow(() -> varType("validName"));
        assertDoesNotThrow(() -> varType("valid_name"));
        assertDoesNotThrow(() -> varType("validName123"));
        assertDoesNotThrow(() -> varType("v"));
        assertDoesNotThrow(() -> varType("_"));
        assertDoesNotThrow(() -> varType("va_lid"));
        assertDoesNotThrow(() -> varType("_valid"));
    }

    @Test
    public void varTypesShouldThrowExceptionForInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> varType("Invalid:Name"));
        assertThrows(IllegalArgumentException.class, () -> varType("123InvalidName"));
        assertThrows(IllegalArgumentException.class, () -> varType("invalid-Name"));
        assertThrows(IllegalArgumentException.class, () -> varType("invalid name"));
        assertThrows(IllegalArgumentException.class, () -> varType("@invalidName"));
    }

    @Test
    public void nonNullsShouldNotThrowExceptionForValidName() {
        assertDoesNotThrow(() -> nonNull("ValidName"));
        assertDoesNotThrow(() -> nonNull("validName"));
        assertDoesNotThrow(() -> nonNull("valid_name"));
        assertDoesNotThrow(() -> nonNull("validName123"));
        assertDoesNotThrow(() -> nonNull("v"));
        assertDoesNotThrow(() -> nonNull("_"));
        assertDoesNotThrow(() -> nonNull("va_lid"));
        assertDoesNotThrow(() -> nonNull("_valid"));
    }

    @Test
    public void nonNullsShouldThrowExceptionForInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> nonNull("Invalid:Name"));
        assertThrows(IllegalArgumentException.class, () -> nonNull("123InvalidName"));
        assertThrows(IllegalArgumentException.class, () -> nonNull("invalid-Name"));
        assertThrows(IllegalArgumentException.class, () -> nonNull("invalid name"));
        assertThrows(IllegalArgumentException.class, () -> nonNull("@invalidName"));
    }

    @Test
    public void listsShouldNotThrowExceptionForValidName() {
        assertDoesNotThrow(() -> list("ValidName"));
        assertDoesNotThrow(() -> list("validName"));
        assertDoesNotThrow(() -> list("valid_name"));
        assertDoesNotThrow(() -> list("validName123"));
        assertDoesNotThrow(() -> list("v"));
        assertDoesNotThrow(() -> list("_"));
        assertDoesNotThrow(() -> list("va_lid"));
        assertDoesNotThrow(() -> list("_valid"));
    }

    @Test
    public void listsShouldThrowExceptionForInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> list("Invalid:Name"));
        assertThrows(IllegalArgumentException.class, () -> list("123InvalidName"));
        assertThrows(IllegalArgumentException.class, () -> list("invalidName-"));
        assertThrows(IllegalArgumentException.class, () -> list("invalid name"));
        assertThrows(IllegalArgumentException.class, () -> list("@invalidName"));
    }
}
