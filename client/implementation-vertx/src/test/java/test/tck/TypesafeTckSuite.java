package test.tck;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import tck.graphql.typesafe.TypesafeTCK;

class TypesafeTckSuite extends TypesafeTCK {
    @BeforeAll
    static void beforeAll() {
        // These properties are for the ‘TypesafeGraphQLClientBuilder#builder’ to differentiate between the typesafe-client
        // using the client model and the one that does not.

        System.clearProperty("clientModelCase");
        System.setProperty("clientModelCase", "false");
    }

    // This test aims to initialize system properties.
    // With it, the *beforeAll* method will be executed (before all the suite tests).
    @Test
    void minimalTest() {
    }
}
