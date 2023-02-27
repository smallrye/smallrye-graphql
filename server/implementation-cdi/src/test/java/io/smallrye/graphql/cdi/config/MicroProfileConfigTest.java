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
        assertEquals(List.of("a", "b", "c", "b", "c", "d"), config.getHideErrorMessageList().get());
    }

    @Test
    void testShowErrorMessageList() {
        assertTrue(config.getShowErrorMessageList().isPresent());
        assertEquals(List.of("a", "b", "c", "b", "c", "d"), config.getShowErrorMessageList().get());
    }

    @Test
    void testUnwrapExceptions() {
        assertTrue(config.getUnwrapExceptions().isPresent());
        assertEquals(List.of("a", "b", "c"), config.getUnwrapExceptions().get());
    }

    @Test
    void testErrorExtensionFields() {
        assertTrue(config.getErrorExtensionFields().isPresent());
        assertEquals(List.of("a", "b", "c"), config.getErrorExtensionFields().get());
    }

    @Test
    void testQueryComplexityInstrumentation() {
        assertTrue(config.getQueryComplexityInstrumentation().isPresent());
        assertEquals(1337, config.getQueryComplexityInstrumentation().get());
    }

    @Test
    void testQueryDepthInstrumentation() {
        assertTrue(config.getQueryDepthInstrumentation().isPresent());
        assertEquals(1338, config.getQueryDepthInstrumentation().get());
    }

    @Test
    void testParserMaxTokens() {
        assertTrue(config.getParserMaxTokens().isPresent());
        assertEquals(1339, config.getParserMaxTokens().get());
    }

    @Test
    void testParserMaxWhitespaceTokens() {
        assertTrue(config.getParserMaxWhitespaceTokens().isPresent());
        assertEquals(1340, config.getParserMaxWhitespaceTokens().get());
    }

    @Test
    void testParserCaptureSourceLocation() {
        assertTrue(config.isParserCaptureSourceLocation().isPresent());
        assertEquals(Boolean.TRUE, config.isParserCaptureSourceLocation().get());
    }

    @Test
    void testParserCaptureLineComments() {
        assertTrue(config.isParserCaptureLineComments().isPresent());
        assertEquals(Boolean.TRUE, config.isParserCaptureLineComments().get());
    }

    @Test
    void testParserCaptureIgnoredChars() {
        assertTrue(config.isParserCaptureIgnoredChars().isPresent());
        assertEquals(Boolean.TRUE, config.isParserCaptureIgnoredChars().get());
    }
}