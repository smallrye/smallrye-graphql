package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.api.Context;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

public class MappingTest {

    private static final JsonNodeFactory NF = JsonNodeFactory.instance;

    @Test
    public void toMap_string() {
        Map<String, Object> expected = Collections.singletonMap("firstName", "John");
        ObjectNode jo = NF.objectNode().put("firstName", "John");
        assertEquals(expected, Context.VariablesParser.toMap(toInput(jo).get("variables")).get());
    }

    @Test
    public void toMap_boolean() {
        Map<String, Object> expected = Collections.singletonMap("certified", true);
        ObjectNode jo = NF.objectNode().put("certified", true);
        assertEquals(expected, Context.VariablesParser.toMap(toInput(jo).get("variables")).get());

        expected = Collections.singletonMap("refurbished", false);
        jo = NF.objectNode().put("refurbished", false);
        assertEquals(expected, Context.VariablesParser.toMap(toInput(jo).get("variables")).get());
    }

    @Test
    public void toMap_numbers() {
        ObjectNode jo = NF.objectNode()
                .put("weight", 17.003)
                .put("block_count", 1025)
                .put("bigNum", 1234567890987654321L)
                .put("float", 0.00000023f);
        Map<String, Object> returned = Context.VariablesParser.toMap(toInput(jo).get("variables")).get();
        assertEquals(4, returned.size());
        assertEquals(17.003, ((BigDecimal) returned.get("weight")).doubleValue(), 0.001);
        assertEquals(1025, ((BigDecimal) returned.get("block_count")).intValue());
        assertEquals(1234567890987654321L, ((BigDecimal) returned.get("bigNum")).longValue());
        assertEquals(0.00000023f, ((BigDecimal) returned.get("float")).floatValue(), 0.000001);
    }

    @Test
    public void toMap_objects() {
        Map<String, Object> expected = new HashMap<>();
        Map<String, Object> childMap = new HashMap<>();
        Map<String, Object> childMap2 = new HashMap<>();
        expected.put("customer", childMap);
        childMap.put("name", "Joe Busy");
        childMap.put("address", childMap2);
        childMap.put("acct", new BigDecimal(12345));
        childMap2.put("street_num", "1003");
        childMap2.put("street_name", "Elm Boulevard");
        childMap2.put("city", "Nowhere");
        childMap2.put("state", "AL");
        childMap2.put("zip", "99992");

        ObjectNode address = NF.objectNode()
                .put("street_num", "1003")
                .put("street_name", "Elm Boulevard")
                .put("city", "Nowhere")
                .put("state", "AL")
                .put("zip", "99992");
        ObjectNode customer = NF.objectNode()
                .put("name", "Joe Busy");
        customer.set("address", address);
        customer.put("acct", 12345);
        ObjectNode jo = NF.objectNode();
        jo.set("customer", customer);

        assertEquals(expected, Context.VariablesParser.toMap(toInput(jo).get("variables")).get());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void toMap_array() {
        ObjectNode jo = NF.objectNode();
        jo.set("names", NF.arrayNode().add("bob").add("tom").add("dick"));
        jo.set("games", NF.arrayNode().add("basketball").add("hockey").add("rugby").add("baseball"));
        jo.set("numbers", NF.arrayNode().add(3.14).add(17).add(20.003f).add(98765432123456789L));

        ObjectNode widget = NF.objectNode().put("name", "Widget").put("length", 29);
        jo.set("mixed", NF.arrayNode().add(65535).add("fred").add(true).add(widget));
        jo.set("empty", NF.arrayNode());

        Map<String, Object> returned = Context.VariablesParser.toMap(toInput(jo).get("variables")).get();
        assertEquals(5, returned.size());
        assertEquals(Arrays.asList("bob", "tom", "dick"), returned.get("names"));
        assertEquals(Arrays.asList("basketball", "hockey", "rugby", "baseball"), returned.get("games"));

        List<Object> arr = (List<Object>) returned.get("numbers");
        assertEquals(3.14, ((BigDecimal) arr.get(0)).doubleValue(), 0.0001);
        assertEquals(17, ((BigDecimal) arr.get(1)).intValue());
        assertEquals(20.003f, ((BigDecimal) arr.get(2)).floatValue(), 0.00001);
        assertEquals(98765432123456789L, ((BigDecimal) arr.get(3)).longValue());

        Map<String, Object> embeddedObject = new HashMap<>();
        embeddedObject.put("name", "Widget");
        embeddedObject.put("length", new BigDecimal(29));

        arr = (List<Object>) returned.get("mixed");
        assertEquals(65535, ((BigDecimal) arr.get(0)).intValue());
        assertEquals("fred", arr.get(1));
        assertEquals(true, arr.get(2));
        assertEquals("Widget", ((Map<String, Object>) arr.get(3)).get("name"));
        assertEquals(new BigDecimal(29), ((Map<String, Object>) arr.get(3)).get("length"));

        assertEquals(Collections.emptyList(), returned.get("empty"));
    }

    private ObjectNode toInput(ObjectNode jo) {
        ObjectNode input = NF.objectNode();
        input.set("variables", jo);
        return input;
    }
}
