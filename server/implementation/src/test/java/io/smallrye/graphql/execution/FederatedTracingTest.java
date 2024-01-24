package io.smallrye.graphql.execution;

import io.smallrye.graphql.spi.config.Config;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.apollographql.federation.graphqljava.tracing.FederatedTracingInstrumentation.FEDERATED_TRACING_HEADER_NAME;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for Federated tracing
 *
 * @author Rok Miklavčič (rokmiklavcic9@gmail.com)
 */
public class FederatedTracingTest extends ExecutionTestBase {
    private final TestConfig config = (TestConfig) Config.get();

    @AfterEach
    void tearDown() {
        config.reset();
    }

    @Test
    public void testFederatedTracingEnabledWithFederationDisabled() {
        config.federationEnabled = false;
        String ftKey = "ftv1";

        Map<String, Object> metaData = createMetaData(ftKey);

        JsonObject extensions = executeAndGetExtensions(TEST_QUERY, metaData);
        assertNull(extensions);
    }

    @Test
    public void testFederatedTracingEnabledWithFederationEnabled() {
        config.federationEnabled = true;
        String ftKey = "ftv1";

        Map<String, Object> metaData = createMetaData(ftKey);

        JsonObject extensions = executeAndGetExtensions(TEST_QUERY, metaData);
        assertNotNull(extensions);
        assertTrue(extensions.containsKey(ftKey));

        JsonString ftValue = extensions.getJsonString(ftKey);
        assertNotNull(ftValue);
        assertFalse(ftValue.getString().isEmpty());
    }

    @Test
    public void testFederatedTracingDisabledWithFederationEnabled() {
        config.federationEnabled = true;
        String ftKey = "invalidKey";

        Map<String, Object> metaData = createMetaData(ftKey);

        JsonObject extensions = executeAndGetExtensions(TEST_QUERY, metaData);
        assertNull(extensions);
    }

    private Map<String, Object> createMetaData(String ftKey) {
        Map<String, Object> metaData = new ConcurrentHashMap<>();
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(FEDERATED_TRACING_HEADER_NAME, List.of(ftKey));
        metaData.put("httpHeaders", headers);
        return metaData;
    }

    private static final String TEST_QUERY = "{\n" +
            "  _service{\n" +
            "    sdl\n" +
            "  }\n" +
            "}";
}
