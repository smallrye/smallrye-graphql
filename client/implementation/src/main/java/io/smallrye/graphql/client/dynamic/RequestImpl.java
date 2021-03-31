package io.smallrye.graphql.client.dynamic;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import io.smallrye.graphql.client.Request;

public class RequestImpl implements Request {

    private final String document;
    private Map<String, Object> variables;

    public RequestImpl(String document) {
        this.document = document;
        this.variables = new HashMap<>();
    }

    public String toJson() {
        JsonObjectBuilder queryBuilder = Json.createObjectBuilder().add("query", document);
        if (!variables.isEmpty()) {
            queryBuilder.add("variables", _formatJsonVariables());
        }

        return queryBuilder.build().toString();
    }

    private JsonObject _formatJsonVariables() {
        JsonObjectBuilder varBuilder = Json.createObjectBuilder();

        variables.forEach((k, v) -> {
            // Other types to process here
            if (v instanceof String) {
                varBuilder.add(k, (String) v);
            } else if (v instanceof Integer) {
                varBuilder.add(k, (Integer) v);
            }
        });

        return varBuilder.build();
    }

    public String getDocument() {
        return document;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public Object getVariable(String key) {
        return variables.get(key);
    }

    public Request setVariable(String key, Object value) {
        variables.put(key, value);
        return this;
    }

    public Request resetVariables() {
        variables.clear();
        return this;
    }

    public String toString() {
        return "GraphQLRequest{" + "document=" + document + ", variables=" + variables + '}';
    }

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
        if (!Objects.equals(this.variables, other.variables)) {
            return false;
        }

        return true;
    }
}
