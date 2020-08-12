/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.smallrye.graphql.api;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import io.smallrye.common.annotation.Experimental;

/**
 * Holing context for the current request
 * There are two parts to this. The initial request, that can be a aggregation of requests, and the current execution context.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Experimental("Request context to allow downstream operations to get insight into the request. Not covered by the specification. "
        + "Subject to change.")
public interface Context {
    public static final String QUERY = "query";
    public static final String OPERATION_NAME = "operationName";
    public static final String VARIABLES = "variables";

    public enum OperationType {
        Query,
        Mutation,
        Source,
        SourceList,
        Subscription
    }

    /**
     * Get the full body of the request.
     * This includes the query, variables and operation name
     * 
     * @return JsonObject
     */
    public JsonObject getRequest();

    /**
     * Get the query part of the request.
     * TODO: Consider creating a domain object for this (rather than String).
     * 
     * @return raw string query
     */
    default String getQuery() {
        return getRequest().getString(QUERY);
    }

    /**
     * Get the operationName of the request.
     * Could be null (not set)
     * 
     * @return the operation name if set
     */
    default Optional<String> getOperationName() {
        return Optional.ofNullable(hasOperationName() ? getRequest().getString(OPERATION_NAME) : null);
    }

    /**
     * Check if the request contains an operation name
     * 
     * @return true if it does
     */
    default boolean hasOperationName() {
        return getRequest().containsKey(OPERATION_NAME)
                && getRequest().get(OPERATION_NAME) != null
                && !getRequest().get(OPERATION_NAME).getValueType().equals(JsonValue.ValueType.NULL);
    }

    /**
     * Get the variables of the request
     * Could be null (not set)
     * 
     * @return
     */
    default Optional<Map<String, Object>> getVariables() {
        if (hasVariables()) {
            JsonValue jsonValue = getRequest().get(VARIABLES);
            return VariablesParser.toMap(jsonValue);
        }
        return Optional.empty();
    }

    /**
     * Check if the request contains variables
     * 
     * @return true if it does
     */
    default boolean hasVariables() {
        return (getRequest().containsKey(VARIABLES)
                && getRequest().get(VARIABLES) != null
                && !getRequest().get(VARIABLES).getValueType().equals(JsonValue.ValueType.NULL));
    }

    /**
     * Get the execution ID.
     * 
     * @return the ID as a String
     */
    public String getExecutionId();

    /**
     * Get the field name
     * 
     * @return name of the field
     */
    public String getFieldName();

    /**
     * Return true if the argument exist
     * 
     * @param name the argument name
     * @return true if there
     */
    public boolean hasArgument(String name);

    /**
     * Get the argument using a name
     * This return the argument instance if it exists
     */
    public <T> T getArgument(String name);

    /**
     * Get a loaded instance of the operation class
     * 
     * @return instance
     */
    public Object getOperationInstance();

    /**
     * Get the applicable method that declared the current request
     * 
     * @return the applicable method
     */
    public Method getOperationMethod();

    /**
     * Get the (already transformed) arguments for the method
     * 
     * @return array of argument instances
     */
    public Object[] getOperationTransformedArguments();

    /**
     * Same as above but with the option to do a default value
     * 
     * @param <T>
     * @param name
     * @param defaultValue
     * @return the argument instance if it exists, else the provided default
     */
    default <T> T getArgumentOrDefault(String name, T defaultValue) {
        T t = getArgument(name);
        if (t == null)
            return defaultValue;
        return t;
    }

    /**
     * Get all the arguments
     * 
     * @return a map with name and instance of the argument
     */
    public Map<String, Object> getArguments();

    default boolean hasSource() {
        Object o = getSource();
        return o != null;
    }

    public <T> T getSource();

    /**
     * Return the current path
     * 
     * @return the path as a String
     */
    public String getPath();

    /**
     * Return the fields selected in the request
     * 
     * @return JsonArray of fields selected
     */
    default JsonArray getSelectedFields() {
        return getSelectedFields(false);
    }

    /**
     * Return the fields in the request
     * 
     * @param includeSourceFields should we include source fields ?
     * @return JsonArray of fields selected
     */
    public JsonArray getSelectedFields(boolean includeSourceFields);

    /**
     * Return the current type (Query, Mutation ext)
     * Current type means the type currently being executed.
     * 
     * @return enum that indicate the operation type
     */
    public OperationType getOperationType();

    /**
     * Return all the operation types requested (unique list)
     * A Request can contain more than one operation. This will return a unique list of types.
     * So if there is 2 Queries, it will only return one QUERY type, but if there is two
     * queries and a mutation, it will return QUERY,MUTATION
     * 
     * @return
     */
    public List<OperationType> getRequestedOperationTypes();

    /**
     * Return all exceptions that has happened up to now
     * 
     * @return Stack of throwable, if any
     */
    public Stack<Throwable> getExceptionStack();

    /**
     * Return if there is any exceptions
     * 
     * @return true/false
     */
    default boolean hasException() {
        return getExceptionStack() != null && !getExceptionStack().isEmpty();
    }

    /**
     * Return the type name of the parent (if any)
     * 
     * @return the parent type name maybe
     */
    public Optional<String> getParentTypeName();

    /**
     * This leaky abstraction allows falling down to the underlying implementation
     * 
     * @param <T> the implementation class
     * @param wrappedType the class type of T
     * @return instance of the implementation class
     */
    public <T> T unwrap(Class<T> wrappedType);

    /**
     * Help to parse the variables
     */
    class VariablesParser {

        public static Optional<Map<String, Object>> toMap(JsonValue jsonValue) {
            if (null != jsonValue
                    && !JsonValue.NULL.equals(jsonValue)
                    && !JsonValue.EMPTY_JSON_OBJECT.equals(jsonValue)
                    && !JsonValue.EMPTY_JSON_ARRAY.equals(jsonValue)) {
                return Optional.of(toMap(jsonValue.asJsonObject()));
            }
            return Optional.empty();
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
}
