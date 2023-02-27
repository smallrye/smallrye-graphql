package io.smallrye.graphql.cdi.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MicroProfileConfigTest {

    private final MicroProfileConfig config = new MicroProfileConfig();

    @Test
    void testHideErrorMessageList() {
        assertTrue(config.getHideErrorMessageList().isPresent());
        List<String> hiddenMessages = config.getHideErrorMessageList().get();
        assertEquals(List.of("a", "b", "c", "b", "c", "d"), hiddenMessages);
    }

    @Test
    void testShowErrorMessageList() {
        assertTrue(config.getShowErrorMessageList().isPresent());
        List<String> shownMessages = config.getShowErrorMessageList().get();
        assertEquals(List.of("a", "b", "c", "b", "c", "d"), shownMessages);
    }

    @Test
    void testUnwrapExceptions() {
        assertTrue(config.getUnwrapExceptions().isPresent());
        List<String> unwrappedExceptions = config.getUnwrapExceptions().get();
        assertEquals(List.of("a", "b", "c"), unwrappedExceptions);
    }

    @Test
    void testErrorExtensionFields() {
        assertTrue(config.getErrorExtensionFields().isPresent());
        List<String> errorExtensionFields = config.getErrorExtensionFields().get();
        assertEquals(List.of("a", "b", "c"), errorExtensionFields);
    }

    @Test
    void testQueryComplexityInstrumentation() {
        assertTrue(config.getQueryComplexityInstrumentation().isPresent());
        Integer queryComplexityInstrumentation = config.getQueryComplexityInstrumentation().get();
        assertEquals(1337, queryComplexityInstrumentation);
    }

    @Test
    void testQueryDepthInstrumentation() {
        assertTrue(config.getQueryDepthInstrumentation().isPresent());
        Integer queryDepthInstrumentation = config.getQueryDepthInstrumentation().get();
        assertEquals(1338, queryDepthInstrumentation);
    }

    @Test
    void testParserMaxTokens() {
        assertTrue(config.getParserMaxTokens().isPresent());
        Integer parserMaxTokens = config.getParserMaxTokens().get();
        assertEquals(1339, parserMaxTokens);
    }

    @Test
    void testParserMaxWhitespaceTokens() {
        assertTrue(config.getParserMaxWhitespaceTokens().isPresent());
        Integer parserMaxWhitespaceTokens = config.getParserMaxWhitespaceTokens().get();
        assertEquals(1340, parserMaxWhitespaceTokens);
    }

    @Test
    void testParserCaptureSourceLocation() {
        assertTrue(config.isParserCaptureSourceLocation().isPresent());
        Boolean isParserCaptureSourceLocation = config.isParserCaptureSourceLocation().get();
        assertEquals(Boolean.TRUE, isParserCaptureSourceLocation);
    }

    @Test
    void testParserCaptureLineComments() {
        assertTrue(config.isParserCaptureLineComments().isPresent());
        assertEquals(Boolean.TRUE, config.isParserCaptureLineComments().get());
    }
}