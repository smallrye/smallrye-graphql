package tck.graphql.dynamic.core;

import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.core.Operation;

public class OperationsTest {
    @Test
    public void operationsShouldNotThrowExceptionForValidNameTest() {
        assertDoesNotThrow(() -> operation("myOperation"));
        assertDoesNotThrow(() -> operation("_myOperation"));
        assertDoesNotThrow(() -> operation("my_operation"));
        assertDoesNotThrow(() -> operation("my123Operation"));
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
        assertThrows(IllegalArgumentException.class, () -> operation("Invalid Name"));
        assertThrows(IllegalArgumentException.class, () -> operation(":InvalidName"));
        assertThrows(IllegalArgumentException.class, () -> operation("InvalidName:"));
        assertThrows(IllegalArgumentException.class, () -> operation("::InvalidName"));
        assertThrows(IllegalArgumentException.class, () -> operation("InvalidName::"));
        assertThrows(IllegalArgumentException.class, () -> operation("@InvalidName"));
        assertThrows(IllegalArgumentException.class, () -> operation("my.Operation"));
        assertThrows(IllegalArgumentException.class, () -> operation("my-Operation"));
    }
}
