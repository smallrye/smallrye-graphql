package tck.graphql.dynamic.core;

import static io.smallrye.graphql.client.core.FragmentReference.fragmentRef;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * This class tests creating fragment references (via DSL).
 */
public class FragmentReferencesTest {
    @Test
    public void fragmentReferencesShouldNotThrowExceptionForValidName() {
        assertDoesNotThrow(() -> fragmentRef("MyFragment"));
        assertDoesNotThrow(() -> fragmentRef("_myFragment"));
        assertDoesNotThrow(() -> fragmentRef("my_fragment"));
        assertDoesNotThrow(() -> fragmentRef("my123Fragment"));
        assertDoesNotThrow(() -> fragmentRef("f"));
        assertDoesNotThrow(() -> fragmentRef("_"));
        assertDoesNotThrow(() -> fragmentRef("frag_ment"));
        assertDoesNotThrow(() -> fragmentRef("one"));
    }

    @Test
    public void fragmentReferencesShouldThrowExceptionForInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> fragmentRef(""));
        assertThrows(IllegalArgumentException.class, () -> fragmentRef((String) null));
        assertThrows(IllegalArgumentException.class, () -> fragmentRef(" "));
        assertThrows(IllegalArgumentException.class, () -> fragmentRef("in validFragment"));
        assertThrows(IllegalArgumentException.class, () -> fragmentRef("invalid!"));
        assertThrows(IllegalArgumentException.class, () -> fragmentRef(":invalid"));
        assertThrows(IllegalArgumentException.class, () -> fragmentRef("in:valid"));
        assertThrows(IllegalArgumentException.class, () -> fragmentRef("inv@lid"));
        assertThrows(IllegalArgumentException.class, () -> fragmentRef("on"));
    }
}
