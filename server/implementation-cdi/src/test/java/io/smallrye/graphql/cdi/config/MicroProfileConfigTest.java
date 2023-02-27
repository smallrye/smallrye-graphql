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
        assertEquals(List.of("a,b,c,d"), hiddenMessages);
    }

    @Test
    void testShowErrorMessageList() {
        MicroProfileConfig config = new MicroProfileConfig();
        assertTrue(config.getShowErrorMessageList().isPresent());
        List<String> shownMessages = config.getShowErrorMessageList().get();
        assertEquals(List.of("a,b,c,d"), shownMessages);
    }
}