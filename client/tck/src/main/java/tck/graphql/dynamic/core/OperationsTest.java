package tck.graphql.dynamic.core;

import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import io.smallrye.graphql.client.core.Operation;

public class OperationsTest {

    private static final String NAME_URL = "https://spec.graphql.org/draft/#Name";

    @Test
    public void operationsShouldNotThrowExceptionForValidNameTest() {
        assertDoesNotThrow(() -> operation("myOperation"));
        assertDoesNotThrow(() -> operation("_myOperation"));
        assertDoesNotThrow(() -> operation("my_operation"));
        assertDoesNotThrow(() -> operation("my123Operation"));
        assertDoesNotThrow(() -> operation(",, ,myOperation ,"));
        assertDoesNotThrow(() -> operation("o"));
        assertDoesNotThrow(() -> operation("_"));
        assertDoesNotThrow(() -> operation("op_eration"));
        assertDoesNotThrow(() -> operation(""));
        assertDoesNotThrow(() -> {
            Operation operation = operation((String) null);
            assertEquals(operation.getName(), "");
        });
    }

    @Test
    public void operationsShouldThrowExceptionForInvalidNameTest() {
        assertThrowsWithUrlMessage(() -> operation("Invalid Name"));
        assertThrowsWithUrlMessage(() -> operation(":InvalidName"));
        assertThrowsWithUrlMessage(() -> operation("InvalidName:"));
        assertThrowsWithUrlMessage(() -> operation("::InvalidName"));
        assertThrowsWithUrlMessage(() -> operation("InvalidName::"));
        assertThrowsWithUrlMessage(() -> operation("@InvalidName"));
        assertThrowsWithUrlMessage(() -> operation("my.Operation"));
        assertThrowsWithUrlMessage(() -> operation("my,Operation"));
        assertThrowsWithUrlMessage(() -> operation("my-Operation"));
        assertThrowsWithUrlMessage(() -> operation("[myOperation]"));
        assertThrowsWithUrlMessage(() -> operation("myOperation!"));
    }

    private void assertThrowsWithUrlMessage(Executable lambda) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, lambda);
        assertTrue(ex.getMessage().contains(NAME_URL),
                "Expected the '%s' message to contain '%s'".formatted(ex.getMessage(), NAME_URL));
    }
}
