package io.smallrye.graphql.cdi.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MicroProfileConfigTest {

    @Test
    void testHideErrorMessageList() {
        MicroProfileConfig config = new MicroProfileConfig();
        assertTrue(config.getHideErrorMessageList().isPresent());
        List<String> hiddenMessages = config.getHideErrorMessageList().get();
        assertEquals(List.of("a", "b", "c", "b", "c", "d"), hiddenMessages);
    }

    @Test
    void testShowErrorMessageList() {
        MicroProfileConfig config = new MicroProfileConfig();
        assertTrue(config.getShowErrorMessageList().isPresent());
        List<String> shownMessages = config.getShowErrorMessageList().get();
        assertEquals(List.of("a", "b", "c", "b", "c", "d"), shownMessages);
    }

    @Test
    void testUnwrapExceptions() {
        MicroProfileConfig config = new MicroProfileConfig();
        assertTrue(config.getUnwrapExceptions().isPresent());
        List<String> unwrappedExceptions = config.getUnwrapExceptions().get();
        assertEquals(List.of("a", "b", "c"), unwrappedExceptions);
    }

    @Test
    void testErrorExtensionFields() {
        MicroProfileConfig config = new MicroProfileConfig();
        assertTrue(config.getErrorExtensionFields().isPresent());
        List<String> errorExtensionFields = config.getErrorExtensionFields().get();
        assertEquals(List.of("a", "b", "c"), errorExtensionFields);
    }

    @Test
    void testQueryComplexityInstrumentation() {
        MicroProfileConfig config = new MicroProfileConfig();
        assertTrue(config.getQueryComplexityInstrumentation().isPresent());
        Integer queryComplexityInstrumentation = config.getQueryComplexityInstrumentation().get();
        assertEquals(1337, queryComplexityInstrumentation);
    }
}