package io.smallrye.graphql.client.impl;

import static io.smallrye.graphql.client.impl.JsonProviderHolder.JSON_PROVIDER;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import io.smallrye.graphql.client.Request;

public class RequestImpl implements Request {
    private static final JsonBuilderFactory JSON = JSON_PROVIDER.createBuilderFactory(null);
    private static final Jsonb JSONB = JsonbBuilder.create();

    private final String document;
    private Map<String, Object> variables;
    private String operationName;
    private Map<String, Object> extensions;

    public RequestImpl(String document) {
        this.document = document;
        this.variables = new HashMap<>();
    }

    public static Builder builder(String query) {
        return new Builder(query);
    }

    public static class Builder {
        private final String query;
        private Map<String, Object> variables;
        private String operationName;
        private Map<String, Object> extensions;

        private Builder(String query) {
            this.query = query;
        }

        public Builder variables(Map<String, Object> variables) {
            this.variables = variables;
            return this;
        }

        public Builder operationName(String operationName) {
            this.operationName = operationName;
            return this;
        }

        public Builder extensions(Map<String, Object> extensions) {
            this.extensions = extensions;
            return this;
        }

        public RequestImpl build() {
            RequestImpl request = new RequestImpl(query);
            if (variables != null) {
                request.setVariables(variables);
            }
            if (operationName != null && !operationName.isEmpty()) {
                request.setOperationName(operationName);
            }
            if (extensions != null && !extensions.isEmpty()) {
                request.setExtensions(extensions);
            }
            return request;
        }
    }

    @Override
    public String toJson() {
        JsonObjectBuilder queryBuilder = JSON.createObjectBuilder().add("query", document);
        if (!variables.isEmpty()) {
            queryBuilder.add("variables", _formatJsonVariables());
        }
        if (operationName != null && !operationName.isEmpty()) {
            queryBuilder.add("operationName", operationName);
        }
        if (extensions != null && !extensions.isEmpty()) {
            queryBuilder.add("extensions", _formatJsonMap(extensions));
        }
        return queryBuilder.build().toString();
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObjectBuilder queryBuilder = JSON.createObjectBuilder().add("query", document);
        if (!variables.isEmpty()) {
            queryBuilder.add("variables", _formatJsonVariables());
        }
        if (operationName != null && !operationName.isEmpty()) {
            queryBuilder.add("operationName", operationName);
        }
        if (extensions != null && !extensions.isEmpty()) {
            queryBuilder.add("extensions", _formatJsonMap(extensions));
        }
        return queryBuilder.build();
    }

    private JsonObject _formatJsonVariables() {
        return _formatJsonMap(variables);
    }

    private static JsonObject _formatJsonMap(Map<String, Object> map) {
        JsonObjectBuilder builder = JSON.createObjectBuilder();

        map.forEach((k, v) -> {
            if (v instanceof String) {
                builder.add(k, (String) v);
            } else if (v instanceof Integer) {
                builder.add(k, (Integer) v);
            } else if (v instanceof JsonValue) {
                builder.add(k, (JsonValue) v);
            } else if (v instanceof Boolean) {
                builder.add(k, (Boolean) v);
            } else if (v instanceof Long) {
                builder.add(k, (Long) v);
            } else if (v instanceof Double) {
                builder.add(k, (Double) v);
            } else if (v instanceof Enum<?>) {
                builder.add(k, ((Enum<?>) v).name());
            } else if (v == null) {
                builder.addNull(k);
            } else {
                builder.add(k, JSON_PROVIDER.createReader(new StringReader(JSONB.toJson(v))).read());
            }
        });

        return builder.build();
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
    public Map<String, Object> getExtensions() {
        return extensions;
    }

    @Override
    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
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
