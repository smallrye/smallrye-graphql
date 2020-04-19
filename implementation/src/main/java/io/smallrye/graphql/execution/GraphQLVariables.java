package io.smallrye.graphql.execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 * The variables on the GraphQL Operation
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class GraphQLVariables {

    public static Optional<Map<String, Object>> getVariables(JsonValue jsonValue) {
        if (null != jsonValue
                && !JsonValue.NULL.equals(jsonValue)
                && !JsonValue.EMPTY_JSON_OBJECT.equals(jsonValue)
                && !JsonValue.EMPTY_JSON_ARRAY.equals(jsonValue)) {
            return Optional.of(toMap(jsonValue.asJsonObject()));
        }
        return Optional.empty();
    }

    private GraphQLVariables() {
    }

    private static Map<String, Object> toMap(JsonObject jo) {
        Map<String, Object> ro = new HashMap<>();
        if (jo != null) {
            Set<Map.Entry<String, JsonValue>> entrySet = jo.entrySet();
            for (Map.Entry<String, JsonValue> es : entrySet) {
                ro.put(es.getKey(), toObject(es.getValue()));
            }
        }
        return ro;
    }

    private static Object toObject(JsonValue jsonValue) {
        Object ret = null;
        JsonValue.ValueType typ = jsonValue.getValueType();
        if (null != typ)
            switch (typ) {
                case NUMBER:
                    ret = ((JsonNumber) jsonValue).bigDecimalValue();
                    break;
                case STRING:
                    ret = ((JsonString) jsonValue).getString();
                    break;
                case FALSE:
                    ret = Boolean.FALSE;
                    break;
                case TRUE:
                    ret = Boolean.TRUE;
                    break;
                case ARRAY:
                    JsonArray arr = (JsonArray) jsonValue;
                    List<Object> vals = new ArrayList<>();
                    int sz = arr.size();
                    for (int i = 0; i < sz; i++) {
                        JsonValue v = arr.get(i);
                        vals.add(toObject(v));
                    }
                    ret = vals;
                    break;
                case OBJECT:
                    ret = toMap((JsonObject) jsonValue);
                    break;
                default:
                    break;
            }
        return ret;
    }

}
