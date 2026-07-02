package io.smallrye.graphql.client.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.smallrye.graphql.client.Request;
import io.smallrye.graphql.jackson.jsonb.JsonbCompatModule;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

public class RequestImpl implements Request {
    public static final ObjectMapper MAPPER = JsonMapper.builder()
            .addModule(new JsonbCompatModule())
            .enable(tools.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .disable(tools.jackson.databind.cfg.JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES)
            .build();

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
        try {
            return MAPPER.writeValueAsString(toJsonObject());
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize request to JSON", e);
        }
    }

    @Override
    public ObjectNode toJsonObject() {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("query", document);
        if (!variables.isEmpty()) {
            node.set("variables", _formatJsonMap(variables));
        }
        if (operationName != null && !operationName.isEmpty()) {
            node.put("operationName", operationName);
        }
        if (extensions != null && !extensions.isEmpty()) {
            node.set("extensions", _formatJsonMap(extensions));
        }
        return node;
    }

    private ObjectNode _formatJsonMap(Map<String, Object> map) {
        ObjectNode node = MAPPER.createObjectNode();
        map.forEach((k, v) -> {
            if (v == null) {
                node.putNull(k);
            } else if (v instanceof JsonNode) {
                node.set(k, (JsonNode) v);
            } else {
                node.set(k, MAPPER.valueToTree(v));
            }
        });
        return node;
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
