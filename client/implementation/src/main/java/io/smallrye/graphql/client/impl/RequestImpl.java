package io.smallrye.graphql.client.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import org.eclipse.yasson.internal.JsonBinding;

import io.smallrye.graphql.client.Request;

public class RequestImpl implements Request {

    private final String document;
    private Map<String, Object> variables;
    private String operationName;

    public RequestImpl(String document) {
        this.document = document;
        this.variables = new HashMap<>();
    }

    @Override
    public String toJson() {
        JsonObjectBuilder queryBuilder = Json.createObjectBuilder().add("query", document);
        if (!variables.isEmpty()) {
            queryBuilder.add("variables", _formatJsonVariables());
        }
        if (operationName != null && !operationName.isEmpty()) {
            queryBuilder.add("operationName", operationName);
        }
        return queryBuilder.build().toString();
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObjectBuilder queryBuilder = Json.createObjectBuilder().add("query", document);
        if (!variables.isEmpty()) {
            queryBuilder.add("variables", _formatJsonVariables());
        }
        if (operationName != null && !operationName.isEmpty()) {
            queryBuilder.add("operationName", operationName);
        }
        return queryBuilder.build();
    }

    private JsonObject _formatJsonVariables() {
        JsonObjectBuilder varBuilder = Json.createObjectBuilder();

        variables.forEach((k, v) -> {
            // Other types to process here
            if (v instanceof String) {
                varBuilder.add(k, (String) v);
            } else if (v instanceof Integer) {
                varBuilder.add(k, (Integer) v);
            } else if (v instanceof JsonValue) {
                varBuilder.add(k, (JsonValue) v);
            } else {
                try (Jsonb jsonb = JsonbBuilder.create()) {
                    JsonStructure struct = ((JsonBinding) jsonb).toJsonStructure(v);
                    varBuilder.add(k, struct);
                } catch (Exception ignore) {
                }
            }
        });

        return varBuilder.build();
    }

    @Override
    public String getDocument() {
        return document;
    }

    @Override
    public Map<String, Object> getVariables() {
        return variables;
    }

    @Override
    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    @Override
    public Object getVariable(String key) {
        return variables.get(key);
    }

    @Override
    public Request setVariable(String key, Object value) {
        variables.put(key, value);
        return this;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    @Override
    public Request resetVariables() {
        variables.clear();
        return this;
    }

    @Override
    public String toString() {
        return "GraphQLRequest{" + "document=" + document + ", variables=" + variables + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final RequestImpl other = (RequestImpl) obj;
        if (!Objects.equals(this.document, other.document)) {
            return false;
        }
        return Objects.equals(this.variables, other.variables);
    }
}
