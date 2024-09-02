package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.function.BiFunction;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.IndexView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.spi.config.Config;
import io.smallrye.graphql.test.namespace.NamedNamespaceModel;
import io.smallrye.graphql.test.namespace.NamedNamespaceWIthGroupingKeyModel;
import io.smallrye.graphql.test.namespace.SourceNamespaceModel;
import io.smallrye.graphql.test.namespace.UnamedModel;

/**
 * Test for Federated namespaces
 */
public class FederatedNamespaceTest extends ExecutionTestBase {
    private final TestConfig config = (TestConfig) Config.get();

    @AfterEach
    void tearDown() {
        config.reset();
    }

    protected IndexView getIndex() {
        config.federationEnabled = true;
        System.setProperty("smallrye.graphql.federation.enabled", "true");
        return CompositeIndex.create(Indexer.getTestIndex("io/smallrye/graphql/test/namespace"),
                Indexer.getTestIndex("io/smallrye/graphql/api/federation"));
    }

    private void test(String type, String id) {
        JsonObject jsonObject = executeAndGetData(TEST_QUERY.apply(type, id));
        assertNotNull(jsonObject);

        JsonValue jsonValue = jsonObject.get("_entities").asJsonArray().get(0).asJsonObject().get("value");
        String value = ((JsonString) jsonValue).getString();
        assertEquals(value, id);
    }

    @Test
    public void findEntityWithoutNamespace() {
        test(UnamedModel.class.getSimpleName(), "unnamed_id");
    }

    @Test
    public void findEntityWithNameNamespace() {
        test(NamedNamespaceModel.class.getSimpleName(), "named_id");
    }

    @Test
    public void findEntityWithSourceNamespace() {
        test(SourceNamespaceModel.class.getSimpleName(), "source_id");
    }

    @Test
    public void findEntityWithWithGroupedKeyAndNamespace() {
        String id = "grouped_key";

        JsonObject jsonObject = executeAndGetData(
                GROUPED_KEY_QUERY.apply(NamedNamespaceWIthGroupingKeyModel.class.getSimpleName(), id));
        assertNotNull(jsonObject);

        JsonValue jsonValue = jsonObject.get("_entities").asJsonArray().get(0).asJsonObject().get("value");
        String value = ((JsonString) jsonValue).getString();
        assertEquals(value, id);

        jsonValue = jsonObject.get("_entities").asJsonArray().get(0).asJsonObject().get("anotherId");
        String anotherId = ((JsonString) jsonValue).getString();
        assertEquals(anotherId, "otherKey_" + id);
    }

    private static final BiFunction<String, String, String> GROUPED_KEY_QUERY = (type, id) -> "query {\n" +
            "_entities(\n" +
            "    representations: { id: \"" + id + "\", anotherId : \"otherKey_" + id + "\", __typename: \"" + type + "\" }\n" +
            ") {\n" +
            "    __typename\n" +
            "    ... on " + type + " {\n" +
            "        id\n" +
            "        anotherId\n" +
            "        value\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final BiFunction<String, String, String> TEST_QUERY = (type, id) -> "query {\n" +
            "_entities(\n" +
            "    representations: { id: \"" + id + "\", __typename: \"" + type + "\" }\n" +
            ") {\n" +
            "    __typename\n" +
            "    ... on " + type + " {\n" +
            "        id\n" +
            "        value\n" +
            "    }\n" +
            "  }\n" +
            "}";
}
