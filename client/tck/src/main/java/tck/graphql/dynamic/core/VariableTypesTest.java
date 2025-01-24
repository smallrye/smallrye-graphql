package tck.graphql.dynamic.core;

import static io.smallrye.graphql.client.core.VariableType.list;
import static io.smallrye.graphql.client.core.VariableType.nonNull;
import static io.smallrye.graphql.client.core.VariableType.varType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class VariableTypesTest {
    private static final String TYPE_NAME_URL = "https://spec.graphql.org/draft/#sec-Type-References";
    private static final String LIST = "io.smallrye.graphql.client.core.VariableType.list";
    private static final String NON_NULL = "io.smallrye.graphql.client.core.VariableType.nonNull";

    @Test
    public void varTypesShouldNotThrowExceptionForValidName() {
        assertDoesNotThrow(() -> varType("ValidName"));
        assertDoesNotThrow(() -> varType("validName"));
        assertDoesNotThrow(() -> varType("valid_name"));
        assertDoesNotThrow(() -> varType("validName123"));
        assertDoesNotThrow(() -> varType("v"));
        assertDoesNotThrow(() -> varType("_"));
        assertDoesNotThrow(() -> varType("va_lid"));
        assertDoesNotThrow(() -> varType(" ,va_lid,, "));
        assertDoesNotThrow(() -> varType("_valid"));
    }

    @Test
    public void varTypesShouldThrowExceptionForInvalidName() {
        assertThrowsWithUrlMessage(() -> varType("Invalid:Name"));
        assertThrowsWithUrlMessage(() -> varType("Invalid,Name"));
        assertThrowsWithUrlMessage(() -> varType("123InvalidName"));
        assertThrowsWithUrlMessage(() -> varType("invalid-Name"));
        assertThrowsWithUrlMessage(() -> varType("invalid name"));
        assertThrowsWithUrlMessage(() -> varType("@invalidName"));
        assertThrowsWithListMessage(() -> varType("[invalidName]"));
        assertThrowsWithNonNullMessage(() -> varType("invalidName!"));
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
        assertDoesNotThrow(() -> nonNull(" ,va_lid,, "));
        assertDoesNotThrow(() -> nonNull("_valid"));
    }

    @Test
    public void nonNullsShouldThrowExceptionForInvalidName() {
        assertThrowsWithUrlMessage(() -> nonNull("Invalid:Name"));
        assertThrowsWithUrlMessage(() -> nonNull("Invalid,Name"));
        assertThrowsWithUrlMessage(() -> nonNull("123InvalidName"));
        assertThrowsWithUrlMessage(() -> nonNull("invalid-Name"));
        assertThrowsWithUrlMessage(() -> nonNull("invalid name"));
        assertThrowsWithUrlMessage(() -> nonNull("@invalidName"));
        assertThrowsWithListMessage(() -> nonNull("[invalidName]"));
        assertThrowsWithNonNullMessage(() -> nonNull("invalidName!"));
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
        assertDoesNotThrow(() -> list(" ,va_lid,, "));
        assertDoesNotThrow(() -> list("_valid"));
    }

    @Test
    public void listsShouldThrowExceptionForInvalidName() {
        assertThrowsWithUrlMessage(() -> list("Invalid:Name"));
        assertThrowsWithUrlMessage(() -> list("Invalid,Name"));
        assertThrowsWithUrlMessage(() -> list("123InvalidName"));
        assertThrowsWithUrlMessage(() -> list("invalid-Name"));
        assertThrowsWithUrlMessage(() -> list("invalid name"));
        assertThrowsWithUrlMessage(() -> list("@invalidName"));
        assertThrowsWithListMessage(() -> list("[invalidName]"));
        assertThrowsWithNonNullMessage(() -> list("invalidName!"));
    }

    private void assertThrowsWithUrlMessage(Executable lambda) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, lambda);
        assertTrue(ex.getMessage().contains(TYPE_NAME_URL),
                "Expected the '%s' message to contain '%s'".formatted(ex.getMessage(), TYPE_NAME_URL));
    }

    private void assertThrowsWithListMessage(Executable lambda) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, lambda);
        assertTrue(ex.getMessage().contains(LIST),
                "Expected the '%s' message to contain '%s'".formatted(ex.getMessage(), LIST));
    }

    private void assertThrowsWithNonNullMessage(Executable lambda) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, lambda);
        assertTrue(ex.getMessage().contains(NON_NULL),
                "Expected the '%s' message to contain '%s'".formatted(ex.getMessage(), NON_NULL));
    }
}
