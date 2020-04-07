/*
 * Copyright 2020 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.smallrye.graphql.execution;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;

public class ExecutionServiceTest {

    private static Map<String, Object> toMap(JsonObject jo) {
        return new ExecutionService().toMap(jo);
    }

    @Test
    public void toMap_string() {
        Map<String, Object> expected = Collections.singletonMap("firstName", "John");
        JsonObject jo = Json.createObjectBuilder()
                .add("firstName", "John")
                .build();
        assertEquals(expected, toMap(jo));
    }

    @Test
    public void toMap_boolean() {
        Map<String, Object> expected = Collections.singletonMap("certified", true);
        JsonObject jo = Json.createObjectBuilder().add("certified", true).build();
        assertEquals(expected, toMap(jo));

        expected = Collections.singletonMap("refurbished", false);
        jo = Json.createObjectBuilder().add("refurbished", false).build();
        assertEquals(expected, toMap(jo));
    }

    @Test
    public void toMap_numbers() {
        JsonObject jo = Json.createObjectBuilder()
                .add("weight", 17.003)
                .add("block_count", 1025)
                .add("bigNum", 1234567890987654321L)
                .add("float", 0.00000023f)
                .build();
        Map<String, Object> returned = toMap(jo);
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

        JsonObject jo = Json.createObjectBuilder()
                .add("customer", Json.createObjectBuilder()
                        .add("name", "Joe Busy")
                        .add("address", Json.createObjectBuilder()
                                .add("street_num", "1003")
                                .add("street_name", "Elm Boulevard")
                                .add("city", "Nowhere")
                                .add("state", "AL")
                                .add("zip", "99992")
                                .build())
                        .add("acct", 12345)
                        .build())
                .build();
        assertEquals(expected, toMap(jo));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void toMap_array() {
        JsonObject jo = Json.createObjectBuilder()
                .add("names", Json.createArrayBuilder()
                        .add("bob")
                        .add("tom")
                        .add("dick")
                        .build())
                .add("games", Json.createArrayBuilder()
                        .add("basketball")
                        .add("hockey")
                        .add("rugby")
                        .add("baseball")
                        .build())
                .add("numbers", Json.createArrayBuilder()
                        .add(3.14)
                        .add(17)
                        .add(20.003f)
                        .add(98765432123456789L)
                        .build())
                .add("mixed", Json.createArrayBuilder()
                        .add(65535)
                        .add("fred")
                        .add(true)
                        .add(Json.createObjectBuilder()
                                .add("name", "Widget")
                                .add("length", 29)
                                .build())
                        .build())
                .add("empty", Json.createArrayBuilder().build())
                .build();
        Map<String, Object> returned = toMap(jo);
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
}
