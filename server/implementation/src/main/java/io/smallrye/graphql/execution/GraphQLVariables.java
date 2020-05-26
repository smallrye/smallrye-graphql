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

import io.smallrye.graphql.SmallRyeGraphQLServerLogging;

/**
 * The variables on the GraphQL Operation
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class GraphQLVariables {

    public Optional<Map<String, Object>> getVariables(JsonObject jsonInput) {
        if (!jsonInput.containsKey(VARIABLES)
                || jsonInput.get(VARIABLES) == null
                || jsonInput.get(VARIABLES).getValueType().equals(JsonValue.ValueType.NULL)) {
            return Optional.empty();
        }

        JsonValue.ValueType valueType = jsonInput.get(VARIABLES).getValueType();

        if (valueType.equals(JsonValue.ValueType.STRING)) {
            String stringVars = jsonInput.getString(VARIABLES);
            SmallRyeGraphQLServerLogging.log.stringInputForVariables(stringVars);
            return Optional.empty();
        } else {
            JsonValue jvariables = jsonInput.get(VARIABLES);
            return getVariables(jvariables);
        }
    }

    private Optional<Map<String, Object>> getVariables(JsonValue jsonValue) {
        if (null != jsonValue
                && !JsonValue.NULL.equals(jsonValue)
                && !JsonValue.EMPTY_JSON_OBJECT.equals(jsonValue)
                && !JsonValue.EMPTY_JSON_ARRAY.equals(jsonValue)) {
            return Optional.of(toMap(jsonValue.asJsonObject()));
        }
        return Optional.empty();
    }

    private Map<String, Object> toMap(JsonObject jo) {
        Map<String, Object> ro = new HashMap<>();
        if (jo != null) {
            Set<Map.Entry<String, JsonValue>> entrySet = jo.entrySet();
            for (Map.Entry<String, JsonValue> es : entrySet) {
                ro.put(es.getKey(), toObject(es.getValue()));
            }
        }
        return ro;
    }

    private Object toObject(JsonValue jsonValue) {
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

    private static final String VARIABLES = "variables";
}
